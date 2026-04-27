package com.pdffox.adv.use.adv

import android.annotation.SuppressLint
import android.util.Log
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.applovin.mediation.ads.MaxInterstitialAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.pdffox.adv.use.log.LogAdData
import com.pdffox.adv.use.log.LogAdParam
import com.pdffox.adv.use.log.LogUtil
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import com.thinkup.core.api.TUAdInfo
import com.thinkup.interstitial.api.TUInterstitial
import com.thinkup.interstitial.api.TUInterstitialListener
import com.thinkup.splashad.api.TUSplashAd
import com.thinkup.splashad.api.TUSplashAdExtraInfo
import com.thinkup.splashad.api.TUSplashAdListener
import org.json.JSONException
import org.json.JSONObject

/**
 * 广告池
 */
object AdPool {

	private const val TAG = "AdPool"

	val admobInterPool = mutableMapOf<InterstitialAd, Long>()
	val admobOpenPool = mutableMapOf<AppOpenAd, Long>()

	val admobNativePool = java.util.ArrayDeque<NativeAdContent>()
	val maxInterPool = mutableMapOf<MaxInterstitialAd, Long>()
	val maxOpenPool = mutableMapOf<MaxAppOpenAd, Long>()
	val topOnOpenPool = mutableMapOf<TUSplashAd, Long>()
	val topOnInterPool = mutableMapOf<TUInterstitial, Long>()

	var admobInterIsLoadingNum = 0
	var admobOpenIsLoadingNum = 0
	var admobNativeIsLoadingNum = 0

	var maxInterIsLoadingNum = 0
	var maxOpenIsLoadingNum = 0
	var toponInterIsLoadingNum = 0
	var toponOpenIsLoadingNum = 0

	fun putAdmobOpen(ad: AppOpenAd) {
		admobOpenPool[ad] = System.currentTimeMillis()
	}

	fun getAdmobOpen(areaKey: String, onClosed: () -> Unit, onDisplayed: () -> Unit): AppOpenAd? {
		AdChecker.checkExpiredAdmobOpen()
		val admobAppOpenAd = admobOpenPool.entries.firstOrNull { (_, time) ->
			System.currentTimeMillis() - time < AdConfig.adload_cache_time
		}?.key
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
		val occurTime = System.currentTimeMillis()
		admobAppOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
				override fun onAdDismissedFullScreenContent() {
					LogUtil.log(
						LogAdData.ad_close,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
							LogAdParam.duration to System.currentTimeMillis() - occurTime,
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
							LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
							LogAdParam.ad_preload to true,
						)
					)
					onClosed()
				}

				override fun onAdFailedToShowFullScreenContent(adError: AdError) {
					Log.e(TAG, adError.message)
					onClosed()
				}

				override fun onAdShowedFullScreenContent() {
					Log.e(TAG, "Ad showed fullscreen content.")
				}

				override fun onAdImpression() {
					AdvCheckManager.params.openTimes++
					onDisplayed()
				}

				override fun onAdClicked() {
					LogUtil.log(
						LogAdData.ad_click,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
							LogAdParam.duration to 0L,
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
							LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
							LogAdParam.ad_preload to true,
						)
					)
					AppOpenHelper.spSwitch = true
				}
		}
		admobAppOpenAd?.onPaidEventListener = OnPaidEventListener { adValue -> // 可获取的核心参数：
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
					FirebaseAnalytics.Param.AD_SOURCE to (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
					FirebaseAnalytics.Param.CURRENCY to currency,
					FirebaseAnalytics.Param.VALUE to revenue,
					LogAdParam.ad_preload to true,
				)
			)
			LogUtil.logTaiChiAdmob(adValue)
		}
		admobAppOpenAd?.let {
			admobOpenPool.remove(it)
		}
		return admobAppOpenAd
	}

	fun putAdmobInter(ad: InterstitialAd) {
		admobInterPool[ad] = System.currentTimeMillis()
	}

	fun getAdmobInter(areaKey: String, onClosed: () -> Unit, onDisplayed: () -> Unit): InterstitialAd? {
		AdChecker.checkExpiredAdmobInter()
		val admobInter = admobInterPool.entries.firstOrNull { (_, time) ->
			System.currentTimeMillis() - time < AdConfig.adload_cache_time
		}?.key
		LogUtil.log(
			LogAdData.ad_occur,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
				LogAdParam.ad_preload to true,
			)
		)
		val occurTime = System.currentTimeMillis()
		admobInter?.fullScreenContentCallback = object : FullScreenContentCallback() {
			override fun onAdClicked() {
				super.onAdClicked()
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to System.currentTimeMillis() - occurTime,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to (admobInter.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
						LogAdParam.ad_preload to true,
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
						LogAdParam.duration to 0L,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to (admobInter.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
						LogAdParam.ad_preload to true,
					)
				)
				onClosed()
			}

			override fun onAdFailedToShowFullScreenContent(p0: AdError) {
				super.onAdFailedToShowFullScreenContent(p0)
				LogUtil.log(
					LogAdData.ad_show_fail,
					mapOf(
						"msg" to p0.message,
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to 0L,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to (admobInter.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
						LogAdParam.ad_preload to true,
					)
				)
			}

			override fun onAdImpression() {
				super.onAdImpression()
				AdvCheckManager.params.interTimes++
				onDisplayed()
			}

			override fun onAdShowedFullScreenContent() {
				super.onAdShowedFullScreenContent()
			}
		}
		admobInter?.onPaidEventListener = OnPaidEventListener { adValue ->
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
					FirebaseAnalytics.Param.AD_SOURCE to (admobInter.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
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
					FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobInterstitialId(),
					FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_interstitial,
					FirebaseAnalytics.Param.AD_SOURCE to (admobInter.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
					FirebaseAnalytics.Param.CURRENCY to currency,
					FirebaseAnalytics.Param.VALUE to revenue,
					LogAdParam.ad_preload to true,
				)
			)
			LogUtil.logTaiChiAdmob(adValue)
		}
		admobInter?.let {
			admobInterPool.remove(it)
		}
		return admobInter
	}

	fun putAdmobNative(content: NativeAdContent) {
		admobNativePool.offer(content) // 将新广告加入队尾
		Log.e(TAG, "NativeAdContent 已入队，当前池大小: ${admobNativePool.size}")
	}

	fun getAdmobNative(): NativeAdContent? {
		val content = admobNativePool.poll()
		if (content == null) {
			Log.e(TAG, "getAdmobNative: 队列为空，无可用广告, 实时加载广告")
		}
		return content
	}

	fun putMaxInter(ad: MaxInterstitialAd) {
		maxInterPool[ad] = System.currentTimeMillis()
	}

	fun getMaxInter(areaKey: String, onClosed: () -> Unit, onDisplayed: () -> Unit): MaxInterstitialAd? {
		AdChecker.checkExpiredMaxInter()
		val maxInter = maxInterPool.maxByOrNull {
			(it.key as MaxAd).revenue
		}?.key

		LogUtil.log(
			LogAdData.ad_occur,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_max,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
				LogAdParam.ad_unit_name to AdvIDs.MAX_INTERSTITIAL_ID,
				LogAdParam.ad_preload to true,
			)
		)
		maxInter?.setListener(object : MaxAdListener {
			override fun onAdLoaded(maxAd: MaxAd) {}

			override fun onAdDisplayed(maxAd: MaxAd) {
				AdvCheckManager.params.interTimes++
				LogUtil.log(
					LogAdData.ad_impression,
					mapOf(
						LogAdParam.ad_areakey to areaKey,
						FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_max,
						FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.MAX_INTERSTITIAL_ID,
						FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to maxAd.networkName,
						FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
						FirebaseAnalytics.Param.VALUE to maxAd.revenue,
						LogAdParam.ad_preload to true,
					)
				)
				onDisplayed()
			}

			override fun onAdHidden(maxAd: MaxAd) {
				LogUtil.log(
					LogAdData.ad_close,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_max,
						LogAdParam.duration to 0L,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to maxAd.networkName,
						LogAdParam.ad_unit_name to AdvIDs.MAX_INTERSTITIAL_ID,
						LogAdParam.ad_preload to true,
					)
				)
				onClosed()
			}

			override fun onAdClicked(maxAd: MaxAd) {
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_max,
						LogAdParam.duration to 0L,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to maxAd.networkName,
						LogAdParam.ad_unit_name to AdvIDs.MAX_INTERSTITIAL_ID,
						LogAdParam.ad_preload to true,
					)
				)
				AppOpenHelper.spSwitch = true
			}

			override fun onAdLoadFailed(s: String, maxError: MaxError) {}

			override fun onAdDisplayFailed(maxAd: MaxAd, maxError: MaxError) {
				onClosed()
			}
		})
		maxInter?.setRevenueListener { maxAd: MaxAd? ->
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
					LogAdParam.ad_preload to true,
				)
			)
			LogUtil.logTaiChiMax(maxAd)
		}
		maxInter?.let {
			maxInterPool.remove(it)
		}
		return maxInter
	}

	fun putMaxOpen(ad: MaxAppOpenAd) {
		maxOpenPool[ad] = System.currentTimeMillis()
	}

	fun getMaxOpen(areaKey: String, onClosed: () -> Unit, onDisplayed: () -> Unit): MaxAppOpenAd? {
		AdChecker.checkExpiredMaxOpen()
		val maxAd = maxOpenPool.maxByOrNull {
			(it.key as MaxAd).revenue
		}?.key
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
		maxAd?.setListener(object : MaxAdListener {
			override fun onAdLoaded(maxAd: MaxAd) {}
			override fun onAdLoadFailed(p0: String, p1: MaxError) {}
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
				onDisplayed()
			}
			override fun onAdHidden(maxAd: MaxAd) {
				LogUtil.log(
					LogAdData.ad_close,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_max,
						LogAdParam.duration to 0L,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_open,
						LogAdParam.ad_source to maxAd.networkName,
						LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
						LogAdParam.ad_preload to true,
					)
				)
				onClosed()
			}
			override fun onAdClicked(maxAd: MaxAd) {
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_max,
						LogAdParam.duration to 0L,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_open,
						LogAdParam.ad_source to maxAd.networkName,
						LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
						LogAdParam.ad_preload to true,
					)
				)
				AppOpenHelper.spSwitch = true
			}
			override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
				onClosed()
			}
		})
		maxAd?.setRevenueListener { maxAd: MaxAd? ->
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
		}
		maxAd?.let {
			maxOpenPool.remove(it)
		}
		return maxAd
	}

	fun putTopOnOpen(ad: TUSplashAd) {
		topOnOpenPool[ad] = System.currentTimeMillis()
	}

	fun getTopOnOpen(areaKey: String, onClosed: () -> Unit, onDisplayed: () -> Unit): TUSplashAd? {
		AdChecker.checkExpiredTopOnOpen()
		val tuAd = topOnOpenPool.maxByOrNull {
			it.key.checkAdStatus().tuTopAdInfo.publisherRevenue
		}?.key
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
		tuAd?.setAdListener(object : TUSplashAdListener{
			override fun onAdLoaded(p0: Boolean) {}
			override fun onAdLoadTimeout() {}
			override fun onNoAdError(p0: com.thinkup.core.api.AdError?) {
				onClosed()
			}

			@SuppressLint("MissingPermission")
			override fun onAdShow(p0: TUAdInfo?) {
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
						LogAdParam.ad_preload to true,
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
						LogAdParam.ad_preload to true,
					)
				)
				LogUtil.logTaiChiTopOn(tuAd)
				onDisplayed()
			}

			override fun onAdClick(p0: TUAdInfo?) {
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
						LogAdParam.duration to 0L,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_open,
						LogAdParam.ad_source to tuAd.checkAdStatus().tuTopAdInfo.networkName,
						LogAdParam.ad_unit_name to AdvIDs.TopON_OPEN_ID,
						LogAdParam.ad_preload to true,
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
						LogAdParam.duration to 0L,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_open,
						LogAdParam.ad_source to tuAd.checkAdStatus().tuTopAdInfo.networkName,
						LogAdParam.ad_unit_name to AdvIDs.TopON_OPEN_ID,
						LogAdParam.ad_preload to true,
					)
				)
				onClosed()
			}

		})
		tuAd?.let {
			topOnOpenPool.remove(it)
		}
		return tuAd
	}

	fun putTopOnInter(ad: TUInterstitial) {
		topOnInterPool[ad] = System.currentTimeMillis()
	}

	fun getTopOnInter(areaKey: String, onClosed: () -> Unit, onDisplayed: () -> Unit): TUInterstitial? {
		AdChecker.checkExpiredTopOnInter()
		val tuAd = topOnInterPool.maxByOrNull {
			it.key.checkAdStatus().tuTopAdInfo.publisherRevenue
		}?.key
		LogUtil.log(
			LogAdData.ad_occur,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
				LogAdParam.ad_unit_name to AdvIDs.TopON_INTERSTITIAL_ID,
				LogAdParam.ad_preload to true,
			)
		)
		tuAd?.setAdListener(object : TUInterstitialListener {

			override fun onInterstitialAdLoaded() {}

			override fun onInterstitialAdLoadFail(p0: com.thinkup.core.api.AdError?) {
				onClosed()
			}

			override fun onInterstitialAdClicked(p0: TUAdInfo?) {
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
						LogAdParam.duration to 0L,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to tuAd.checkAdStatus().tuTopAdInfo.networkName,
						LogAdParam.ad_unit_name to AdvIDs.TopON_INTERSTITIAL_ID,
						LogAdParam.ad_preload to true,
					)
				)
				AppOpenHelper.spSwitch = true
			}

			@SuppressLint("MissingPermission")
			override fun onInterstitialAdShow(p0: TUAdInfo?) {
				AdvCheckManager.params.interTimes++
				LogUtil.log(
					LogAdData.ad_impression,
					mapOf(
						LogAdParam.ad_areakey to areaKey,
						FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_topon,
						FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.TopON_INTERSTITIAL_ID,
						FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to tuAd.checkAdStatus().tuTopAdInfo.networkName,
						FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
						FirebaseAnalytics.Param.VALUE to tuAd.checkAdStatus().tuTopAdInfo.publisherRevenue,
						LogAdParam.ad_preload to true,
					)
				)
				val revenue = tuAd.checkAdStatus().tuTopAdInfo.publisherRevenue
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
						LogAdParam.ad_source to tuAd.checkAdStatus().tuTopAdInfo.networkName,
						FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
						FirebaseAnalytics.Param.VALUE to tuAd.checkAdStatus().tuTopAdInfo.publisherRevenue,
						LogAdParam.ad_preload to true,
					)
				)
				LogUtil.logTaiChiTopOn(tuAd)
				onDisplayed()
			}

			override fun onInterstitialAdClose(p0: TUAdInfo?) {
				LogUtil.log(
					LogAdData.ad_close,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_topon,
						LogAdParam.duration to 0L,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to tuAd.checkAdStatus().tuTopAdInfo.networkName,
						LogAdParam.ad_unit_name to AdvIDs.TopON_INTERSTITIAL_ID,
						LogAdParam.ad_preload to true,
					)
				)
				onClosed()
			}

			override fun onInterstitialAdVideoStart(p0: TUAdInfo?) {}

			override fun onInterstitialAdVideoEnd(p0: TUAdInfo?) {}

			override fun onInterstitialAdVideoError(p0: com.thinkup.core.api.AdError?) {
				onClosed()
			}

		})
		tuAd?.let {
			topOnInterPool.remove(it)
		}
		return tuAd
	}

}