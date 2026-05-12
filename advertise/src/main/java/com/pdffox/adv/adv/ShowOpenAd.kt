package com.pdffox.adv.adv

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.pdffox.adv.Config
import com.pdffox.adv.adv.policy.AdPolicyManager
import com.pdffox.adv.log.LogAdData
import com.pdffox.adv.log.LogAdParam
import com.pdffox.adv.log.LogUtil

object ShowOpenAd {

	fun interface OpenAdCloseListener {
		fun onClose()
	}

	fun interface OpenAdLoadedListener {
		fun onLoaded()
	}

	fun interface OpenAdPaidListener {
		fun onPaid(value: Long)
	}

	private const val TAG = "ShowOpenAd"

	fun showOpenAd(
		activity: Activity,
		areaKey: String,
		onCloseListener: OpenAdCloseListener?,
		onLoadedListener: OpenAdLoadedListener?,
		onPaidListener: OpenAdPaidListener?,
	) {
		if (AdvCheckManager.params.limitTime > System.currentTimeMillis()) {
			onCloseListener?.onClose()
			return
		}
		if (AdConfig.isNewAdPolicy && !AdPolicyManager.checkAdUnit(areaKey)) {
			onCloseListener?.onClose()
			return
		}
		if (ShowInterstitialAdActivity.isShowing) {
			onCloseListener?.onClose()
			return
		}
		if (Config.activeAdPlatform() != LogAdParam.ad_platform_admob) {
			onCloseListener?.onClose()
			return
		}
		val admobOpenAd = AdPool.getAdmobOpen(areaKey, {
			onCloseListener?.onClose()
		}) {
			if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
				AdLoader.loadOpen(activity)
			}
		}
		if (admobOpenAd != null) {
			showAdmobOpenAdFromCache(admobOpenAd, activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
		} else {
			showAdmobOpenAd(activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
		}
	}

	fun showAdmobOpenAdFromCache(
		admobOpenAd: AppOpenAd,
		activity: Activity,
		areaKey: String,
		onCloseListener: OpenAdCloseListener?,
		onLoadedListener: OpenAdLoadedListener?,
		onPaidListener: OpenAdPaidListener?,
	) {
		onLoadedListener?.onLoaded()
		LogUtil.log(
			LogAdData.ad_occur,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_open,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
				LogAdParam.ad_preload to true,
			)
		)
		val startShowAd = System.currentTimeMillis()
		admobOpenAd.fullScreenContentCallback = object : FullScreenContentCallback() {
			override fun onAdDismissedFullScreenContent() {
				LogUtil.log(
					LogAdData.ad_close,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to (System.currentTimeMillis() - startShowAd),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_open,
						LogAdParam.ad_source to (admobOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
						LogAdParam.ad_preload to true,
					)
				)
				onCloseListener?.onClose()
			}

			override fun onAdFailedToShowFullScreenContent(adError: AdError) {
				Log.e(TAG, adError.message)
				onCloseListener?.onClose()
			}

			override fun onAdImpression() {
				AdvCheckManager.params.openTimes++
			}

			override fun onAdClicked() {
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to (System.currentTimeMillis() - startShowAd),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_open,
						LogAdParam.ad_source to (admobOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
						LogAdParam.ad_preload to true,
					)
				)
				AppOpenHelper.spSwitch = true
			}
		}
		admobOpenAd.onPaidEventListener = paidListener(
			ad = admobOpenAd,
			areaKey = areaKey,
			preload = true,
			onPaidListener = onPaidListener
		)
		admobOpenAd.show(activity)
	}

	fun showAdmobOpenAd(
		context: Activity,
		areaKey: String,
		onCloseListener: OpenAdCloseListener?,
		onLoadedListener: OpenAdLoadedListener?,
		onPaidListener: OpenAdPaidListener?,
	) {
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
		val startLoadingTime = System.currentTimeMillis()
		AppOpenAd.load(
			context,
			AdvIDs.getAdmobOpenId(),
			AdRequest.Builder().build(),
			object : AppOpenAd.AppOpenAdLoadCallback() {
				override fun onAdLoaded(admobAppOpenAd: AppOpenAd) {
					onLoadedListener?.onLoaded()
					LogUtil.log(
						LogAdData.ad_finish_loading,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
							LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
							LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
							LogAdParam.ad_preload to false,
						)
					)
					admobAppOpenAd.fullScreenContentCallback = object : FullScreenContentCallback() {
						override fun onAdDismissedFullScreenContent() {
							LogUtil.log(
								LogAdData.ad_close,
								mapOf(
									LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
									LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
									LogAdParam.ad_areakey to areaKey,
									LogAdParam.ad_format to LogAdParam.ad_format_open,
									LogAdParam.ad_source to (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
									LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
									LogAdParam.ad_preload to false,
								)
							)
							onCloseListener?.onClose()
						}

						override fun onAdFailedToShowFullScreenContent(adError: AdError) {
							Log.e(TAG, adError.message)
							onCloseListener?.onClose()
						}

						override fun onAdImpression() {
							AdvCheckManager.params.openTimes++
						}

						override fun onAdClicked() {
							LogUtil.log(
								LogAdData.ad_click,
								mapOf(
									LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
									LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
									LogAdParam.ad_areakey to areaKey,
									LogAdParam.ad_format to LogAdParam.ad_format_open,
									LogAdParam.ad_source to (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
									LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
									LogAdParam.ad_preload to false,
								)
							)
							AppOpenHelper.spSwitch = true
						}
					}
					admobAppOpenAd.onPaidEventListener = paidListener(
						ad = admobAppOpenAd,
						areaKey = areaKey,
						preload = false,
						onPaidListener = onPaidListener
					)
					admobAppOpenAd.show(context)
				}

				override fun onAdFailedToLoad(loadAdError: LoadAdError) {
					Log.e(TAG, "showAdmobOpenAd failed: ${loadAdError.message}")
					onCloseListener?.onClose()
				}
			},
		)
	}

	private fun paidListener(
		ad: AppOpenAd,
		areaKey: String,
		preload: Boolean,
		onPaidListener: OpenAdPaidListener?,
	): OnPaidEventListener = OnPaidEventListener { adValue ->
		val revenue = adValue.valueMicros / 1_000_000.0
		LogUtil.logSingularAdRevenue(LogAdParam.OpenAd, LogAdParam.adMob, revenue)
		logOpenRevenue(LogAdData.ad_impression, areaKey, ad, adValue.currencyCode, revenue, preload)
		logOpenRevenue(LogAdData.ad_revenue, areaKey, ad, adValue.currencyCode, revenue, preload)
		LogUtil.logTaiChiAdmob(adValue)
		onPaidListener?.onPaid(adValue.valueMicros)
	}

	private fun logOpenRevenue(
		eventName: String,
		areaKey: String,
		ad: AppOpenAd,
		currency: String,
		revenue: Double,
		preload: Boolean,
	) {
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
				LogAdParam.ad_preload to preload,
			)
		)
	}
}
