package com.pdffox.adv.adv

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.pdffox.adv.AdvRuntime
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.Config
import com.pdffox.adv.adv.policy.AdPolicyManager
import com.pdffox.adv.log.LogAdData
import com.pdffox.adv.log.LogAdParam
import com.pdffox.adv.log.LogUtil
import com.pdffox.adv.util.PreferenceDelegate

object AppOpenHelper : DefaultLifecycleObserver {

	private const val TAG = "AppOpenHelper"

	var spSwitch = false

	private var isAppInBackground = false

	var showOpenAdvTime: Long by PreferenceDelegate("showOpenAdvTime", 0L)
	var enterBackgroundTime = 0L

	var hasInitAdmob = false

	var areaKey = LogAdParam.foregroundKey

	var isShowing = false
	private var isObserving = false
	private var isLoadingAdmobAd = false
	var isShowingAdmobAd = false

	override fun onStart(owner: LifecycleOwner) {
		super.onStart(owner)
		if (com.pdffox.adv.Config.isTest && spSwitch) {
			Log.e(TAG, "onStart: spSwitch = $spSwitch")
			Toast.makeText(AdvRuntime.application, "onStart: spSwitch = $spSwitch", Toast.LENGTH_SHORT).show()
			spSwitch = false
			return
		}
		if (!com.pdffox.adv.Config.isTest && (Config.isGoogleIP || Config.paid_0)) {
			return
		}
		if (!isAppInBackground) {
			return
		}
		isAppInBackground = false
		if (AdvCheckManager.params.limitTime > System.currentTimeMillis()) {
			return
		}
		if (isShowing || ShowInterstitialAdActivity.isShowing) {
			return
		}
		if (AdConfig.isNewAdPolicy) {
			showAdIfReady()
		} else if (enterBackgroundTime + AdvCheckManager.params.backgroundDuration * 1000 < System.currentTimeMillis()) {
			if (AdConfig.isOpenAppOpenHelper) {
				showAdIfReady()
			}
		}
	}

	override fun onStop(owner: LifecycleOwner) {
		super.onStop(owner)
		if (com.pdffox.adv.Config.isTest && spSwitch) {
			Log.e(TAG, "onStop: spSwitch = $spSwitch")
			Toast.makeText(AdvRuntime.application, "onStop: spSwitch = $spSwitch", Toast.LENGTH_SHORT).show()
			return
		}
		if (!com.pdffox.adv.Config.isTest && (Config.isGoogleIP || Config.paid_0)) {
			return
		}
		isAppInBackground = true
		enterBackgroundTime = System.currentTimeMillis()
		if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_ENTER_BACKGROUND)) {
			AdLoader.loadOpen(AdvRuntime.application)
		}
		if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_ENTER_BACKGROUND)) {
			AdLoader.loadInter(AdvRuntime.application)
		}
	}

	fun startObserve() {
		if (isObserving) {
			return
		}
		isObserving = true
		ProcessLifecycleOwner.get().lifecycle.addObserver(this)
	}

	fun showAdIfReady() {
		if (AdvCheckManager.params.limitTime > System.currentTimeMillis()) {
			return
		}
		if (AdConfig.isNewAdPolicy && !AdPolicyManager.checkAdUnit(areaKey)) {
			return
		}
		if (Config.activeAdPlatform() != LogAdParam.ad_platform_admob) {
			return
		}
		val admobOpenAd = AdPool.getAdmobOpen(areaKey, {
			isShowing = false
		}) {
			isShowing = true
			if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
				AdLoader.loadOpen(AdvRuntime.application)
			}
		}
		if (admobOpenAd != null) {
			AdvRuntime.currentActivity?.let {
				admobOpenAd.show(it)
			}
		} else {
			showAdmob()
		}
	}

	private fun showAdmob() {
		if (isLoadingAdmobAd || Config.activeAdPlatform() != LogAdParam.ad_platform_admob) {
			return
		}
		isLoadingAdmobAd = true
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_open,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
				LogAdParam.ad_preload to false,
			)
		)
		LogUtil.log(
			LogAdData.ad_occur,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_open,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
				LogAdParam.ad_preload to false,
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		AppOpenAd.load(
			AdvRuntime.application,
			AdvIDs.getAdmobOpenId(),
			AdRequest.Builder().build(),
			object : AppOpenAd.AppOpenAdLoadCallback() {
				override fun onAdLoaded(ad: AppOpenAd) {
					isLoadingAdmobAd = false
					LogUtil.log(
						LogAdData.ad_finish_loading,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
							LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
							LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
							LogAdParam.ad_preload to false,
						)
					)
					ad.fullScreenContentCallback = object : FullScreenContentCallback() {
						override fun onAdDismissedFullScreenContent() {
							LogUtil.log(
								LogAdData.ad_close,
								mapOf(
									LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
									LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
									LogAdParam.ad_areakey to areaKey,
									LogAdParam.ad_format to LogAdParam.ad_format_open,
									LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
									LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
									LogAdParam.ad_preload to false,
								)
							)
							isShowingAdmobAd = false
							isShowing = false
						}

						override fun onAdFailedToShowFullScreenContent(adError: AdError) {
							Log.e(TAG, adError.message)
							isShowingAdmobAd = false
							isShowing = false
						}

						override fun onAdShowedFullScreenContent() {
							isShowing = true
							isShowingAdmobAd = true
						}

						override fun onAdImpression() {
							AdvCheckManager.params.openTimes++
							if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
								AdLoader.loadOpen(AdvRuntime.application)
							}
						}

						override fun onAdClicked() {
							LogUtil.log(
								LogAdData.ad_click,
								mapOf(
									LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
									LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
									LogAdParam.ad_areakey to areaKey,
									LogAdParam.ad_format to LogAdParam.ad_format_open,
									LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
									LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
									LogAdParam.ad_preload to false,
								)
							)
							spSwitch = true
						}
					}
					ad.onPaidEventListener = OnPaidEventListener { adValue ->
						val revenue = adValue.valueMicros / 1_000_000.0
						LogUtil.logSingularAdRevenue(LogAdParam.OpenAd, LogAdParam.adMob, revenue)
						logOpenRevenue(LogAdData.ad_impression, ad, adValue.currencyCode, revenue)
						logOpenRevenue(LogAdData.ad_revenue, ad, adValue.currencyCode, revenue)
						LogUtil.logTaiChiAdmob(adValue)
					}
					AdvRuntime.currentActivity?.let {
						ad.show(it)
					} ?: run {
						isShowing = false
					}
				}

				override fun onAdFailedToLoad(loadAdError: LoadAdError) {
					isLoadingAdmobAd = false
					isShowingAdmobAd = false
					isShowing = false
					Log.e(TAG, "onAdFailedToLoad: ${loadAdError.message}")
				}
			},
		)
	}

	private fun logOpenRevenue(eventName: String, ad: AppOpenAd, currency: String, revenue: Double) {
		LogUtil.log(
			eventName,
			mapOf(
				LogAdParam.ad_areakey to areaKey,
				FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
				FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobOpenId(),
				FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
				FirebaseAnalytics.Param.AD_SOURCE to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
				FirebaseAnalytics.Param.CURRENCY to currency,
				FirebaseAnalytics.Param.VALUE to revenue,
				LogAdParam.ad_preload to false,
			)
		)
	}
}
