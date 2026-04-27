package com.pdffox.adv.use.adv

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.applovin.mediation.ads.MaxAdView
import com.google.android.gms.ads.AdView
import com.pdffox.adv.use.Ads
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.use.Config
import com.pdffox.adv.R

open class AdvActivity: AppCompatActivity() {

	companion object {
		private const val TAG = "AdvActivity"
	}

	val adViewSet = mutableSetOf<ViewGroup>()
	lateinit var interstitialLauncher: ActivityResultLauncher<Intent>
	var onClosedCallback: (() -> Unit)? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		interstitialLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == ADV_RESULT_CODE) {
				onClosedCallback?.invoke()
				onClosedCallback = null
			}
		}
	}

	@RequiresPermission(Manifest.permission.INTERNET)
	fun getBannerAd(context: Context, areaKey: String): ViewGroup? {
		if ((!BuildConfig.DEBUG) && (Config.paid_0 || Config.isGoogleIP)) {
			Log.e(TAG, "getBannerAd: $areaKey 自然量屏蔽" )
			return null
		}
		val adView = Ads.getBannerAd(context, areaKey)
		adView?.let {
			adViewSet.add(adView)
		}
		return adView
	}

	fun removeBannerAd(adView: ViewGroup) {
		adViewSet.remove(adView)
	}

	override fun onResume() {
		super.onResume()
		for (adView in adViewSet) {
			if (adView is MaxAdView) {
				adView.startAutoRefresh()
			}
			if (adView is AdView) {
				adView.resume()
			}
		}
	}

	override fun onStop() {
		super.onStop()
		for (adView in adViewSet) {
			if (adView is MaxAdView) {
				adView.stopAutoRefresh()
			}
			if (adView is AdView) {
				adView.pause()
			}
		}

	}

	var progressView: View ?= null
	fun showProgress() {
		if (progressView == null) {
            progressView = layoutInflater.inflate(R.layout.a_activity_show_interstitial_ad,null)
			progressView?.let {
				it.setOnClickListener {  }
				addContentView(it, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
			}
		}
		progressView?.visibility = View.VISIBLE
		Log.e(TAG, "showProgress: ", )
	}

	fun hideProgress() {
		Log.e(TAG, "hideProgress: ", )
		progressView?.visibility = View.GONE
	}

}
