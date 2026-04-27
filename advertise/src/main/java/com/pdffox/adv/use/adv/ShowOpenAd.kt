package com.pdffox.adv.use.adv

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.pdffox.adv.use.Config
import com.pdffox.adv.use.adv.policy.AdPolicyManager
import com.pdffox.adv.use.log.LogAdData
import com.pdffox.adv.use.log.LogAdParam
import com.pdffox.adv.use.log.LogUtil
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import com.thinkup.core.api.TUAdInfo
import com.thinkup.splashad.api.TUSplashAd
import com.thinkup.splashad.api.TUSplashAdEZListener
import com.thinkup.splashad.api.TUSplashAdExtraInfo
import com.thinkup.splashad.api.TUSplashAdListener
import org.json.JSONException
import org.json.JSONObject

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

	fun showOpenAd(activity: Activity, areaKey: String, onCloseListener: OpenAdCloseListener?, onLoadedListener: OpenAdLoadedListener?, onPaidListener: OpenAdPaidListener?) {
		if (AdvCheckManager.params.limitTime > System.currentTimeMillis()) {
			Log.e(TAG, "showOpenAd: onClose 1")
			onCloseListener?.onClose()
			return
		}
		if (AdConfig.isNewAdPolicy) {
			if (!AdPolicyManager.checkAdUnit(areaKey)) {
				Log.e(TAG, "showOpenAd: onClose 2")
				onCloseListener?.onClose()
				return
			}
		}
		if (ShowInterAd.isShowing) {
			Log.e(TAG, "showOpenAd: onClose 3")
			onCloseListener?.onClose()
			return
		}
		if (Config.showAdPlatform == LogAdParam.ad_platform_bidding) {
			val maxOpenAd = AdPool.getMaxOpen(areaKey, {}, {
				if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadOpen(activity)
				}
			})
			val topOn = AdPool.getTopOnOpen(areaKey, {}, {
				if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadOpen(activity)
				}
			})
			when {
				maxOpenAd != null && topOn != null -> {
					val maxAd: MaxAd = maxOpenAd as MaxAd
					if (maxAd.revenue >= topOn.checkAdStatus().tuTopAdInfo.publisherRevenue) {
						showMaxOpenAdFromCache(maxOpenAd, activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
					} else {
						showTopOnOpenAdFromCache(topOn, activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
					}
				}
				maxOpenAd != null -> {
					showMaxOpenAdFromCache(maxOpenAd, activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
				}
				topOn != null -> {
					showTopOnOpenAdFromCache(topOn, activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
				}
				else -> {
					val admobOpenAd = AdPool.getAdmobOpen(areaKey, {}, {
						if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
							AdLoader.loadOpen(activity)
						}
					})
					if (admobOpenAd != null) {
						showAdmobOpenAdFromCache(admobOpenAd, activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
					} else {
						showAdmobOpenAd(activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
					}
				}
			}
		}
		if (Config.showAdPlatform == LogAdParam.ad_platform_admob) {
			val admobOpenAd = AdPool.getAdmobOpen(areaKey, {
				Log.e(TAG, "showOpenAd: onClose 4")
				onCloseListener?.onClose()
			}, {
				if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadOpen(activity)
				}
			})
			if (admobOpenAd != null) {
				showAdmobOpenAdFromCache(admobOpenAd, activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
			} else {
				showAdmobOpenAd(activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
			}
		}
		if (Config.showAdPlatform == LogAdParam.ad_platform_max) {
			val maxOpenAd = AdPool.getMaxOpen(areaKey, {}, {
				if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadOpen(activity)
				}
			})
			if (maxOpenAd != null) {
				showMaxOpenAdFromCache(maxOpenAd, activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
			} else {
				showMAXOpenAd(activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
			}
		}
		if (Config.showAdPlatform == LogAdParam.ad_platform_topon) {
			val tuAd = AdPool.getTopOnOpen(areaKey, {}, {
				if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadOpen(activity)
				}
			})
			if(tuAd != null){
				showTopOnOpenAdFromCache(tuAd, activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
			} else {
				showTopOn(activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
			}
		}
	}

	fun showAdmobOpenAdFromCache(admobOpenAd: AppOpenAd, activity: Activity, areaKey: String, onCloseListener: OpenAdCloseListener?, onLoadedListener: OpenAdLoadedListener?, onPaidListener: OpenAdPaidListener?){
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
		admobOpenAd.fullScreenContentCallback =
			object : FullScreenContentCallback() {
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
					Log.e(TAG, "showOpenAd: onClose 5")
					onCloseListener?.onClose()
				}

				override fun onAdFailedToShowFullScreenContent(adError: AdError) {
					Log.e(TAG, adError.message)
					Log.e(TAG, "showOpenAd: onClose 6")
					onCloseListener?.onClose()
				}

				override fun onAdShowedFullScreenContent() {
					Log.e(TAG, "Ad showed fullscreen content.")
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
		admobOpenAd.onPaidEventListener = OnPaidEventListener { adValue -> // 可获取的核心参数：
			val micros = adValue.valueMicros         // 广告价值（微元单位，需除以1,000,000得到实际金额）
			val currency = adValue.currencyCode     // ISO 4217货币代码（如："USD"）
			val precision = adValue.precisionType    // 金额精度类型（0=估算，1=发布商定义，2=精确计算）
			// 收入跟踪（示例：转换为美元）
			val revenue = micros / 1_000_000.0
			val att = JSONObject()
			try {
				att.put(LogAdParam.revenue, revenue)
				att.put(LogAdParam.adType, LogAdParam.OpenAd)
			} catch (e: JSONException) {
				e.printStackTrace()
				Log.e(TAG, "loadAdmobInterstitialAd: ", e)
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
					FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobOpenId(),
					FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
					FirebaseAnalytics.Param.AD_SOURCE to (admobOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
					FirebaseAnalytics.Param.CURRENCY to currency,
					FirebaseAnalytics.Param.VALUE to revenue,
					LogAdParam.ad_preload to true,
				)
			)
			LogUtil.log(
				LogAdData.ad_revenue,
				mapOf(
					LogAdParam.ad_areakey to areaKey,
					FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
					FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobOpenId(),
					FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
					FirebaseAnalytics.Param.AD_SOURCE to (admobOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
					FirebaseAnalytics.Param.CURRENCY to currency,
					FirebaseAnalytics.Param.VALUE to revenue,
					LogAdParam.ad_preload to true,
				)
			)
			LogUtil.logTaiChiAdmob(adValue)
			onPaidListener?.onPaid(micros)
		}
		admobOpenAd.show(activity)
	}

	fun showMaxOpenAdFromCache(maxOpenAd: MaxAppOpenAd, activity: Activity, areaKey: String, onCloseListener: OpenAdCloseListener?, onLoadedListener: OpenAdLoadedListener?, onPaidListener: OpenAdPaidListener?) {
		maxOpenAd.apply {
			onLoadedListener?.onLoaded()
			LogUtil.log(
				LogAdData.ad_occur,
				mapOf(
					LogAdParam.ad_platform to LogAdParam.ad_platform_max,
					LogAdParam.ad_areakey to areaKey,
					LogAdParam.ad_format to LogAdParam.ad_format_open,
					LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
					LogAdParam.ad_preload to true,
				)
			)
			val startShowAd = System.currentTimeMillis()
			this.setListener(object : MaxAdListener {
				override fun onAdLoaded(maxAd: MaxAd) {
					onLoadedListener?.onLoaded()
					LogUtil.log(
						LogAdData.ad_finish_loading,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_max,
							LogAdParam.duration to (System.currentTimeMillis() - startShowAd),
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to maxAd.networkName,
							LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
							LogAdParam.ad_preload to true,
						)
					)
					maxAppOpenAd?.showAd()
				}

				override fun onAdDisplayed(maxAd: MaxAd) {
					AdvCheckManager.params.openTimes++

					LogUtil.log(
						LogAdData.ad_impression,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_max,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.MAX_OPEN_ID,
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
							LogAdParam.ad_source to maxAd.networkName,
							FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
							FirebaseAnalytics.Param.VALUE to maxAd.revenue,
							LogAdParam.ad_preload to true,
						)
					)
					maxAppOpenAd?.loadAd()
				}

				override fun onAdHidden(maxAd: MaxAd) {
					LogUtil.log(
						LogAdData.ad_close,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_max,
							LogAdParam.duration to (System.currentTimeMillis() - startShowAd),
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to maxAd.networkName,
							LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
							LogAdParam.ad_preload to true,
						)
					)
					maxAppOpenAd?.loadAd()
					onCloseListener?.onClose()
				}

				override fun onAdClicked(maxAd: MaxAd) {
					LogUtil.log(
						LogAdData.ad_click,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_max,
							LogAdParam.duration to (System.currentTimeMillis() - startShowAd),
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to maxAd.networkName,
							LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
							LogAdParam.ad_preload to true,
						)
					)
					AppOpenHelper.spSwitch = true
				}

				override fun onAdLoadFailed(p0: String, p1: MaxError) {
					maxAppOpenAd?.loadAd()
					onCloseListener?.onClose()
				}

				override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
					maxAppOpenAd?.loadAd()
					onCloseListener?.onClose()
				}
			})
			this.setRevenueListener { maxAd: MaxAd? ->
				val revenue = maxAd!!.revenue
				val att = JSONObject()
				try {
					att.put(LogAdParam.revenue, revenue)
					att.put(LogAdParam.adType, LogAdParam.OpenAd)
				} catch (e: JSONException) {
					e.printStackTrace()
					Log.e(TAG, "createInterstitialAd: ", e)
				}
				Singular.eventJSON(LogAdData.ad_revenue, att)
				if (revenue > 0) {
					val data = SingularAdData(
						LogAdParam.ad_platform_max,
						LogAdParam.USD,
						revenue
					)
					Singular.adRevenue(data)
				}
				LogUtil.log(
					LogAdData.ad_revenue,
					mapOf(
						LogAdParam.ad_areakey to areaKey,
						FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_max,
						FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.MAX_OPEN_ID,
						FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
						LogAdParam.ad_source to maxAd.networkName,
						FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
						FirebaseAnalytics.Param.VALUE to maxAd.revenue,
						LogAdParam.ad_preload to true,
					)
				)
				LogUtil.logTaiChiMax(maxAd)
				onPaidListener?.onPaid((maxAd.revenue * 1000).toLong())
			}
			showAd()
		}
	}

	fun showTopOnOpenAdFromCache(tuAd: TUSplashAd, activity: Activity, areaKey: String, onCloseListener: OpenAdCloseListener?, onLoadedListener: OpenAdLoadedListener?, onPaidListener: OpenAdPaidListener?) {
		// 播放从缓存获取的TopOn广告
		tuAd.apply {
			onLoadedListener?.onLoaded()
			LogUtil.log(
				LogAdData.ad_occur,
				mapOf(
					LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
					LogAdParam.ad_areakey to areaKey,
					LogAdParam.ad_format to LogAdParam.ad_format_open,
					LogAdParam.ad_unit_name to AdvIDs.TopON_OPEN_ID,
					LogAdParam.ad_preload to true,
				)
			)
			val startShowAd = System.currentTimeMillis()
			setAdListener(object : TUSplashAdListener {
				override fun onAdLoaded(p0: Boolean) {}
				override fun onAdLoadTimeout() {
					onCloseListener?.onClose()
				}
				override fun onNoAdError(p0: com.thinkup.core.api.AdError?) {
					onCloseListener?.onClose()
				}

				override fun onAdShow(p0: TUAdInfo?) {
					AdvCheckManager.params.openTimes++
					LogUtil.log(
						LogAdData.ad_impression,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_topon,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.TopON_OPEN_ID,
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
							LogAdParam.ad_source to ShowOpenAd.tuAd.checkAdStatus().tuTopAdInfo.networkName,
							FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
							FirebaseAnalytics.Param.VALUE to ShowOpenAd.tuAd.checkAdStatus().tuTopAdInfo.publisherRevenue,
							LogAdParam.ad_preload to false,
						)
					)

					val revenue = ShowOpenAd.tuAd.checkAdStatus().tuTopAdInfo.publisherRevenue
					val att = JSONObject()
					try {
						att.put(LogAdParam.revenue, revenue)
						att.put(LogAdParam.adType, LogAdParam.OpenAd)
					} catch (e: JSONException) {
						e.printStackTrace()
						Log.e(TAG, "createInterstitialAd: ", e)
					}
					Singular.eventJSON(LogAdData.ad_revenue, att)
					if (revenue > 0) {
						val data = SingularAdData(
							LogAdParam.ad_platform_topon,
							LogAdParam.USD,
							revenue
						)
						Singular.adRevenue(data)
					}
					LogUtil.log(
						LogAdData.ad_revenue,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_topon,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.TopON_OPEN_ID,
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
							LogAdParam.ad_source to ShowOpenAd.tuAd.checkAdStatus().tuTopAdInfo.networkName,
							FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
							FirebaseAnalytics.Param.VALUE to ShowOpenAd.tuAd.checkAdStatus().tuTopAdInfo.publisherRevenue,
							LogAdParam.ad_preload to false,
						)
					)
					LogUtil.logTaiChiTopOn(ShowOpenAd.tuAd)
					onPaidListener?.onPaid((ShowOpenAd.tuAd.checkAdStatus().tuTopAdInfo.publisherRevenue * 1000).toLong())
				}

				override fun onAdClick(p0: TUAdInfo?) {
					LogUtil.log(
						LogAdData.ad_click,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
							LogAdParam.duration to (System.currentTimeMillis() - startShowAd),
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to ShowOpenAd.tuAd.checkAdStatus().tuTopAdInfo.networkName,
							LogAdParam.ad_unit_name to AdvIDs.TopON_OPEN_ID,
							LogAdParam.ad_preload to false,
						)
					)
					AppOpenHelper.spSwitch = true
				}

				override fun onAdDismiss(
					p0: TUAdInfo?,
					p1: TUSplashAdExtraInfo?
				) {
					LogUtil.log(
						LogAdData.ad_close,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
							LogAdParam.duration to (System.currentTimeMillis() - startShowAd),
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to ShowOpenAd.tuAd.checkAdStatus().tuTopAdInfo.networkName,
							LogAdParam.ad_unit_name to AdvIDs.TopON_OPEN_ID,
							LogAdParam.ad_preload to true,
						)
					)
					onCloseListener?.onClose()
				}
			})
			val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
			ShowOpenAd.tuAd.show(activity, rootView)
		}
	}

	fun showAdmobOpenAd(context: Activity, areaKey: String, onCloseListener: OpenAdCloseListener?, onLoadedListener: OpenAdLoadedListener?, onPaidListener: OpenAdPaidListener?) {
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
							LogAdParam.ad_source to (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: "unknow"),
							LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
							LogAdParam.ad_preload to false,
						)
					)
					admobAppOpenAd.fullScreenContentCallback =
						object : FullScreenContentCallback() {
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
								Log.e(TAG, "showOpenAd: onClose 7")
							}

							override fun onAdFailedToShowFullScreenContent(adError: AdError) {
								Log.e(TAG, adError.message)
								onCloseListener?.onClose()
								Log.e(TAG, "showOpenAd: onClose 8")
							}

							override fun onAdShowedFullScreenContent() {
								Log.e(TAG, "Ad showed fullscreen content.")
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
					admobAppOpenAd.onPaidEventListener = OnPaidEventListener { adValue -> // 可获取的核心参数：
						val micros = adValue.valueMicros         // 广告价值（微元单位，需除以1,000,000得到实际金额）
						val currency = adValue.currencyCode     // ISO 4217货币代码（如："USD"）
						val precision = adValue.precisionType    // 金额精度类型（0=估算，1=发布商定义，2=精确计算）
						// 收入跟踪（示例：转换为美元）
						val revenue = micros / 1_000_000.0
						val att = JSONObject()
						try {
							att.put(LogAdParam.revenue, revenue)
							att.put(LogAdParam.adType, LogAdParam.OpenAd)
						} catch (e: JSONException) {
							e.printStackTrace()
							Log.e(TAG, "loadAdmobInterstitialAd: ", e)
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
								FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobOpenId(),
								FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
								FirebaseAnalytics.Param.AD_SOURCE to (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
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
								FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobOpenId(),
								FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
								FirebaseAnalytics.Param.AD_SOURCE to (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
								FirebaseAnalytics.Param.CURRENCY to currency,
								FirebaseAnalytics.Param.VALUE to revenue,
								LogAdParam.ad_preload to false,
							)
						)
						LogUtil.logTaiChiAdmob(adValue)
						onPaidListener?.onPaid(micros)
					}
					admobAppOpenAd.show(context)
				}

				override fun onAdFailedToLoad(loadAdError: LoadAdError) {
					Log.e(TAG, "showOpenAd: onClose 9")

					onCloseListener?.onClose()
				}
			},
		)
	}

	private var maxAppOpenAd: MaxAppOpenAd? = null
	fun showMAXOpenAd(activity: Activity, areaKey: String, onCloseListener: OpenAdCloseListener?, onLoadedListener: OpenAdLoadedListener?, onPaidListener: OpenAdPaidListener?) {
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_max,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_open,
				LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
				LogAdParam.ad_preload to false,
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		maxAppOpenAd = MaxAppOpenAd(AdvIDs.MAX_OPEN_ID).apply {
			this.setListener(object : MaxAdListener {
				override fun onAdLoaded(maxAd: MaxAd) {
					onLoadedListener?.onLoaded()
					LogUtil.log(
						LogAdData.ad_finish_loading,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_max,
							LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to maxAd.networkName,
							LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
							LogAdParam.ad_preload to false,
						)
					)
					maxAppOpenAd?.showAd()
				}

				override fun onAdDisplayed(maxAd: MaxAd) {
					AdvCheckManager.params.openTimes++
					LogUtil.log(
						LogAdData.ad_impression,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_max,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.MAX_OPEN_ID,
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
							LogAdParam.ad_source to maxAd.networkName,
							FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
							FirebaseAnalytics.Param.VALUE to maxAd.revenue,
							LogAdParam.ad_preload to false,
						)
					)
				}

				override fun onAdHidden(maxAd: MaxAd) {
					LogUtil.log(
						LogAdData.ad_close,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_max,
							LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to maxAd.networkName,
							LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
							LogAdParam.ad_preload to false,
						)
					)
					onCloseListener?.onClose()
				}

				override fun onAdClicked(maxAd: MaxAd) {
					LogUtil.log(
						LogAdData.ad_click,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_max,
							LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to maxAd.networkName,
							LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
							LogAdParam.ad_preload to false,
						)
					)
					AppOpenHelper.spSwitch = true
				}

				override fun onAdLoadFailed(p0: String, p1: MaxError) {
					onCloseListener?.onClose()
				}

				override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
					onCloseListener?.onClose()
				}
			})
			this.setRevenueListener { maxAd: MaxAd? ->
				val revenue = maxAd!!.revenue
				val att = JSONObject()
				try {
					att.put(LogAdParam.revenue, revenue)
					att.put(LogAdParam.adType, LogAdParam.OpenAd)
				} catch (e: JSONException) {
					e.printStackTrace()
					Log.e(TAG, "createInterstitialAd: ", e)
				}
				Singular.eventJSON(LogAdData.ad_revenue, att)
				if (revenue > 0) {
					val data = SingularAdData(
						LogAdParam.ad_platform_max,
						LogAdParam.USD,
						revenue
					)
					Singular.adRevenue(data)
				}
				LogUtil.log(
					LogAdData.ad_revenue,
					mapOf(
						LogAdParam.ad_areakey to areaKey,
						FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_max,
						FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.MAX_OPEN_ID,
						FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
						LogAdParam.ad_source to maxAd.networkName,
						FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
						FirebaseAnalytics.Param.VALUE to maxAd.revenue,
						LogAdParam.ad_preload to false,
					)
				)
				LogUtil.logTaiChiMax(maxAd)
				onPaidListener?.onPaid((maxAd.revenue * 1000).toLong())
			}
			this.loadAd()
		}
	}

	private lateinit var tuAd: TUSplashAd
	fun showTopOn(activity: Activity, areaKey: String, onCloseListener: OpenAdCloseListener?, onLoadedListener: OpenAdLoadedListener?, onPaidListener: OpenAdPaidListener?) {
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_open,
				LogAdParam.ad_unit_name to AdvIDs.TopON_OPEN_ID,
				LogAdParam.ad_preload to false,
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		tuAd = TUSplashAd(activity, AdvIDs.TopON_OPEN_ID, object : TUSplashAdEZListener() {
			override fun onAdLoaded() {
				Log.e(TAG, "showTopOn onAdLoaded: " + tuAd.checkAdStatus().tuTopAdInfo.publisherRevenue)
				onLoadedListener?.onLoaded()
				LogUtil.log(
					LogAdData.ad_finish_loading,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_open,
						LogAdParam.ad_source to tuAd.checkAdStatus().tuTopAdInfo.networkName,
						LogAdParam.ad_unit_name to AdvIDs.TopON_OPEN_ID,
						LogAdParam.ad_preload to false,
					)
				)
				val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
				tuAd.show(activity, rootView)
			}

			override fun onNoAdError(p0: com.thinkup.core.api.AdError?) {
				Log.e(TAG, "showTopOn onNoAdError: " + p0?.desc)
				onCloseListener?.onClose()
			}

			override fun onAdShow(adInfo: TUAdInfo) {
				AdvCheckManager.params.openTimes++
				LogUtil.log(
					LogAdData.ad_impression,
					mapOf(
						LogAdParam.ad_areakey to areaKey,
						FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_topon,
						FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.TopON_OPEN_ID,
						FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
						LogAdParam.ad_source to tuAd.checkAdStatus().tuTopAdInfo.networkName,
						FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
						FirebaseAnalytics.Param.VALUE to tuAd.checkAdStatus().tuTopAdInfo.publisherRevenue,
						LogAdParam.ad_preload to false,
					)
				)

				val revenue = tuAd.checkAdStatus().tuTopAdInfo.publisherRevenue
				val att = JSONObject()
				try {
					att.put(LogAdParam.revenue, revenue)
					att.put(LogAdParam.adType, LogAdParam.OpenAd)
				} catch (e: JSONException) {
					e.printStackTrace()
					Log.e(TAG, "createInterstitialAd: ", e)
				}
				Singular.eventJSON(LogAdData.ad_revenue, att)
				if (revenue > 0) {
					val data = SingularAdData(
						LogAdParam.ad_platform_topon,
						LogAdParam.USD,
						revenue
					)
					Singular.adRevenue(data)
				}
				LogUtil.log(
					LogAdData.ad_revenue,
					mapOf(
						LogAdParam.ad_areakey to areaKey,
						FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_topon,
						FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.TopON_OPEN_ID,
						FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
						LogAdParam.ad_source to tuAd.checkAdStatus().tuTopAdInfo.networkName,
						FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
						FirebaseAnalytics.Param.VALUE to tuAd.checkAdStatus().tuTopAdInfo.publisherRevenue,
						LogAdParam.ad_preload to false,
					)
				)
				LogUtil.logTaiChiTopOn(tuAd)
				onPaidListener?.onPaid((ShowOpenAd.tuAd.checkAdStatus().tuTopAdInfo.publisherRevenue * 1000).toLong())
			}

			override fun onAdClick(adInfo: TUAdInfo) {
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_open,
						LogAdParam.ad_source to tuAd.checkAdStatus().tuTopAdInfo.networkName,
						LogAdParam.ad_unit_name to AdvIDs.TopON_OPEN_ID,
						LogAdParam.ad_preload to false,
					)
				)
				AppOpenHelper.spSwitch = true
			}

			override fun onAdDismiss(adInfo: TUAdInfo, splashAdExtraInfo: TUSplashAdExtraInfo) {
				onCloseListener?.onClose()
			}
		}, 2000)
		tuAd.loadAd()
	}

}