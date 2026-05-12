package com.pdffox.adv.adv

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.OnPaidEventListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.pdffox.adv.Config
import com.pdffox.adv.adv.policy.AdPolicyManager
import com.pdffox.adv.log.LogAdData
import com.pdffox.adv.log.LogAdParam
import com.pdffox.adv.log.LogUtil

object ShowBannerAd {

	private const val TAG = "ShowBannerAd"

	@RequiresPermission(Manifest.permission.INTERNET)
	fun getBannerAd(context: Context, areaKey: String): ViewGroup? {
		if (!AdPolicyManager.checkAdUnit(areaKey)) {
			Log.e(TAG, "getBannerAd blocked by policy: $areaKey")
			return null
		}
		if (Config.activeAdPlatform() != LogAdParam.ad_platform_admob) {
			return null
		}
		return getAdmobBannerAd(context, areaKey)
	}

	private fun getAdSize(context: Context): AdSize {
		val displayMetrics = context.resources.displayMetrics
		val adWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
		return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
	}

	@RequiresPermission(Manifest.permission.INTERNET)
	fun getAdmobBannerAd(context: Context, areaKey: String): ViewGroup {
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_banner,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobBannerId(),
				LogAdParam.ad_preload to false,
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		val admobAdView = AdView(context).apply {
			adUnitId = AdvIDs.getAdmobBannerId()
			setAdSize(getAdSize(context))
		}

		admobAdView.adListener = object : AdListener() {
			override fun onAdLoaded() {
				LogUtil.log(
					LogAdData.ad_finish_loading,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_banner,
						LogAdParam.ad_source to (admobAdView.responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobBannerId(),
						LogAdParam.ad_preload to false,
					)
				)
			}

			override fun onAdImpression() {
				AdvCheckManager.params.bannerTimes++
			}

			override fun onAdClicked() {
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_banner,
						LogAdParam.ad_source to (admobAdView.responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobBannerId(),
						LogAdParam.ad_preload to false,
					)
				)
				AppOpenHelper.spSwitch = true
			}
		}
		admobAdView.onPaidEventListener = OnPaidEventListener { adValue ->
			val revenue = adValue.valueMicros / 1_000_000.0
			LogUtil.logSingularAdRevenue(LogAdParam.BannerAd, LogAdParam.adMob, revenue)
			logBannerRevenue(LogAdData.ad_impression, areaKey, admobAdView, adValue.currencyCode, revenue)
			logBannerRevenue(LogAdData.ad_revenue, areaKey, admobAdView, adValue.currencyCode, revenue)
			LogUtil.logTaiChiAdmob(adValue)
		}

		val extras = Bundle().apply {
			putString("collapsible", "bottom")
		}
		val adRequest = AdRequest.Builder()
			.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
			.build()
		admobAdView.loadAd(adRequest)
		return admobAdView
	}

	private fun logBannerRevenue(
		eventName: String,
		areaKey: String,
		adView: AdView,
		currency: String,
		revenue: Double,
	) {
		LogUtil.log(
			eventName,
			mapOf(
				LogAdParam.ad_areakey to areaKey,
				FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
				FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobBannerId(),
				FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_banner,
				FirebaseAnalytics.Param.AD_SOURCE to (adView.responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
				FirebaseAnalytics.Param.CURRENCY to currency,
				FirebaseAnalytics.Param.VALUE to revenue,
				LogAdParam.ad_preload to false,
			)
		)
	}
}
