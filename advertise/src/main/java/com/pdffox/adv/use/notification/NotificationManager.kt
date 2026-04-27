package com.pdffox.adv.use.notification
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Notification
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.use.Config
import com.pdffox.adv.R
import com.pdffox.adv.use.adv.AdvCheckManager
import com.pdffox.adv.use.log.LogUtil
import com.pdffox.adv.use.util.PreferenceUtil
import kotlinx.serialization.json.Json
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

@SuppressLint("StaticFieldLeak")
object NotificationManager {

	private const val TAG = "NotificationManager"

	var notificationConfig: NotificationConfig? = null
	var notificationContents: List<Notice>? = null
	private var appContext: Context? = null
	private var context: Context? = null
	private var screenUnlockReceiver: BroadcastReceiver? = null
	private var powerReceiver: BroadcastReceiver? = null
	private var packageReceiver: BroadcastReceiver? = null
	private var screenOnReceiver: BroadcastReceiver? = null
	private var activityLifecycleCallbacks: Application.ActivityLifecycleCallbacks? = null
	private var activityReferences = 0
	private var isActivityChangingConfigurations = false

	private var isAppForeground = false

	private var firstOpenTime = 0L

	fun updateNotificationConfig(strConfig: String) {
		if (strConfig.isEmpty()) return
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "updateNotificationConfig: $strConfig" )
			PreferenceUtil.commitString("updateNotificationConfig", strConfig)
		}
		PreferenceUtil.commitString("notification_config", strConfig)
		val config = Json.decodeFromString<NotificationConfig>(strConfig)
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "setNotificationConfig: $strConfig" )
		}
		notificationConfig = config
		context?.let {
			sendTimerNotification(it)
		}
	}

	fun updateNotificationContent(strContent: String) {
		if (strContent.isEmpty()) return
		PreferenceUtil.commitString("notification_content", strContent)
		val notices: List<Notice> = Json.decodeFromString(strContent)
		//val languagesObj = Json.decodeFromString<Languages>(notices[0].Languages)
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "updateNotificationContent: $notices" )
		}
		notificationContents = notices
	}

	fun startObservers(context: Context) {

		this.context = context
		appContext = context.applicationContext
		firstOpenTime = AdvCheckManager.params.installTime
		if (notificationConfig == null) {
			val strConfig = PreferenceUtil.getString("notification_config", "")
			if (strConfig != null && strConfig.isNotEmpty()) {
				notificationConfig = Json.decodeFromString<NotificationConfig>(strConfig)
			}
		}
		if (notificationContents == null) {
			val strContent = PreferenceUtil.getString("notification_content", "")
			if (strContent != null && strContent.isNotEmpty()) {
				notificationContents = Json.decodeFromString<List<Notice>>(strContent)
			}
		}

		createNotificationChannel(context)
//		Thread {
//			var num = 0;
//			while (true) {
//				Thread.sleep(1000)
//				num++
//				sendNotificationDetail("test$num", "test$num")
//			}
//		}.start()

		// 监听APP切换到后台
		if (appContext is Application) {
			activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
				override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
				override fun onActivityStarted(activity: Activity) {
					if (++activityReferences == 1 && !isActivityChangingConfigurations) {
						// 应用切换到前台
						isAppForeground = true
					}
				}
				override fun onActivityResumed(activity: Activity) {}
				override fun onActivityPaused(activity: Activity) {}
				override fun onActivityStopped(activity: Activity) {
					isActivityChangingConfigurations = activity.isChangingConfigurations
					if (--activityReferences == 0 && !isActivityChangingConfigurations) {
						// 应用切换到后台
						isAppForeground = false
						sendNotification(context,"press_key_home")
					}
				}
				override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
				override fun onActivityDestroyed(activity: Activity) {}
			}
			(appContext as Application).registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
		}

		// 注册屏幕解锁广播接收器
		val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
		screenUnlockReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) {
				if (intent?.action == Intent.ACTION_USER_PRESENT) {
					context?.let {
						sendNotification(context,"screen_unlock")
					}
				}
			}
		}
		appContext?.registerReceiver(screenUnlockReceiver, filter)

		// 监控充电和充电结束
		val powerFilter = IntentFilter().apply {
			addAction(Intent.ACTION_POWER_CONNECTED)
			addAction(Intent.ACTION_POWER_DISCONNECTED)
		}
		powerReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) {
				when (intent?.action) {
					Intent.ACTION_POWER_CONNECTED -> {
						context?.let {
							sendNotification(context,"battery_change")
						}
					}
					Intent.ACTION_POWER_DISCONNECTED -> {
						context?.let {
							sendNotification(context,"battery_change")
						}
					}
				}
			}
		}
		appContext?.registerReceiver(powerReceiver, powerFilter)

		// 监听APP的安装和卸载
		val packageFilter = IntentFilter().apply {
			addAction(Intent.ACTION_PACKAGE_ADDED)
			addAction(Intent.ACTION_PACKAGE_REMOVED)
			addDataScheme("package")
		}
		packageReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) {
				when (intent?.action) {
					Intent.ACTION_PACKAGE_ADDED -> {
						context?.let {
							sendNotification(context,"install_app")
						}
					}
					Intent.ACTION_PACKAGE_REMOVED -> {
						context?.let {
							sendNotification(context,"uninstall_app")
						}
					}
				}
			}
		}
		appContext?.registerReceiver(packageReceiver, packageFilter)

		// 监控屏幕点亮
		val screenOnFilter = IntentFilter(Intent.ACTION_SCREEN_ON)
		screenOnReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) {
				if (intent?.action == Intent.ACTION_SCREEN_ON) {
					sendNotificationList("screen_on")
				}
			}
		}
		appContext?.registerReceiver(screenOnReceiver, screenOnFilter)

		sendTimerNotification(context)
	}

	fun stopObservers() {
		screenUnlockReceiver?.let {
			appContext?.unregisterReceiver(it)
		}
		screenUnlockReceiver = null

		powerReceiver?.let {
			appContext?.unregisterReceiver(it)
		}
		powerReceiver = null

		screenOnReceiver?.let {
			appContext?.unregisterReceiver(it)
		}
		screenOnReceiver = null

		packageReceiver?.let {
			appContext?.unregisterReceiver(it)
		}
		packageReceiver = null

		if (appContext is Application && activityLifecycleCallbacks != null) {
			(appContext as Application).unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
			activityLifecycleCallbacks = null
		}
		appContext = null

	}

	// TODO: 定时推送
	// 比如你想每天 9:30、12:45、18:00 发送推送
	fun sendTimerNotification(context: Context) {
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "sendTimerNotification: timerConfig = ${notificationConfig?.timer}" )
		}
		if (notificationConfig?.timer == null) return
		NotificationScheduler.cancelDailyNotification(context)
		for (timer in notificationConfig?.timer ?: emptyList()) {
			NotificationScheduler.scheduleDailyNotification(context, "daily_notification", timer.HH, timer.MM)
		}
	}

	fun sendNotification(context: Context, notificationType : String) {
		if (notificationConfig == null) return
		if (isAppForeground && notificationConfig?.is_foreground_send != true) return
		// todo: 判断是否有通知栏权限
//		if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
//			if (BuildConfig.DEBUG) {
//				Log.e(TAG, "sendNotification: no notification permission")
//			}
//			return@sendNotification
//		}
		val triggerConfig = notificationConfig?.triggers?.firstOrNull {
			it.name == notificationType
		}
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "sendNotification: $triggerConfig" )
		}
		triggerConfig?.let {
			if (it.offset_second == null || System.currentTimeMillis() - firstOpenTime < (it.offset_second * 1000)) return
			if (NotificationRecordManager.get24HRecordsCount() >= notificationConfig!!.`24HMax`) return
			val lastRecord = NotificationRecordManager.getLastRecord(notificationType)
			if (it.interval_second != null && lastRecord != null && System.currentTimeMillis() - lastRecord.timestamp < (it.interval_second * 1000)) return
			val logParams = mutableMapOf<String, Any>()
			logParams["scene"] = notificationType
			if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
				LogUtil.log("notification_app_shown",logParams)
			}
			repeat(notificationConfig?.each_trigger_sent ?: 1) { index ->
				Handler(Looper.getMainLooper()).postDelayed({
					sendNotificationDetail(notificationType, notificationType)
				}, index * 3000L) // 每次延迟3秒，index从0开始
			}
		}
	}

	fun sendNotificationList(notificationType : String) {
//		if (!AdConfig.isNewPush) return
		if (notificationConfig == null) return
		if (isAppForeground && notificationConfig?.is_foreground_send != true) return
		val triggerConfig = notificationConfig?.triggers?.firstOrNull {
			it.name == notificationType
		}
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "sendNotificationList: $triggerConfig" )
		}
		triggerConfig?.let {
			it.configs?.forEach { config ->
				// 延迟delay秒
				val delayMillis = (config.delay ?: 0) * 1000L
				Handler(Looper.getMainLooper()).postDelayed({
					if (config.offset_second == null || System.currentTimeMillis() - firstOpenTime < (config.offset_second * 1000)) return@postDelayed
					if (NotificationRecordManager.get24HRecordsCount() >= notificationConfig!!.`24HMax`) return@postDelayed
					val lastRecord = NotificationRecordManager.getLastRecord(config.name)
					if (config.interval_second != null && lastRecord != null && System.currentTimeMillis() - lastRecord.timestamp < (config.interval_second * 1000)) return@postDelayed
					val logParams = mutableMapOf<String, Any>()
					logParams["scene"] = notificationType
					context?.let {
						if (NotificationManagerCompat.from(it).areNotificationsEnabled()) {
							LogUtil.log("notification_app_shown",logParams)
						}
					}
					repeat(notificationConfig?.each_trigger_sent ?: 1) { index ->
						Handler(Looper.getMainLooper()).postDelayed({
							sendNotificationDetail(notificationType, config.name)
						}, index * 3000L) // 每次延迟3秒，index从0开始
					}
				}, delayMillis)
			}
		}
	}

	fun createNotificationChannel(context: Context) {
		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//		val notificationManager = context.getSystemService(NotificationManager::class.java)

		val mGroup = NotificationChannelGroup(
			"GROUP_ID",
			"GROUP_NAME"
		)
		notificationManager.createNotificationChannelGroup(mGroup)

		val channelId = "default_channel_id"
		val channelName = "channelName"
		val channelDescription = "channelDescription"
		val channel = android.app.NotificationChannel(
			channelId,
			channelName,
			NotificationManager.IMPORTANCE_HIGH
		).apply {
			description = channelDescription
			group = "GROUP_ID"
			setShowBadge(true)
			enableVibration(true)
			vibrationPattern = longArrayOf(0, 100, 200, 300)
			lockscreenVisibility = Notification.VISIBILITY_PUBLIC
		}
		notificationManager.createNotificationChannel(channel)
	}

	val idQueue: ArrayDeque<Int> = ArrayDeque()
	fun sendNotificationDetail(notificationType : String, configName : String) {
		// todo: 判断是否有通知栏权限
//		context?.let {
//			if (!NotificationManagerCompat.from(it).areNotificationsEnabled()) {
//				if (BuildConfig.DEBUG) {
//					Log.e(TAG, "sendNotification: no notification permission")
//				}
//				return@sendNotificationDetail
//			}
//		}
		Handler(Looper.getMainLooper()).post {
			val max = if (notificationConfig != null) {
				notificationConfig!!.NMax
			} else 3
			if (idQueue.size >= max) {
				idQueue.removeFirst()
			}
			val id = (1..max).firstOrNull { !idQueue.contains(it) } ?: idQueue.removeFirst()

			val notice = notificationContents?.randomOrNull()
			context?.let { context ->
				notice?.let { notice ->
					var title = notice.Title ?: "Title"
					var content = notice.Content ?: "Content"
					var button = notice.Button ?: "Button"
					var img = notice.Img ?: "Img"
					var route = notice.Route ?: "Route"

					val currentLanguage = Locale.getDefault().language
					val languagesObj = Json.decodeFromString<Languages>(notice.Languages)
					val language = languagesObj.keys.firstOrNull {
						it.language == currentLanguage
					}
					if (language != null) {
						title = language.title
						content = language.content
						img = language.img
						button = language.button
					}

					val icon = getIcon(route)
					route = getRoute(route)

					if (BuildConfig.DEBUG) {
						Log.e(TAG, "sendNotificationDetail: $notificationType $configName $title $content $button $img $route" )
					}

					sendTemporaryNotification(
						context,
						id,
						notificationType,
						icon,
						title,
						content,
						img,
						button,
						route
					)
				}
			}

			idQueue.add(id)
			NotificationRecordManager.addRecord(NotificationRecord(configName, System.currentTimeMillis()))
		}
	}

	@SuppressLint("RemoteViewLayout")
	fun sendTemporaryNotification(
		context: Context,
		id: Int,
		scene: String = "",
		icon:Int,
		title: String,
		message: String,
		img: String,
		button: String,
		route: String
	) {
		try {
			// 构建小视图
        val remoteViews = RemoteViews(Config.packageName, R.layout.a_temp_notification)
			remoteViews.setImageViewResource(R.id.iv_push,icon)
			remoteViews.setTextViewText(R.id.tv_detail, title)
			remoteViews.setTextViewText(R.id.button, button)
			// 构建大视图
        val bigRemoteViews = RemoteViews(Config.packageName, R.layout.a_temp_notification_big)
			bigRemoteViews.setImageViewResource(R.id.iv_push,icon)
			bigRemoteViews.setTextViewText(R.id.tv_title, title)
			bigRemoteViews.setTextViewText(R.id.tv_message, message)
			bigRemoteViews.setTextViewText(R.id.button, button)
			val notification =
				NotificationCompat.Builder(appContext!!, "default_channel_id")
					.setContentTitle(title)
                .setSmallIcon(R.drawable.a_nlogo)
					.setColor(context.getColor(R.color.color_purple))
					.setCustomContentView(remoteViews)
					.setCustomHeadsUpContentView(remoteViews)  // 浮动通知布局
					.setCustomBigContentView(bigRemoteViews)
					.setContentIntent(
						getAppPendingIntent(context,route,scene)
					)
					.setGroup("GROUP_ID")
					.setAutoCancel(true)
					.setPriority(NotificationCompat.PRIORITY_HIGH)
					.setCategory(NotificationCompat.CATEGORY_MESSAGE)
					.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
					.setOngoing(false)
					.setOnlyAlertOnce(false)
					.setShowWhen(true)
					.setWhen(System.currentTimeMillis())
					.build()
//			val notificationManager = context.getSystemService(NotificationManager::class.java)
			val notificationManager = appContext?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
			notificationManager?.notify(id, notification)
			//desc，要除以倍数，暂时移到别的地方
//			val logParams = mutableMapOf<String, Any>()
//			logParams["scene"] = route
//			if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
//				LogUtil.log("notification_app_shown",logParams)
//			}
		} catch (e: Exception) {
			Log.e(TAG, "sendTemporaryNotification: ", e)
		}
	}

	fun getRoute(route: String): String {
		return when (route) {
			"/recoverPhotos" -> "Photos"
			"/recoverVideos" -> "Videos"
			"/recoverFiles" -> "Files"
			"/recovered" -> "Recovered"
			else -> ""
		}
	}

	fun getIcon(route: String): Int {
		return when (route) {
            "/recoverPhotos" -> R.drawable.a_notification_photos
            "/recoverVideos" -> R.drawable.a_notification_videos
            "/recoverFiles" -> R.drawable.a_notification_files
            "/recovered" -> R.drawable.a_notification_recovered
            else -> R.drawable.a_notification_photos
		}
	}

	private val requestCodeGenerator = AtomicInteger(0)
	fun getAppPendingIntent(context: Context ,route: String = "", scene: String = ""): PendingIntent {
		val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
		launchIntent?.apply {
//			addCategory(Intent.CATEGORY_DEFAULT)
			putExtra("AppOpenFrom", "app_push")
			putExtra("Route", route)
			putExtra("Scene", scene)
			flags = Intent.FLAG_ACTIVITY_NEW_TASK
		}
		val requestCode = requestCodeGenerator.incrementAndGet()
		Log.e(TAG, "getAppPendingIntent: $route $requestCode")
		val pendingIntent = PendingIntent.getActivity(
			context,
			requestCode,
			launchIntent,
			PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)
		return pendingIntent
	}

}
