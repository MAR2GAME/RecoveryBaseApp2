package com.pdffox.adv

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import cn.thinkingdata.analytics.TDAnalytics
import cn.thinkingdata.analytics.TDConfig
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.remoteConfig
import com.google.gson.Gson
import com.pdffox.adv.adv.AdChecker
import com.pdffox.adv.adv.AdConfig
import com.pdffox.adv.adv.AdLoader
import com.pdffox.adv.adv.AdvIDs
import com.pdffox.adv.adv.AdvCheckManager
import com.pdffox.adv.adv.AdvInit
import com.pdffox.adv.adv.AppOpenHelper
import com.pdffox.adv.adv.NativeConfig
import com.pdffox.adv.adv.ShowBannerAd
import com.pdffox.adv.adv.ShowInterstitialAdActivity
import com.pdffox.adv.adv.ShowNativeAd
import com.pdffox.adv.adv.ShowOpenAd
import com.pdffox.adv.adv.UMPUtil
import com.pdffox.adv.adv.policy.AdPolicyManager
import com.pdffox.adv.adv.policy.data.AdMapping
import com.pdffox.adv.adv.policy.data.parseAdMapping
import com.pdffox.adv.log.LogConfig
import com.pdffox.adv.log.LogParams
import com.pdffox.adv.log.LogUtil
import com.pdffox.adv.log.ThinkingAttr
import com.pdffox.adv.notification.CommonService
import com.pdffox.adv.notification.NotificationManager
import com.pdffox.adv.remoteconfig.RemoteConfig
import com.pdffox.adv.remoteconfig.RemoteConfigManager
import com.pdffox.adv.remoteconfig.RemoteConfigRouting
import com.pdffox.adv.util.PreferenceUtil
import com.singular.sdk.Singular
import com.singular.sdk.SingularConfig
import com.singular.sdk.SingularDeviceAttributionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

internal object Ads {
	private const val TAG = "Ads"
	private const val ADMOB_APPLICATION_ID_META_DATA = "com.google.android.gms.ads.APPLICATION_ID"
	const val LOAD_TIME_OPEN_APP = "open_app"
	const val LOAD_TIME_PLAY_FINISH = "play_finish"
	const val LOAD_TIME_ENTER_BACKGROUND = "enter_background"
	const val LOAD_TIME_RECEIVE_NOTIFICATION = "receive_notification"
	const val LOAD_TIME_ENTER_FEATURE = "enter_features"

	@Volatile
	private var desiredTopic: String = Config.topic
	@Volatile
	private var ipInfoCheckStarted = false
	@Volatile
	private var firebaseAnalyticsInitialized = false
	@Volatile
	private var firebaseMessagingInitialized = false
	@Volatile
	private var singularInitialized = false
	@Volatile
	private var thinkingInitialized = false
	@Volatile
	private var playIntegrityRequested = false

	lateinit var application: Application

	@RequiresPermission(allOf = [Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WAKE_LOCK])
	suspend fun init(
		context: Application,
		isTest: Boolean,
		sdkConfig: AdvertiseSdkConfig = Config.sdkConfig.copy(packageName = Config.sdkConfig.packageName ?: context.packageName),
	) {
		AdvRuntime.init(context)
		application = context
		Config.isTest = isTest
		configure(sdkConfig, context.packageName)
		if (sdkConfig.adMob.enabled) {
			validateAdMobAppId(context, sdkConfig)
		}
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "init: appInstallTime ${getAppInstallTime(context)}")
		}
		PreferenceUtil.init(context)
		AdvRuntime.initConfiguredIntegrations()
		if (sdkConfig.server.enabled) {
			startIpInfoCheck()
		}
		initConfig()
		initFireBase()
		if (sdkConfig.remoteConfig.enabled) {
			RemoteConfigManager.initRemoteConfig()
		}
		if (sdkConfig.singular.enabled) {
			initSingular()
		}
		if (sdkConfig.thinking.enabled) {
			initThinking()
		}
		AdPolicyManager.loadPolicyFromLocal(context)
		if (sdkConfig.adMob.enabled) {
			AdvInit.initAdv(context)
			AdChecker.startAutoCheck()
		}
		if (sdkConfig.notifications.enabled) {
			NotificationManager.startObservers(context)
		}
		if (shouldRequestPlayIntegrity(sdkConfig)) {
			PlayIntegrityHelper().requestPlayIntegrity()
		}
		val isFirstOpen = PreferenceUtil.getBoolean(LogConfig.app_first_open, false)
		if (!isFirstOpen) {
			LogUtil.log(LogConfig.app_first_open, mapOf(
				LogParams.timesmap to System.currentTimeMillis(),
				LogParams.time to ThinkingAttr.convertToCaliforniaTime(System.currentTimeMillis()),
			))
			PreferenceUtil.commitBoolean(LogConfig.app_first_open, true)
		}
	}

	private fun shouldRequestPlayIntegrity(sdkConfig: AdvertiseSdkConfig): Boolean {
		if (!sdkConfig.playIntegrity.enabled || sdkConfig.playIntegrity.cloudProjectNumber <= 0L) {
			return false
		}
		if (com.pdffox.adv.Config.isTest && !sdkConfig.playIntegrity.runInDebugBuilds) {
			return false
		}
		if (playIntegrityRequested) {
			return false
		}
		playIntegrityRequested = true
		return true
	}

	fun configure(sdkConfig: AdvertiseSdkConfig, fallbackPackageName: String? = null) {
		Config.applySdkConfig(sdkConfig, fallbackPackageName)
		AdvIDs.configure(sdkConfig)
		desiredTopic = Config.topic
	}

	private fun validateAdMobAppId(context: Context, sdkConfig: AdvertiseSdkConfig) {
		val configuredAppId = sdkConfig.adMob.appId
		val manifestAppId = readManifestMetaData(context, ADMOB_APPLICATION_ID_META_DATA)
		if (manifestAppId == configuredAppId) {
			return
		}
		val message = "Configured AdMob appId '$configuredAppId' does not match manifest value '${manifestAppId ?: "<missing>"}'. Declare com.google.android.gms.ads.APPLICATION_ID metadata in the host app manifest, directly or through manifestPlaceholders."
		if (com.pdffox.adv.Config.isTest) {
			error(message)
		} else {
			Log.w(TAG, message)
		}
	}

	private fun readManifestMetaData(context: Context, key: String): String? {
		return runCatching {
			@Suppress("DEPRECATION")
			context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
				.metaData
				?.getString(key)
		}.onFailure {
			if (com.pdffox.adv.Config.isTest) {
				Log.w(TAG, "readManifestMetaData: failed to read $key", it)
			}
		}.getOrNull()
	}

	private fun startIpInfoCheck() {
		if (ipInfoCheckStarted || !Config.isServerEnabled || Config.IPInfoV2Url.isBlank()) {
			return
		}
		ipInfoCheckStarted = true
		CoroutineScope(Dispatchers.IO).launch {
			val result = AdvCheckManager.getIpInfoV2()
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "startIpInfoCheck: result=$result")
			}
		}
	}

	fun getAppInstallTime(context: Context): Long {
		return try {
			val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
			packageInfo.firstInstallTime
		} catch (e: Exception) {
			e.printStackTrace()
			0L
		}
	}

	fun initConfig() {
		if (AdvCheckManager.params.isFirstOpen) {
			AdvCheckManager.params.installTime = System.currentTimeMillis()
			AdvCheckManager.params.isFirstOpen = false
		}
		val localeCountry = Locale.getDefault().country
		Config.country = localeCountry
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "initConfig: Config.country = ${Config.country}")
		}
	}

	fun showInterstitialAd(activity: Activity, areaKey: String, onClosed: () -> Unit) {
		if (!Config.hasEnabledAdNetwork()) {
			onClosed()
			return
		}
		ShowInterstitialAdActivity.openPage(activity, areaKey, onClosed)
	}

	@RequiresPermission(Manifest.permission.INTERNET)
	fun getNativeAd(context: Context, areaKey: String, onAdGroupLoaded: () -> Unit): NativeAdContent? {
		if (!Config.hasEnabledAdNetwork()) {
			return null
		}
		return ShowNativeAd.getNativeAd(context, areaKey, onAdGroupLoaded)
	}

	val nativeRefreshTime: Long
		get() = NativeConfig.native_refresh_time

	val guidePageSwapTime: Long
		get() = NativeConfig.guide_page_swap_time

	var isAppOpenAdEnabled: Boolean
		get() = AdConfig.isOpenAppOpenHelper
		set(value) {
			AdConfig.isOpenAppOpenHelper = value
		}

	var suppressNextAppOpenAd: Boolean
		get() = AppOpenHelper.spSwitch
		set(value) {
			AppOpenHelper.spSwitch = value
		}

	val shouldIgnoreGuide: Boolean
		get() = AdConfig.ignoreGuide

	val hasOpenLaunchPage: Boolean
		get() = AdConfig.hasOpenLaungPage

	val isFirstOpenAdEnabled: Boolean
		get() = AdPolicyManager.adPolicy?.first_open_enabled != false

	val isGoogleIp: Boolean
		get() = Config.isGoogleIP

	val isPaidUser: Boolean
		get() = Config.paid_0

	val shouldSuppressAdsForCurrentUser: Boolean
		get() = Config.paid_0 || Config.isGoogleIP

	val isNature: Boolean
		get() = Config.isNature

	val topic: String
		get() = Config.topic

	val privacyUrl: String
		get() = Config.PrivacyUrl

	val termsUrl: String
		get() = Config.TermsUrl

	fun canPreloadOpen(loadTimeKey: String): Boolean = AdConfig.canLoadOpen(loadTimeKey)

	fun canPreloadInterstitial(loadTimeKey: String): Boolean = AdConfig.canLoadInter(loadTimeKey)

	fun canPreloadNative(loadTimeKey: String): Boolean = AdConfig.canLoadNative(loadTimeKey)

	fun preloadOpen(context: Context) {
		AdLoader.loadOpen(context)
	}

	fun preloadInterstitial(context: Context) {
		AdLoader.loadInter(context)
	}

	fun preloadNative(context: Context, onAdGroupLoaded: (() -> Unit)? = null) {
		AdLoader.fillNativePool(context, onAdGroupLoaded)
	}

	@RequiresPermission(Manifest.permission.INTERNET)
	fun getBannerAd(context: Context, areaKey: String): ViewGroup? {
		if (!Config.hasEnabledAdNetwork()) {
			return null
		}
		if (!Config.isTest && (Config.paid_0 || Config.isGoogleIP)) {
			return null
		}
		return ShowBannerAd.getBannerAd(context, areaKey)
	}

	fun showOpenAd(activity: Activity, areaKey: String, onCloseListener: ShowOpenAd.OpenAdCloseListener?, onLoadedListener: ShowOpenAd.OpenAdLoadedListener?, onPaidListener: ShowOpenAd.OpenAdPaidListener?) {
		if (!Config.hasEnabledAdNetwork()) {
			onCloseListener?.onClose()
			return
		}
		ShowOpenAd.showOpenAd(activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
	}

	fun initConsent(activity: Activity, onComplete: (success: Boolean) -> Unit): Boolean {
		return UMPUtil.initUMP(activity, onComplete)
	}

	fun showSplashConsent(activity: Activity, onComplete: () -> Unit) {
		UMPUtil.showSplashUMP(activity, onComplete)
	}

	fun showPrivacyOptions(activity: Activity) {
		UMPUtil.showUMP(activity)
	}

	val isPrivacyOptionsRequired: Boolean
		get() = UMPUtil.isPrivacyOptionsRequired

	fun logEvent(eventName: String, params: Map<String, Any>) {
		LogUtil.log(eventName, params)
	}

	fun getPreferenceString(key: String, defaultValue: String): String {
		return PreferenceUtil.getString(key, defaultValue) ?: defaultValue
	}

	fun putPreferenceString(key: String, value: String) {
		PreferenceUtil.commitString(key, value)
	}

	fun setUserOnceAttr(key: String, value: String) {
		ThinkingAttr.setUserOnceAttr(key, value)
	}

	fun setUserAttr(key: String, value: Any) {
		ThinkingAttr.setUserAttr(key, value)
	}

	fun getFirstOpenTime(): String = ThinkingAttr.getFirstOpenTime()

	fun getLatestOpenTime(): String = ThinkingAttr.getLatestOpenTime()

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
		if (Config.sdkConfig.notifications.enabled) {
			NotificationManager.sendNotificationDetail(notificationType, configName)
		}
	}

	fun ensurePersistentNotificationServiceRunning(context: Context) {
		CommonService.start(context)
	}

	internal fun createLaunchIntent(
		context: Context,
		appOpenFrom: String,
		route: String = "",
		scene: String = "",
	): Intent? {
		val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
		return launchIntent.apply {
			putExtra("AppOpenFrom", appOpenFrom)
			putExtra("Route", route)
			if (scene.isNotBlank()) {
				putExtra("Scene", scene)
			}
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
		}
	}

	@RequiresPermission(allOf = [Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WAKE_LOCK])
	private fun initFireBase() {
		val firebaseConfig = Config.sdkConfig.firebase
		if (!firebaseConfig.analyticsEnabled && !firebaseConfig.messagingEnabled) {
			return
		}
		if (firebaseConfig.analyticsEnabled && !firebaseAnalyticsInitialized) {
			firebaseAnalyticsInitialized = true
			val mFirebaseAnalytics = FirebaseAnalytics.getInstance(application)
			mFirebaseAnalytics.setAnalyticsCollectionEnabled(true)
		}
		if (!firebaseConfig.messagingEnabled || !firebaseConfig.subscribeDefaultTopic) {
			return
		}
		if (firebaseMessagingInitialized) {
			return
		}
		firebaseMessagingInitialized = true
		val defaultTopic = getDefaultTopic()
		RemoteConfig.topicSet.add(defaultTopic)
		FirebaseMessaging.getInstance().subscribeToTopic(defaultTopic)
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					if (desiredTopic == defaultTopic || desiredTopic.isBlank()) {
						rememberActiveTopic(defaultTopic)
					} else {
						FirebaseMessaging.getInstance().unsubscribeFromTopic(defaultTopic)
						if (com.pdffox.adv.Config.isTest) {
							Log.e(TAG, "initFireBase: topic[$defaultTopic] expired, unsubscribed")
						}
					}
					if (com.pdffox.adv.Config.isTest) {
						Log.e(TAG, "initFireBase: subscribed topic[$defaultTopic] success")
					}
				} else {
					if (com.pdffox.adv.Config.isTest) {
						Log.e(TAG, "initFireBase: subscribed topic[$defaultTopic] failed")
					}
				}
			}
	}

	private fun getDefaultTopic(): String = if (com.pdffox.adv.Config.isTest) "debug-all" else "all"

	private fun rememberActiveTopic(topic: String) {
		Config.topic = topic
		RemoteConfig.topicSet.clear()
		RemoteConfig.topicSet.add(topic)
		if (com.pdffox.adv.Config.isTest) {
			PreferenceUtil.commitString("Config.topic", Config.topic)
		}
	}

	fun changeTopic(newTopic: String) {
		if (newTopic.isBlank() || !Config.sdkConfig.firebase.messagingEnabled) {
			return
		}
		desiredTopic = newTopic
		val topicsToUnsubscribe = linkedSetOf<String>().apply {
			addAll(RemoteConfig.topicSet)
			add(Config.topic)
			add(getDefaultTopic())
		}.filter { it.isNotBlank() && it != newTopic }
		if (topicsToUnsubscribe.isEmpty() && Config.topic == newTopic) {
			return
		}
		for (item in topicsToUnsubscribe) {
			FirebaseMessaging.getInstance().unsubscribeFromTopic(item)
		}
		FirebaseMessaging.getInstance().subscribeToTopic(newTopic)
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					if (desiredTopic == newTopic) {
						rememberActiveTopic(newTopic)
						if (com.pdffox.adv.Config.isTest) {
							Log.e(TAG, "subscribed topic[$newTopic] success")
						}
					} else {
						FirebaseMessaging.getInstance().unsubscribeFromTopic(newTopic)
						if (com.pdffox.adv.Config.isTest) {
							Log.e(TAG, "subscribed topic[$newTopic] stale, unsubscribed")
						}
					}
				} else {
					if (com.pdffox.adv.Config.isTest) {
						Log.e(TAG, "subscribed topic[$newTopic] failed")
					}
				}
			}
	}

	private fun initThinking() {
		if (thinkingInitialized || Config.ThinkingKey.isBlank() || Config.ThinkkingUrl.isBlank()) {
			return
		}
		thinkingInitialized = true
		// 获取 TDConfig 实例
		val config = TDConfig.getInstance(application, Config.ThinkingKey, Config.ThinkkingUrl)
		/*
		设置运行模式为 Debug 模式
		NORMAL模式:数据会存入缓存，并依据一定的缓存策略上报,默认为NORMAL模式；建议在线上环境使用
		Debug模式:数据逐条上报。当出现问题时会以日志和异常的方式提示用户；不建议在线上环境使用
		DebugOnly模式:只对数据做校验，不会入库；不建议在线上环境使用
		 */
		config.setMode(if(Config.isTest) TDConfig.TDMode.DEBUG else TDConfig.TDMode.NORMAL)
		// 初始化 SDK
		TDAnalytics.init(config)
		//开启自动采集事件
		TDAnalytics.enableAutoTrack(
			TDAnalytics.TDAutoTrackEventType.APP_START or
					TDAnalytics.TDAutoTrackEventType.APP_END or
					TDAnalytics.TDAutoTrackEventType.APP_INSTALL
//					TDAnalytics.TDAutoTrackEventType.APP_VIEW_SCREEN or
//					TDAnalytics.TDAutoTrackEventType.APP_CLICK or
//					TDAnalytics.TDAutoTrackEventType.APP_CRASH
		)
		//打印SDK日志
//		TDAnalytics.enableLog(Config.isTest);
		if (Config.isTest) {
			val deviceId = TDAnalytics.getDeviceId()
			Log.e(TAG, "initThinking: deviceId = $deviceId" )
		}
		if (com.pdffox.adv.Config.isTest) {
			val superProperties = TDAnalytics.getSuperProperties()
			val presetProperties = TDAnalytics.getPresetProperties()
			Log.e(TAG, "initThinking: superProperties = $superProperties" )
			Log.e(TAG, "initThinking: presetProperties = ${Gson().toJson(presetProperties)}" )
		}
	}

	private fun initSingular() {
		if (singularInitialized || Config.Singular_Api_Key.isBlank() || Config.Singular_Secret.isBlank()) {
			return
		}
		singularInitialized = true
		Log.e(TAG, "initSingular: 开始初始化" )
		val config = SingularConfig(Config.Singular_Api_Key, Config.Singular_Secret)
			.withLoggingEnabled()
			.withLogLevel(1)
			.withSingularDeviceAttribution { attributionData ->
				// TODO: 只有首次安装APP时该方法会被回调
				if (com.pdffox.adv.Config.isTest) {
					Log.e(TAG, "initSingular:  device attribution: $attributionData")
				}
				Config.singularHasResult = true
				val promoteParams = JSONObject()
				try {
					val network = attributionData["network"]?.toString().orEmpty()
					Log.e(TAG, "initSingular: $network")
					promoteParams.put("network", network)
					attributionData["campaign_name"]?.toString()?.let {
						if (it != "") {
							promoteParams.put("campaign_name", it)
						}
					}

					val isNatural = network.equals("organic", ignoreCase = true) || network.isEmpty()
					AdvCheckManager.params.fromNature = isNatural
					if (com.pdffox.adv.Config.isTest) {
						Config.isNature = false
					} else {
						Config.isNature = isNatural
					}
					promoteParams.put("fromNature", isNatural)
//					TDAnalytics.setSuperProperties(promoteParams)
					if (Config.sdkConfig.thinking.enabled) {
						TDAnalytics.userSet(promoteParams)
					}
					if (Config.sdkConfig.remoteConfig.enabled && Config.remoteConfigHasResult) {
						val remoteConfig = Firebase.remoteConfig
						val adMapping = remoteConfig.getString("ad_mapping")
						RemoteConfigRouting.apply(
							remoteConfig = remoteConfig,
							adMapping = adMapping,
							source = "Ads.initSingular"
						)
					} else {
						if (com.pdffox.adv.Config.isTest) {
							Log.e(TAG, "initSingular: RemoteConfig还未更新先不处理" )
						}
					}
				} catch (e: JSONException) {
					// 处理异常
					Log.e(TAG, "onDeviceAttributionInfoReceived: ", e)
				}
			}
		Singular.init(application, config)
		if (com.pdffox.adv.Config.isTest) {
			val singularGlobalProperties = Singular.getGlobalProperties()
			Log.e(TAG, "initSingular: singularGlobalProperties = ${Gson().toJson(singularGlobalProperties)}" )
		}
	}

}
