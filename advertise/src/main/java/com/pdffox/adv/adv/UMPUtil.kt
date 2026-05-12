package com.pdffox.adv.adv

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.util.PreferenceDelegate

object UMPUtil {

	private const val TAG = "UMPUtil"

	var isPrivacyOptionsRequired = false

	var requestUMPTime: Long by PreferenceDelegate("requestUMPTime", 0L)
	var requestUMPResult: Boolean by PreferenceDelegate("requestUMPResult", false)

	fun initUMP(activity: Activity, onComplete: (success: Boolean) -> Unit) : Boolean {
		val TAG = "initUMP"
		Log.e(TAG, "initUMP: ", )

		// 检测缓存结果
		var cacheTime = if (com.pdffox.adv.Config.isTest) {
			1000 * 60 * 1
		} else {
			1000 * 60 * 60 * 24
		}
		if (System.currentTimeMillis() - requestUMPTime < cacheTime) {
			Log.e(TAG, "initUMP: 命中缓存" )
			return true
		}

		val params = ConsentRequestParameters.Builder()
			// 如果是调试模式，可以打开下面代码，模拟测试环境
			.setConsentDebugSettings(
				if (com.pdffox.adv.Config.isTest) {
					ConsentDebugSettings.Builder(activity)
						.setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
						.addTestDeviceHashedId("11BF2FA96B84C5054A2D65A532710C0D")
						.build()
				} else {
					ConsentDebugSettings.Builder(activity)
						.build()
				}
			)
			.build()

		val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
		if (com.pdffox.adv.Config.isTest) {
			consentInformation.reset()
		}

		val handler = android.os.Handler(activity.mainLooper)
		var isCallbackCalled = false

		// 6秒超时任务
		val timeoutRunnable = Runnable {
			if (!isCallbackCalled) {
				isCallbackCalled = true
				Log.e(TAG, "initUMP: requestConsentInfoUpdate 超时")
				onComplete.invoke(false)
			}
		}
		handler.postDelayed(timeoutRunnable, 6000)

		consentInformation.requestConsentInfoUpdate(
			activity,
			params,
			{
				Log.e(TAG, "initUMP: requestConsentInfoUpdate success: ${consentInformation.consentStatus} ${consentInformation.isConsentFormAvailable}", )
				if (!isCallbackCalled) {
					isCallbackCalled = true
					handler.removeCallbacks(timeoutRunnable)
					isPrivacyOptionsRequired = consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
					requestUMPTime = System.currentTimeMillis()
					requestUMPResult = isPrivacyOptionsRequired
					onComplete.invoke(true)
				}
			},
			{ formError ->
				Log.e(TAG, "UMP requestConsentInfoUpdate failed: ${formError.message}")
				if (!isCallbackCalled) {
					isCallbackCalled = true
					handler.removeCallbacks(timeoutRunnable)
					onComplete.invoke(false)
				}
			}
		)
		return false
	}

	fun showSplashUMP(activity: Activity, onComplete: () -> Unit) {
		val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
		if (consentInformation.isConsentFormAvailable) {
			loadConsentForm(activity, onComplete)
		} else {
			onComplete.invoke()
		}
	}

	private fun loadConsentForm(activity: Activity, onComplete: () -> Unit) {
		UserMessagingPlatform.loadConsentForm(
			activity,
			{ consentForm ->
				val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
				if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
					consentForm.show(
						activity,
						{
							// 用户完成同意后，重新检查状态
							val updatedConsentInformation = UserMessagingPlatform.getConsentInformation(activity)
							if (updatedConsentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
								Log.e(TAG, "用户已同意")
							}
							onComplete.invoke()
						}
					)
				} else {
					onComplete.invoke()
				}
			},
			{ loadError ->
				Log.e(TAG, "UMP loadConsentForm failed: ${loadError.message}")
				onComplete.invoke()
			}
		)
	}

	fun showUMP(activity: Activity) {
		UserMessagingPlatform.showPrivacyOptionsForm(activity) { }
	}
}