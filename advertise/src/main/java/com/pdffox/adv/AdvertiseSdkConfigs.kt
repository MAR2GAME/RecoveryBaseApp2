package com.pdffox.adv

import android.content.Context

object AdvertiseSdkConfigs {
	fun create(
		context: Context,
		isDebug: Boolean,
		configure: AdvertiseSdkConfigBuilder.() -> Unit,
	): AdvertiseSdkConfig {
		return create(context.packageName, isDebug, configure)
	}

	fun create(
		packageName: String,
		isDebug: Boolean,
		configure: AdvertiseSdkConfigBuilder.() -> Unit,
	): AdvertiseSdkConfig {
		return AdvertiseSdkConfigBuilder(packageName, isDebug)
			.apply(configure)
			.build()
	}
}

class AdvertiseSdkConfigBuilder internal constructor(
	private val packageName: String,
	private val isDebug: Boolean,
) {
	private val defaults = AdvertiseSdkConfig(packageName = packageName)
	private var privacyUrl = defaults.privacyUrl
	private var termsUrl = defaults.termsUrl
	private var defaultTopic = defaults.defaultTopic.orEmpty()
	private var resources = defaults.resources
	private var server = defaults.server
	private var firebase = defaults.firebase
	private var remoteConfig = defaults.remoteConfig
	private var thinking = defaults.thinking
	private var singular = defaults.singular
	private var adMob = defaults.adMob
	private var facebook = defaults.facebook
	private var tiktok = defaults.tiktok
	private var safe = defaults.safe
	private var push = defaults.push
	private var notifications = defaults.notifications
	private var playIntegrity = defaults.playIntegrity

	fun legal(
		privacyUrl: String,
		termsUrl: String,
	) {
		this.privacyUrl = privacyUrl
		this.termsUrl = termsUrl
	}

	fun defaultTopic(value: String) {
		defaultTopic = value
	}

	fun resources(
		adPolicyRawResId: Int = R.raw.ad_policy,
		cloudCidrsRawResId: Int = R.raw.cloud,
		googleCidrsRawResId: Int = R.raw.google,
		pushConfigRawResId: Int = 0,
	) {
		resources = AdvertiseResourcesConfig(
			adPolicyRawResId = adPolicyRawResId,
			cloudCidrsRawResId = cloudCidrsRawResId,
			googleCidrsRawResId = googleCidrsRawResId,
			pushConfigRawResId = pushConfigRawResId,
		)
	}

	fun server(
		enabled: Boolean,
		releaseHost: String,
		testHost: String,
		parseTokenKey: String,
		checkPath: String = "/check",
		appConfigPath: String = "/getAppConfig",
		ipInfoPath: String = "/getIpInfo",
		ipInfoV2Path: String = "/getIpInfoV2",
		pushPath: String = "/publish",
		parseTokenPath: String = "/parseToken",
	) {
		server = AdvertiseServerConfig(
			enabled = enabled,
			releaseHost = releaseHost,
			testHost = testHost,
			checkPath = checkPath,
			appConfigPath = appConfigPath,
			ipInfoPath = ipInfoPath,
			ipInfoV2Path = ipInfoV2Path,
			pushPath = pushPath,
			parseTokenPath = parseTokenPath,
			parseTokenKey = parseTokenKey,
		)
	}

	fun firebase(
		analyticsEnabled: Boolean,
		messagingEnabled: Boolean,
		subscribeDefaultTopic: Boolean,
	) {
		firebase = FirebaseConfig(
			analyticsEnabled = analyticsEnabled,
			messagingEnabled = messagingEnabled,
			subscribeDefaultTopic = subscribeDefaultTopic,
		)
	}

	fun remoteConfig(enabled: Boolean) {
		remoteConfig = RemoteConfigFeatureConfig(enabled = enabled)
	}

	fun thinking(
		enabled: Boolean,
		appKey: String,
		serverUrl: String,
	) {
		thinking = ThinkingConfig(
			enabled = enabled,
			appKey = appKey,
			serverUrl = serverUrl,
		)
	}

	fun singular(
		enabled: Boolean,
		apiKey: String,
		secret: String,
	) {
		singular = SingularConfigValues(
			enabled = enabled,
			apiKey = apiKey,
			secret = secret,
		)
	}

	fun adMob(
		enabled: Boolean,
		appId: String,
		bannerId: String,
		interstitialId: String,
		nativeId: String,
		openId: String,
		debugNativeIdsJson: String = DEFAULT_DEBUG_NATIVE_IDS_JSON,
	) {
		adMob = AdMobConfig(
			enabled = enabled,
			appId = appId,
			bannerId = bannerId,
			interstitialId = interstitialId,
			nativeId = nativeId,
			openId = openId,
			debugNativeIdsJson = debugNativeIdsJson,
		)
	}

	fun facebook(
		enabled: Boolean,
		appId: String,
		clientToken: String,
		advertiserIdCollectionEnabled: Boolean = true,
	) {
		facebook = FacebookConfig(
			enabled = enabled,
			appId = appId,
			clientToken = clientToken,
			advertiserIdCollectionEnabled = advertiserIdCollectionEnabled,
		)
	}

	fun tiktok(
		enabled: Boolean,
		accessToken: String,
		ttAppId: String,
		appId: String?,
		startTrackOnInit: Boolean = enabled,
	) {
		tiktok = TikTokConfig(
			enabled = enabled,
			accessToken = accessToken,
			ttAppId = ttAppId,
			appId = appId?.takeIf { it.isNotBlank() },
			startTrackOnInit = startTrackOnInit,
		)
	}

	fun safe(
		enabled: Boolean,
		expectedSignatures: String,
		rejectDebuggableBuilds: Boolean = true,
		rejectDebuggerAttached: Boolean = true,
		killProcessOnFailure: Boolean = true,
		expectedPackageName: String? = null,
		enforceInDebugBuilds: Boolean = false,
	) {
		val shouldEnforce = !isDebug || enforceInDebugBuilds
		safe = SafeConfig(
			enabled = enabled,
			expectedPackageName = expectedPackageName,
			expectedSignatures = if (shouldEnforce) parseSignatures(expectedSignatures) else emptySet(),
			rejectDebuggableBuilds = shouldEnforce && rejectDebuggableBuilds,
			rejectDebuggerAttached = shouldEnforce && rejectDebuggerAttached,
			killProcessOnFailure = shouldEnforce && killProcessOnFailure,
		)
	}

	fun push(
		enabled: Boolean,
		persistentServiceEnabled: Boolean,
		firebaseMessagingServiceEnabled: Boolean,
		serviceStarterJobEnabled: Boolean,
		bootReceiverEnabled: Boolean,
		notificationDeletedReceiverEnabled: Boolean,
		fileProviderEnabled: Boolean,
		deletionObserverEnabled: Boolean,
		sceneKeys: PushSceneKeyConfig = PushSceneKeyConfig(),
		commonServiceClassName: String? = null,
		notificationDeletedAction: String? = null,
		fileProviderAuthority: String? = null,
	) {
		push = PushConfig(
			enabled = enabled,
			persistentServiceEnabled = persistentServiceEnabled,
			firebaseMessagingServiceEnabled = firebaseMessagingServiceEnabled,
			serviceStarterJobEnabled = serviceStarterJobEnabled,
			bootReceiverEnabled = bootReceiverEnabled,
			notificationDeletedReceiverEnabled = notificationDeletedReceiverEnabled,
			fileProviderEnabled = fileProviderEnabled,
			deletionObserverEnabled = deletionObserverEnabled,
			sceneKeys = sceneKeys,
			commonServiceClassName = commonServiceClassName,
			notificationDeletedAction = notificationDeletedAction,
			fileProviderAuthority = fileProviderAuthority,
		)
	}

	fun notifications(config: NotificationFeatureConfig) {
		notifications = config
	}

	fun playIntegrity(
		enabled: Boolean,
		cloudProjectNumber: Long,
		runInDebugBuilds: Boolean = false,
	) {
		playIntegrity = PlayIntegrityConfig(
			enabled = enabled,
			cloudProjectNumber = cloudProjectNumber,
			runInDebugBuilds = runInDebugBuilds,
		)
	}

	fun build(): AdvertiseSdkConfig {
		return AdvertiseSdkConfig(
			packageName = packageName,
			privacyUrl = privacyUrl,
			termsUrl = termsUrl,
			defaultTopic = resolvedDefaultTopic(),
			resources = resources,
			server = server,
			firebase = firebase,
			remoteConfig = remoteConfig,
			thinking = thinking,
			singular = singular,
			adMob = adMob,
			facebook = facebook,
			tiktok = tiktok,
			safe = safe,
			push = push,
			notifications = notifications,
			playIntegrity = playIntegrity,
		)
	}

	private fun resolvedDefaultTopic(): String {
		return defaultTopic.takeIf { it.isNotBlank() }
			?: if (isDebug) "debug-all" else "all"
	}

	private fun parseSignatures(value: String): Set<String> {
		return value
			.split(',', ';', '\n')
			.map { it.trim() }
			.filter { it.isNotEmpty() }
			.toSet()
	}
}
