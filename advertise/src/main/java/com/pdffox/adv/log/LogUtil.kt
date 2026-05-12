package com.pdffox.adv.log

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import cn.thinkingdata.analytics.TDAnalytics
import com.google.android.gms.ads.AdValue
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.remoteConfig
import com.pdffox.adv.Ads
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.Config
import com.pdffox.adv.adv.AdvCheckManager
import com.pdffox.adv.adv.policy.AdPlayRecordManager
import com.pdffox.adv.adv.policy.NativeAdPlayRecordManager
import com.pdffox.adv.adv.policy.data.AdRecord
import com.pdffox.adv.push.LogPushData
import com.pdffox.adv.remoteconfig.RemoteConfigRouting
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import org.json.JSONObject

object LogUtil {
	private const val TAG = "LogUtil"

	fun log(eventName: String, params: Map<String, Any>) {
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "log: $eventName $params")
		}
		if (eventName == "notification_app_shown") {
			val firstOpenTime = AdvCheckManager.params.installTime
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "log: firstOpenTime = $firstOpenTime")
			}
			if (firstOpenTime > 0 && System.currentTimeMillis() - firstOpenTime > Config.log_time * 3600 * 1000) {
				if (com.pdffox.adv.Config.isTest) {
					Log.e(TAG, "log: skip notification_app_shown after log window")
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
				Config.paid_0 = (params[FirebaseAnalytics.Param.VALUE] as? Number)?.toDouble() == 0.0
				if (Config.sdkConfig.remoteConfig.enabled && Config.remoteConfigHasResult) {
					val remoteConfig = Firebase.remoteConfig
					val adMapping = remoteConfig.getString("ad_mapping")
					RemoteConfigRouting.apply(
						remoteConfig = remoteConfig,
						adMapping = adMapping,
						source = "LogUtil.adImpression"
					)
				} else if (com.pdffox.adv.Config.isTest) {
					Log.e(TAG, "log: RemoteConfig not ready")
				}
			}
		}
		if (eventName != LogPushData.notification_shown && eventName != LogAdData.adv_sdk_initcomplete) {
			logThinking(eventName, params)
		}
	}

	@SuppressLint("MissingPermission")
	fun logFirebase(eventName: String, params: Map<String, Any>) {
		if (!Config.sdkConfig.firebase.analyticsEnabled) {
			return
		}
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
		if (!Config.sdkConfig.thinking.enabled) {
			return
		}
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

	fun logSingularAdRevenue(adType: String, adPlatform: String, revenue: Double, trackRevenue: Boolean = true) {
		if (!Config.sdkConfig.singular.enabled) {
			return
		}
		try {
			val att = JSONObject().apply {
				put(LogAdParam.revenue, revenue)
				put(LogAdParam.adType, adType)
			}
			Singular.eventJSON(LogAdData.ad_revenue, att)
			if (trackRevenue && revenue > 0) {
				Singular.adRevenue(
					SingularAdData(
						adPlatform,
						LogAdParam.USD,
						revenue
					)
				)
			}
		} catch (e: Exception) {
			Log.e(TAG, "logSingularAdRevenue error: ${e.message}")
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
		if (!Config.sdkConfig.firebase.analyticsEnabled) {
			return
		}
		val firebaseAnalytics = FirebaseAnalytics.getInstance(Ads.application)
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
		firebaseAnalytics.logEvent(LogAdData.ad_Impression_Revenue, params)

		val previousTaichiTroasCache = taichiAdmobPref.getFloat(LogAdParam.admobTaichiTroasCache, 0f)
		val currentTaichiTroasCache = previousTaichiTroasCache + currentImpressionRevenue.toFloat()
		if (currentTaichiTroasCache >= 0.01f) {
			val roasBundle = Bundle().apply {
				putDouble(FirebaseAnalytics.Param.VALUE, currentTaichiTroasCache.toDouble())
				putString(FirebaseAnalytics.Param.CURRENCY, LogAdParam.USD)
			}
			firebaseAnalytics.logEvent(LogAdData.total_Ads_Revenue_001, roasBundle)
			taichiAdmobSharedPreferencesEditor.putFloat(LogAdParam.admobTaichiTroasCache, 0f)
		} else {
			taichiAdmobSharedPreferencesEditor.putFloat(LogAdParam.admobTaichiTroasCache, currentTaichiTroasCache)
		}
		taichiAdmobSharedPreferencesEditor.commit()
	}

}
