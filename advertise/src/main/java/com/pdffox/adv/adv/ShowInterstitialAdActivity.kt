package com.pdffox.adv.adv

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.pdffox.adv.Config
import com.pdffox.adv.adv.policy.AdPolicyManager
import com.pdffox.adv.databinding.ActivityShowInterstitialAdBinding
import com.pdffox.adv.log.AREA_KEY
import com.pdffox.adv.log.LogAdData
import com.pdffox.adv.log.LogAdParam
import com.pdffox.adv.log.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ShowInterstitialAdActivity : AppCompatActivity() {
	companion object {
		private const val TAG = "ShowInterstitialAdActivity"
		private const val EXTRA_REQUEST_ID = "adv_request_id"
		private val closedCallbacks = ConcurrentHashMap<String, () -> Unit>()

		@Volatile
		var isShowing = false
			private set

		fun getIntent(context: Activity, areaKey: String, requestId: String): Intent {
			return Intent(context, ShowInterstitialAdActivity::class.java).apply {
				putExtra(AREA_KEY, areaKey)
				putExtra(EXTRA_REQUEST_ID, requestId)
			}
		}

		fun openPage(activity: Activity, areaKey: String, onClosed: () -> Unit) {
			if (isShowing) {
				onClosed()
				return
			}
			if (AdvCheckManager.params.limitTime > System.currentTimeMillis()) {
				onClosed()
				return
			}
			CoroutineScope(Dispatchers.IO).launch {
				val canPlay = if (AdConfig.isNewAdPolicy) {
					AdPolicyManager.checkAdUnit(areaKey)
				} else {
					AdvCheckManager.checkAdv(areaKey)
				}
				withContext(Dispatchers.Main) {
					if (!canPlay || activity.isFinishing || activity.isDestroyedCompat()) {
						onClosed()
						return@withContext
					}
					val requestId = UUID.randomUUID().toString()
					closedCallbacks[requestId] = onClosed
					activity.startActivity(getIntent(activity, areaKey, requestId))
				}
			}
		}

		private fun Activity.isDestroyedCompat(): Boolean {
			return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed
		}

		private fun consumeClosedCallback(requestId: String): (() -> Unit)? {
			return closedCallbacks.remove(requestId)
		}
	}

	private val binding by lazy {
		ActivityShowInterstitialAdBinding.inflate(layoutInflater)
	}
	private val areaKey: String by lazy {
		intent.getStringExtra(AREA_KEY) ?: ""
	}
	private val requestId: String by lazy {
		intent.getStringExtra(EXTRA_REQUEST_ID).orEmpty()
	}

	private val timer = Timer()
	private var progress = 0
	private var hasStartedAd = false
	private var callbackDelivered = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(binding.root)
		isShowing = true
		showLoading()
		if (!hasStartedAd) {
			hasStartedAd = true
			if (Config.activeAdPlatform() == LogAdParam.ad_platform_admob) {
				showAdmobAdv()
			} else {
				finish()
			}
		}
	}

	override fun onDestroy() {
		runCatching { timer.cancel() }
		isShowing = false
		deliverClosedCallback()
		super.onDestroy()
	}

	private fun showLoading() {
		timer.schedule(object : TimerTask() {
			override fun run() {
				progress++
				runOnUiThread {
					binding.progressBar.progress = progress
					if (progress >= 100) {
						finish()
					}
				}
				if (progress >= 100) {
					timer.cancel()
				}
			}
		}, 0, 60)
	}

	private fun showAdmobAdv() {
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
					LogUtil.log(
						LogAdData.ad_finish_loading,
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
					ad.fullScreenContentCallback = object : FullScreenContentCallback() {
						override fun onAdClicked() {
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

						override fun onAdFailedToShowFullScreenContent(adError: AdError) {
							Log.e(TAG, adError.message)
							retryShowAdmob()
						}

						override fun onAdImpression() {
							AdvCheckManager.params.interTimes++
							timer.cancel()
							if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_PLAY_FINISH)) {
								AdLoader.loadInter(this@ShowInterstitialAdActivity)
							}
						}
					}
					ad.onPaidEventListener = OnPaidEventListener { adValue ->
						val revenue = adValue.valueMicros.toDouble() / 1_000_000.0
						LogUtil.logSingularAdRevenue(LogAdParam.InterAd, LogAdParam.adMob, revenue)
						logInterstitialRevenue(LogAdData.ad_impression, ad, adValue.currencyCode, revenue)
						logInterstitialRevenue(LogAdData.ad_revenue, ad, adValue.currencyCode, revenue)
						LogUtil.logTaiChiAdmob(adValue)
					}
					ad.show(this@ShowInterstitialAdActivity)
				}

				override fun onAdFailedToLoad(adError: LoadAdError) {
					Log.e(TAG, adError.message)
					retryShowAdmob()
				}
			},
		)
	}

	private fun retryShowAdmob() {
		Handler(Looper.getMainLooper()).postDelayed({
			if (!isFinishing && !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed)) {
				showAdmobAdv()
			}
		}, 1000)
	}

	private fun deliverClosedCallback() {
		if (callbackDelivered) {
			return
		}
		callbackDelivered = true
		if (requestId.isBlank()) {
			return
		}
		consumeClosedCallback(requestId)?.invoke()
	}

	private fun logInterstitialRevenue(
		eventName: String,
		ad: InterstitialAd,
		currency: String,
		revenue: Double,
	) {
		LogUtil.log(
			eventName,
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
	}
}
