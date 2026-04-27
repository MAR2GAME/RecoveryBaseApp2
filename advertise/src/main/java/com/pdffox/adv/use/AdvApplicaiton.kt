package com.pdffox.adv.use

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
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.mar2game.safe.SafeChecker
import com.pdffox.adv.use.log.ThinkingAttr
import com.pdffox.adv.use.util.PreferenceUtil
import android.os.Process
import com.pdffox.adv.use.adv.AdvCheckManager
import com.pdffox.adv.use.update.AppUpdateHelper
import com.tiktok.TikTokBusinessSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

open class AdvApplicaiton: Application() {

	companion object{
		private const val TAG = "AdvApplicaiton"
		lateinit var instance: AdvApplicaiton
			private set
	}

	var currentActivity: Activity? = null
	private var activityCount = 0
	private val detectionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
	private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
	@Volatile
	private var isForceUpdateRetryPending = false

	var startAppTime = 0L
    override fun onCreate() {
        super.onCreate()
	    instance = this
	    Config.packageName = packageName
	    addListener()
	    FacebookSdk.sdkInitialize(applicationContext)
	    AppEventsLogger.activateApp(this)
	    FacebookSdk.setAdvertiserIDCollectionEnabled(true)
	    initTiktok()
//	    scheduleSafetyCheck()
	    CoroutineScope(Dispatchers.IO).launch {
		    val result = AdvCheckManager.getIpInfoV2()
		    Log.e(TAG, "onCreate: getIpInfoV2 = $result", )
		}
    }

	fun addListener() {
		registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
			@SuppressLint("SuspiciousIndentation")
			override fun onActivityResumed(activity: Activity) {
				Log.e(TAG, "onActivityResumed: " )
				currentActivity = activity
				val appRecognitionVerdict = PreferenceUtil.getBoolean("appRecognitionVerdict", true)
		        if (!appRecognitionVerdict) {
		            currentActivity?.finishAffinity()
		            Process.killProcess(Process.myPid())
		            exitProcess(0)
		        }
			}

			override fun onActivityPaused(activity: Activity) {
				Log.e(TAG, "onActivityPaused: " )
				if (currentActivity == activity) {
					currentActivity = null
				}
			}

			override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
			override fun onActivityStarted(activity: Activity) {
				Log.e(TAG, "onActivityStarted: " )
				currentActivity = activity
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
				activityCount--
				if (activityCount == 0) {
//					if (!AdConfig.isNewPush){
//						PushManager.notifyServerAppExit()
//					}
					val dur: Long = System.currentTimeMillis() - startAppTime
					val hasNotificationPermission = NotificationManagerCompat.from(this@AdvApplicaiton).areNotificationsEnabled()
					ThinkingAttr.setUserSetAttr(ThinkingAttr.has_notification_permission, hasNotificationPermission)
					val hasFAllFillPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
						Environment.isExternalStorageManager()
					} else {
						val readPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
						val writePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
						readPermission && writePermission
					}
					ThinkingAttr.setUserSetAttr(ThinkingAttr.has_allfile_permission, hasFAllFillPermission)

				}
			}
			override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
			override fun onActivityDestroyed(activity: Activity) {}
		})
	}

	fun initTiktok() {
		val ttConfig = TikTokBusinessSdk.TTConfig(applicationContext, "TTpjhQJCkNhW2m9kobQVUiIOUciGopjh")
		.setAppId(packageName) // Android package name，例如：com.pdffox.adv.use
		.setTTAppId("7624342473057271816") // 从 TikTok 广告后台获取的 App ID

		// 使用带回调的初始化方式
		TikTokBusinessSdk.initializeSdk(ttConfig, object : TikTokBusinessSdk.TTInitCallback {
			override fun success() {
				// 初始化成功
				Log.e("TikTokSDK", "Initialization successful")
			}
			override fun fail(code: Int, msg: String) {
				// 初始化失败
				Log.e("TikTokSDK", "Initialization failed: $code, $msg")
			}
		})

		// 开始追踪事件
		// 如果需要用户同意隐私协议后再发送数据，可以将此行代码移至用户点击同意的位置
		TikTokBusinessSdk.startTrack()
	}

	private fun scheduleSafetyCheck() {
		detectionScope.launch {
			delay(1000)
			SafeChecker.checkAndShutDown(this@AdvApplicaiton)
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
		if (BuildConfig.DEBUG) {
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
				packageManager.getPackageInfo(
					packageName,
					PackageManager.PackageInfoFlags.of(0),
				).longVersionCode
			} else {
				@Suppress("DEPRECATION")
				packageManager.getPackageInfo(packageName, 0).longVersionCode
			}
		} catch (throwable: Exception) {
			Log.e(TAG, "getCurrentVersionCode: failed to read package info", throwable)
			0L
		}
	}
}
