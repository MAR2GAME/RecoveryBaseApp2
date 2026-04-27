package com.pdffox.adv.use.log

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresPermission
import cn.thinkingdata.analytics.TDAnalytics
import com.applovin.mediation.MaxAd
import com.google.android.gms.ads.AdValue
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.remoteConfig
import com.pdffox.adv.use.Ads
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.use.Config
import com.pdffox.adv.use.adv.AdvCheckManager
import com.pdffox.adv.use.adv.policy.AdPlayRecordManager
import com.pdffox.adv.use.adv.policy.NativeAdPlayRecordManager
import com.pdffox.adv.use.adv.policy.data.AdRecord
import com.pdffox.adv.use.push.LogPushData
import com.pdffox.adv.use.remoteconfig.RemoteConfigRouting
import com.thinkup.interstitial.api.TUInterstitial
import com.thinkup.splashad.api.TUSplashAd
import org.json.JSONObject
import kotlin.collections.iterator

object LogUtil {
	private const val TAG = "LogUtil"

	fun log(eventName: String, params: Map<String, Any>) {
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "log: $eventName $params")
		}
		if (eventName == "notification_app_shown" ) {
			val firstOpenTime = AdvCheckManager.params.installTime
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "log: firstOpenTime = $firstOpenTime" )
			}
			if (firstOpenTime > 0 && (System.currentTimeMillis() - firstOpenTime) > (Config.log_time * 3600 * 1000)) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "log: 屏蔽48小时后的 notification_app_shown 事件上报", )
				}
				return
			}
		}
		if (eventName != LogAdData.ad_impression) {
			logFirebase(eventName, params)
		}
		if (eventName == LogAdData.ad_impression) {
			val adRecord = AdRecord(
				areakey = params[LogAdParam.ad_areakey] as String,
				adFormat = params[FirebaseAnalytics.Param.AD_FORMAT] as String,
				showAdPlatform = params[FirebaseAnalytics.Param.AD_PLATFORM] as String,
				timestamp = System.currentTimeMillis(),
			)
			if (params[FirebaseAnalytics.Param.AD_FORMAT] == LogAdParam.ad_format_native) {
				NativeAdPlayRecordManager.addRecord(adRecord)
			} else {
				AdPlayRecordManager.addRecord(adRecord)
			}
			if (!Config.paid0HasResult) {
				Config.paid0HasResult = true
				// 将其转换为 Number 后取 doubleValue 进行比较，可以同时兼容 Int, Long, Float, Double
				Config.paid_0 = (params[FirebaseAnalytics.Param.VALUE] as? Number)?.toDouble() == 0.0
				// TODO: 走一遍广告策略设置
				if (Config.remoteConfigHasResult) {
					val remoteConfig = Firebase.remoteConfig
					val adMapping = remoteConfig.getString("ad_mapping")
					RemoteConfigRouting.apply(
						remoteConfig = remoteConfig,
						adMapping = adMapping,
						source = "LogUtil.adImpression"
					)
				} else {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "initSingular: RemoteConfig还未更新先不处理" )
					}
				}
			}
		}
		if (eventName != LogPushData.notification_shown && eventName != LogAdData.adv_sdk_initcomplete) {
			logThinking(eventName, params)
		}
	}

	@SuppressLint("MissingPermission")
	fun logFirebase(eventName: String, params: Map<String, Any>) {
		val firebaseAnalytics = FirebaseAnalytics.getInstance(Ads.application)
		val bundle = Bundle()
		for ((key, value) in params) {
			when (value) {
				is String -> bundle.putString(key, value)
				is Int -> bundle.putInt(key, value)
				is Long -> bundle.putLong(key, value)
				is Double -> bundle.putDouble(key, value)
				is Float -> bundle.putFloat(key, value)
				is Boolean -> bundle.putBoolean(key, value)
				else -> bundle.putString(key, value.toString())
			}
		}
		firebaseAnalytics.logEvent(eventName, bundle)
	}

	fun logThinking(eventName: String, params: Map<String, Any>) {
		try {
			val jsonObject = JSONObject()
			for ((key, value) in params) {
				jsonObject.put(key, value)
			}
			TDAnalytics.track(eventName, jsonObject)
		} catch (e: Exception) {
			Log.e(TAG, "logThinking error: ${e.message}")
		}
	}

	private val taichiAdmobPref by lazy {
		Ads.application.getSharedPreferences(LogAdParam.admobTaichiTroasCache, 0)
	}
	private val taichiAdmobSharedPreferencesEditor by lazy {
		taichiAdmobPref.edit()
	}

	@SuppressLint("MissingPermission")
	fun logTaiChiAdmob(adValue: AdValue) {
		val mFirebaseAnalytics = FirebaseAnalytics.getInstance(Ads.application)
		val currentImpressionRevenue = adValue.valueMicros.toDouble() / 1_000_000.0

		val precisionType = when (adValue.precisionType) {
			0 -> "UNKNOWN"
			1 -> "ESTIMATED"
			2 -> "PUBLISHER_PROVIDED"
			3 -> "PRECISE"
			else -> "Invalid"
		}

		val params = Bundle().apply {
			putDouble(FirebaseAnalytics.Param.VALUE, currentImpressionRevenue)
			putString(FirebaseAnalytics.Param.CURRENCY, LogAdParam.USD)
			putString("precisionType", precisionType)
		}

		mFirebaseAnalytics.logEvent(LogAdData.ad_Impression_Revenue, params) // 给Taichi用

		val previousTaichiTroasCache = taichiAdmobPref.getFloat(LogAdParam.admobTaichiTroasCache, 0f)
		val currentTaichiTroasCache = previousTaichiTroasCache + currentImpressionRevenue.toFloat()

		if (currentTaichiTroasCache >= 0.01f) {
			val roasBundle = Bundle().apply {
				putDouble(FirebaseAnalytics.Param.VALUE, currentTaichiTroasCache.toDouble())
				putString(FirebaseAnalytics.Param.CURRENCY, LogAdParam.USD)
			}
			mFirebaseAnalytics.logEvent(LogAdData.total_Ads_Revenue_001, roasBundle)
			taichiAdmobSharedPreferencesEditor.putFloat(LogAdParam.admobTaichiTroasCache, 0f)
		} else {
			taichiAdmobSharedPreferencesEditor.putFloat(LogAdParam.admobTaichiTroasCache, currentTaichiTroasCache)
		}
		taichiAdmobSharedPreferencesEditor.commit()
	}

	private val taichiMaxPref by lazy {
		Ads.application.getSharedPreferences(LogAdParam.taichiMaxTroasCache, 0)
	}
	private val taichiMaxSharedPreferencesEditor by lazy {
		taichiMaxPref.edit()
	}

	@SuppressLint("MissingPermission")
	fun logTaiChiMax(impressionData: MaxAd) {
		val mFirebaseAnalytics = FirebaseAnalytics.getInstance(Ads.application)
		val currentImpressionRevenue = impressionData.revenue // USD单位

		val params = Bundle().apply {
			putString(FirebaseAnalytics.Param.AD_PLATFORM, LogAdParam.appLovin)
			putString(FirebaseAnalytics.Param.AD_SOURCE, impressionData.networkName)
			putString(FirebaseAnalytics.Param.AD_FORMAT, impressionData.format.displayName)
			putString(FirebaseAnalytics.Param.AD_UNIT_NAME, impressionData.adUnitId)
			putDouble(FirebaseAnalytics.Param.VALUE, currentImpressionRevenue)
			putString(FirebaseAnalytics.Param.CURRENCY, LogAdParam.USD)
		}

		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, params) // 给ARO用
		mFirebaseAnalytics.logEvent(LogAdData.ad_Impression_Revenue, params) // 给Taichi用

		val previousTaichiTroasCache = taichiMaxPref.getFloat(LogAdParam.taichiMaxTroasCache, 0f)
		val currentTaichiTroasCache = previousTaichiTroasCache + currentImpressionRevenue.toFloat()

		if (currentTaichiTroasCache >= 0.01f) {
			val roasBundle = Bundle().apply {
				putDouble(FirebaseAnalytics.Param.VALUE, currentTaichiTroasCache.toDouble())
				putString(FirebaseAnalytics.Param.CURRENCY, LogAdParam.USD)
			}
			mFirebaseAnalytics.logEvent(LogAdData.total_Ads_Revenue_001, roasBundle) // 给Taichi用
			taichiMaxSharedPreferencesEditor.putFloat(LogAdParam.taichiMaxTroasCache, 0f) // 重新清零
		} else {
			taichiMaxSharedPreferencesEditor.putFloat(LogAdParam.taichiMaxTroasCache, currentTaichiTroasCache) // 缓存
		}
		taichiMaxSharedPreferencesEditor.commit()
	}

	@RequiresPermission(allOf = [Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WAKE_LOCK])
	fun logTaiChiTopOn(impressionData: TUSplashAd) {
		val mFirebaseAnalytics = FirebaseAnalytics.getInstance(Ads.application)
		val currentImpressionRevenue = impressionData.checkAdStatus().tuTopAdInfo.publisherRevenue

		val params = Bundle().apply {
			putString(FirebaseAnalytics.Param.AD_PLATFORM, LogAdParam.appLovin)
			putString(FirebaseAnalytics.Param.AD_SOURCE, impressionData.checkAdStatus().tuTopAdInfo.networkName)
			putString(FirebaseAnalytics.Param.AD_FORMAT, impressionData.checkAdStatus().tuTopAdInfo.format)
			putString(FirebaseAnalytics.Param.AD_UNIT_NAME, impressionData.checkAdStatus().tuTopAdInfo.adsourceId)
			putDouble(FirebaseAnalytics.Param.VALUE, currentImpressionRevenue)
			putString(FirebaseAnalytics.Param.CURRENCY, LogAdParam.USD)
		}

		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, params) // 给ARO用
		mFirebaseAnalytics.logEvent(LogAdData.ad_Impression_Revenue, params) // 给Taichi用

		val previousTaichiTroasCache = taichiMaxPref.getFloat(LogAdParam.taichiMaxTroasCache, 0f)
		val currentTaichiTroasCache = previousTaichiTroasCache + currentImpressionRevenue.toFloat()

		if (currentTaichiTroasCache >= 0.01f) {
			val roasBundle = Bundle().apply {
				putDouble(FirebaseAnalytics.Param.VALUE, currentTaichiTroasCache.toDouble())
				putString(FirebaseAnalytics.Param.CURRENCY, LogAdParam.USD)
			}
			mFirebaseAnalytics.logEvent(LogAdData.total_Ads_Revenue_001, roasBundle) // 给Taichi用
			taichiMaxSharedPreferencesEditor.putFloat(LogAdParam.taichiMaxTroasCache, 0f) // 重新清零
		} else {
			taichiMaxSharedPreferencesEditor.putFloat(LogAdParam.taichiMaxTroasCache, currentTaichiTroasCache) // 缓存
		}
		taichiMaxSharedPreferencesEditor.commit()
	}

	@RequiresPermission(allOf = [Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WAKE_LOCK])
	fun logTaiChiTopOn(impressionData: TUInterstitial) {
		val mFirebaseAnalytics = FirebaseAnalytics.getInstance(Ads.application)
		val currentImpressionRevenue = impressionData.checkAdStatus().tuTopAdInfo.publisherRevenue

		val params = Bundle().apply {
			putString(FirebaseAnalytics.Param.AD_PLATFORM, LogAdParam.appLovin)
			putString(FirebaseAnalytics.Param.AD_SOURCE, impressionData.checkAdStatus().tuTopAdInfo.networkName)
			putString(FirebaseAnalytics.Param.AD_FORMAT, impressionData.checkAdStatus().tuTopAdInfo.format)
			putString(FirebaseAnalytics.Param.AD_UNIT_NAME, impressionData.checkAdStatus().tuTopAdInfo.adsourceId)
			putDouble(FirebaseAnalytics.Param.VALUE, currentImpressionRevenue)
			putString(FirebaseAnalytics.Param.CURRENCY, LogAdParam.USD)
		}

		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, params) // 给ARO用
		mFirebaseAnalytics.logEvent(LogAdData.ad_Impression_Revenue, params) // 给Taichi用

		val previousTaichiTroasCache = taichiMaxPref.getFloat(LogAdParam.taichiMaxTroasCache, 0f)
		val currentTaichiTroasCache = previousTaichiTroasCache + currentImpressionRevenue.toFloat()

		if (currentTaichiTroasCache >= 0.01f) {
			val roasBundle = Bundle().apply {
				putDouble(FirebaseAnalytics.Param.VALUE, currentTaichiTroasCache.toDouble())
				putString(FirebaseAnalytics.Param.CURRENCY, LogAdParam.USD)
			}
			mFirebaseAnalytics.logEvent(LogAdData.total_Ads_Revenue_001, roasBundle) // 给Taichi用
			taichiMaxSharedPreferencesEditor.putFloat(LogAdParam.taichiMaxTroasCache, 0f) // 重新清零
		} else {
			taichiMaxSharedPreferencesEditor.putFloat(LogAdParam.taichiMaxTroasCache, currentTaichiTroasCache) // 缓存
		}
		taichiMaxSharedPreferencesEditor.commit()
	}
}
