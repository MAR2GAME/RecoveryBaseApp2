package com.pdffox.adv.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.provider.MediaStore
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.pdffox.adv.Ads
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.Config
import com.pdffox.adv.NotificationActionConfig
import com.pdffox.adv.R
import com.pdffox.adv.adv.AdConfig
import com.pdffox.adv.adv.AdLoader
import com.pdffox.adv.adv.AdvCheckManager
import com.pdffox.adv.log.LogUtil
import com.pdffox.adv.push.PushIntegration
import com.pdffox.adv.util.PreferenceDelegate
import com.pdffox.adv.util.PreferenceUtil
import java.util.concurrent.atomic.AtomicInteger

class CommonService : Service() {

	companion object {
		private const val NOTIFICATION_ID = 2001
		private const val CHANNEL_ID = "channel_id_common_notify"
		private const val TAG = "CommonService"
		private const val MAX_PERSISTENT_ACTIONS = 4

		var pushConfig: PushConfig? = null

		private val requestCodeGenerator = AtomicInteger(0)

		fun intent(context: Context): Intent {
			return Intent(context, CommonService::class.java).apply {
				setPackage(context.packageName)
			}
		}

		fun start(context: Context) {
			if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.persistentServiceEnabled) {
				return
			}
			runCatching {
				ContextCompat.startForegroundService(context, intent(context))
			}.onFailure {
				Log.e(TAG, "start: failed to launch foreground service", it)
			}
		}
	}

    private lateinit var notificationManager: NotificationManager

	lateinit var wakeLock: PowerManager.WakeLock
	private fun acquireWakeLock() {
		val powerManager = getSystemService(POWER_SERVICE) as PowerManager
		wakeLock = powerManager.newWakeLock(
			PowerManager.PARTIAL_WAKE_LOCK,
			"KeepAliveService::WakeLock"
		)
		wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
	}

	private fun releaseWakeLock() {
		if (::wakeLock.isInitialized && wakeLock.isHeld) {
			wakeLock.release()
		}
	}

	override fun onCreate() {
		super.onCreate()
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.persistentServiceEnabled) {
			stopSelf()
			return
		}
		initPushConfig()
		notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		registerContentObservers()
		acquireWakeLock()
		NotificationChannelManager.createNormalChannel(this)
        // 注册所有广播接收器
        registerAllReceivers()

		try {
			val notification = createNotificationChannel()
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
				startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
			} else {
				startForeground(NOTIFICATION_ID, notification)
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun initPushConfig(){
		if (pushConfig == null) {
			val config = PreferenceUtil.getString("Contextualized_Push", "")
			pushConfig = Gson().fromJson(config, PushConfig::class.java)
		}
		if(pushConfig ==null){
			pushConfig =parsePushConfigFromRaw()
		}
	}
	fun parsePushConfigFromRaw(): PushConfig? {
		if (Config.resourceConfig.pushConfigRawResId == 0) {
			return null
		}
		return try {
			val inputStream = this.resources.openRawResource(Config.resourceConfig.pushConfigRawResId)
			val jsonString = inputStream.bufferedReader().use { it.readText() }
			val gson = Gson()
			gson.fromJson(jsonString, PushConfig::class.java)
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.persistentServiceEnabled) {
			stopSelf()
			return START_NOT_STICKY
		}
		try {
			val notification = createNotificationChannel()
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
				startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
			} else {
				startForeground(NOTIFICATION_ID, notification)
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
		return START_STICKY
	}

	override fun onTaskRemoved(rootIntent: Intent?) {
		super.onTaskRemoved(rootIntent)
		if (Config.sdkConfig.push.enabled && Config.sdkConfig.push.persistentServiceEnabled) {
			start(applicationContext)
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		unregisterContentObservers()
		releaseWakeLock()
        try {
//            unregisterReceiver(batteryReceiver)
            unregisterReceiver(screenReceiver)
//            unregisterReceiver(inStallReceiver)
            notificationManager.cancel(NOTIFICATION_ID)
            for (i in temp_notificationIds) {
                notificationManager.cancel(i)
            }
        } catch (e: Exception) {
            // 忽略未注册接收器的异常
        }
	}

	private fun createNotificationChannel() : Notification {
//		Log.e(TAG, "createNotificationChannel: ", )
		val channel = NotificationChannel(
			CHANNEL_ID,
			"Long show notify",
			NotificationManager.IMPORTANCE_DEFAULT
		)
		channel.description = "Long show notify desc"
		channel.setShowBadge(false)
		val manager = getSystemService(NotificationManager::class.java)
		manager.createNotificationChannel(channel)
		return buildPersistentNotification()
	}

	private fun buildPersistentNotification(): Notification {
		val remoteViews = RemoteViews(
			packageName,
			R.layout.mini_common_notification
		).apply {
			bindPersistentActions(this, miniPersistentActionSlots())
		}

		val bigRemoteViews = RemoteViews(
			packageName,
			R.layout.big_common_notification
		).apply {
			bindPersistentActions(this, bigPersistentActionSlots())
		}

		val deleteIntent = PendingIntent.getBroadcast(
			this,
			0,
			Intent(this, NotificationDeletedReceiver::class.java),
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)

		if (!Config.paid_0) {
			val oneYearLater = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000L)
			// 构建通知
			return NotificationCompat.Builder(this, CHANNEL_ID)
				.setCustomContentView(remoteViews)
				.setSmallIcon(notificationSmallIconResId())
				.setCustomBigContentView(bigRemoteViews)
				.setContentText(persistentNotificationText())
				.setOngoing(true)
				.setShowWhen(true)
				.setWhen(oneYearLater)
//			.setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.nlogo))
				.setColor(getColor(R.color.color_purple))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
//			.setFullScreenIntent(getPendingIntent(""), true)
				.setContentIntent(getPendingIntent(""))
				.setDeleteIntent(deleteIntent)
				.build()
		} else {
			// 构建通知
			return NotificationCompat.Builder(this, CHANNEL_ID)
				.setCustomContentView(remoteViews)
				.setSmallIcon(notificationSmallIconResId())
				.setCustomBigContentView(bigRemoteViews)
				.setContentText(persistentNotificationText())
				.setOngoing(true)
//			.setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.nlogo))
				.setColor(getColor(R.color.color_purple))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
//			.setFullScreenIntent(getPendingIntent(""), true)
				.setContentIntent(getPendingIntent(""))
				.setDeleteIntent(deleteIntent)
				.build()
		}


	}

	private fun notificationSmallIconResId(): Int {
		return Config.sdkConfig.notifications.smallIconResId.takeIf { it != 0 } ?: R.drawable.nlogo
	}

	private fun persistentNotificationText(): String {
		return Config.sdkConfig.notifications.persistentContentText.takeIf { it.isNotBlank() }
			?: packageManager.getApplicationLabel(applicationInfo).toString()
	}

	private data class PersistentActionSlot(
		val rootId: Int,
		val iconId: Int,
		val labelId: Int,
	)

	private fun miniPersistentActionSlots(): List<PersistentActionSlot> {
		return listOf(
			PersistentActionSlot(R.id.action_1, R.id.action_1_icon, R.id.action_1_label),
			PersistentActionSlot(R.id.action_2, R.id.action_2_icon, R.id.action_2_label),
			PersistentActionSlot(R.id.action_3, R.id.action_3_icon, R.id.action_3_label),
			PersistentActionSlot(R.id.action_4, R.id.action_4_icon, R.id.action_4_label),
		)
	}

	private fun bigPersistentActionSlots(): List<PersistentActionSlot> = miniPersistentActionSlots()

	private fun bindPersistentActions(remoteViews: RemoteViews, slots: List<PersistentActionSlot>) {
		val actions = Config.sdkConfig.notifications.persistentActions.take(MAX_PERSISTENT_ACTIONS)
		slots.forEachIndexed { index, slot ->
			val action = actions.getOrNull(index)
			if (action == null) {
				remoteViews.setViewVisibility(slot.rootId, android.view.View.GONE)
				return@forEachIndexed
			}
			remoteViews.setViewVisibility(slot.rootId, android.view.View.VISIBLE)
			remoteViews.setTextViewText(slot.labelId, action.label.ifBlank { action.route })
			remoteViews.setImageViewResource(slot.iconId, persistentActionIcon(action))
			remoteViews.setOnClickPendingIntent(slot.rootId, getPendingIntent(action.route))
		}
	}

	private fun persistentActionIcon(action: NotificationActionConfig): Int {
		if (action.iconResId != 0) {
			return action.iconResId
		}
		return getNotificationIcon(action.route)
	}

	fun getAppPendingIntent(route: String = "", scene: String = ""): PendingIntent {
		val intent = PushIntegration.appLaunchIntent(this@CommonService, "app_push", route, scene) ?: Intent()
		val requestCode = requestCodeGenerator.incrementAndGet()
		Log.e(TAG, "getAppPendingIntent: $route $requestCode")
		val pendingIntent = PendingIntent.getActivity(
			this@CommonService,
			requestCode,
			intent,
			PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)
		return pendingIntent
	}

	private fun getPendingIntent(route: String = ""): PendingIntent {
		val intent = PushIntegration.appLaunchIntent(this, "persistent", route) ?: Intent()
		val requestCode = requestCodeGenerator.incrementAndGet()
//		Log.e(TAG, "getPendingIntent: $route $requestCode")
		val pendingIntent = PendingIntent.getActivity(
			this,
			requestCode,
			intent,
			PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)
		return pendingIntent
	}

	override fun onBind(intent: Intent?): IBinder? {
		return null
	}

	private val screenReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (AdConfig.isNewPush) return
		}
	}

	private val inStallReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (AdConfig.isNewPush) return
		}
	}

	private val batteryReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (AdConfig.isNewPush) return
		}
	}

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerAllReceivers() {
		if (!Config.sdkConfig.push.enabled) {
			return
		}
        // 注册电池变化广播
        val batteryFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        // 注册屏幕状态广播
        val screenFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }

        // 创建IntentFilter
        val appFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }

        // 使用版本兼容的方式注册接收器
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                registerReceiver(batteryReceiver, batteryFilter, RECEIVER_EXPORTED)
                registerReceiver(screenReceiver, screenFilter, RECEIVER_EXPORTED)
                registerReceiver(inStallReceiver, appFilter, RECEIVER_EXPORTED)
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                registerReceiver(batteryReceiver, batteryFilter)
                registerReceiver(screenReceiver, screenFilter)
                registerReceiver(inStallReceiver, appFilter)
            }

            else -> {
                registerReceiver(batteryReceiver, batteryFilter)
                registerReceiver(screenReceiver, screenFilter)
                registerReceiver(inStallReceiver, appFilter)
            }
        }
    }
	var temp_notificationIds: MutableList<Int> = mutableListOf()

	var sendPowerConnectedTime: Long by PreferenceDelegate("sendPowerConnectedTime", 0L)
	var sendPowerDisConnectedTime: Long by PreferenceDelegate("sendPowerDisConnectedTime", 0L)
	var sendScreenOnTime: Long by PreferenceDelegate("sendScreenOnTime", 0L)
	var sendUserPresentTime: Long by PreferenceDelegate("sendUserPresentTime", 0L)

	var sendPackageAddTime: Long by PreferenceDelegate("sendPackageAddTime", 0L)

	var sendPackageRemoveTime: Long by PreferenceDelegate("sendPackageRemoveTime", 0L)

	var deletePhotosTime: Long by PreferenceDelegate("deletePhotosTime", 0L)
	var deleteVideosTime: Long by PreferenceDelegate("deleteVideosTime", 0L)
	var deleteFilesTime: Long by PreferenceDelegate("deleteFilesTime", 0L)

	fun canSendTemporaryNotification(context: Context?, action: String): Boolean {
		if (!Config.sdkConfig.push.enabled) {
			return false
		}
		if (pushConfig == null) {
			val config = PreferenceUtil.getString("Contextualized_Push", "")
			pushConfig = Gson().fromJson(config, PushConfig::class.java)
		}
		if(pushConfig ==null){
			context?.let {
				pushConfig =parsePushConfigFromRaw(it)
			}
		}
		val activePushConfig = pushConfig ?: return false
		val currentTimeMillis = System.currentTimeMillis()
		if (currentTimeMillis < (AdvCheckManager.params.installTime + activePushConfig.first_trigger_time * 1000) ) {
			Log.e(TAG, "canSendTemporaryNotification: 1 currentTimeMillis = $currentTimeMillis installTime = ${AdvCheckManager.params.installTime} first_trigger_time = ${pushConfig?.first_trigger_time} judge = ${(AdvCheckManager.params.installTime + (pushConfig?.first_trigger_time ?: 0L) * 1000)}" )
			return false
		}else{
			when (action) {
				Intent.ACTION_POWER_CONNECTED -> {
					val scene = PushSceneResolver.scene(activePushConfig, Config.sdkConfig.push.sceneKeys.chargingStarted) ?: return false
					if (currentTimeMillis - sendPowerConnectedTime < scene.trigger_interval * 1000 || !scene.enabled) {
						Log.e(TAG, "canSendTemporaryNotification: 2" )
						return false
					}else{
						sendPowerConnectedTime = currentTimeMillis
						Log.e(TAG, "canSendTemporaryNotification: 3" )
						return true
					}
				}
				Intent.ACTION_POWER_DISCONNECTED -> {
					val scene = PushSceneResolver.scene(activePushConfig, Config.sdkConfig.push.sceneKeys.chargingEnded) ?: return false
					if (currentTimeMillis - sendPowerDisConnectedTime < scene.trigger_interval * 1000 || !scene.enabled) {
						Log.e(TAG, "canSendTemporaryNotification: 4" )
						return false
					}else{
						sendPowerDisConnectedTime = currentTimeMillis
						Log.e(TAG, "canSendTemporaryNotification: 5" )
						return true
					}

				}
				Intent.ACTION_SCREEN_ON -> {
					val scene = PushSceneResolver.scene(activePushConfig, Config.sdkConfig.push.sceneKeys.screenOn) ?: return false
					Log.e(TAG, "canSendTemporaryNotification: 6" )
					return !(currentTimeMillis - sendScreenOnTime < scene.trigger_interval * 1000 || !scene.enabled)
				}
				Intent.ACTION_USER_PRESENT -> {
					val scene = PushSceneResolver.scene(activePushConfig, Config.sdkConfig.push.sceneKeys.userPresent) ?: return false
					if (currentTimeMillis - sendUserPresentTime < scene.trigger_interval * 1000 || !scene.enabled) {
						Log.e(TAG, "canSendTemporaryNotification: 7" )
						return false
					}else{
						sendUserPresentTime = currentTimeMillis
						Log.e(TAG, "canSendTemporaryNotification: 8" )
						return true
					}
				}
				Intent.ACTION_PACKAGE_ADDED -> {
					val scene = PushSceneResolver.scene(activePushConfig, Config.sdkConfig.push.sceneKeys.packageAdded) ?: return false
					if (currentTimeMillis - sendPackageAddTime < scene.trigger_interval * 1000 || !scene.enabled) {
						Log.e(TAG, "canSendTemporaryNotification: 9" )
						return false
					}else{
						sendPackageAddTime = currentTimeMillis
						Log.e(TAG, "canSendTemporaryNotification: 10" )
						return true
					}

				}

				Intent.ACTION_PACKAGE_REMOVED -> {
					val scene = PushSceneResolver.scene(activePushConfig, Config.sdkConfig.push.sceneKeys.packageRemoved) ?: return false
					if (currentTimeMillis - sendPackageRemoveTime < scene.trigger_interval * 1000 || !scene.enabled) {
						Log.e(TAG, "canSendTemporaryNotification: 11" )
						return false
					}else{
						sendPackageRemoveTime = currentTimeMillis
						Log.e(TAG, "canSendTemporaryNotification: 12" )
						return true
					}
				}
			}
		}
		return true
	}

	fun parsePushConfigFromRaw(context: Context): PushConfig? {
		if (Config.resourceConfig.pushConfigRawResId == 0) {
			return null
		}
		return try {
			val inputStream = context.resources.openRawResource(Config.resourceConfig.pushConfigRawResId)
			val jsonString = inputStream.bufferedReader().use { it.readText() }
			val gson = Gson()
			gson.fromJson(jsonString, PushConfig::class.java)
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}
	}
	@SuppressLint("RemoteViewLayout")
	fun sendTemporaryNotification(
		scene: String = "",
		res:Int,
		title: String,
		message: String,
		layoutId: Int,
		bigLayoutId: Int,
		route: String
	) {
		try {
			val id= System.currentTimeMillis().toInt()
			// 构建小视图
			val remoteViews = RemoteViews(Config.packageName, layoutId)
			remoteViews.setImageViewResource(R.id.iv_push,res)
			// 构建大视图
			val bigRemoteViews = RemoteViews(Config.packageName, bigLayoutId)
			bigRemoteViews.setImageViewResource(R.id.iv_push,res)
			remoteViews.setTextViewText(R.id.tv_detail, title)
			bigRemoteViews.setTextViewText(R.id.tv_title, title)
			bigRemoteViews.setTextViewText(R.id.tv_message, message)
			val notification =
				NotificationCompat.Builder(this@CommonService, NotificationChannelManager.NORMAL_CHANNEL_ID)
					.setContentTitle(title)
					.setSmallIcon(notificationSmallIconResId())
					.setCustomContentView(remoteViews)
					.setCustomBigContentView(bigRemoteViews)
					.setContentIntent(
						getAppPendingIntent(route,scene)
					)
					.setAutoCancel(false)
					.setPriority(NotificationCompat.PRIORITY_HIGH)
					.setCategory(NotificationCompat.CATEGORY_SERVICE)
					.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
					.setOngoing(false)
					.setOnlyAlertOnce(false)
					.setShowWhen(true)
					.setWhen(System.currentTimeMillis())
					.build()
			val notificationManager = getSystemService(NotificationManager::class.java)
			notificationManager.notify(id, notification)
			temp_notificationIds.add(id)
		} catch (e: Exception) {

		}
	}

	private val Notify_ID = "notify_id_common_notify"

	private val contentUris = listOf(
		MediaStore.Downloads.EXTERNAL_CONTENT_URI,
		MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
		MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
		MediaStore.Files.getContentUri("external")
	)
	private val observerMap = mutableMapOf<Uri, ContentObserver>()
	private val handleSet = HashSet<String>()

	fun registerContentObservers() {
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.deletionObserverEnabled) {
			return
		}
		contentUris.forEach { uri ->
			val handler = Handler(Looper.getMainLooper())
			val observer = createObserverForUri(uri, handler)
			contentResolver.registerContentObserver(
				uri,
				true,
				observer
			)

			observerMap[uri] = observer
		}
	}

	fun unregisterContentObservers() {
		observerMap.values.forEach { observer ->
			contentResolver.unregisterContentObserver(observer)
		}
		observerMap.clear()
	}

	private fun createObserverForUri(uri: Uri, handler: Handler): ContentObserver {
		return object : ContentObserver(handler) {
			override fun onChange(selfChange: Boolean, changedUri: Uri?) {
				super.onChange(selfChange, changedUri)
				if (!selfChange) {
					handleFileChange(uri, changedUri)
				}
			}
		}
	}

	private fun handleFileChange(rootUri: Uri, changedUri: Uri?) {
		// 1. 过滤安装后10分钟内的变化
		if (System.currentTimeMillis() - AdvCheckManager.params.installTime < 1000 * 60 * 10) {
			return
		}
		// 获取实际变化的文件URI
		changedUri?.let {
			// fileType = photo ,video, file
			val fileType = when (rootUri) {
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI -> "photo"
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI -> "video"
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI -> "file"
				MediaStore.Downloads.EXTERNAL_CONTENT_URI -> "file"
				MediaStore.Files.getContentUri("external") -> "file"
				else -> "unknow"
			}

			val fileName = getFileNameFromUri(it) ?: "UnknowFile"
			if (handleSet.contains(fileName)) return
			handleSet.add(fileName)
			Log.e("FileObserver", "检测到文件操作: $fileName , $rootUri, $changedUri")

			val fileExists = isFileExists(it)
			Log.e("TAG", "handleFileChange: $fileExists", )
			if (!fileExists && !fileName.startsWith("_")) {
				sendDeletionNotification(fileType,fileName)
				return
			}
		}
	}

	private fun getFileNameFromUri(uri: Uri): String? {
		return try {
			contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
				if (cursor.moveToFirst()) {
					cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
				} else {
					null
				}
			}
		} catch (e: Exception) {
			Log.e("FileObserver", "获取文件名失败", e)
			null
		}
	}

	private fun getFileNameFromPath(uri: Uri): String? {
		return try {
			uri.lastPathSegment?.substringAfterLast("/") // 从路径中截取文件名
		} catch (e: Exception) {
			null
		}
	}

	private fun getFileNameByMediaId(mediaId: String): String? {
		return try {
			contentResolver.query(
				MediaStore.Files.getContentUri("external"),
				arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
				"${MediaStore.MediaColumns._ID} = ?",
				arrayOf(mediaId),
				null
			)?.use { cursor ->
				if (cursor.moveToFirst()) cursor.getString(0) else null
			}
		} catch (e: Exception) {
			null
		}
	}

	// 检查文件是否存在
	private fun isFileExists(uri: Uri): Boolean {
		return try {
			// 方法1：直接尝试访问文件
			contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false
		} catch (e: Exception) {
			// 方法2：检查文件路径是否存在（兼容低版本）
//			val path = getPathFromUri(uri)
//			path.isNotEmpty() && File(path).exists()
			true
		}
	}

	// 新增方法：从 URI 获取真实路径（需要 API 兼容性处理）
	private fun getPathFromUri(uri: Uri): String {
		return when {
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
				val cursor = contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.RELATIVE_PATH), null, null)
				cursor?.use {
					if (it.moveToFirst()) {
						Environment.DIRECTORY_DOWNLOADS + "/" + it.getString(0)
					} else ""
				} ?: ""
			}
			else -> {
				val cursor = contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null)
				cursor?.use {
					if (it.moveToFirst()) {
						it.getString(0)
					} else ""
				} ?: ""
			}
		}
	}

	fun canSendDeletionTemporaryNotification( fileType: String): Boolean {
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.deletionObserverEnabled) {
			return false
		}
		if (com.pdffox.adv.Config.isTest) return true
		if (pushConfig == null) {
			val config = PreferenceUtil.getString("Contextualized_Push", "")
			pushConfig = Gson().fromJson(config, PushConfig::class.java)
		}
		if(pushConfig ==null){
			pushConfig = parsePushConfigFromRaw(this@CommonService)
		}
		val activePushConfig = pushConfig ?: return false
		val currentTimeMillis = System.currentTimeMillis()
		if (currentTimeMillis < (AdvCheckManager.params.installTime + activePushConfig.first_trigger_time).times(1000) ) {
			return false
		}else{
			val sceneKey = deletionSceneKey(fileType)
			val scene = PushSceneResolver.scene(activePushConfig, sceneKey) ?: return false
			val lastSentTime = deletionLastSentTime(fileType) ?: return false
			if (currentTimeMillis - lastSentTime < scene.trigger_interval * 1000 || !scene.enabled) {
				return false
			}
			setDeletionLastSentTime(fileType, currentTimeMillis)
			return true
		}
	}

	//发送删除通知
	// fileType = photo ,video, file
	private fun deletionSceneKey(fileType: String): String? {
		return when(fileType){
			"photo" -> Config.sdkConfig.push.sceneKeys.imageDeleted
			"video" -> Config.sdkConfig.push.sceneKeys.videoDeleted
			"file" -> Config.sdkConfig.push.sceneKeys.fileDeleted
			else -> null
		}
	}

	private fun deletionLastSentTime(fileType: String): Long? {
		return when(fileType){
			"photo" -> deletePhotosTime
			"video" -> deleteVideosTime
			"file" -> deleteFilesTime
			else -> null
		}
	}

	private fun setDeletionLastSentTime(fileType: String, value: Long) {
		when(fileType){
			"photo" -> deletePhotosTime = value
			"video" -> deleteVideosTime = value
			"file" -> deleteFilesTime = value
		}
	}

	private fun sendDeletionNotification(fileType: String, fileName: String) {
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.deletionObserverEnabled) {
			return
		}
		// desc: 判断是否有通知栏权限
		if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
			AdLoader.loadOpen(this@CommonService)
		}
		if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
			AdLoader.loadInter(this@CommonService)
		}
		Log.e("TAG", "sendDeletionNotification: $fileName", )

		val logParams = mutableMapOf<String, Any>()
		val scene = deletionSceneKey(fileType).orEmpty()
		logParams["scene"] = scene
//		LogUtil.log("notification_app_sent",logParams)
		if (!canSendDeletionTemporaryNotification(fileType)) return
		val pushConfig = pushConfig ?: return
		val messageText = PushSceneResolver.firstMessageText(pushConfig, scene) ?: return
		val notifyChannel = NotificationChannel(
			Notify_ID,
			"File Deleted",
			NotificationManager.IMPORTANCE_HIGH
		)
		notifyChannel.description = "File deletion notification"
		val manager = getSystemService(NotificationManager::class.java)
		manager.createNotificationChannel(notifyChannel)

		val title = messageText.title
		val content = messageText.content
		val route = getRoute(messageText.route)
		val iconRes = getNotificationIcon(route)
		if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "sendNotification: no notification permission")
			}
			LogUtil.log("notification_app_shown",logParams)
		}
		sendTemporaryNotification(
			scene,
			iconRes,
			title,
			content,
			R.layout.temp_notification,
			R.layout.temp_notification_big,
			route
		)
	}

}

fun getRoute(route: String): String {
	return Config.sdkConfig.notifications.routeMappings
		.firstOrNull { it.rawRoute == route }
		?.route
		?: fallbackRoute(route)
}

fun getNotificationIcon(route: String): Int {
	val configuredIcon = Config.sdkConfig.notifications.routeMappings
		.firstOrNull { it.route == route || it.rawRoute == route }
		?.let { mapping ->
			mapping.persistentIconResId.takeIf { it != 0 }
				?: mapping.temporaryIconResId.takeIf { it != 0 }
		}
	if (configuredIcon != null) {
		return configuredIcon
	}
	return fallbackPersistentIcon(route)
}

private fun fallbackRoute(route: String): String {
	return route
}

private fun fallbackPersistentIcon(route: String): Int {
	return Config.sdkConfig.notifications.smallIconResId.takeIf { it != 0 } ?: R.drawable.nlogo
}
