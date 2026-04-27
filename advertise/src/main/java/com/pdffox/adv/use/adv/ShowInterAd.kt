package com.pdffox.adv.use.adv

import android.os.Handler
import android.util.Log
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.use.Config
import com.pdffox.adv.use.adv.policy.AdPolicyManager
import com.pdffox.adv.use.log.LogAdData
import com.pdffox.adv.use.log.LogAdParam
import com.pdffox.adv.use.log.LogUtil
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import com.thinkup.core.api.TUAdInfo
import com.thinkup.interstitial.api.TUInterstitial
import com.thinkup.interstitial.api.TUInterstitialListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

object ShowInterAd {
	private const val TAG = "ShowInterAd"
	var isShowing = false
	var areaKey: String = ""
	var onClosed: () -> Unit = {}

	private const val maxRetryCount = 3
	private const val timeoutMillis = 6000L

	private var admobRetryCount = 0
	private var maxRetryCountCurrent = 0
	private var topOnRetryCountCurrent = 0
	private var timeoutHandler: Handler? = null
	private var timeoutRunnable: Runnable? = null
	fun showIntAd(context: AdvActivity, areaKey: String, onClosed: () -> Unit) {
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "showIntAd: $areaKey" )
		}
		if (isShowing) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "showIntAd: $areaKey isShowing = $isShowing now go back!")
			}
			onClosed()
			return
		}
		if (AdvCheckManager.params.limitTime > System.currentTimeMillis()) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "showIntAd: $areaKey limitTime = ${AdvCheckManager.params.limitTime}" )
			}
			onClosed()
		} else {
//			context.showProgress()
			CoroutineScope(Dispatchers.IO).launch {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "showIntAd: $areaKey isNewAdPolicy = ${AdConfig.isNewAdPolicy}" )
				}
				val canPlay = if (AdConfig.isNewAdPolicy) {
					AdPolicyManager.checkAdUnit(areaKey)
				} else {
					AdvCheckManager.checkAdv(areaKey)
				}
				withContext(Dispatchers.Main) {
					if (canPlay) {
						isShowing = true
						this@ShowInterAd.areaKey = areaKey
						this@ShowInterAd.onClosed = onClosed
						admobRetryCount = 0
						maxRetryCountCurrent = 0
						topOnRetryCountCurrent = 0
						timeoutHandler = Handler()
						timeoutRunnable = Runnable {
							Log.e(TAG, "Ad show timeout")
							isShowing = false
							context.hideProgress()
							onClosed()
						}
						timeoutHandler?.postDelayed(timeoutRunnable!!, timeoutMillis)
						showAd(context)
					} else {
						LogUtil.log(
							LogAdData.ad_show_timeout,
							mapOf(
								LogAdParam.ad_platform to Config.showAdPlatform,
								LogAdParam.ad_areakey to ShowInterAd.areaKey,
								LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
								LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
								LogAdParam.ad_preload to false,
							)
						)
						context.hideProgress()
						onClosed()
					}
				}
			}
		}
	}

	private fun showAd(context: AdvActivity) {
		if (Config.showAdPlatform == LogAdParam.ad_platform_bidding) {
			val maxInter = AdPool.getMaxInter(areaKey, onClosed) {
				if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadInter(context)
				}
			}
			val topOnInter = AdPool.getTopOnInter(areaKey, onClosed) {
				if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadInter(context)
				}
			}
			when {
				maxInter != null && topOnInter != null -> {
					if (isShowing) {
						val maxAd = maxInter as MaxAd
						if (maxAd.revenue >= topOnInter.checkAdStatus().tuTopAdInfo.publisherRevenue) {
							if (maxInter.isReady) {
								maxInter.showAd(context)
							}
						} else {
							if (topOnInter.isAdReady) {
								topOnInter.show(context)
							}
						}
					}
				}
				maxInter != null -> {
					if (isShowing) {
						if (maxInter.isReady) {
							maxInter.showAd(context)
						}
					}
				}
				topOnInter != null -> {
					if (isShowing) {
						if (topOnInter.isAdReady) {
							topOnInter.show(context)
						}
					}
				}
				else -> {
					val admobInter = AdPool.getAdmobInter(areaKey, onClosed) {
						if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_PLAY_FINISH)) {
							AdLoader.loadInter(context)
						}
					}
					if (admobInter != null) {
						if (isShowing) {
							Log.e(TAG, "showAd: 从缓存播放广告", )
							admobInter.show(context)
							context.hideProgress()
						} else {
							// TODO: 逻辑缺失
						}
					} else {
						Log.e(TAG, "showAd: 从广告池拉取广告失败", )
						showAdmobAdv(context)
					}
				}
			}
		} else if (Config.showAdPlatform == LogAdParam.ad_platform_admob) {
			val admobInter = AdPool.getAdmobInter(areaKey, onClosed) {
				if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadInter(context)
				}
			}
			if (admobInter != null) {
				if (isShowing) {
					Log.e(TAG, "showAd: 从缓存播放广告", )
					admobInter.show(context)
					context.hideProgress()
				}
			} else {
				Log.e(TAG, "showAd: 从广告池拉取广告失败", )
				showAdmobAdv(context)
			}
		} else if (Config.showAdPlatform == LogAdParam.ad_platform_max) {
			val maxInter = AdPool.getMaxInter(areaKey, onClosed) {
				if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadInter(context)
				}
			}
			if (maxInter != null) {
				if (isShowing) {
					if (maxInter.isReady) {
						maxInter.showAd(context)
					}
				}
			} else {
				showMaxAdv(context)
			}
		}else if (Config.showAdPlatform == LogAdParam.ad_platform_topon) {
			val topOnInter = AdPool.getTopOnInter(areaKey, onClosed) {
				if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadInter(context)
				}
			}
			if (topOnInter != null) {
				if (isShowing) {
					if (topOnInter.isAdReady) {
						topOnInter.show(context)
					}
				}
			} else {
				showTopOnAdv(context)
			}
		}
	}

	private fun showAdmobAdv(context: AdvActivity) {
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
				LogAdParam.ad_preload to false,
			)
		)
//		LogUtil.log(
//			LogAdData.ad_occur,
//			mapOf(
//				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
//				LogAdParam.ad_areakey to areaKey,
//				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
//				LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
//				LogAdParam.ad_preload to false,
//			)
//		)
		val startLoadingTime = System.currentTimeMillis()
		context.showProgress()
		InterstitialAd.load(
			context,
			AdvIDs.getAdmobInterstitialId(),
			AdRequest.Builder().build(),
			object : InterstitialAdLoadCallback() {
				override fun onAdLoaded(ad: InterstitialAd) {
					Log.e(TAG, "showAdmobAdv Ad was loaded.")
					LogUtil.log(
						LogAdData.ad_finish_loading,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
							LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
							LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: "unknow"),
							LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
							LogAdParam.ad_preload to false,
						)
					)
					ad.fullScreenContentCallback = object : FullScreenContentCallback() {
						override fun onAdClicked() {
							super.onAdClicked()
							LogUtil.log(
								LogAdData.ad_click,
								mapOf(
									LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
									LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
									LogAdParam.ad_areakey to areaKey,
									LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
									LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
									LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
									LogAdParam.ad_preload to false,
								)
							)
							AppOpenHelper.spSwitch = true
						}

						override fun onAdDismissedFullScreenContent() {
							super.onAdDismissedFullScreenContent()
							LogUtil.log(
								LogAdData.ad_close,
								mapOf(
									LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
									LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
									LogAdParam.ad_areakey to areaKey,
									LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
									LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
									LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
									LogAdParam.ad_preload to false,
								)
							)
							context.hideProgress()
							isShowing = false
							onClosed()
						}

						override fun onAdFailedToShowFullScreenContent(p0: AdError) {
							super.onAdFailedToShowFullScreenContent(p0)
							LogUtil.log(
								LogAdData.ad_show_fail,
								mapOf(
									"msg" to p0.message,
									LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
									LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
									LogAdParam.ad_areakey to areaKey,
									LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
									LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
									LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
									LogAdParam.ad_preload to false,
								)
							)
							context.hideProgress()
							isShowing = false
							onClosed()
						}

						override fun onAdImpression() {
							super.onAdImpression()
							AdvCheckManager.params.interTimes++
							if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_PLAY_FINISH)) {
								AdLoader.loadInter(context)
							}
						}

						override fun onAdShowedFullScreenContent() {
							super.onAdShowedFullScreenContent()
						}
					}
					ad.onPaidEventListener = OnPaidEventListener { adValue ->
						val micros = adValue.valueMicros         // 广告价值（微元单位，需除以1,000,000得到实际金额）
						val currency = adValue.currencyCode     // ISO 4217货币代码（如："USD"）
						val precision = adValue.precisionType    // 金额精度类型（0=估算，1=发布商定义，2=精确计算）
						// 收入跟踪（示例：转换为美元）
						val revenue = micros.toDouble() / 1_000_000.0
						val att = JSONObject()
						try {
							att.put(LogAdParam.revenue, revenue)
							att.put(LogAdParam.adType, LogAdParam.InterAd)
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
								FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobInterstitialId(),
								FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_interstitial,
								FirebaseAnalytics.Param.AD_SOURCE to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
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
								FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobInterstitialId(),
								FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_interstitial,
								FirebaseAnalytics.Param.AD_SOURCE to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
								FirebaseAnalytics.Param.CURRENCY to currency,
								FirebaseAnalytics.Param.VALUE to revenue,
								LogAdParam.ad_preload to false,
							)
						)
						LogUtil.logTaiChiAdmob(adValue)
					}
					timeoutHandler?.removeCallbacks(timeoutRunnable!!)
					if (isShowing) {
						Log.e(TAG, "onAdLoaded: 展示实时拉取d广告", )
						ad.show(context)
						context.hideProgress()
					}
				}

				override fun onAdFailedToLoad(adError: LoadAdError) {
					Log.e(TAG, adError.message)
					admobRetryCount++
					if (admobRetryCount <= maxRetryCount) {
						Handler().postDelayed({
							showAdmobAdv(context)
						}, 1000)
					} else {
						// 超过最大重试次数，关闭广告流程
						timeoutHandler?.removeCallbacks(timeoutRunnable!!)
						context.hideProgress()
						isShowing = false
						onClosed()
					}
				}
			},
		)
	}

	private fun showMaxAdv(context: AdvActivity) {
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_max,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
				LogAdParam.ad_unit_name to AdvIDs.MAX_INTERSTITIAL_ID,
				LogAdParam.ad_preload to false,
			)
		)
//		LogUtil.log(
//			LogAdData.ad_occur,
//			mapOf(
//				LogAdParam.ad_platform to LogAdParam.ad_platform_max,
//				LogAdParam.ad_areakey to areaKey,
//				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
//				LogAdParam.ad_unit_name to AdvIDs.MAX_INTERSTITIAL_ID,
//				LogAdParam.ad_preload to false,
//			)
//		)
		val startLoadingTime = System.currentTimeMillis()
		val interstitialAd = MaxInterstitialAd(AdvIDs.MAX_INTERSTITIAL_ID)
		interstitialAd.setListener(object : MaxAdListener {
			override fun onAdLoaded(maxAd: MaxAd) {
				LogUtil.log(
					LogAdData.ad_finish_loading,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_max,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to maxAd.networkName,
						LogAdParam.ad_unit_name to AdvIDs.MAX_INTERSTITIAL_ID,
						LogAdParam.ad_preload to false,
					)
				)
				timeoutHandler?.removeCallbacks(timeoutRunnable!!)
				if (interstitialAd.isReady) {
					interstitialAd.showAd(context)
				} else {
					showMaxAdv(context)
				}
			}

			override fun onAdDisplayed(maxAd: MaxAd) {
				AdvCheckManager.params.interTimes++
				LogUtil.log(
					LogAdData.ad_impression,
					mapOf(
						LogAdParam.ad_areakey to areaKey,
						FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_max,
						FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobInterstitialId(),
						FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to maxAd.networkName,
						FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
						FirebaseAnalytics.Param.VALUE to maxAd.revenue,
						LogAdParam.ad_preload to false,
					)
				)
				if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadInter(context)
				}
			}

			override fun onAdHidden(maxAd: MaxAd) {
				LogUtil.log(
					LogAdData.ad_close,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_max,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to maxAd.networkName,
						LogAdParam.ad_unit_name to AdvIDs.MAX_INTERSTITIAL_ID,
						LogAdParam.ad_preload to false,
					)
				)
				timeoutHandler?.removeCallbacks(timeoutRunnable!!)
				context.hideProgress()
				onClosed()
			}

			override fun onAdClicked(maxAd: MaxAd) {
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_max,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to maxAd.networkName,
						LogAdParam.ad_unit_name to AdvIDs.MAX_INTERSTITIAL_ID,
						LogAdParam.ad_preload to false,
					)
				)
				AppOpenHelper.spSwitch = true
			}

			override fun onAdLoadFailed(s: String, maxError: MaxError) {
				maxRetryCountCurrent++
				if (maxRetryCountCurrent <= maxRetryCount) {
					Handler().postDelayed({
						showMaxAdv(context)
					}, 1000)
				} else {
					timeoutHandler?.removeCallbacks(timeoutRunnable!!)
					context.hideProgress()
					isShowing = false
					onClosed()
				}
			}

			override fun onAdDisplayFailed(maxAd: MaxAd, maxError: MaxError) {
				maxRetryCountCurrent++
				if (maxRetryCountCurrent <= maxRetryCount) {
					Handler().postDelayed({
						showMaxAdv(context)
					}, 1000)
				} else {
					timeoutHandler?.removeCallbacks(timeoutRunnable!!)
					context.hideProgress()
					isShowing = false
					onClosed()
				}
			}
		})
		interstitialAd.setRevenueListener { maxAd: MaxAd? ->
			val revenue = maxAd!!.revenue
			val att = JSONObject()
			try {
				att.put(LogAdParam.revenue, revenue)
				att.put(LogAdParam.adType, LogAdParam.InterAd)
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
					FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.MAX_INTERSTITIAL_ID,
					FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_interstitial,
					LogAdParam.ad_source to maxAd.networkName,
					FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
					FirebaseAnalytics.Param.VALUE to maxAd.revenue,
					LogAdParam.ad_preload to false,
				)
			)
			LogUtil.logTaiChiMax(maxAd)
		}
		interstitialAd.loadAd()
	}

	private fun showTopOnAdv(context: AdvActivity) {
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
				LogAdParam.ad_unit_name to AdvIDs.TopON_INTERSTITIAL_ID,
				LogAdParam.ad_preload to false,
			)
		)
//		LogUtil.log(
//			LogAdData.ad_occur,
//			mapOf(
//				LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
//				LogAdParam.ad_areakey to areaKey,
//				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
//				LogAdParam.ad_unit_name to AdvIDs.TopON_INTERSTITIAL_ID,
//				LogAdParam.ad_preload to false,
//			)
//		)
		val startLoadingTime = System.currentTimeMillis()
		val mInterstitialAd = TUInterstitial(context, AdvIDs.TopON_INTERSTITIAL_ID)
		// 添加监听
		mInterstitialAd.setAdListener(object : TUInterstitialListener{
			override fun onInterstitialAdLoaded() {
				Log.e(TAG, "预加载广告成功 loadTopOnInter Ad was loaded.")
				LogUtil.log(
					LogAdData.ad_finish_loading,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to (mInterstitialAd.checkAdStatus().tuTopAdInfo.networkName ?: "unknow"),
						LogAdParam.ad_unit_name to AdvIDs.TopON_INTERSTITIAL_ID,
						LogAdParam.ad_preload to false,
					)
				)
				timeoutHandler?.removeCallbacks(timeoutRunnable!!)
				if (mInterstitialAd.isAdReady) {
					mInterstitialAd.show(context)
				} else {
					showTopOnAdv(context)
				}
			}

			override fun onInterstitialAdLoadFail(p0: com.thinkup.core.api.AdError?) {
				topOnRetryCountCurrent++
				if (topOnRetryCountCurrent <= maxRetryCount) {
					Handler().postDelayed({
						showTopOnAdv(context)
					}, 1000)
				} else {
					timeoutHandler?.removeCallbacks(timeoutRunnable!!)
					context.hideProgress()
					isShowing = false
					onClosed()
				}
			}

			override fun onInterstitialAdClicked(p0: TUAdInfo?) {
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to (mInterstitialAd.checkAdStatus().tuTopAdInfo.networkName ?: "unknow"),
						LogAdParam.ad_unit_name to AdvIDs.TopON_INTERSTITIAL_ID,
						LogAdParam.ad_preload to false,
					)
				)
				AppOpenHelper.spSwitch = true
			}

			override fun onInterstitialAdShow(p0: TUAdInfo?) {
				AdvCheckManager.params.interTimes++
				LogUtil.log(
					LogAdData.ad_impression,
					mapOf(
						LogAdParam.ad_areakey to areaKey,
						FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_topon,
						LogAdParam.ad_unit_name to AdvIDs.TopON_INTERSTITIAL_ID,
						FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to (mInterstitialAd.checkAdStatus().tuTopAdInfo.networkName ?: "unknow"),
						FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
						FirebaseAnalytics.Param.VALUE to (mInterstitialAd.checkAdStatus().tuTopAdInfo.publisherRevenue ?: 0.0),
						LogAdParam.ad_preload to false,
					)
				)
				val revenue = mInterstitialAd.checkAdStatus().tuTopAdInfo.publisherRevenue
				val att = JSONObject()
				try {
					att.put(LogAdParam.revenue, revenue)
					att.put(LogAdParam.adType, LogAdParam.InterAd)
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
						FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.TopON_INTERSTITIAL_ID,
						FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to mInterstitialAd.checkAdStatus().tuTopAdInfo.networkName,
						FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
						FirebaseAnalytics.Param.VALUE to mInterstitialAd.checkAdStatus().tuTopAdInfo.publisherRevenue,
						LogAdParam.ad_preload to true,
					)
				)
				LogUtil.logTaiChiTopOn(mInterstitialAd)
				if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadInter(context)
				}
			}

			override fun onInterstitialAdClose(p0: TUAdInfo?) {
				LogUtil.log(
					LogAdData.ad_close,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to (mInterstitialAd.checkAdStatus().tuTopAdInfo.networkName ?: "unknow"),
						LogAdParam.ad_unit_name to AdvIDs.TopON_INTERSTITIAL_ID,
						LogAdParam.ad_preload to false,
					)
				)
				context.hideProgress()
				isShowing = false
				onClosed()
			}
			override fun onInterstitialAdVideoStart(p0: TUAdInfo?) {}
			override fun onInterstitialAdVideoEnd(p0: TUAdInfo?) {}
			override fun onInterstitialAdVideoError(p0: com.thinkup.core.api.AdError?) {}
		})
		mInterstitialAd.load()
	}
}