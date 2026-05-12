package com.pdffox.adv

import com.pdffox.adv.log.LogAdParam
import com.pdffox.adv.util.PreferenceDelegate

object Config {

	var sdkConfig: AdvertiseSdkConfig = AdvertiseSdkConfig()
		private set

	var isTest = false

	var update_version: Long = 0

	var openReview = false

	var country = "US"

	var topic = defaultTopic()

	var singularHasResult : Boolean by PreferenceDelegate("singularHasResult", false)

	var isNature: Boolean by PreferenceDelegate("isNature", false)

	var remoteConfigHasResult = false

	var paid0HasResult = false
	var paid_0 = true

	var ipCheckHasResult : Boolean by PreferenceDelegate("ipCheckHasResult", false)
	var isGoogleIP: Boolean by PreferenceDelegate("isGoogleIP", false)

	var openAdmobMediation = true
	var showAdPlatform = LogAdParam.ad_platform_admob

	var log_time: Long = 48


	fun setConfig(openAdmobMediation: Boolean, showAdPlatform: String) {
		this.openAdmobMediation = openAdmobMediation
		val requestedPlatform = when (showAdPlatform) {
			LogAdParam.ad_platform_admob -> showAdPlatform
			else -> LogAdParam.ad_platform_admob
		}
		this.showAdPlatform = normalizeAdPlatform(requestedPlatform) ?: requestedPlatform
	}

	var packageName = sdkConfig.packageName.orEmpty()

	val resourceConfig: AdvertiseResourcesConfig
		get() = sdkConfig.resources

	fun applySdkConfig(config: AdvertiseSdkConfig, fallbackPackageName: String? = null) {
		sdkConfig = config
		packageName = config.packageName ?: fallbackPackageName ?: packageName
		val requestedDefaultTopic = config.defaultTopic?.takeIf { it.isNotBlank() }
		if (requestedDefaultTopic != null) {
			topic = requestedDefaultTopic
		} else if (topic.isBlank() || topic == "all" || topic == "debug-all") {
			topic = defaultTopic()
		}
		showAdPlatform = normalizeAdPlatform(showAdPlatform) ?: showAdPlatform
	}

	fun hasEnabledAdNetwork(): Boolean {
		return sdkConfig.adMob.enabled
	}

	fun activeAdPlatform(): String? {
		return normalizeAdPlatform(showAdPlatform)
	}

	private fun normalizeAdPlatform(requestedPlatform: String): String? {
		return when {
			requestedPlatform == LogAdParam.ad_platform_admob && sdkConfig.adMob.enabled -> LogAdParam.ad_platform_admob
			sdkConfig.adMob.enabled -> LogAdParam.ad_platform_admob
			else -> null
		}
	}

	private fun defaultTopic(): String {
		return if (isTest) "debug-all" else "all"
	}

	val PrivacyUrl: String
		get() = sdkConfig.privacyUrl
	val TermsUrl: String
		get() = sdkConfig.termsUrl

	val ThinkingKey: String
		get() = sdkConfig.thinking.appKey
	val ThinkkingUrl: String
		get() = sdkConfig.thinking.serverUrl

	val Singular_Api_Key: String
		get() = sdkConfig.singular.apiKey
	val Singular_Secret: String
		get() = sdkConfig.singular.secret

	val TestADVHost: String
		get() = sdkConfig.server.testHost
	val ADVHost: String
		get() = sdkConfig.server.releaseHost

	val host: String
		get() = if (isTest && TestADVHost.isNotBlank()) TestADVHost else ADVHost
//		get() =  ADVHost

	val isServerEnabled: Boolean
		get() = sdkConfig.server.enabled && host.isNotBlank()

	val CheckUrl: String
		get() = endpoint(sdkConfig.server.checkPath)

	val AppConfigUrl: String
		get() = endpoint(sdkConfig.server.appConfigPath)
	val IPInfoUrl: String
		get() = endpoint(sdkConfig.server.ipInfoPath)

	val IPInfoV2Url: String
		get() = endpoint(sdkConfig.server.ipInfoV2Path)

	val PushUrl: String
		get() = endpoint(sdkConfig.server.pushPath)

	val ParseTokenUrl: String
		get() = endpoint(sdkConfig.server.parseTokenPath)

	private fun endpoint(path: String): String {
		if (!isServerEnabled) {
			return ""
		}
		val normalizedPath = if (path.startsWith("/")) path else "/$path"
		return host.trimEnd('/') + normalizedPath
	}

}
