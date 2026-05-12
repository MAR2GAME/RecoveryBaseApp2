package com.pdffox.adv

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.pdffox.adv.log.ThinkingAttr
import com.pdffox.adv.safe.SafeChecker
import com.pdffox.adv.update.AppUpdateHelper
import com.pdffox.adv.util.PreferenceUtil
import com.tiktok.TikTokBusinessSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.system.exitProcess

object AdvRuntime {
	private const val TAG = "AdvRuntime"

	lateinit var application: Application
		private set

	private var currentActivityRef: WeakReference<Activity>? = null

	val currentActivity: Activity?
		get() = currentActivityRef?.get()

	var startAppTime = 0L
		private set

	private var activityCount = 0
	private val detectionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
	private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
	private var registeredApplication: Application? = null
	private var facebookInitialized = false
	private var tiktokInitialized = false
	private var safetyCheckScheduled = false

	@Volatile
	private var isForceUpdateRetryPending = false

	val isInitialized: Boolean
		get() = ::application.isInitialized

	fun init(application: Application) {
		this.application = application
		Config.packageName = application.packageName
		if (registeredApplication !== application) {
			registeredApplication = application
			addListener(application)
		}
	}

	@SuppressLint("SuspiciousIndentation")
	private fun addListener(application: Application) {
		application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
			override fun onActivityResumed(activity: Activity) {
				Log.e(TAG, "onActivityResumed: ")
				setCurrentActivity(activity)
				val appRecognitionVerdict = PreferenceUtil.getBoolean("appRecognitionVerdict", true)
				if (!appRecognitionVerdict) {
					currentActivity?.finishAffinity()
					Process.killProcess(Process.myPid())
					exitProcess(0)
				}
			}

			override fun onActivityPaused(activity: Activity) {
				Log.e(TAG, "onActivityPaused: ")
				if (currentActivity == activity) {
					clearCurrentActivity()
				}
			}

			override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

			override fun onActivityStarted(activity: Activity) {
				Log.e(TAG, "onActivityStarted: ")
				setCurrentActivity(activity)
				val wasInBackground = activityCount == 0
				activityCount++
				if (activityCount == 1) {
					startAppTime = System.currentTimeMillis()
				}
				if (wasInBackground) {
					maybeForceUpdate()
				}
			}

			override fun onActivityStopped(activity: Activity) {
				activityCount = (activityCount - 1).coerceAtLeast(0)
				if (activityCount == 0 && Config.sdkConfig.thinking.enabled) {
					val hasNotificationPermission =
						NotificationManagerCompat.from(application).areNotificationsEnabled()
					ThinkingAttr.setUserSetAttr(
						ThinkingAttr.has_notification_permission,
						hasNotificationPermission,
					)
					val hasAllFilePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
						Environment.isExternalStorageManager()
					} else {
						val readPermission =
							application.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
						val writePermission =
							application.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
						readPermission && writePermission
					}
					ThinkingAttr.setUserSetAttr(ThinkingAttr.has_allfile_permission, hasAllFilePermission)
				}
			}

			override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

			override fun onActivityDestroyed(activity: Activity) {
				if (currentActivity == activity) {
					clearCurrentActivity()
				}
			}
		})
	}

	private fun setCurrentActivity(activity: Activity) {
		currentActivityRef = WeakReference(activity)
	}

	private fun clearCurrentActivity() {
		currentActivityRef = null
	}

	fun initConfiguredIntegrations() {
		initFacebook()
		initTiktok()
		scheduleSafetyCheck()
	}

	private fun initFacebook() {
		val facebookConfig = Config.sdkConfig.facebook
		if (!facebookConfig.enabled || facebookConfig.appId.isBlank() || facebookConfig.clientToken.isBlank()) {
			return
		}
		if (facebookInitialized) {
			return
		}
		facebookInitialized = true
		FacebookSdk.setApplicationId(facebookConfig.appId)
		FacebookSdk.setClientToken(facebookConfig.clientToken)
		FacebookSdk.fullyInitialize()
		AppEventsLogger.activateApp(application)
		FacebookSdk.setAdvertiserIDCollectionEnabled(facebookConfig.advertiserIdCollectionEnabled)
	}

	private fun initTiktok() {
		val tiktokConfig = Config.sdkConfig.tiktok
		if (!tiktokConfig.enabled || tiktokConfig.accessToken.isBlank() || tiktokConfig.ttAppId.isBlank()) {
			return
		}
		if (tiktokInitialized) {
			return
		}
		tiktokInitialized = true
		val ttConfig = TikTokBusinessSdk.TTConfig(application.applicationContext)
			.setAppId(tiktokConfig.appId ?: application.packageName)
			.setTTAppId(tiktokConfig.ttAppId)

		TikTokBusinessSdk.initializeSdk(ttConfig, object : TikTokBusinessSdk.TTInitCallback {
			override fun success() {
				Log.e("TikTokSDK", "Initialization successful")
			}

			override fun fail(code: Int, msg: String) {
				Log.e("TikTokSDK", "Initialization failed: $code, $msg")
			}
		})

		if (tiktokConfig.startTrackOnInit) {
			TikTokBusinessSdk.startTrack()
		}
	}

	private fun scheduleSafetyCheck() {
		if (!Config.sdkConfig.safe.enabled || safetyCheckScheduled) {
			return
		}
		safetyCheckScheduled = true
		detectionScope.launch {
			delay(1000)
			SafeChecker.checkAndShutDown(application)
		}
	}

	private fun maybeForceUpdate() {
		val targetVersion = Config.update_version
		if (targetVersion <= 0L) {
			return
		}
		val currentVersion = getCurrentVersionCode()
		if (currentVersion == 0L || currentVersion >= targetVersion) {
			return
		}
		if (com.pdffox.adv.Config.isTest) {
			Log.d(
				TAG,
				"maybeForceUpdate: currentVersion=$currentVersion, target=$targetVersion",
			)
		}
		val activity = currentActivity
		if (activity != null) {
			AppUpdateHelper.forceImmediateUpdate(activity)
		} else {
			scheduleForceUpdateRetry()
		}
	}

	private fun scheduleForceUpdateRetry() {
		if (isForceUpdateRetryPending) {
			return
		}
		isForceUpdateRetryPending = true
		mainHandler.post {
			isForceUpdateRetryPending = false
			val activity = currentActivity ?: return@post
			val targetVersion = Config.update_version
			val currentVersion = getCurrentVersionCode()
			if (targetVersion > 0 && currentVersion > 0 && currentVersion < targetVersion) {
				AppUpdateHelper.forceImmediateUpdate(activity)
			}
		}
	}

	private fun getCurrentVersionCode(): Long {
		return try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				application.packageManager.getPackageInfo(
					application.packageName,
					PackageManager.PackageInfoFlags.of(0),
				).longVersionCode
			} else {
				@Suppress("DEPRECATION")
				application.packageManager.getPackageInfo(application.packageName, 0).longVersionCode
			}
		} catch (throwable: Exception) {
			Log.e(TAG, "getCurrentVersionCode: failed to read package info", throwable)
			0L
		}
	}

	fun currentPackageName(): String {
		return if (isInitialized) application.packageName else Config.packageName
	}

	fun finishCurrentActivity() {
		currentActivity?.finishAffinity()
	}
}
