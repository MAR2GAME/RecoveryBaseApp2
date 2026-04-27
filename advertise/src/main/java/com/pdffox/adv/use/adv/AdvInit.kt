package com.pdffox.adv.use.adv

import android.app.Activity
import android.app.Application
import androidx.core.net.toUri
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.facebook.ads.AdSettings
import com.google.android.gms.ads.MobileAds
import com.pdffox.adv.use.AdvApplicaiton
import com.pdffox.adv.use.Config
import com.pdffox.adv.use.log.LogAdData
import com.pdffox.adv.use.log.LogAdParam
import com.pdffox.adv.use.log.LogUtil
import com.pdffox.adv.use.util.PreferenceUtil
import com.thinkup.core.api.TUSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AdvInit {

	fun initAdv(context: AdvApplicaiton) {
		initAdmob(context)
		// TODO: 第一次打开APP只初始化admob
//		val isFirstInitAdv = PreferenceUtil.getBoolean("isFirstInitAdv", true)
//		if (isFirstInitAdv) {
//			PreferenceUtil.commitBoolean("isFirstInitAdv", false)
//		} else {
//			initMAX(context)
//			initTopOn(context)
//			AppOpenHelper.startObserve()
//		}
		initMAX(context)
		initTopOn(context)
		AppOpenHelper.startObserve()
	}

	fun initAdmob(context: AdvApplicaiton) {
		CoroutineScope(Dispatchers.IO).launch {
			val advInitTime = System.currentTimeMillis()
			if (!Config.openAdmobMediation) {
				MobileAds.disableMediationAdapterInitialization(context)
			}
			MobileAds.initialize(context) { initializationStatus ->
				LogUtil.log(
					LogAdData.adv_sdk_initcomplete,
					mapOf(LogAdParam.ad_platform to LogAdParam.ad_platform_admob, LogAdParam.duration to (System.currentTimeMillis() - advInitTime))
				)
				CoroutineScope(Dispatchers.Main).launch {
					AppOpenHelper.hasInitAdmob = true
				}
			}
		}
	}

	fun initMAX(context: AdvApplicaiton) {
		CoroutineScope(Dispatchers.IO).launch {
			val advInitTime = System.currentTimeMillis()
			AdSettings.setDataProcessingOptions(arrayOf<String?>())
			val initConfig = AppLovinSdkInitializationConfiguration.builder(AdvIDs.MAX_SDK_KEY)
				.setMediationProvider(AppLovinMediationProvider.MAX)
				.build()
			val settings = AppLovinSdk.getInstance(context).settings
			settings.termsAndPrivacyPolicyFlowSettings.isEnabled = false
			settings.termsAndPrivacyPolicyFlowSettings.privacyPolicyUri = Config.PrivacyUrl.toUri()
			settings.termsAndPrivacyPolicyFlowSettings.termsOfServiceUri = Config.TermsUrl.toUri()

			AppLovinSdk.getInstance(context).initialize(
				initConfig
			) { sdkConfig: AppLovinSdkConfiguration? ->
				LogUtil.log(
					LogAdData.adv_sdk_initcomplete,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_max,
						LogAdParam.duration to (System.currentTimeMillis() - advInitTime)
					)
				)
				CoroutineScope(Dispatchers.Main).launch {
					AppOpenHelper.hasInitMax = true
				}
			}
		}

	}

	fun initTopOn(context: AdvApplicaiton) {
		TUSDK.init(context, AdvIDs.TopON_APP_ID, AdvIDs.TopON_APP_KEY)
		AppOpenHelper.hasInitTopOn = true

	}

}