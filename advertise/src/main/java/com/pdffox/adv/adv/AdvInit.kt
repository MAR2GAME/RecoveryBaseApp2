package com.pdffox.adv.adv

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.pdffox.adv.Config
import com.pdffox.adv.log.LogAdData
import com.pdffox.adv.log.LogAdParam
import com.pdffox.adv.log.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AdvInit {
	@Volatile
	private var admobInitStarted = false

	fun initAdv(context: Application) {
		if (Config.sdkConfig.adMob.enabled) {
			initAdmob(context)
		}
		// TODO: 第一次打开APP只初始化admob
		if (Config.sdkConfig.adMob.enabled) {
			AppOpenHelper.startObserve()
		}
	}

	fun initAdmob(context: Application) {
		if (admobInitStarted) {
			return
		}
		admobInitStarted = true
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

}
