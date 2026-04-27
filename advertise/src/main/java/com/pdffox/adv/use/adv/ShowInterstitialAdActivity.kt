package com.pdffox.adv.use.adv

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import com.pdffox.adv.use.Config
import com.pdffox.adv.databinding.ActivityShowInterstitialAdBinding
import com.pdffox.adv.use.log.AREA_KEY
import com.pdffox.adv.use.log.FROM
import com.pdffox.adv.use.log.LogAdData
import com.pdffox.adv.use.log.LogAdParam
import com.pdffox.adv.use.log.LogUtil
import com.pdffox.adv.use.log.ROUTE
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask

const val ADV_RESULT_CODE = 8888
class ShowInterstitialAdActivity : AppCompatActivity() {
	companion object{
		private const val TAG = "ShowInterstitialAdActivity"

		fun getIntent(context: AdvActivity, areaKey: String): Intent {
			return Intent(context, ShowInterstitialAdActivity::class.java).apply {
				putExtra(AREA_KEY, areaKey)
			}
		}

		fun openPage(context: AdvActivity, areaKey: String, onClosed: () -> Unit){
			if (AdvCheckManager.params.limitTime > System.currentTimeMillis()) {
				onClosed()
			} else {
				CoroutineScope(Dispatchers.IO).launch {
					val canPlay = AdvCheckManager.checkAdv(areaKey)
					with(Dispatchers.Main) {
						if (canPlay) {
							context.onClosedCallback = onClosed
							context.interstitialLauncher.launch(getIntent(context, areaKey))
						} else {
							onClosed()
						}
					}
				}
			}
		}
	}
	val binding by lazy {
		ActivityShowInterstitialAdBinding.inflate(layoutInflater)
	}
	val route: String by lazy {
		(intent.getStringExtra(FROM) ?: "") + ROUTE
	}
	val areaKey: String by lazy {
		intent.getStringExtra(AREA_KEY) ?: ""
	}

	var isShowing = false;

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(binding.root)
		initData()
		initUi()
	}

	override fun onStop() {
		super.onStop()
		isShowing = false
	}

	fun initData() {
		setResult(ADV_RESULT_CODE)
	}

	fun initUi() {
		showLoading()
		if (!isShowing) {
			isShowing = true
			if (Config.showAdPlatform == LogAdParam.ad_platform_admob) {
				showAdmobAdv()
			} else if (Config.showAdPlatform == LogAdParam.ad_platform_max) {
				showMaxAdv()
			}
		}
	}
	val timer = Timer()
	fun showLoading() {
		timer.schedule(object : TimerTask() {
			override fun run() {
				updateUi()
			}
		}, 0,60)
	}
	var mProgress = 0
	fun updateUi() {
		mProgress++
		binding.progressBar.progress = mProgress
		if (mProgress >= 100) {
			runOnUiThread {
				finish()
			}
			timer.cancel()
		}
	}

	fun showAdmobAdv() {
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
		LogUtil.log(
			LogAdData.ad_occur,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
				LogAdParam.ad_preload to false,
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		InterstitialAd.load(
			this,
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
							finish()
						}

						override fun onAdFailedToShowFullScreenContent(p0: AdError) {
							super.onAdFailedToShowFullScreenContent(p0)
							Handler().postDelayed({
								showAdmobAdv()
							}, 1000)
						}

						override fun onAdImpression() {
							super.onAdImpression()
							AdvCheckManager.params.interTimes++
							timer.cancel()
							if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_PLAY_FINISH)) {
								AdLoader.loadInter(this@ShowInterstitialAdActivity)
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
					ad.show(this@ShowInterstitialAdActivity)
				}

				override fun onAdFailedToLoad(adError: LoadAdError) {
					Log.e(TAG, adError.message)
					Handler().postDelayed({
						showAdmobAdv()
					}, 1000)
				}
			},
		)
	}

	fun showMaxAdv() {
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
		LogUtil.log(
			LogAdData.ad_occur,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_max,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
				LogAdParam.ad_unit_name to AdvIDs.MAX_INTERSTITIAL_ID,
				LogAdParam.ad_preload to false,
			)
		)
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
				if (interstitialAd.isReady) {
					interstitialAd.showAd(this@ShowInterstitialAdActivity)
				} else {
					showMaxAdv()
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
					AdLoader.loadInter(this@ShowInterstitialAdActivity)
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
				finish()
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
				showMaxAdv()
			}

			override fun onAdDisplayFailed(maxAd: MaxAd, maxError: MaxError) {
				showMaxAdv()
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


}