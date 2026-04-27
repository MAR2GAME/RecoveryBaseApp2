package com.pdffox.adv.use.adv

import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
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
import com.pdffox.adv.use.AdvApplicaiton
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.use.Config
import com.pdffox.adv.use.adv.policy.AdPolicyManager
import com.pdffox.adv.use.log.LogAdData
import com.pdffox.adv.use.log.LogAdParam
import com.pdffox.adv.use.log.LogUtil
import com.pdffox.adv.use.util.PreferenceDelegate
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import com.thinkup.core.api.TUAdInfo
import com.thinkup.splashad.api.TUSplashAd
import com.thinkup.splashad.api.TUSplashAdEZListener
import com.thinkup.splashad.api.TUSplashAdExtraInfo
import org.json.JSONException
import org.json.JSONObject

object AppOpenHelper: DefaultLifecycleObserver {

	private const val TAG = "AppOpenHelper"

	var spSwitch = false

	private var isAppInBackground = false

	var showOpenAdvTime: Long by PreferenceDelegate("showOpenAdvTime", 0L)
	var enterBackgroundTime = 0L

	var hasInitAdmob = false
	var hasInitMax = false
	var hasInitTopOn = false

	var areaKey = LogAdParam.foregroundKey

	var isShowing = false

	override fun onStart(owner: LifecycleOwner) {
		super.onStart(owner)
		if (BuildConfig.DEBUG && spSwitch) {
			Log.e(TAG, "onStart: spSwitch = $spSwitch" )
			Toast.makeText(AdvApplicaiton.instance, "onStart: spSwitch = $spSwitch", Toast.LENGTH_SHORT).show()
			spSwitch = false
			return
		}
		if (!BuildConfig.DEBUG && (Config.isGoogleIP || Config.paid_0)) {
			return
		}
		if (isAppInBackground) {
			isAppInBackground = false
			Log.e(TAG, "onStart: $showOpenAdvTime ${AdvCheckManager.params.limitTime} ${AdvCheckManager.params.backgroundDuration} ${System.currentTimeMillis()}", )
			if (AdvCheckManager.params.limitTime > System.currentTimeMillis()) {
				Log.e(TAG, "onStart: 触发广告限制", )
				return
			}
			if (isShowing || ShowInterAd.isShowing) {
				Log.e(TAG, "onStart: 广告正在展示")
				return
			} else {
				Log.e(TAG, "onStart: 没有广告正在展示可以展示广告 ${AdvCheckManager.params.backgroundDuration}", )
			}
			if (AdConfig.isNewAdPolicy) {
				showAdIfReady()
			} else if (enterBackgroundTime + AdvCheckManager.params.backgroundDuration * 1000 < System.currentTimeMillis()) {
				if (AdConfig.isOpenAppOpenHelper) {
					showAdIfReady()
				}
			}
		}
	}

	override fun onStop(owner: LifecycleOwner) {
		super.onStop(owner)
		if (BuildConfig.DEBUG && spSwitch) {
			Log.e(TAG, "onStop: spSwitch = $spSwitch" )
			Toast.makeText(AdvApplicaiton.instance, "onStop: spSwitch = $spSwitch", Toast.LENGTH_SHORT).show()
			return
		}
		if (!BuildConfig.DEBUG && (Config.isGoogleIP || Config.paid_0)) {
			return
		}
		isAppInBackground = true
		enterBackgroundTime = System.currentTimeMillis()
		Log.e(TAG, "onStop: $enterBackgroundTime" )
		if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_ENTER_BACKGROUND)) {
			AdLoader.loadOpen(AdvApplicaiton.instance)
		}
		if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_ENTER_BACKGROUND)) {
			AdLoader.loadInter(AdvApplicaiton.instance)
		}
	}

	fun startObserve() {
		ProcessLifecycleOwner.get().lifecycle.addObserver(this)
	}

	fun showAdIfReady() {
		Log.e(TAG, "showAdIfReady: ${AdvCheckManager.params.limitTime > System.currentTimeMillis()}", )
		if (AdvCheckManager.params.limitTime > System.currentTimeMillis()) {
			return
		}
		if (AdConfig.isNewAdPolicy) {
			if (!AdPolicyManager.checkAdUnit(areaKey)) {
				return
			}
		}
		if (Config.showAdPlatform == LogAdParam.ad_platform_bidding) {
			val maxOpenAd = AdPool.getMaxOpen(areaKey, {
				isShowing = false
			}, {
				isShowing = true
				if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadOpen(AdvApplicaiton.instance)
				}
			})
			val topOnOpenAd = AdPool.getTopOnOpen(areaKey, {
				isShowing = false
			}, {
				isShowing = true
				if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadOpen(AdvApplicaiton.instance)
				}
			})
			when {
				maxOpenAd != null && topOnOpenAd != null -> {
					val maxAd: MaxAd = maxOpenAd as MaxAd
					if (maxAd.revenue >= topOnOpenAd.checkAdStatus().tuTopAdInfo.publisherRevenue) {
						AdvApplicaiton.instance.currentActivity?.let {
							maxOpenAd.showAd()
						}
					} else {
						AdvApplicaiton.instance.currentActivity?.let {
							val rootView = it.findViewById<ViewGroup>(android.R.id.content)
							topOnOpenAd.show(it, rootView)
						}
					}
				}
				maxOpenAd != null -> {
					AdvApplicaiton.instance.currentActivity?.let {
						maxOpenAd.showAd()
					}
				}
				topOnOpenAd != null -> {
					AdvApplicaiton.instance.currentActivity?.let {
						val rootView = it.findViewById<ViewGroup>(android.R.id.content)
						topOnOpenAd.show(it, rootView)
					}
				}
				else -> {
					val admobOpenAd = AdPool.getAdmobOpen(areaKey, {
						isShowing = false
					}, {
						isShowing = true
						if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
							AdLoader.loadOpen(AdvApplicaiton.instance)
						}
					})
					if (admobOpenAd != null) {
						AdvApplicaiton.instance.currentActivity?.let {
							admobOpenAd.show(it)
						}
					} else {
						showAdmob()
					}
				}
			}
		} else if (Config.showAdPlatform == LogAdParam.ad_platform_admob) {
			val admobOpenAd = AdPool.getAdmobOpen(areaKey, {
				isShowing = false
			}, {
				isShowing = true
				if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadOpen(AdvApplicaiton.instance)
				}
			})
			if (admobOpenAd != null) {
				AdvApplicaiton.instance.currentActivity?.let {
					admobOpenAd.show(it)
				}
			} else {
				showAdmob()
			}
		} else if (Config.showAdPlatform == LogAdParam.ad_platform_max) {
			val maxOpenAd = AdPool.getMaxOpen(areaKey, {
				isShowing = false
			}, {
				isShowing = true
				if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadOpen(AdvApplicaiton.instance)
				}
			})
			if (maxOpenAd != null) {
				AdvApplicaiton.instance.currentActivity?.let {
					maxOpenAd.showAd()
				}
			} else {
				showMax()
			}
		} else if (Config.showAdPlatform == LogAdParam.ad_platform_topon) {
			val topOnOpenAd = AdPool.getTopOnOpen(areaKey, {
				isShowing = false
			}, {
				isShowing = true
				if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadOpen(AdvApplicaiton.instance)
				}
			})
			if (topOnOpenAd != null) {
				AdvApplicaiton.instance.currentActivity?.let {
					val rootView = it.findViewById<ViewGroup>(android.R.id.content)
					topOnOpenAd.show(it, rootView)
				}
			} else {
				showTopOn()
			}
		}
	}

	private var isLoadingAdmobAd = false
	var isShowingAdmobAd = false

	private fun showAdmob() {
		Log.e(TAG, "showAdmob: " + AdvIDs.getAdmobOpenId() )
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
				AdvApplicaiton.instance,
			AdvIDs.getAdmobOpenId(),
			AdRequest.Builder().build(),
			object : AppOpenAd.AppOpenAdLoadCallback() {
				override fun onAdLoaded(ad: AppOpenAd) {
					LogUtil.log(
						LogAdData.ad_finish_loading,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
							LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: "unknow"),
							LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
							LogAdParam.ad_preload to false,
						)
					)
					isLoadingAdmobAd = false

					ad.fullScreenContentCallback =
						object : FullScreenContentCallback() {
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
								Log.e(TAG, "Ad showed fullscreen content.")
							}

							override fun onAdImpression() {
								AdvCheckManager.params.openTimes++
								if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
									AdLoader.loadOpen(AdvApplicaiton.instance)
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
								AppOpenHelper.spSwitch = true
							}
						}
					ad.onPaidEventListener =
						OnPaidEventListener { adValue -> // 可获取的核心参数：
							val micros =
								adValue.valueMicros         // 广告价值（微元单位，需除以1,000,000得到实际金额）
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
									FirebaseAnalytics.Param.AD_SOURCE to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName
										?: LogAdParam.unknow),
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
									FirebaseAnalytics.Param.AD_SOURCE to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName
										?: LogAdParam.unknow),
									FirebaseAnalytics.Param.CURRENCY to currency,
									FirebaseAnalytics.Param.VALUE to revenue,
									LogAdParam.ad_preload to false,
								)
							)
							LogUtil.logTaiChiAdmob(adValue)
						}

					if (AdvApplicaiton.instance.currentActivity == null) {
						Log.e(TAG, "onAdLoaded: currentActivity is null")
					}
					AdvApplicaiton.instance.currentActivity?.let {
						ad.show(it)
					}
				}

				override fun onAdFailedToLoad(loadAdError: LoadAdError) {
					isShowingAdmobAd = false
					Log.e(TAG, "onAdFailedToLoad: ${loadAdError.message}")
				}
			},
		)
	}

	private fun showMax() {
		Log.e(TAG, "showMax: ", )
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
		LogUtil.log(
			LogAdData.ad_occur,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_max,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_open,
				LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
				LogAdParam.ad_preload to false,
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		MaxAppOpenAd(AdvIDs.MAX_OPEN_ID).apply {
			this.setListener(object : MaxAdListener {
				override fun onAdLoaded(maxAd: MaxAd) {
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
					this@apply.showAd()
				}

				override fun onAdDisplayed(maxAd: MaxAd) {
					isShowing = true
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
					if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
						AdLoader.loadOpen(AdvApplicaiton.instance)
					}
				}

				override fun onAdHidden(maxAd: MaxAd) {
					isShowing = false
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
				}

				override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
					isShowing = false
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
			}
			this.loadAd()
		}
	}

	private lateinit var tuAd: TUSplashAd
	fun showTopOn() {
			AdvApplicaiton.instance.currentActivity?.let { activity ->
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
				}

				override fun onAdShow(adInfo: TUAdInfo) {
					isShowing = true
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
					isShowing = false
				}
			}, 2000)
			tuAd.loadAd()
		}
	}
}