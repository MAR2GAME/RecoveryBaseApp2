package com.pdffox.adv

data class AdvertiseSdkConfig(
	val packageName: String? = "com.datatool.photorecovery",
	val privacyUrl: String = "https://sites.google.com/view/photo-recovery-privacy-policy/",
	val termsUrl: String = "https://sites.google.com/view/photo-recovery-terms-condition/",
	val defaultTopic: String? = null,
	val resources: AdvertiseResourcesConfig = AdvertiseResourcesConfig(),
	val server: AdvertiseServerConfig = AdvertiseServerConfig(),
	val firebase: FirebaseConfig = FirebaseConfig(),
	val remoteConfig: RemoteConfigFeatureConfig = RemoteConfigFeatureConfig(),
	val thinking: ThinkingConfig = ThinkingConfig(),
	val singular: SingularConfigValues = SingularConfigValues(),
	val adMob: AdMobConfig = AdMobConfig(),
	val facebook: FacebookConfig = FacebookConfig(),
	val tiktok: TikTokConfig = TikTokConfig(),
	val safe: SafeConfig = SafeConfig(),
	val push: PushConfig = PushConfig(),
	val notifications: NotificationFeatureConfig = NotificationFeatureConfig(),
	val playIntegrity: PlayIntegrityConfig = PlayIntegrityConfig(),
)

data class AdvertiseResourcesConfig(
	val adPolicyRawResId: Int = R.raw.ad_policy,
	val cloudCidrsRawResId: Int = R.raw.cloud,
	val googleCidrsRawResId: Int = R.raw.google,
	val pushConfigRawResId: Int = 0,
)

data class AdvertiseServerConfig(
	val enabled: Boolean = true,
	val releaseHost: String = "https://api.newminigame.online",
	val testHost: String = "http://192.168.110.68:10002",
	val checkPath: String = "/check",
	val appConfigPath: String = "/getAppConfig",
	val ipInfoPath: String = "/getIpInfo",
	val ipInfoV2Path: String = "/getIpInfoV2",
	val pushPath: String = "/publish",
	val parseTokenPath: String = "/parseToken",
	val parseTokenKey: String = "TianWangGaiDiHu",
)

data class FirebaseConfig(
	val analyticsEnabled: Boolean = true,
	val messagingEnabled: Boolean = true,
	val subscribeDefaultTopic: Boolean = true,
)

data class RemoteConfigFeatureConfig(
	val enabled: Boolean = true,
)

data class ThinkingConfig(
	val enabled: Boolean = true,
	val appKey: String = "1467641c0ffa4ef6a20f8a8b7e1d64a7",
	val serverUrl: String = "https://mar2.top",
)

data class SingularConfigValues(
	val enabled: Boolean = true,
	val apiKey: String = "mar2game_f7b9272a",
	val secret: String = "72b3df2ee5d0a64a6c404ce01937c3d6",
)

data class AdMobConfig(
	val enabled: Boolean = true,
	val appId: String = "ca-app-pub-3615322193850391~3893272881",
	val bannerId: String = "ca-app-pub-3615322193850391/4485010266",
	val interstitialId: String = "ca-app-pub-3615322193850391/2830923916",
	val nativeId: String = "ca-app-pub-3615322193850391/5283086610",
	val openId: String = "ca-app-pub-3615322193850391/9204760570",
	val testBannerId: String = "ca-app-pub-3940256099942544/9214589741",
	val testInterstitialId: String = "ca-app-pub-3940256099942544/1033173712",
	val testNativeId: String = "ca-app-pub-3940256099942544/2247696110",
	val testOpenId: String = "ca-app-pub-3940256099942544/9257395921",
	val debugNativeIdsJson: String = DEFAULT_DEBUG_NATIVE_IDS_JSON,
)

data class FacebookConfig(
	val enabled: Boolean = true,
	val appId: String = "1590185508637811",
	val clientToken: String = "6d8edd1c9853e57c091f57e390421ddd",
	val advertiserIdCollectionEnabled: Boolean = true,
)

data class TikTokConfig(
	val enabled: Boolean = true,
	val accessToken: String = "TTpjhQJCkNhW2m9kobQVUiIOUciGopjh",
	val ttAppId: String = "7624342473057271816",
	val appId: String? = "com.datatool.photorecovery",
	val startTrackOnInit: Boolean = true,
)

data class SafeConfig(
	val enabled: Boolean = true,
	val expectedPackageName: String? = "com.datatool.photorecovery",
	val expectedSignatures: Set<String> = setOf(
		"BC9226C0D24125D7BFF05CF3D746EFFCF72AB101E8B14BAFB1EB7C08557BECDC",
		"4CCC0599E4F4718BEA0E7BE46A21D4FC0D5F35656CAD670784AE5893DB0C075D",
	),
	val rejectDebuggableBuilds: Boolean = true,
	val rejectDebuggerAttached: Boolean = true,
	val killProcessOnFailure: Boolean = true,
)

data class PushConfig(
	val enabled: Boolean = true,
	val persistentServiceEnabled: Boolean = true,
	val firebaseMessagingServiceEnabled: Boolean = true,
	val serviceStarterJobEnabled: Boolean = true,
	val bootReceiverEnabled: Boolean = true,
	val notificationDeletedReceiverEnabled: Boolean = true,
	val fileProviderEnabled: Boolean = true,
	val deletionObserverEnabled: Boolean = true,
	val sceneKeys: PushSceneKeyConfig = PushSceneKeyConfig(),
	val commonServiceClassName: String? = null,
	val notificationDeletedAction: String? = null,
	val fileProviderAuthority: String? = null,
)

data class PushSceneKeyConfig(
	val chargingStarted: String = "charging_start",
	val chargingEnded: String = "charging_end",
	val screenOn: String = "screen_on_5s",
	val userPresent: String = "phone_unlock",
	val packageAdded: String = "app_installed",
	val packageRemoved: String = "app_uninstalled",
	val imageDeleted: String = "delete_photos",
	val videoDeleted: String = "delete_videos",
	val fileDeleted: String = "delete_files",
)

data class NotificationFeatureConfig(
	val enabled: Boolean = true,
	val smallIconResId: Int = 0,
	val persistentContentText: String = "",
	val persistentActions: List<NotificationActionConfig> = emptyList(),
	val routeMappings: List<NotificationRouteMapping> = emptyList(),
)

data class NotificationActionConfig(
	val route: String,
	val label: String = route,
	val iconResId: Int = 0,
)

data class NotificationRouteMapping(
	val rawRoute: String,
	val route: String,
	val temporaryIconResId: Int = 0,
	val persistentIconResId: Int = 0,
)

data class PlayIntegrityConfig(
	val enabled: Boolean = true,
	val cloudProjectNumber: Long = 804850522653L,
	val runInDebugBuilds: Boolean = false,
)

const val DEFAULT_DEBUG_NATIVE_IDS_JSON = """
[
  {
    "highPriceID": "ca-app-pub-3940256099942544/2247696110",
    "midPriceID": "ca-app-pub-3940256099942544/2247696110",
    "lowPriceID": "ca-app-pub-3940256099942544/2247696110"
  },
  {
    "highPriceID": "ca-app-pub-3940256099942544/2247696110",
    "midPriceID": "ca-app-pub-3940256099942544/2247696110",
    "lowPriceID": "ca-app-pub-3940256099942544/2247696110"
  },
  {
    "highPriceID": "ca-app-pub-3940256099942544/2247696110",
    "midPriceID": "ca-app-pub-3940256099942544/2247696110",
    "lowPriceID": "ca-app-pub-3940256099942544/2247696110"
  }
]
"""
