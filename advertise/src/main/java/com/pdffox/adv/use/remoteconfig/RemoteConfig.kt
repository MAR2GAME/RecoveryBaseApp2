package com.pdffox.adv.use.remoteconfig

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.use.Config
import com.pdffox.adv.use.adv.AdvIDs
import com.pdffox.adv.use.log.ThinkingAttr
import com.pdffox.adv.use.adv.AdConfig
import com.pdffox.adv.use.adv.NativeConfig
import com.pdffox.adv.use.adv.policy.NativePolicyManager
import com.pdffox.adv.use.adv.policy.data.parseAdMapping
import com.pdffox.adv.use.notification.NotificationManager
import com.pdffox.adv.use.util.PreferenceUtil

object RemoteConfig {
	private const val TAG = "RemoteConfig"
	var ABTestName: String = ""

	var str_adLoad_config : String = ""
	var str_ad_policy : String = ""
	var str_notification_config : String = ""

	val topicSet = HashSet<String>()

	fun update(remoteConfig: FirebaseRemoteConfig) {

		if (BuildConfig.DEBUG) {
			Log.e(TAG, "update: RemoteConfig singularHasResult = ${Config.singularHasResult}")
			Log.e(TAG, "update: RemoteConfig isNature = ${Config.isNature}")
		}

		ABTestName = remoteConfig.getString("ABTestName")
		ThinkingAttr.setUserAttr(ThinkingAttr.ab_test, ABTestName)

		Config.update_version = remoteConfig.getLong("update_version")


		Config.remoteConfigHasResult = true

		// 广告设置
		val openAdmobMediation = remoteConfig.getBoolean("OpenAdmobMediation")
		val showAdPlatform = remoteConfig.getString("ShowAdPlatform")
		Config.setConfig(openAdmobMediation, showAdPlatform)

		// admob相关配置
		val admobBanner = remoteConfig.getString("Admob_Banner")
		val admobInterset = remoteConfig.getString("Admob_Interset")
		val admobNative = remoteConfig.getString("Admob_Native")
		val admobOpen = remoteConfig.getString("Admob_Open")
		AdvIDs.setAdmobIDs(admobBanner, admobInterset, admobNative, admobOpen)
		// max相关配置
		val maxInterset = remoteConfig.getString("Max_Interset")
		val maxBanner = remoteConfig.getString("Max_Banner")
		val maxOpen = remoteConfig.getString("Max_Open")
		AdvIDs.setMaxIDs(maxInterset, maxBanner, maxOpen)

		var config= remoteConfig.getString("Contextualized_Push")
		PreferenceUtil.commitString("Contextualized_Push", config)

		Config.openReview = remoteConfig.getBoolean("openReview")
		PreferenceUtil.commitBoolean("openReview", Config.openReview)
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "update: openReview = ${Config.openReview}")
		}

		val adMapping = remoteConfig.getString("ad_mapping")
		PreferenceUtil.commitString("ad_mapping", adMapping)
		if (adMapping != "") {
			val adMappingObj = parseAdMapping(adMapping)
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "update: adMappingObj = $adMappingObj")
			}
		}
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "update: ad_mapping=$adMapping")
		}

		val adload_config = remoteConfig.getString("adload_config")
		str_adLoad_config = adload_config

		val ad_policy = remoteConfig.getString("ad_policy")
		str_ad_policy = ad_policy
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "update: ad_policy=$ad_policy")
		}

		AdConfig.isNewAdPolicy = remoteConfig.getBoolean("isNewAdPolicy")
		if(BuildConfig.DEBUG) {
			Log.e(TAG, "update: isNewAdPolicy = ${AdConfig.isNewAdPolicy}")
		}

		AdConfig.isNewPush = remoteConfig.getBoolean("isNewPush")
		if(BuildConfig.DEBUG) {
			Log.e(TAG, "update: isNewPush = ${AdConfig.isNewPush}")
		}

		AdConfig.ignoreGuide = remoteConfig.getBoolean("ignoreGuide")
		if(BuildConfig.DEBUG) {
			Log.e(TAG, "update: ignoreGuide = ${AdConfig.ignoreGuide}")
		}
		AdConfig.hasOpenLaungPage = remoteConfig.getBoolean("hasOpenLaungPage")
		if(BuildConfig.DEBUG) {
			Log.e(TAG, "update: hasOpenLaungPage = ${AdConfig.hasOpenLaungPage}")
		}

		val notification_config = remoteConfig.getString("notification_config")
		str_notification_config = notification_config
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "update: notification_config = $notification_config")
		}

		val notification_content = remoteConfig.getString("notification_content")
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "update: notification_content = $notification_content")
		}
		NotificationManager.updateNotificationContent(notification_content)

		val allowTargetedSelection = Config.singularHasResult
		if (BuildConfig.DEBUG && !allowTargetedSelection) {
			Log.e(TAG, "update: Singular result unavailable, keep default routing")
		}
		RemoteConfigRouting.apply(
			remoteConfig = remoteConfig,
			adMapping = adMapping,
			source = "RemoteConfig.update",
			allowTargetedSelection = allowTargetedSelection
		)

		NativeConfig.show_home_banner = remoteConfig.getBoolean("show_home_banner")
		if(BuildConfig.DEBUG) {
			Log.e(TAG, "update: show_home_banner = ${NativeConfig.show_home_banner}")
		}
		NativeConfig.show_home_native = remoteConfig.getBoolean("show_home_native")
		if(BuildConfig.DEBUG) {
			Log.e(TAG, "update: show_home_native = ${NativeConfig.show_home_native}")
		}

		NativeConfig.guide_page_swap_time = remoteConfig.getLong("guide_page_swap_time")
		if(BuildConfig.DEBUG) {
			Log.e(TAG, "update: guide_page_swap_time = ${NativeConfig.guide_page_swap_time}")
		}

		val native_refresh_time = remoteConfig.getLong("native_refresh_time")
		if (native_refresh_time > 0) {
			NativeConfig.native_refresh_time = native_refresh_time * 1000
		}
		if(BuildConfig.DEBUG) {
			Log.e(TAG, "update: native_refresh_time = ${NativeConfig.native_refresh_time}")
		}

		val str_native_ad_ids = remoteConfig.getString("native_ad_ids")
		if(BuildConfig.DEBUG) {
			Log.e(TAG, "update: str_native_ad_ids = $str_native_ad_ids")
		}
		AdvIDs.setNativeIDs(str_native_ad_ids)

		val str_native_ad_policy = remoteConfig.getString("native_ad_policy")
		if(BuildConfig.DEBUG) {
			Log.e(TAG, "update: str_native_ad_policy = $str_native_ad_policy")
		}
		NativePolicyManager.setPolicyFromJson(str_native_ad_policy)

		Config.log_time = remoteConfig.getLong("log_time")
		if (Config.log_time == 0L) {
			Config.log_time = 48
		}
		if(BuildConfig.DEBUG) {
			Log.e(TAG, "update: log_time = ${Config.log_time}")
		}
	}

}
