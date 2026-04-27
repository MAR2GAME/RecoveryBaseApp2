package com.pdffox.adv.use

import android.Manifest
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import cn.thinkingdata.analytics.TDAnalytics
import cn.thinkingdata.analytics.TDConfig
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.remoteConfig
import com.google.gson.Gson
import com.pdffox.adv.use.adv.AdChecker
import com.pdffox.adv.use.adv.AdvActivity
import com.pdffox.adv.use.adv.AdvCheckManager
import com.pdffox.adv.use.adv.AdvInit
import com.pdffox.adv.use.adv.ShowBannerAd
import com.pdffox.adv.use.adv.ShowInterAd
import com.pdffox.adv.use.adv.ShowOpenAd
import com.pdffox.adv.use.adv.policy.AdPolicyManager
import com.pdffox.adv.use.log.LogConfig
import com.pdffox.adv.use.log.LogParams
import com.pdffox.adv.use.log.LogUtil
import com.pdffox.adv.use.log.ThinkingAttr
import com.pdffox.adv.use.notification.NotificationManager
import com.pdffox.adv.use.remoteconfig.RemoteConfig
import com.pdffox.adv.use.remoteconfig.RemoteConfigManager
import com.pdffox.adv.use.remoteconfig.RemoteConfigRouting
import com.pdffox.adv.use.util.PreferenceUtil
import com.singular.sdk.Singular
import com.singular.sdk.SingularConfig
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

object Ads {
	private const val TAG = "Ads"
	@Volatile
	private var desiredTopic: String = Config.topic

	lateinit var application: AdvApplicaiton

	@RequiresPermission(allOf = [Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WAKE_LOCK])
	suspend fun init(context: AdvApplicaiton, isTest: Boolean) {
		application = context
		Config.isTest = isTest
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "init: appInstallTime ${getAppInstallTime(context)}")
		}
		PreferenceUtil.init(context)
		initConfig()
		initFireBase()
		RemoteConfigManager.initRemoteConfig()
		initSingular()
		initThinking()
		AdPolicyManager.loadPolicyFromLocal(context)
		AdvInit.initAdv(context)
//		withContext(Dispatchers.IO) {
//			AdvCheckManager.getAppConfig()
//		}
		AdChecker.startAutoCheck()
		NotificationManager.startObservers(context)
		if(!BuildConfig.DEBUG) {
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
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "initConfig: Config.country = ${Config.country}")
		}
	}

	fun showInterstitialAd(activity: AdvActivity, areaKey: String, onClosed: () -> Unit) {
		return ShowInterAd.showIntAd(activity, areaKey, onClosed)
	}

	@RequiresPermission(Manifest.permission.INTERNET)
	fun getBannerAd(context: Context, areaKey: String): ViewGroup? {
		return ShowBannerAd.getBannerAd(context, areaKey)
	}

	fun showOpenAd(activity: Activity, areaKey: String, onCloseListener: ShowOpenAd.OpenAdCloseListener?, onLoadedListener: ShowOpenAd.OpenAdLoadedListener?, onPaidListener: ShowOpenAd.OpenAdPaidListener?) {
		ShowOpenAd.showOpenAd(activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
	}

	@RequiresPermission(allOf = [Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WAKE_LOCK])
	private fun initFireBase() {
		val mFirebaseAnalytics = FirebaseAnalytics.getInstance(application)
		mFirebaseAnalytics.setAnalyticsCollectionEnabled(true)
		val defaultTopic = getDefaultTopic()
		RemoteConfig.topicSet.add(defaultTopic)
		FirebaseMessaging.getInstance().subscribeToTopic(defaultTopic)
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					if (desiredTopic == defaultTopic || desiredTopic.isBlank()) {
						rememberActiveTopic(defaultTopic)
					} else {
						FirebaseMessaging.getInstance().unsubscribeFromTopic(defaultTopic)
						if (BuildConfig.DEBUG) {
							Log.e(TAG, "initFireBase: topic[$defaultTopic] expired, unsubscribed")
						}
					}
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "initFireBase: subscribed topic[$defaultTopic] success")
					}
				} else {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "initFireBase: subscribed topic[$defaultTopic] failed")
					}
				}
			}
	}

	private fun getDefaultTopic(): String = if (BuildConfig.DEBUG) "debug-all" else "all"

	private fun rememberActiveTopic(topic: String) {
		Config.topic = topic
		RemoteConfig.topicSet.clear()
		RemoteConfig.topicSet.add(topic)
		if (BuildConfig.DEBUG) {
			PreferenceUtil.commitString("Config.topic", Config.topic)
		}
	}

	fun changeTopic(newTopic: String) {
		if (newTopic.isBlank()) {
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
						if (BuildConfig.DEBUG) {
							Log.e(TAG, "subscribed topic[$newTopic] success")
						}
					} else {
						FirebaseMessaging.getInstance().unsubscribeFromTopic(newTopic)
						if (BuildConfig.DEBUG) {
							Log.e(TAG, "subscribed topic[$newTopic] stale, unsubscribed")
						}
					}
				} else {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "subscribed topic[$newTopic] failed")
					}
				}
			}
	}

	private fun initThinking() {
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
		if (BuildConfig.DEBUG) {
			val superProperties = TDAnalytics.getSuperProperties()
			val presetProperties = TDAnalytics.getPresetProperties()
			Log.e(TAG, "initThinking: superProperties = $superProperties" )
			Log.e(TAG, "initThinking: presetProperties = ${Gson().toJson(presetProperties)}" )
		}
	}

	private fun initSingular() {
		Log.e(TAG, "initSingular: 开始初始化" )
		val config = SingularConfig(Config.Singular_Api_Key, Config.Singular_Secret)
			.withLoggingEnabled()
			.withLogLevel(1)
			.withSingularDeviceAttribution { attributionData ->
				// TODO: 只有首次安装APP时该方法会被回调
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "initSingular:  device attribution: $attributionData")
				}
				Config.singularHasResult = true
				val promoteParams = JSONObject()
				try {
					val network = attributionData["network"]?.toString().orEmpty()
					promoteParams.put("network", network)
					attributionData["campaign_name"]?.toString()?.let {
						if (it != "") {
							promoteParams.put("campaign_name", it)
						}
					}

					val isNatural = network.equals("organic", ignoreCase = true) || network.isEmpty()
					AdvCheckManager.params.fromNature = isNatural
					if (BuildConfig.DEBUG) {
						Config.isNature = false
					} else {
						Config.isNature = isNatural
					}
					promoteParams.put("fromNature", isNatural)
//					TDAnalytics.setSuperProperties(promoteParams)
					TDAnalytics.userSet(promoteParams)
					if (Config.remoteConfigHasResult) {
						val remoteConfig = Firebase.remoteConfig
						val adMapping = remoteConfig.getString("ad_mapping")
						RemoteConfigRouting.apply(
							remoteConfig = remoteConfig,
							adMapping = adMapping,
							source = "Ads.initSingular"
						)
					} else {
						if (BuildConfig.DEBUG) {
							Log.e(TAG, "initSingular: RemoteConfig还未更新先不处理" )
						}
					}
				} catch (e: JSONException) {
					// 处理异常
					Log.e(TAG, "onDeviceAttributionInfoReceived: ", e)
				}
			}
		Singular.init(application, config)
		if (BuildConfig.DEBUG) {
			val singularGlobalProperties = Singular.getGlobalProperties()
			Log.e(TAG, "initSingular: singularGlobalProperties = ${Gson().toJson(singularGlobalProperties)}" )
		}
	}

}
