package com.pdffox.adv

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvertiseSdkConfigsTest {
	@Test
	fun `uses debug default topic when host topic is blank`() {
		val config = AdvertiseSdkConfigs.create("com.example", isDebug = true) {
			defaultTopic("")
		}

		assertEquals("com.example", config.packageName)
		assertEquals("debug-all", config.defaultTopic)
	}

	@Test
	fun `uses release default topic when host topic is blank`() {
		val config = AdvertiseSdkConfigs.create("com.example", isDebug = false) {
			defaultTopic("")
		}

		assertEquals("all", config.defaultTopic)
	}

	@Test
	fun `keeps host configured topic`() {
		val config = AdvertiseSdkConfigs.create("com.example", isDebug = true) {
			defaultTopic("custom-topic")
		}

		assertEquals("custom-topic", config.defaultTopic)
	}

	@Test
	fun `does not enforce safe signatures in debug by default`() {
		val config = AdvertiseSdkConfigs.create("com.example", isDebug = true) {
			safe(
				enabled = true,
				expectedSignatures = "aa:bb, cc:dd",
				rejectDebuggableBuilds = true,
				rejectDebuggerAttached = true,
				killProcessOnFailure = true,
			)
		}

		assertTrue(config.safe.enabled)
		assertTrue(config.safe.expectedSignatures.isEmpty())
		assertFalse(config.safe.rejectDebuggableBuilds)
		assertFalse(config.safe.rejectDebuggerAttached)
		assertFalse(config.safe.killProcessOnFailure)
	}

	@Test
	fun `parses safe signatures in release`() {
		val config = AdvertiseSdkConfigs.create("com.example", isDebug = false) {
			safe(
				enabled = true,
				expectedSignatures = "aa:bb, cc:dd\n ee:ff",
			)
		}

		assertEquals(setOf("aa:bb", "cc:dd", "ee:ff"), config.safe.expectedSignatures)
		assertTrue(config.safe.rejectDebuggableBuilds)
		assertTrue(config.safe.rejectDebuggerAttached)
		assertTrue(config.safe.killProcessOnFailure)
	}

	@Test
	fun `maps host feature configuration into sdk config`() {
		val config = AdvertiseSdkConfigs.create("com.example", isDebug = false) {
			legal(privacyUrl = "https://example.com/privacy", termsUrl = "https://example.com/terms")
			resources(pushConfigRawResId = 123)
			server(
				enabled = true,
				releaseHost = "https://api.example.com",
				testHost = "https://test.example.com",
				parseTokenKey = "token-key",
			)
			firebase(
				analyticsEnabled = true,
				messagingEnabled = true,
				subscribeDefaultTopic = true,
			)
			remoteConfig(enabled = true)
			thinking(enabled = true, appKey = "thinking-key", serverUrl = "https://thinking.example.com")
			singular(enabled = true, apiKey = "singular-key", secret = "singular-secret")
			adMob(
				enabled = true,
				appId = "app-id",
				bannerId = "banner-id",
				interstitialId = "interstitial-id",
				nativeId = "native-id",
				openId = "open-id",
			)
			facebook(enabled = true, appId = "facebook-id", clientToken = "facebook-token")
			tiktok(enabled = true, accessToken = "access-token", ttAppId = "tt-app-id", appId = "")
			push(
				enabled = true,
				persistentServiceEnabled = true,
				firebaseMessagingServiceEnabled = true,
				serviceStarterJobEnabled = true,
				bootReceiverEnabled = true,
				notificationDeletedReceiverEnabled = true,
				fileProviderEnabled = true,
				deletionObserverEnabled = true,
				sceneKeys = PushSceneKeyConfig(imageDeleted = "image-scene"),
			)
			notifications(NotificationFeatureConfig(enabled = true, smallIconResId = 456))
			playIntegrity(enabled = true, cloudProjectNumber = 789L)
		}

		assertEquals("https://example.com/privacy", config.privacyUrl)
		assertEquals("https://example.com/terms", config.termsUrl)
		assertEquals(123, config.resources.pushConfigRawResId)
		assertTrue(config.server.enabled)
		assertEquals("token-key", config.server.parseTokenKey)
		assertTrue(config.firebase.analyticsEnabled)
		assertTrue(config.firebase.messagingEnabled)
		assertTrue(config.firebase.subscribeDefaultTopic)
		assertTrue(config.remoteConfig.enabled)
		assertEquals("thinking-key", config.thinking.appKey)
		assertEquals("singular-key", config.singular.apiKey)
		assertEquals("app-id", config.adMob.appId)
		assertEquals("facebook-id", config.facebook.appId)
		assertEquals(null, config.tiktok.appId)
		assertTrue(config.push.enabled)
		assertEquals("image-scene", config.push.sceneKeys.imageDeleted)
		assertEquals(456, config.notifications.smallIconResId)
		assertEquals(789L, config.playIntegrity.cloudProjectNumber)
	}
}
