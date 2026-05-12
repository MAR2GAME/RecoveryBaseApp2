package com.pdffox.adv.adv

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.ads.nativead.NativeAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.Config
import com.pdffox.adv.NativeAdContent
import com.pdffox.adv.adv.policy.NativePolicyManager
import com.pdffox.adv.log.LogAdData
import com.pdffox.adv.log.LogAdParam
import com.pdffox.adv.log.LogUtil

object ShowNativeAd {

	private const val TAG = "ShowNativeAd"

	@RequiresPermission(Manifest.permission.INTERNET)
	fun getNativeAd(context: Context, areaKey: String, onAdGroupLoaded: () -> Unit): NativeAdContent? {
		if (!Config.sdkConfig.adMob.enabled) {
			return null
		}
		// 原生广告屏蔽掉自然量
		if ( (!com.pdffox.adv.Config.isTest) && (Config.paid_0 || Config.isGoogleIP)) {
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
					LogUtil.logSingularAdRevenue(LogAdParam.BannerAd, LogAdParam.adMob, revenue)
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
					if (com.pdffox.adv.Config.isTest || ((!Config.paid_0) && (!Config.isGoogleIP) && AdConfig.canLoadNative(AdConfig.LOAD_TIME_PLAY_FINISH))) {
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
					LogUtil.logSingularAdRevenue(LogAdParam.BannerAd, LogAdParam.adMob, revenue)
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
					if (com.pdffox.adv.Config.isTest || ((!Config.paid_0) && (!Config.isGoogleIP) && AdConfig.canLoadNative(AdConfig.LOAD_TIME_PLAY_FINISH))) {
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
					LogUtil.logSingularAdRevenue(LogAdParam.BannerAd, LogAdParam.adMob, revenue)
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
					if (com.pdffox.adv.Config.isTest || ((!Config.paid_0) && (!Config.isGoogleIP) && AdConfig.canLoadNative(AdConfig.LOAD_TIME_PLAY_FINISH))) {
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
