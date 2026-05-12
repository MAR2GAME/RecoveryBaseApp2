package com.pdffox.adv

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import cn.thinkingdata.analytics.TDAnalytics
import com.pdffox.adv.adv.ShowOpenAd
import org.json.JSONObject

object AdvertiseSdk {
	const val LOAD_TIME_OPEN_APP = "open_app"
	const val LOAD_TIME_PLAY_FINISH = "play_finish"
	const val LOAD_TIME_ENTER_BACKGROUND = "enter_background"
	const val LOAD_TIME_RECEIVE_NOTIFICATION = "receive_notification"
	const val LOAD_TIME_ENTER_FEATURE = "enter_features"

	fun interface OpenAdCloseListener {
		fun onClose()
	}

	fun interface OpenAdLoadedListener {
		fun onLoaded()
	}

	fun interface OpenAdPaidListener {
		fun onPaid(value: Long)
	}

	@RequiresPermission(allOf = [Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WAKE_LOCK])
	suspend fun init(
		context: Application,
		isTest: Boolean,
		sdkConfig: AdvertiseSdkConfig = Config.sdkConfig.copy(packageName = Config.sdkConfig.packageName ?: context.packageName),
	) {
		Ads.init(context, isTest, sdkConfig)
	}

	fun showInterstitialAd(activity: Activity, areaKey: String, onClosed: () -> Unit) {
		Ads.showInterstitialAd(activity, areaKey, onClosed)
	}

	@RequiresPermission(Manifest.permission.INTERNET)
	fun getBannerAd(context: Context, areaKey: String): ViewGroup? {
		return Ads.getBannerAd(context, areaKey)
	}

	fun showOpenAd(
		activity: Activity,
		areaKey: String,
		onCloseListener: OpenAdCloseListener?,
		onLoadedListener: OpenAdLoadedListener?,
		onPaidListener: OpenAdPaidListener?,
	) {
		Ads.showOpenAd(
			activity = activity,
			areaKey = areaKey,
			onCloseListener = onCloseListener?.let { listener ->
				ShowOpenAd.OpenAdCloseListener { listener.onClose() }
			},
			onLoadedListener = onLoadedListener?.let { listener ->
				ShowOpenAd.OpenAdLoadedListener { listener.onLoaded() }
			},
			onPaidListener = onPaidListener?.let { listener ->
				ShowOpenAd.OpenAdPaidListener { value -> listener.onPaid(value) }
			},
		)
	}

	val guidePageSwapTime: Long
		get() = Ads.guidePageSwapTime

	var isAppOpenAdEnabled: Boolean
		get() = Ads.isAppOpenAdEnabled
		set(value) {
			Ads.isAppOpenAdEnabled = value
		}

	var suppressNextAppOpenAd: Boolean
		get() = Ads.suppressNextAppOpenAd
		set(value) {
			Ads.suppressNextAppOpenAd = value
		}

	val shouldIgnoreGuide: Boolean
		get() = Ads.shouldIgnoreGuide

	val hasOpenLaunchPage: Boolean
		get() = Ads.hasOpenLaunchPage

	val isFirstOpenAdEnabled: Boolean
		get() = Ads.isFirstOpenAdEnabled

	val isGoogleIp: Boolean
		get() = Ads.isGoogleIp

	val isPaidUser: Boolean
		get() = Ads.isPaidUser

	val shouldSuppressAdsForCurrentUser: Boolean
		get() = Ads.shouldSuppressAdsForCurrentUser

	val isNature: Boolean
		get() = Ads.isNature

	val topic: String
		get() = Ads.topic

	val privacyUrl: String
		get() = Ads.privacyUrl

	val termsUrl: String
		get() = Ads.termsUrl

	fun canPreloadOpen(loadTimeKey: String): Boolean = Ads.canPreloadOpen(loadTimeKey)

	fun canPreloadInterstitial(loadTimeKey: String): Boolean = Ads.canPreloadInterstitial(loadTimeKey)

	fun canPreloadNative(loadTimeKey: String): Boolean = Ads.canPreloadNative(loadTimeKey)

	fun preloadOpen(context: Context) {
		Ads.preloadOpen(context)
	}

	fun preloadInterstitial(context: Context) {
		Ads.preloadInterstitial(context)
	}

	fun preloadNative(context: Context, onAdGroupLoaded: (() -> Unit)? = null) {
		Ads.preloadNative(context, onAdGroupLoaded)
	}

	fun initConsent(activity: Activity, onComplete: (success: Boolean) -> Unit): Boolean {
		return Ads.initConsent(activity, onComplete)
	}

	fun showSplashConsent(activity: Activity, onComplete: () -> Unit) {
		Ads.showSplashConsent(activity, onComplete)
	}

	fun showPrivacyOptions(activity: Activity) {
		Ads.showPrivacyOptions(activity)
	}

	val isPrivacyOptionsRequired: Boolean
		get() = Ads.isPrivacyOptionsRequired

	fun logEvent(eventName: String, params: Map<String, Any>) {
		Ads.logEvent(eventName, params)
	}

	fun getPreferenceString(key: String, defaultValue: String): String {
		return Ads.getPreferenceString(key, defaultValue)
	}

	fun putPreferenceString(key: String, value: String) {
		Ads.putPreferenceString(key, value)
	}

	fun setUserOnceAttr(key: String, value: String) {
		Ads.setUserOnceAttr(key, value)
	}

	fun setUserAttr(key: String, value: Any) {
		Ads.setUserAttr(key, value)
	}

	fun getFirstOpenTime(): String = Ads.getFirstOpenTime()

	fun getLatestOpenTime(): String = Ads.getLatestOpenTime()

	fun setSuperProperties(properties: Map<String, Any?>) {
		val json = JSONObject()
		properties.forEach { (key, value) ->
			json.put(key, value)
		}
		TDAnalytics.setSuperProperties(json)
	}

	fun getThinkingDeviceId(): String = TDAnalytics.getDeviceId()

	object ThinkingKeys {
		const val firstOpenTime = "first_open_time"
		const val latestOpenTime = "latest_open_time"
	}

	object PushLog {
		const val notificationClicked = "notification_clicked"
		const val msgId = "msg_id"
		const val targetUserId = "target_user_id"
	}

	fun sendDebugNotification(context: Context, notificationType: String, configName: String) {
		Ads.sendDebugNotification(context, notificationType, configName)
	}

	fun ensurePersistentNotificationServiceRunning(context: Context) {
		Ads.ensurePersistentNotificationServiceRunning(context)
	}
}
