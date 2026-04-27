package com.pdffox.adv.use.adv

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.ads.nativead.NativeAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.use.Config
import com.pdffox.adv.use.adv.policy.NativePolicyManager
import com.pdffox.adv.use.log.LogAdData
import com.pdffox.adv.use.log.LogAdParam
import com.pdffox.adv.use.log.LogUtil
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import org.json.JSONException
import org.json.JSONObject

object ShowNativeAd {

	private const val TAG = "ShowNativeAd"

	@RequiresPermission(Manifest.permission.INTERNET)
	fun getNativeAd(context: Context, areaKey: String, onAdGroupLoaded: () -> Unit): NativeAdContent? {
		// 原生广告屏蔽掉自然量
		if ( (!BuildConfig.DEBUG) && (Config.paid_0 || Config.isGoogleIP)) {
			Log.e(TAG, "getNativeAd: 屏蔽掉自然量" )
			return null
		}

		val canPlay = NativePolicyManager.checkAdUnit(areaKey)
		if (!canPlay) {
			Log.e(TAG, "getNativeAd: 后台配置筛掉 $areaKey" )
			return null
		}

		Log.e(TAG, "getNativeAd: 加载原生广告" )

		val adContent = AdPool.getAdmobNative()
		if (adContent != null) {
			if (adContent.hAd != null) {
				val resultAd = adContent.hAd
				resultAd?.setOnPaidEventListener { adValue ->
					val micros = adValue.valueMicros         // 广告价值（微元单位，需除以1,000,000得到实际金额）
					val currency = adValue.currencyCode     // ISO 4217货币代码（如："USD"）
					val precision = adValue.precisionType    // 金额精度类型（0=估算，1=发布商定义，2=精确计算）
					// 收入跟踪（示例：转换为美元）
					val revenue = micros / 1_000_000.0
					val att = JSONObject()
					try {
						att.put(LogAdParam.revenue, revenue)
						att.put(LogAdParam.adType, LogAdParam.BannerAd)
					} catch (e: JSONException) {
						e.printStackTrace()
						Log.e(TAG, "getNativeAd: ", e)
					}
					Singular.eventJSON(LogAdData.ad_revenue, att)
					if (revenue > 0) {
						val data = SingularAdData(
							LogAdParam.adMob,
							LogAdParam.USD,
							revenue
						)
						Singular.adRevenue(data)
					}
					LogUtil.log(
						LogAdData.ad_impression,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobBannerId(),
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_native,
							FirebaseAnalytics.Param.AD_SOURCE to LogAdParam.adMob,
							FirebaseAnalytics.Param.CURRENCY to currency,
							FirebaseAnalytics.Param.VALUE to revenue,
							LogAdParam.ad_preload to false,
						)
					)
					LogUtil.log(
						LogAdData.ad_revenue,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobBannerId(),
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_native,
							FirebaseAnalytics.Param.AD_SOURCE to LogAdParam.adMob,
							FirebaseAnalytics.Param.CURRENCY to currency,
							FirebaseAnalytics.Param.VALUE to revenue,
							LogAdParam.ad_preload to false,
						)
					)
					LogUtil.logTaiChiAdmob(adValue)
					if (BuildConfig.DEBUG || ((!Config.paid_0) && (!Config.isGoogleIP) && AdConfig.canLoadNative(
                            AdConfig.LOAD_TIME_PLAY_FINISH))) {
						AdLoader.fillNativePool(context)
					}
				}
				resultAd?.setUnconfirmedClickListener(object : NativeAd.UnconfirmedClickListener{
					override fun onUnconfirmedClickReceived(p0: String) {
						LogUtil.log(
							LogAdData.ad_click,
							mapOf(
								LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
								LogAdParam.ad_areakey to areaKey,
								LogAdParam.ad_format to LogAdParam.ad_format_native,
								LogAdParam.ad_preload to true,
								"onUnconfirmedClickReceived" to true
							)
						)
					}

					override fun onUnconfirmedClickCancelled() {
						LogUtil.log(
							LogAdData.ad_click,
							mapOf(
								LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
								LogAdParam.ad_areakey to areaKey,
								LogAdParam.ad_format to LogAdParam.ad_format_native,
								LogAdParam.ad_preload to true,
								"onUnconfirmedClickReceived" to false
							)
						)
					}
				})
			} else if (adContent.mAd != null) {
				val resultAd = adContent.mAd
				resultAd?.setOnPaidEventListener { adValue ->
					val micros = adValue.valueMicros         // 广告价值（微元单位，需除以1,000,000得到实际金额）
					val currency = adValue.currencyCode     // ISO 4217货币代码（如："USD"）
					val precision = adValue.precisionType    // 金额精度类型（0=估算，1=发布商定义，2=精确计算）
					// 收入跟踪（示例：转换为美元）
					val revenue = micros / 1_000_000.0
					val att = JSONObject()
					try {
						att.put(LogAdParam.revenue, revenue)
						att.put(LogAdParam.adType, LogAdParam.BannerAd)
					} catch (e: JSONException) {
						e.printStackTrace()
						Log.e(TAG, "getNativeAd: ", e)
					}
					Singular.eventJSON(LogAdData.ad_revenue, att)
					if (revenue > 0) {
						val data = SingularAdData(
							LogAdParam.adMob,
							LogAdParam.USD,
							revenue
						)
						Singular.adRevenue(data)
					}
					LogUtil.log(
						LogAdData.ad_impression,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobBannerId(),
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_native,
							FirebaseAnalytics.Param.AD_SOURCE to LogAdParam.adMob,
							FirebaseAnalytics.Param.CURRENCY to currency,
							FirebaseAnalytics.Param.VALUE to revenue,
							LogAdParam.ad_preload to false,
						)
					)
					LogUtil.log(
						LogAdData.ad_revenue,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobBannerId(),
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_native,
							FirebaseAnalytics.Param.AD_SOURCE to LogAdParam.adMob,
							FirebaseAnalytics.Param.CURRENCY to currency,
							FirebaseAnalytics.Param.VALUE to revenue,
							LogAdParam.ad_preload to false,
						)
					)
					LogUtil.logTaiChiAdmob(adValue)
					if (BuildConfig.DEBUG || ((!Config.paid_0) && (!Config.isGoogleIP) && AdConfig.canLoadNative(
                            AdConfig.LOAD_TIME_PLAY_FINISH))) {
						AdLoader.fillNativePool(context)
					}
				}
				resultAd?.setUnconfirmedClickListener(object : NativeAd.UnconfirmedClickListener{
					override fun onUnconfirmedClickReceived(p0: String) {
						LogUtil.log(
							LogAdData.ad_click,
							mapOf(
								LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
								LogAdParam.ad_areakey to areaKey,
								LogAdParam.ad_format to LogAdParam.ad_format_native,
								LogAdParam.ad_preload to true,
								"onUnconfirmedClickReceived" to true
							)
						)
					}

					override fun onUnconfirmedClickCancelled() {
						LogUtil.log(
							LogAdData.ad_click,
							mapOf(
								LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
								LogAdParam.ad_areakey to areaKey,
								LogAdParam.ad_format to LogAdParam.ad_format_native,
								LogAdParam.ad_preload to true,
								"onUnconfirmedClickReceived" to false
							)
						)
					}
				})
			} else if (adContent.lAd != null) {
				val resultAd = adContent.lAd
				resultAd?.setOnPaidEventListener { adValue ->
					val micros = adValue.valueMicros         // 广告价值（微元单位，需除以1,000,000得到实际金额）
					val currency = adValue.currencyCode     // ISO 4217货币代码（如："USD"）
					val precision = adValue.precisionType    // 金额精度类型（0=估算，1=发布商定义，2=精确计算）
					// 收入跟踪（示例：转换为美元）
					val revenue = micros / 1_000_000.0
					val att = JSONObject()
					try {
						att.put(LogAdParam.revenue, revenue)
						att.put(LogAdParam.adType, LogAdParam.BannerAd)
					} catch (e: JSONException) {
						e.printStackTrace()
						Log.e(TAG, "getNativeAd: ", e)
					}
					Singular.eventJSON(LogAdData.ad_revenue, att)
					if (revenue > 0) {
						val data = SingularAdData(
							LogAdParam.adMob,
							LogAdParam.USD,
							revenue
						)
						Singular.adRevenue(data)
					}
					LogUtil.log(
						LogAdData.ad_impression,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobBannerId(),
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_native,
							FirebaseAnalytics.Param.AD_SOURCE to LogAdParam.adMob,
							FirebaseAnalytics.Param.CURRENCY to currency,
							FirebaseAnalytics.Param.VALUE to revenue,
							LogAdParam.ad_preload to false,
						)
					)
					LogUtil.log(
						LogAdData.ad_revenue,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobBannerId(),
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_native,
							FirebaseAnalytics.Param.AD_SOURCE to LogAdParam.adMob,
							FirebaseAnalytics.Param.CURRENCY to currency,
							FirebaseAnalytics.Param.VALUE to revenue,
							LogAdParam.ad_preload to false,
						)
					)
					LogUtil.logTaiChiAdmob(adValue)
					if (BuildConfig.DEBUG || ((!Config.paid_0) && (!Config.isGoogleIP) && AdConfig.canLoadNative(
                            AdConfig.LOAD_TIME_PLAY_FINISH))) {
						AdLoader.fillNativePool(context)
					}
				}
				resultAd?.setUnconfirmedClickListener(object : NativeAd.UnconfirmedClickListener{
					override fun onUnconfirmedClickReceived(p0: String) {
						LogUtil.log(
							LogAdData.ad_click,
							mapOf(
								LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
								LogAdParam.ad_areakey to areaKey,
								LogAdParam.ad_format to LogAdParam.ad_format_native,
								LogAdParam.ad_preload to true,
								"onUnconfirmedClickReceived" to true
							)
						)
					}

					override fun onUnconfirmedClickCancelled() {
						LogUtil.log(
							LogAdData.ad_click,
							mapOf(
								LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
								LogAdParam.ad_areakey to areaKey,
								LogAdParam.ad_format to LogAdParam.ad_format_native,
								LogAdParam.ad_preload to true,
								"onUnconfirmedClickReceived" to false
							)
						)
					}
				})
			}
		}

		if (adContent == null) {
			Log.e(TAG, "getNativeAd: fillNativePool 开始填充原生广告" )
			AdLoader.fillNativePool(context, onAdGroupLoaded)
		}

		return adContent
	}

}