//package com.datatool.photorecovery.core.advertise
//
//import android.content.Context
//import com.datatool.photorecovery.BuildConfig
//import com.datatool.photorecovery.R
//import com.pdffox.adv.AdvertiseSdkConfig
//import com.pdffox.adv.AdvertiseSdkConfigs
//import com.pdffox.adv.NotificationActionConfig
//import com.pdffox.adv.NotificationFeatureConfig
//import com.pdffox.adv.NotificationRouteMapping
//import com.pdffox.adv.PushSceneKeyConfig
//
//object AdvertiseConfigFactory {
//	fun create(context: Context): AdvertiseSdkConfig {
//		return AdvertiseSdkConfigs.create(context, BuildConfig.DEBUG) {
//			legal(
//				privacyUrl = BuildConfig.ADV_PRIVACY_URL,
//				termsUrl = BuildConfig.ADV_TERMS_URL,
//			)
//			defaultTopic(BuildConfig.ADV_DEFAULT_TOPIC)
//			resources(pushConfigRawResId = R.raw.push)
//			server(
//				enabled = BuildConfig.ADV_SERVER_ENABLED,
//				releaseHost = BuildConfig.ADV_SERVER_RELEASE_HOST,
//				testHost = BuildConfig.ADV_SERVER_TEST_HOST,
//				parseTokenKey = BuildConfig.ADV_SERVER_PARSE_TOKEN_KEY,
//			)
//			firebase(
//				analyticsEnabled = BuildConfig.ADV_FIREBASE_ANALYTICS_ENABLED,
//				messagingEnabled = BuildConfig.ADV_FIREBASE_MESSAGING_ENABLED,
//				subscribeDefaultTopic = BuildConfig.ADV_FIREBASE_SUBSCRIBE_DEFAULT_TOPIC,
//			)
//			remoteConfig(enabled = BuildConfig.ADV_REMOTE_CONFIG_ENABLED)
//			thinking(
//				enabled = BuildConfig.ADV_THINKING_ENABLED,
//				appKey = BuildConfig.ADV_THINKING_APP_KEY,
//				serverUrl = BuildConfig.ADV_THINKING_SERVER_URL,
//			)
//			singular(
//				enabled = BuildConfig.ADV_SINGULAR_ENABLED,
//				apiKey = BuildConfig.ADV_SINGULAR_API_KEY,
//				secret = BuildConfig.ADV_SINGULAR_SECRET,
//			)
//			adMob(
//				enabled = BuildConfig.ADV_ADMOB_ENABLED,
//				appId = BuildConfig.ADV_ADMOB_APP_ID,
//				bannerId = BuildConfig.ADV_ADMOB_BANNER_ID,
//				interstitialId = BuildConfig.ADV_ADMOB_INTERSTITIAL_ID,
//				nativeId = BuildConfig.ADV_ADMOB_NATIVE_ID,
//				openId = BuildConfig.ADV_ADMOB_OPEN_ID,
//			)
//			facebook(
//				enabled = BuildConfig.ADV_FACEBOOK_ENABLED,
//				appId = BuildConfig.ADV_FACEBOOK_APP_ID,
//				clientToken = BuildConfig.ADV_FACEBOOK_CLIENT_TOKEN,
//			)
//			tiktok(
//				enabled = BuildConfig.ADV_TIKTOK_ENABLED,
//				accessToken = BuildConfig.ADV_TIKTOK_ACCESS_TOKEN,
//				ttAppId = BuildConfig.ADV_TIKTOK_TT_APP_ID,
//				appId = BuildConfig.ADV_TIKTOK_APP_ID,
//			)
//			safe(
//				enabled = BuildConfig.ADV_SAFE_ENABLED,
//				expectedSignatures = BuildConfig.ADV_SAFE_EXPECTED_SIGNATURES,
//				rejectDebuggableBuilds = BuildConfig.ADV_SAFE_REJECT_DEBUGGABLE_BUILDS,
//				rejectDebuggerAttached = BuildConfig.ADV_SAFE_REJECT_DEBUGGER_ATTACHED,
//				killProcessOnFailure = BuildConfig.ADV_SAFE_KILL_PROCESS_ON_FAILURE,
//			)
//			push(
//				enabled = BuildConfig.ADV_PUSH_ENABLED,
//				persistentServiceEnabled = BuildConfig.ADV_PUSH_PERSISTENT_SERVICE_ENABLED,
//				firebaseMessagingServiceEnabled = BuildConfig.ADV_PUSH_FIREBASE_MESSAGING_SERVICE_ENABLED,
//				serviceStarterJobEnabled = BuildConfig.ADV_PUSH_SERVICE_STARTER_JOB_ENABLED,
//				bootReceiverEnabled = BuildConfig.ADV_PUSH_BOOT_RECEIVER_ENABLED,
//				notificationDeletedReceiverEnabled = BuildConfig.ADV_PUSH_NOTIFICATION_DELETED_RECEIVER_ENABLED,
//				fileProviderEnabled = BuildConfig.ADV_PUSH_FILE_PROVIDER_ENABLED,
//				deletionObserverEnabled = BuildConfig.ADV_PUSH_DELETION_OBSERVER_ENABLED,
//				sceneKeys = PushSceneKeyConfig(
//					chargingStarted = BuildConfig.ADV_PUSH_SCENE_CHARGING_STARTED,
//					chargingEnded = BuildConfig.ADV_PUSH_SCENE_CHARGING_ENDED,
//					screenOn = BuildConfig.ADV_PUSH_SCENE_SCREEN_ON,
//					userPresent = BuildConfig.ADV_PUSH_SCENE_USER_PRESENT,
//					packageAdded = BuildConfig.ADV_PUSH_SCENE_PACKAGE_ADDED,
//					packageRemoved = BuildConfig.ADV_PUSH_SCENE_PACKAGE_REMOVED,
//					imageDeleted = BuildConfig.ADV_PUSH_SCENE_IMAGE_DELETED,
//					videoDeleted = BuildConfig.ADV_PUSH_SCENE_VIDEO_DELETED,
//					fileDeleted = BuildConfig.ADV_PUSH_SCENE_FILE_DELETED,
//				),
//			)
//			notifications(notificationConfig(context))
//			playIntegrity(
//				enabled = BuildConfig.ADV_PLAY_INTEGRITY_ENABLED,
//				cloudProjectNumber = BuildConfig.ADV_PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER,
//			)
//		}
//	}
//
//	private fun notificationConfig(context: Context): NotificationFeatureConfig {
//		return NotificationFeatureConfig(
//			enabled = BuildConfig.ADV_NOTIFICATIONS_ENABLED,
//			smallIconResId = R.mipmap.logo_2026,
//			persistentContentText = context.getString(R.string.app_name),
//			persistentActions = listOf(
//				NotificationActionConfig(
//					route = "Photos",
//					label = context.getString(R.string.photos),
//					iconResId = com.pdffox.adv.R.drawable.notification_photos,
//				),
//				NotificationActionConfig(
//					route = "Videos",
//					label = context.getString(R.string.videos),
//					iconResId = com.pdffox.adv.R.drawable.notification_videos,
//				),
//				NotificationActionConfig(
//					route = "Files",
//					label = context.getString(R.string.other_files),
//					iconResId = com.pdffox.adv.R.drawable.notification_files,
//				),
//				NotificationActionConfig(
//					route = "Recovered",
//					label = context.getString(R.string.recovered),
//					iconResId = com.pdffox.adv.R.drawable.notification_recovered,
//				),
//			),
//			routeMappings = listOf(
//				NotificationRouteMapping(
//					rawRoute = "/recoverPhotos",
//					route = "Photos",
//					temporaryIconResId = com.pdffox.adv.R.drawable.notification_photos,
//					persistentIconResId = com.pdffox.adv.R.mipmap.ic_push_photos,
//				),
//				NotificationRouteMapping(
//					rawRoute = "/recoverVideos",
//					route = "Videos",
//					temporaryIconResId = com.pdffox.adv.R.drawable.notification_videos,
//					persistentIconResId = com.pdffox.adv.R.mipmap.ic_push_videos,
//				),
//				NotificationRouteMapping(
//					rawRoute = "/recoverFiles",
//					route = "Files",
//					temporaryIconResId = com.pdffox.adv.R.drawable.notification_files,
//					persistentIconResId = com.pdffox.adv.R.mipmap.ic_push_files,
//				),
//				NotificationRouteMapping(
//					rawRoute = "/recovered",
//					route = "Recovered",
//					temporaryIconResId = com.pdffox.adv.R.drawable.notification_recovered,
//					persistentIconResId = com.pdffox.adv.R.mipmap.ic_push_recoverd,
//				),
//			),
//		)
//	}
//
//}
