package com.pdffox.adv.use

import com.chartboost.sdk.impl.fa
import com.pdffox.adv.use.log.LogAdParam
import com.pdffox.adv.use.util.PreferenceDelegate

object Config {

	var isTest = false

	var update_version: Long = 0

	var openReview = false

	var country = "US"

	var topic = if (BuildConfig.DEBUG) "debug-all" else "all"

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
		this.showAdPlatform = showAdPlatform
	}

	var packageName = "com.datatool.photorecovery"

	const val PrivacyUrl = "https://sites.google.com/view/photo-recovery-privacy-policy/"
	const val TermsUrl = "https://sites.google.com/view/photo-recovery-terms-condition/"

	const val ThinkingKey = "1467641c0ffa4ef6a20f8a8b7e1d64a7"
	const val ThinkkingUrl = "https://mar2.top"

	const val Singular_Api_Key = "mar2game_f7b9272a"
	const val Singular_Secret = "72b3df2ee5d0a64a6c404ce01937c3d6"

	const val TestADVHost = "http://192.168.111.34:10002"
	const val ADVHost = "https://api.newminigame.online"

	val host: String
		get() = if (isTest) TestADVHost else ADVHost
//		get() =  ADVHost

	val CheckUrl: String
		get() = "$host/check"

	val AppConfigUrl: String
		get() = "$host/getAppConfig"
	val IPInfoUrl: String
		get() = "$host/getIpInfo"

	val IPInfoV2Url: String
		get() = "$host/getIpInfoV2"

	val PushUrl: String
		get() = "$host/publish"

	val ParseTokenUrl: String
		get() = "$host/parseToken"

}