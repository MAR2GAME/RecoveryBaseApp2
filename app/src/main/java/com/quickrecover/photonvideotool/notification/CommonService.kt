//package com.quickrecover.photonvideotool.notification
//
//import android.annotation.SuppressLint
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.app.Service
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.content.pm.ServiceInfo
//import android.database.ContentObserver
//import android.graphics.BitmapFactory
//import android.net.Uri
//import android.os.BatteryManager
//import android.os.Build
//import android.os.Environment
//import android.os.Handler
//import android.os.IBinder
//import android.os.Looper
//import android.os.PowerManager
//import android.provider.MediaStore
//import android.util.Log
//import android.widget.RemoteViews
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import com.datatool.photorecovery.MainActivity
//import com.google.gson.Gson
//import com.pdffox.adv.BuildConfig
//import com.pdffox.adv.Config
//import com.pdffox.adv.R
//import com.pdffox.adv.adv.AdConfig
//import com.pdffox.adv.adv.AdLoader
//import com.pdffox.adv.adv.AdvCheckManager
//import com.pdffox.adv.adv.NativeConfig
//import com.pdffox.adv.log.LogUtil
//import com.pdffox.adv.util.PreferenceDelegate
//import com.pdffox.adv.util.PreferenceUtil
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import java.util.Locale
//import java.util.concurrent.atomic.AtomicInteger
//
//class CommonService : Service() {
//
//	companion object {
//		private const val NOTIFICATION_ID = 2001
//		private const val NOTIFICATION_New_ID = 2002
//		private const val CHANNEL_ID = "channel_id_common_notify"
//		private const val Notify_ID = "notify_id_common_notify"
//		private const val TAG = "CommonService"
//
//		var pushConfig: PushConfig? = null
//
//		private val requestCodeGenerator = AtomicInteger(0)
//
//	}
//
//    private lateinit var notificationManager: NotificationManager
//
//	lateinit var wakeLock: PowerManager.WakeLock
//	private fun acquireWakeLock() {
//		val powerManager = getSystemService(POWER_SERVICE) as PowerManager
//		wakeLock = powerManager.newWakeLock(
//			PowerManager.PARTIAL_WAKE_LOCK,
//			"KeepAliveService::WakeLock"
//		)
//		wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
//	}
//
//	private fun releaseWakeLock() {
//		if (::wakeLock.isInitialized && wakeLock.isHeld) {
//			wakeLock.release()
//		}
//	}
//
//	override fun onCreate() {
//		super.onCreate()
//		initPushConfig()
//		notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//		registerContentObservers()
//		acquireWakeLock()
//		NotificationChannelManager.createNormalChannel(this)
//        // 注册所有广播接收器
//        registerAllReceivers()
//
//		try {
//			val notification = createNotificationChannel()
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//				startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
//			} else {
//				startForeground(NOTIFICATION_ID, notification)
//			}
//		} catch (e: Exception) {
//			e.printStackTrace()
//		}
//	}
//
//	fun initPushConfig(){
//		if (pushConfig == null) {
//			val config = PreferenceUtil.getString("Contextualized_Push", "")
//			pushConfig = Gson().fromJson(config, PushConfig::class.java)
//		}
//		if(pushConfig ==null){
//			pushConfig =parsePushConfigFromRaw()
//		}
//	}
//	fun parsePushConfigFromRaw(): PushConfig? {
//		return try {
//			val inputStream = this.resources.openRawResource(R.raw.push)
//			val jsonString = inputStream.bufferedReader().use { it.readText() }
//			val gson = Gson()
//			gson.fromJson(jsonString, PushConfig::class.java)
//		} catch (e: Exception) {
//			e.printStackTrace()
//			null
//		}
//	}
//
//	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//		try {
//			val notification = createNotificationChannel()
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//				startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
//			} else {
//				startForeground(NOTIFICATION_ID, notification)
//			}
//		} catch (e: Exception) {
//			e.printStackTrace()
//		}
//		return START_STICKY
//	}
//
//	override fun onTaskRemoved(rootIntent: Intent?) {
//		super.onTaskRemoved(rootIntent)
//		val restartServiceIntent = Intent(applicationContext, CommonService::class.java)
//		restartServiceIntent.setPackage(packageName)
//		startForegroundService(restartServiceIntent)
//	}
//
//	override fun onDestroy() {
//		super.onDestroy()
//		unregisterContentObservers()
//		releaseWakeLock()
//        try {
////            unregisterReceiver(batteryReceiver)
//            unregisterReceiver(screenReceiver)
////            unregisterReceiver(inStallReceiver)
//            notificationManager.cancel(NOTIFICATION_ID)
//            for (i in temp_notificationIds) {
//                notificationManager.cancel(i)
//            }
//        } catch (e: Exception) {
//            // 忽略未注册接收器的异常
//        }
//	}
//
//	private fun createNotificationChannel() : Notification {
////		Log.e(TAG, "createNotificationChannel: ", )
//		val channel = NotificationChannel(
//			CHANNEL_ID,
//			"Long show notify",
//			NotificationManager.IMPORTANCE_DEFAULT
//		)
//		channel.description = "Long show notify desc"
//		channel.setShowBadge(false)
//		val manager = getSystemService(NotificationManager::class.java)
//		manager.createNotificationChannel(channel)
//		return buildPersistentNotification()
//	}
//
//	private fun buildPersistentNotification(): Notification {
//		val remoteViews = RemoteViews(
//			packageName,
//			R.layout.a_mini_common_notification
//		).apply {
//			setOnClickPendingIntent(R.id.photos, getPendingIntent("Photos"))
//			setOnClickPendingIntent(R.id.videos, getPendingIntent("Videos"))
//			setOnClickPendingIntent(R.id.files, getPendingIntent("Files"))
//			setOnClickPendingIntent(R.id.recovered, getPendingIntent("Recovered"))
//		}
//
//		val bigRemoteViews = RemoteViews(
//			packageName,
//			R.layout.a_big_common_notification
//		).apply {
//			setOnClickPendingIntent(R.id.photos, getPendingIntent("Photos"))
//			setOnClickPendingIntent(R.id.videos, getPendingIntent("Videos"))
//			setOnClickPendingIntent(R.id.files, getPendingIntent("Files"))
//			setOnClickPendingIntent(R.id.recovered, getPendingIntent("Recovered"))
//		}
//
//		val deleteIntent = PendingIntent.getBroadcast(
//			this,
//			0,
//			Intent(this, NotificationDeletedReceiver::class.java),
//			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//		)
//
//		if (!Config.paid_0) {
//			val oneYearLater = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000L)
//			// 构建通知
//			return NotificationCompat.Builder(this, CHANNEL_ID)
//				.setCustomContentView(remoteViews)
//				.setSmallIcon(R.drawable.a_nlogo)
//				.setCustomBigContentView(bigRemoteViews)
//				.setContentText(getString(R.string.app_name))
//				.setOngoing(true)
//				.setShowWhen(true)
//				.setWhen(oneYearLater)
////			.setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.a_nlogo))
//				.setColor(getColor(R.color.color_purple))
//				.setPriority(NotificationCompat.PRIORITY_HIGH)
////			.setFullScreenIntent(getPendingIntent(""), true)
//				.setContentIntent(getPendingIntent(""))
//				.setDeleteIntent(deleteIntent)
//				.build()
//		} else {
//			// 构建通知
//			return NotificationCompat.Builder(this, CHANNEL_ID)
//				.setCustomContentView(remoteViews)
//				.setSmallIcon(R.drawable.a_nlogo)
//				.setCustomBigContentView(bigRemoteViews)
//				.setContentText(getString(R.string.app_name))
//				.setOngoing(true)
////			.setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.a_nlogo))
//				.setColor(getColor(R.color.color_purple))
//				.setPriority(NotificationCompat.PRIORITY_HIGH)
////			.setFullScreenIntent(getPendingIntent(""), true)
//				.setContentIntent(getPendingIntent(""))
//				.setDeleteIntent(deleteIntent)
//				.build()
//		}
//
//
//	}
//
//	fun getAppPendingIntent(route: String = "", scene: String = ""): PendingIntent {
//		val intent = Intent(this@CommonService, MainActivity::class.java).apply {
////			addCategory(Intent.CATEGORY_DEFAULT)
//			putExtra("AppOpenFrom", "app_push")
//			putExtra("Route", route)
//			putExtra("Scene", scene)
//			flags = Intent.FLAG_ACTIVITY_NEW_TASK
//		}
//		val requestCode = requestCodeGenerator.incrementAndGet()
//		Log.e(TAG, "getAppPendingIntent: $route $requestCode")
//		val pendingIntent = PendingIntent.getActivity(
//			this@CommonService,
//			requestCode,
//			intent,
//			PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
//		)
//		return pendingIntent
//	}
//
//	private fun getPendingIntent(route: String = ""): PendingIntent {
//		val intent = Intent(this, MainActivity::class.java).apply {
//			putExtra("AppOpenFrom", "persistent")
//			putExtra("Route", route)
//			flags = Intent.FLAG_ACTIVITY_NEW_TASK
//		}
//		val requestCode = requestCodeGenerator.incrementAndGet()
////		Log.e(TAG, "getPendingIntent: $route $requestCode")
//		val pendingIntent = PendingIntent.getActivity(
//			this,
//			requestCode,
//			intent,
//			PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
//		)
//		return pendingIntent
//	}
//
//	override fun onBind(intent: Intent?): IBinder? {
//		return null
//	}
//
//	private val screenReceiver = object: BroadcastReceiver() {
//		private val logParams = mutableMapOf<String, Any>()
//		private val serviceScope = CoroutineScope(Dispatchers.Main)
//
//		override fun onReceive(context: Context?, intent: Intent?) {
//			if (AdConfig.isNewPush) return
////			if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
////				AdLoader.loadOpen(this@CommonService)
////			}
////			if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
////				AdLoader.loadInter(this@CommonService)
////			}
////			when (intent?.action) {
////				Intent.ACTION_SCREEN_ON -> {
////					logParams.clear()
////					logParams.put("scene","screen_on_5s")
//////					LogUtil.log("notification_app_sent",logParams)
////					// 不发送通知，避免频繁打扰
////					if (canSendTemporaryNotification(context ,Intent.ACTION_SCREEN_ON)){
////						logParams.clear()
////						logParams.put("scene","screen_on_5s")
////						LogUtil.log("notification_app_shown",logParams)
////						serviceScope.launch {
////							delay(5000)
////							sendScreenOnTime= System.currentTimeMillis()
////							var title = ""
////							var message = ""
////							val currentLocale = Locale.getDefault()
////							when {
////								currentLocale.isFrench() -> {
////									title =
////										CommonService.pushConfig!!.scene.screen_on_5s.messages[0].keys[1].title
////									message =
////										CommonService.pushConfig!!.scene.screen_on_5s.messages[0].keys[1].content
////								}
////
////								currentLocale.isSpanish() -> {
////									title =
////										CommonService.pushConfig!!.scene.screen_on_5s.messages[0].keys[0].title
////									message =
////										CommonService.pushConfig!!.scene.screen_on_5s.messages[0].keys[0].content
////								}
////
////								currentLocale.isKorean() -> {
////									title =
////										CommonService.pushConfig!!.scene.screen_on_5s.messages[0].keys[2].title
////									message =
////										CommonService.pushConfig!!.scene.screen_on_5s.messages[0].keys[2].content
////								}
////
////								else -> {
////									title = CommonService.pushConfig!!.scene.screen_on_5s.messages[0].title
////									message =
////										CommonService.pushConfig!!.scene.screen_on_5s.messages[0].content
////								}
////							}
////							val route = getRoute(CommonService.pushConfig!!.scene.screen_on_5s.messages[0].route)
////							val iconRes = getNotificationIcon(route)
////							sendTemporaryNotification(
////								"screen_on_5s",
////								iconRes,
////								title,
////								message,
////								R.layout.a_temp_notification,
////								R.layout.a_temp_notification_big,
////								route
////							)
////						}
////
////					}
////				}
////
////				Intent.ACTION_USER_PRESENT -> {
////					logParams.clear()
////					logParams.put("scene","phone_unlock")
//////					LogUtil.log("notification_app_sent",logParams)
////					if (canSendTemporaryNotification(context ,Intent.ACTION_USER_PRESENT)) {
////						logParams.clear()
////						logParams.put("scene","phone_unlock")
////						LogUtil.log("notification_app_shown",logParams)
////						var title = ""
////						var message = ""
////						val currentLocale = Locale.getDefault()
////						when {
////							currentLocale.isFrench() -> {
////								title =
////									CommonService.pushConfig!!.scene.phone_unlock.messages[0].keys[1].title
////								message =
////									CommonService.pushConfig!!.scene.phone_unlock.messages[0].keys[1].content
////							}
////
////							currentLocale.isSpanish() -> {
////								title =
////									CommonService.pushConfig!!.scene.phone_unlock.messages[0].keys[0].title
////								message =
////									CommonService.pushConfig!!.scene.phone_unlock.messages[0].keys[0].content
////							}
////
////							currentLocale.isKorean() -> {
////								title =
////									CommonService.pushConfig!!.scene.phone_unlock.messages[0].keys[2].title
////								message =
////									CommonService.pushConfig!!.scene.phone_unlock.messages[0].keys[2].content
////							}
////
////							else -> {
////								title = CommonService.pushConfig!!.scene.phone_unlock.messages[0].title
////								message =
////									CommonService.pushConfig!!.scene.phone_unlock.messages[0].content
////							}
////						}
////						val route = getRoute(CommonService.pushConfig!!.scene.phone_unlock.messages[0].route)
////						val iconRes = getNotificationIcon(route)
////						sendTemporaryNotification(
////							"phone_unlock",
////							iconRes,
////							title,
////							message,
////							R.layout.a_temp_notification,
////							R.layout.a_temp_notification_big,
////							route
////						)
////					}
////				}
////			}
//		}
//	}
//	private val inStallReceiver = object: BroadcastReceiver() {
//		private val logParams = mutableMapOf<String, Any>()
//		private val serviceScope = CoroutineScope(Dispatchers.Main)
//
//		override fun onReceive(context: Context?, intent: Intent?) {
//			if (AdConfig.isNewPush) return
////
////			if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
////				AdLoader.loadOpen(this@CommonService)
////
////			}
////			if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
////				AdLoader.loadInter(this@CommonService)
////			}
////			when (intent?.action) {
////				Intent.ACTION_PACKAGE_ADDED -> {
////					logParams.clear()
////					logParams.put("scene","app_installed")
//////					LogUtil.log("notification_app_sent",logParams)
////					// 应用被安装
////					if (canSendTemporaryNotification(context ,Intent.ACTION_PACKAGE_ADDED)) {
////
////						logParams.clear()
////						logParams.put("scene","app_installed")
////						LogUtil.log("notification_app_shown",logParams)
////						var title = ""
////						var message = ""
////						val route = getRoute(CommonService.pushConfig!!.scene.app_installed.messages[0].route)
////						val iconRes = getNotificationIcon(route)
////						val currentLocale = Locale.getDefault()
////						when {
////							currentLocale.isFrench() -> {
////								title =
////									CommonService.pushConfig!!.scene.app_installed.messages[0].keys[1].title
////								message =
////									CommonService.pushConfig!!.scene.app_installed.messages[0].keys[1].content
////							}
////
////							currentLocale.isSpanish() -> {
////								title =
////									CommonService.pushConfig!!.scene.app_installed.messages[0].keys[0].title
////								message =
////									CommonService.pushConfig!!.scene.app_installed.messages[0].keys[0].content
////							}
////
////							currentLocale.isKorean() -> {
////								title =
////									CommonService.pushConfig!!.scene.app_installed.messages[0].keys[2].title
////								message =
////									CommonService.pushConfig!!.scene.app_installed.messages[0].keys[2].content
////							}
////
////							else -> {
////								title = CommonService.pushConfig!!.scene.app_installed.messages[0].title
////								message =
////									CommonService.pushConfig!!.scene.app_installed.messages[0].content
////							}
////						}
////						sendTemporaryNotification(
////							"app_installed",
////							iconRes,
////							title,
////							message,
////							R.layout.a_temp_notification,
////							R.layout.a_temp_notification_big,
////							route
////						)
////					}
////				}
////
////				Intent.ACTION_PACKAGE_REMOVED -> {
////					logParams.clear()
////					logParams.put("scene","app_uninstalled")
//////					LogUtil.log("notification_app_sent",logParams)
////					if (canSendTemporaryNotification(context ,Intent.ACTION_PACKAGE_REMOVED)) {
////						logParams.clear()
////						logParams.put("scene","app_uninstalled")
////						LogUtil.log("notification_app_shown",logParams)
////						var title = ""
////						var message = ""
////						val currentLocale = Locale.getDefault()
////						when {
////							currentLocale.isFrench() -> {
////								title =
////									CommonService.pushConfig!!.scene.app_uninstalled.messages[0].keys[1].title
////								message =
////									CommonService.pushConfig!!.scene.app_uninstalled.messages[0].keys[1].content
////							}
////
////							currentLocale.isSpanish() -> {
////								title =
////									CommonService.pushConfig!!.scene.app_uninstalled.messages[0].keys[0].title
////								message =
////									CommonService.pushConfig!!.scene.app_uninstalled.messages[0].keys[0].content
////							}
////
////							currentLocale.isKorean() -> {
////								title =
////									CommonService.pushConfig!!.scene.app_uninstalled.messages[0].keys[2].title
////								message =
////									CommonService.pushConfig!!.scene.app_uninstalled.messages[0].keys[2].content
////							}
////
////							else -> {
////								title =
////									CommonService.pushConfig!!.scene.app_uninstalled.messages[0].title
////								message =
////									CommonService.pushConfig!!.scene.app_uninstalled.messages[0].content
////							}
////						}
////						val route = getRoute(CommonService.pushConfig!!.scene.app_uninstalled.messages[0].route)
////						val iconRes = getNotificationIcon(route)
////						sendTemporaryNotification(
////							"app_uninstalled",
////							iconRes,
////							title,
////							message,
////							R.layout.a_temp_notification,
////							R.layout.a_temp_notification_big,
////							route
////						)
////					}
////					// 应用被卸载
////
////				}
////			}
//		}
//	}
//	private val batteryReceiver = object:  BroadcastReceiver() {
//		private val logParams = mutableMapOf<String, Any>()
//		private val serviceScope = CoroutineScope(Dispatchers.Main)
//
//		override fun onReceive(context: Context?, intent: Intent?) {
//			if (AdConfig.isNewPush) return
//
////			if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
////				AdLoader.loadOpen(this@CommonService)
////
////			}
////			if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
////				AdLoader.loadInter(this@CommonService)
////
////			}
////			when (intent?.action) {
////				Intent.ACTION_BATTERY_CHANGED -> {
//////					val newBatteryLevel = getBatteryLevel(intent)
//////					if (newBatteryLevel != batteryLevel) {
//////						batteryLevel = newBatteryLevel
//////						updateNotification()
//////					}
////				}
////
////				Intent.ACTION_POWER_CONNECTED -> {
////					logParams.clear()
////					logParams.put("scene","charging_start")
//////					LogUtil.log("notification_app_sent",logParams)
////					if (canSendTemporaryNotification(context ,Intent.ACTION_POWER_CONNECTED)) {
////						logParams.clear()
////						logParams.put("scene","charging_start")
////						LogUtil.log("notification_app_shown",logParams)
////						var title = ""
////						var message = ""
////						val currentLocale = Locale.getDefault()
////						when {
////							currentLocale.isFrench() -> {
////								title =
////									pushConfig!!.scene.charging_start.messages[0].keys[1].title
////								message =
////									pushConfig!!.scene.charging_start.messages[0].keys[1].content
////							}
////
////							currentLocale.isSpanish() -> {
////								title =
////									pushConfig!!.scene.charging_start.messages[0].keys[0].title
////								message =
////									pushConfig!!.scene.charging_start.messages[0].keys[0].content
////							}
////
////							currentLocale.isKorean() -> {
////								title =
////									pushConfig!!.scene.charging_start.messages[0].keys[2].title
////								message =
////									pushConfig!!.scene.charging_start.messages[0].keys[2].content
////							}
////
////							else -> {
////								title =
////									pushConfig!!.scene.charging_start.messages[0].title
////								message =
////									pushConfig!!.scene.charging_start.messages[0].content
////							}
////						}
////						val route = getRoute(pushConfig!!.scene.charging_start.messages[0].route)
////						val iconRes = getNotificationIcon(route)
////						sendTemporaryNotification(
////							"charging_start",
////							iconRes,
////							title,
////							message,
////							R.layout.a_temp_power_notification,
////							R.layout.a_temp_power_big_notification,
////							route
////						)
////
////					}
////
////				}
////
////				Intent.ACTION_POWER_DISCONNECTED -> {
////					logParams.clear()
////					logParams.put("scene","charging_end")
//////					LogUtil.log("notification_app_sent",logParams)
////					if (canSendTemporaryNotification(context ,Intent.ACTION_POWER_DISCONNECTED)) {
////						logParams.clear()
////						logParams.put("scene","charging_end")
////						LogUtil.log("notification_app_shown",logParams)
////						var title = ""
////						var message = ""
////						val currentLocale = Locale.getDefault()
////						when {
////							currentLocale.isFrench() -> {
////								title =
////									pushConfig!!.scene.charging_end.messages[0].keys[1].title
////								message =
////									pushConfig!!.scene.charging_end.messages[0].keys[1].content
////							}
////
////							currentLocale.isSpanish() -> {
////								title =
////									pushConfig!!.scene.charging_end.messages[0].keys[0].title
////								message =
////									pushConfig!!.scene.charging_end.messages[0].keys[0].content
////							}
////
////							currentLocale.isKorean() -> {
////								title =
////									pushConfig!!.scene.charging_end.messages[0].keys[2].title
////								message =
////									pushConfig!!.scene.charging_end.messages[0].keys[2].content
////							}
////
////							else -> {
////								title = pushConfig!!.scene.charging_end.messages[0].title
////								message =
////									pushConfig!!.scene.charging_end.messages[0].content
////							}
////						}
////						val route = getRoute(pushConfig!!.scene.charging_end.messages[0].route)
////						val iconRes = getNotificationIcon(route)
////						sendTemporaryNotification(
////							"charging_end",
////							iconRes,
////							title,
////							message,
////							R.layout.a_temp_power_notification,
////							R.layout.a_temp_power_big_notification,
////							route
////						)
////					}
////				}
////			}
//		}
//
//		private fun getBatteryLevel(batteryStatus: Intent?): Int {
//			val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
//			val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
//			if (level != null) {
//				if (scale != null) {
//					return if (level >= 0 && scale > 0) {
//						(level * 100 / scale.toFloat()).toInt()
//					} else {
//						-1
//					}
//				}
//			}
//
//			return 0
//		}
//	}
//
//    @SuppressLint("UnspecifiedRegisterReceiverFlag")
//    private fun registerAllReceivers() {
//        // 注册电池变化广播
//        val batteryFilter = IntentFilter().apply {
//            addAction(Intent.ACTION_BATTERY_CHANGED)
//            addAction(Intent.ACTION_POWER_CONNECTED)
//            addAction(Intent.ACTION_POWER_DISCONNECTED)
//        }
//
//        // 注册屏幕状态广播
//        val screenFilter = IntentFilter().apply {
//            addAction(Intent.ACTION_SCREEN_ON)
//            addAction(Intent.ACTION_USER_PRESENT)
//        }
//
//        // 创建IntentFilter
//        val appFilter = IntentFilter().apply {
//            addAction(Intent.ACTION_PACKAGE_ADDED)
//            addAction(Intent.ACTION_PACKAGE_REMOVED)
//            addDataScheme("package")
//        }
//
//        // 使用版本兼容的方式注册接收器
//        when {
//            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
//                registerReceiver(batteryReceiver, batteryFilter, RECEIVER_EXPORTED)
//                registerReceiver(screenReceiver, screenFilter, RECEIVER_EXPORTED)
//                registerReceiver(inStallReceiver, appFilter, RECEIVER_EXPORTED)
//            }
//
//            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
//                registerReceiver(batteryReceiver, batteryFilter)
//                registerReceiver(screenReceiver, screenFilter)
//                registerReceiver(inStallReceiver, appFilter)
//            }
//
//            else -> {
//                registerReceiver(batteryReceiver, batteryFilter)
//                registerReceiver(screenReceiver, screenFilter)
//                registerReceiver(inStallReceiver, appFilter)
//            }
//        }
//    }
//	var temp_notificationIds: MutableList<Int> = mutableListOf()
//
//	var batteryLevel = -1
//
//	var sendPowerConnectedTime: Long by PreferenceDelegate("sendPowerConnectedTime", 0L)
//	var sendPowerDisConnectedTime: Long by PreferenceDelegate("sendPowerDisConnectedTime", 0L)
//	var sendScreenOnTime: Long by PreferenceDelegate("sendScreenOnTime", 0L)
//	var sendUserPresentTime: Long by PreferenceDelegate("sendUserPresentTime", 0L)
//
//	var sendPackageAddTime: Long by PreferenceDelegate("sendPackageAddTime", 0L)
//
//	var sendPackageRemoveTime: Long by PreferenceDelegate("sendPackageRemoveTime", 0L)
//
//	var deletePhotosTime: Long by PreferenceDelegate("deletePhotosTime", 0L)
//	var deleteVideosTime: Long by PreferenceDelegate("deleteVideosTime", 0L)
//	var deleteFilesTime: Long by PreferenceDelegate("deleteFilesTime", 0L)
//
//	fun canSendTemporaryNotification(context: Context?, action: String): Boolean {
//		if (pushConfig == null) {
//			val config = PreferenceUtil.getString("Contextualized_Push", "")
//			pushConfig = Gson().fromJson(config, PushConfig::class.java)
//		}
//		if(pushConfig ==null){
//			context?.let {
//				pushConfig =parsePushConfigFromRaw(it)
//			}
//		}
//		val currentTimeMillis = System.currentTimeMillis()
//		if (currentTimeMillis < (AdvCheckManager.params.installTime + (pushConfig?.first_trigger_time ?: 0L) * 1000) ) {
//			Log.e(TAG, "canSendTemporaryNotification: 1 currentTimeMillis = $currentTimeMillis installTime = ${AdvCheckManager.params.installTime} first_trigger_time = ${pushConfig?.first_trigger_time} judge = ${(AdvCheckManager.params.installTime + (pushConfig?.first_trigger_time ?: 0L) * 1000)}" )
//			return false
//		}else{
//			when (action) {
//				Intent.ACTION_POWER_CONNECTED -> {
//					if (currentTimeMillis - sendPowerConnectedTime < pushConfig!!.scene.charging_start.trigger_interval* 1000 || !pushConfig!!.scene.charging_start.enabled) {
//						Log.e(TAG, "canSendTemporaryNotification: 2" )
//						return false
//					}else{
//						sendPowerConnectedTime = currentTimeMillis
//						Log.e(TAG, "canSendTemporaryNotification: 3" )
//						return true
//					}
//				}
//				Intent.ACTION_POWER_DISCONNECTED -> {
//					if (currentTimeMillis - sendPowerDisConnectedTime < pushConfig!!.scene.charging_end.trigger_interval* 1000 || !pushConfig!!.scene.charging_end.enabled) {
//						Log.e(TAG, "canSendTemporaryNotification: 4" )
//						return false
//					}else{
//						sendPowerDisConnectedTime = currentTimeMillis
//						Log.e(TAG, "canSendTemporaryNotification: 5" )
//						return true
//					}
//
//				}
//				Intent.ACTION_SCREEN_ON -> {
//					Log.e(TAG, "canSendTemporaryNotification: 6" )
//					return !(currentTimeMillis - sendScreenOnTime < pushConfig!!.scene.screen_on_5s.trigger_interval* 1000 || !pushConfig!!.scene.screen_on_5s.enabled)
//				}
//				Intent.ACTION_USER_PRESENT -> {
//					if (currentTimeMillis - sendUserPresentTime < pushConfig!!.scene.phone_unlock.trigger_interval* 1000 || !pushConfig!!.scene.phone_unlock.enabled) {
//						Log.e(TAG, "canSendTemporaryNotification: 7" )
//						return false
//					}else{
//						sendUserPresentTime = currentTimeMillis
//						Log.e(TAG, "canSendTemporaryNotification: 8" )
//						return true
//					}
//				}
//				Intent.ACTION_PACKAGE_ADDED -> {
//					if (currentTimeMillis - sendPackageAddTime < pushConfig!!.scene.app_installed.trigger_interval* 1000 || !pushConfig!!.scene.app_installed.enabled) {
//						Log.e(TAG, "canSendTemporaryNotification: 9" )
//						return false
//					}else{
//						sendPackageAddTime = currentTimeMillis
//						Log.e(TAG, "canSendTemporaryNotification: 10" )
//						return true
//					}
//
//				}
//
//				Intent.ACTION_PACKAGE_REMOVED -> {
//					if (currentTimeMillis - sendPackageRemoveTime < pushConfig!!.scene.app_uninstalled.trigger_interval* 1000 || !pushConfig!!.scene.app_uninstalled.enabled) {
//						Log.e(TAG, "canSendTemporaryNotification: 11" )
//						return false
//					}else{
//						sendPackageRemoveTime = currentTimeMillis
//						Log.e(TAG, "canSendTemporaryNotification: 12" )
//						return true
//					}
//				}
//			}
//		}
//		return true
//	}
//
//	fun parsePushConfigFromRaw(context: Context): PushConfig? {
//		return try {
//			val inputStream = context.resources.openRawResource(R.raw.push)
//			val jsonString = inputStream.bufferedReader().use { it.readText() }
//			val gson = Gson()
//			gson.fromJson(jsonString, PushConfig::class.java)
//		} catch (e: Exception) {
//			e.printStackTrace()
//			null
//		}
//	}
//	@SuppressLint("RemoteViewLayout")
//	fun sendTemporaryNotification(
//		scene: String = "",
//		res:Int,
//		title: String,
//		message: String,
//		layoutId: Int,
//		bigLayoutId: Int,
//		route: String
//	) {
//		try {
//			val id= System.currentTimeMillis().toInt()
//			// 构建小视图
//			val remoteViews = RemoteViews(Config.packageName, layoutId)
//			remoteViews.setImageViewResource(R.id.iv_push,res)
//			// 构建大视图
//			val bigRemoteViews = RemoteViews(Config.packageName, bigLayoutId)
//			bigRemoteViews.setImageViewResource(R.id.iv_push,res)
//			remoteViews.setTextViewText(R.id.tv_detail, title)
//			bigRemoteViews.setTextViewText(R.id.tv_title, title)
//			bigRemoteViews.setTextViewText(R.id.tv_message, message)
//			val notification =
//				NotificationCompat.Builder(this@CommonService, NotificationChannelManager.NORMAL_CHANNEL_ID)
//					.setContentTitle(title)
//					.setSmallIcon(R.drawable.a_nlogo)
//					.setCustomContentView(remoteViews)
//					.setCustomBigContentView(bigRemoteViews)
//					.setContentIntent(
//						getAppPendingIntent(route,scene)
//					)
//					.setAutoCancel(false)
//					.setPriority(NotificationCompat.PRIORITY_HIGH)
//					.setCategory(NotificationCompat.CATEGORY_SERVICE)
//					.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//					.setOngoing(false)
//					.setOnlyAlertOnce(false)
//					.setShowWhen(true)
//					.setWhen(System.currentTimeMillis())
//					.build()
//			val notificationManager = getSystemService(NotificationManager::class.java)
//			notificationManager.notify(id, notification)
//			temp_notificationIds.add(id)
//		} catch (e: Exception) {
//
//		}
//	}
//
//	private val NOTIFICATION_New_ID = 2002
//	private val Notify_ID = "notify_id_common_notify"
//
//	private val contentUris = listOf(
//		MediaStore.Downloads.EXTERNAL_CONTENT_URI,
//		MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//		MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//		MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//		MediaStore.Files.getContentUri("external")
//	)
//	private val observerMap = mutableMapOf<Uri, ContentObserver>()
//	private val handleSet = HashSet<String>()
//
//	fun registerContentObservers() {
//		contentUris.forEach { uri ->
//			val handler = Handler(Looper.getMainLooper())
//			val observer = createObserverForUri(uri, handler)
//			contentResolver.registerContentObserver(
//				uri,
//				true,
//				observer
//			)
//
//			observerMap[uri] = observer
//		}
//	}
//
//	fun unregisterContentObservers() {
//		observerMap.values.forEach { observer ->
//			contentResolver.unregisterContentObserver(observer)
//		}
//		observerMap.clear()
//	}
//
//	private fun createObserverForUri(uri: Uri, handler: Handler): ContentObserver {
//		return object : ContentObserver(handler) {
//			override fun onChange(selfChange: Boolean, changedUri: Uri?) {
//				super.onChange(selfChange, changedUri)
//				if (!selfChange) {
//					handleFileChange(uri, changedUri)
//				}
//			}
//		}
//	}
//
//	private fun handleFileChange(rootUri: Uri, changedUri: Uri?) {
//		// 1. 过滤安装后10分钟内的变化
//		if (System.currentTimeMillis() - AdvCheckManager.params.installTime < 1000 * 60 * 10) {
//			return
//		}
//		// 获取实际变化的文件URI
//		changedUri?.let {
//			// fileType = photo ,video, file
//			val fileType = when (rootUri) {
//				MediaStore.Images.Media.EXTERNAL_CONTENT_URI -> "photo"
//				MediaStore.Video.Media.EXTERNAL_CONTENT_URI -> "video"
//				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI -> "file"
//				MediaStore.Downloads.EXTERNAL_CONTENT_URI -> "file"
//				MediaStore.Files.getContentUri("external") -> "file"
//				else -> "unknow"
//			}
//
//			val fileName = getFileNameFromUri(it) ?: "UnknowFile"
//			if (handleSet.contains(fileName)) return
//			handleSet.add(fileName)
//			Log.e("FileObserver", "检测到文件操作: $fileName , $rootUri, $changedUri")
//
//			val fileExists = isFileExists(it)
//			Log.e("TAG", "handleFileChange: $fileExists", )
//			if (!fileExists && !fileName.startsWith("_")) {
//				sendDeletionNotification(fileType,fileName)
//				return
//			}
//		}
//	}
//
//	private fun getFileNameFromUri(uri: Uri): String? {
//		return try {
//			contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
//				if (cursor.moveToFirst()) {
//					cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
//				} else {
//					null
//				}
//			}
//		} catch (e: Exception) {
//			Log.e("FileObserver", "获取文件名失败", e)
//			null
//		}
//	}
//
//	private fun getFileNameFromPath(uri: Uri): String? {
//		return try {
//			uri.lastPathSegment?.substringAfterLast("/") // 从路径中截取文件名
//		} catch (e: Exception) {
//			null
//		}
//	}
//
//	private fun getFileNameByMediaId(mediaId: String): String? {
//		return try {
//			contentResolver.query(
//				MediaStore.Files.getContentUri("external"),
//				arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
//				"${MediaStore.MediaColumns._ID} = ?",
//				arrayOf(mediaId),
//				null
//			)?.use { cursor ->
//				if (cursor.moveToFirst()) cursor.getString(0) else null
//			}
//		} catch (e: Exception) {
//			null
//		}
//	}
//
//	// 检查文件是否存在
//	private fun isFileExists(uri: Uri): Boolean {
//		return try {
//			// 方法1：直接尝试访问文件
//			contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false
//		} catch (e: Exception) {
//			// 方法2：检查文件路径是否存在（兼容低版本）
////			val path = getPathFromUri(uri)
////			path.isNotEmpty() && File(path).exists()
//			true
//		}
//	}
//
//	// 新增方法：从 URI 获取真实路径（需要 API 兼容性处理）
//	private fun getPathFromUri(uri: Uri): String {
//		return when {
//			Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
//				val cursor = contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.RELATIVE_PATH), null, null)
//				cursor?.use {
//					if (it.moveToFirst()) {
//						Environment.DIRECTORY_DOWNLOADS + "/" + it.getString(0)
//					} else ""
//				} ?: ""
//			}
//			else -> {
//				val cursor = contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null)
//				cursor?.use {
//					if (it.moveToFirst()) {
//						it.getString(0)
//					} else ""
//				} ?: ""
//			}
//		}
//	}
//
//	fun canSendDeletionTemporaryNotification( fileType: String): Boolean {
//		if (BuildConfig.DEBUG) return true
//		if (pushConfig == null) {
//			val config = PreferenceUtil.getString("Contextualized_Push", "")
//			pushConfig = Gson().fromJson(config, PushConfig::class.java)
//		}
//		if(pushConfig ==null){
//			pushConfig = parsePushConfigFromRaw(this@CommonService)
//		}
//		val currentTimeMillis = System.currentTimeMillis()
//		if (currentTimeMillis < (AdvCheckManager.params.installTime + (pushConfig?.first_trigger_time ?: 0L)).times(1000) ) {
//			return false
//		}else{
//			if (fileType == "photo") {
//				if (currentTimeMillis - deletePhotosTime < pushConfig!!.scene.delete_photos.trigger_interval* 1000 || !pushConfig!!.scene.delete_photos.enabled) {
//					return false
//				}else{
//					deletePhotosTime = currentTimeMillis
//					return true
//				}
//			}
//			if (fileType == "video") {
//				if (currentTimeMillis - deleteVideosTime < pushConfig!!.scene.delete_videos.trigger_interval* 1000 || !pushConfig!!.scene.delete_videos.enabled) {
//					return false
//				}else{
//					deleteVideosTime = currentTimeMillis
//					return true
//				}
//			}
//			if (fileType == "file") {
//				if (currentTimeMillis - deleteFilesTime < pushConfig!!.scene.delete_files.trigger_interval* 1000 || !pushConfig!!.scene.delete_files.enabled) {
//					return false
//				}else{
//					deleteFilesTime = currentTimeMillis
//					return true
//				}
//			}
//			return false
//		}
//	}
//
//	//发送删除通知
//	// fileType = photo ,video, file
//	private fun sendDeletionNotification(fileType: String, fileName: String) {
//		// desc: 判断是否有通知栏权限
//		if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
//			AdLoader.loadOpen(this@CommonService)
//		}
//		if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
//			AdLoader.loadInter(this@CommonService)
//		}
//		Log.e("TAG", "sendDeletionNotification: $fileName", )
//
//		val logParams = mutableMapOf<String, Any>()
//		val scene = when(fileType){
//			"photo" -> "delete_photos"
//			"video" -> "delete_videos"
//			"file" -> "delete_files"
//			else -> ""
//		}
//		logParams["scene"] = scene
////		LogUtil.log("notification_app_sent",logParams)
//		if (!canSendDeletionTemporaryNotification(fileType)) return
//		val notifyChannel = NotificationChannel(
//			Notify_ID,
//			"File Deleted",
//			NotificationManager.IMPORTANCE_HIGH
//		)
//		notifyChannel.description = "File deletion notification"
//		val manager = getSystemService(NotificationManager::class.java)
//		manager.createNotificationChannel(notifyChannel)
//
//		var title = pushConfig!!.scene.delete_photos.messages[0].title ?: "Oops! Deleted a selfie?"
//		var content = pushConfig!!.scene.delete_photos.messages[0].content ?: "It happens! We can get it back. Tap here for a quick recovery."
//		var route = getRoute(pushConfig!!.scene.delete_photos.messages[0].route)
//		var iconRes = getNotificationIcon(route)
//		val currentLocale = Locale.getDefault()
//
//		if (fileType == "photo") {
//			route = getRoute(pushConfig!!.scene.delete_photos.messages[0].route)
//			iconRes = getNotificationIcon(route)
//			when {
//				currentLocale.isFrench() -> {
//					title = pushConfig!!.scene.delete_photos.messages[0].keys[1].title
//					content = pushConfig!!.scene.delete_photos.messages[0].keys[1].content
//				}
//
//				currentLocale.isSpanish() -> {
//					title = pushConfig!!.scene.delete_photos.messages[0].keys[0].title
//					content = pushConfig!!.scene.delete_photos.messages[0].keys[0].content
//				}
//
//				currentLocale.isKorean() -> {
//					title = pushConfig!!.scene.delete_photos.messages[0].keys[2].title
//					content = pushConfig!!.scene.delete_photos.messages[0].keys[2].content
//				}
//
//				else -> {
//					title = pushConfig!!.scene.delete_photos.messages[0].title
//					content = pushConfig!!.scene.delete_photos.messages[0].content
//				}
//			}
//		}
//		if (fileType == "video") {
//			route = getRoute(pushConfig!!.scene.delete_videos.messages[0].route)
//			iconRes = getNotificationIcon(route)
//			when {
//				currentLocale.isFrench() -> {
//					title = pushConfig!!.scene.delete_videos.messages[0].keys[1].title
//					content = pushConfig!!.scene.delete_videos.messages[0].keys[1].content
//				}
//
//				currentLocale.isSpanish() -> {
//					title = pushConfig!!.scene.delete_videos.messages[0].keys[0].title
//					content = pushConfig!!.scene.delete_videos.messages[0].keys[0].content
//				}
//
//				currentLocale.isKorean() -> {
//					title = pushConfig!!.scene.delete_videos.messages[0].keys[2].title
//					content = pushConfig!!.scene.delete_videos.messages[0].keys[2].content
//				}
//
//				else -> {
//					title = pushConfig!!.scene.delete_videos.messages[0].title
//					content = pushConfig!!.scene.delete_videos.messages[0].content
//				}
//			}
//		}
//		if (fileType == "file") {
//			route = getRoute(pushConfig!!.scene.delete_files.messages[0].route)
//			iconRes = getNotificationIcon(route)
//			when {
//				currentLocale.isFrench() -> {
//					title = pushConfig!!.scene.delete_files.messages[0].keys[1].title
//					content = pushConfig!!.scene.delete_files.messages[0].keys[1].content
//				}
//
//				currentLocale.isSpanish() -> {
//					title = pushConfig!!.scene.delete_files.messages[0].keys[0].title
//					content = pushConfig!!.scene.delete_files.messages[0].keys[0].content
//				}
//
//				currentLocale.isKorean() -> {
//					title = pushConfig!!.scene.delete_files.messages[0].keys[2].title
//					content = pushConfig!!.scene.delete_files.messages[0].keys[2].content
//				}
//
//				else -> {
//					title = pushConfig!!.scene.delete_files.messages[0].title
//					content = pushConfig!!.scene.delete_files.messages[0].content
//				}
//			}
//		}
//		if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
//			if (BuildConfig.DEBUG) {
//				Log.e(TAG, "sendNotification: no notification permission")
//			}
//			LogUtil.log("notification_app_shown",logParams)
//		}
//		sendTemporaryNotification(
//			scene,
//			iconRes,
//			title,
//			content,
//			R.layout.a_temp_notification,
//			R.layout.a_temp_notification_big,
//			route
//		)
//	}
//
//}
//
//fun Locale.isFrench(): Boolean {
//	return this.language.equals("fr", ignoreCase = true)
//}
//
//fun Locale.isSpanish(): Boolean {
//	return this.language.equals("es", ignoreCase = true)
//}
//
//fun Locale.isKorean(): Boolean {
//	return this.language.equals("ko", ignoreCase = true)
//}
//
//fun getRoute(route: String): String {
//	return when (route) {
//		"/recoverPhotos" -> "Photos"
//		"/recoverVideos" -> "Videos"
//		"/recoverFiles" -> "Files"
//		"/recovered" -> "Recovered"
//		else -> ""
//	}
//}
//
//fun getNotificationIcon(route: String): Int {
//	return when (route) {
//		"Photos" -> R.mipmap.ic_push_photos
//		"Videos" -> R.mipmap.ic_push_videos
//		"Files" -> R.mipmap.ic_push_files
//		"Recovered" -> R.mipmap.ic_push_recoverd
//		else -> R.mipmap.ic_push_photos
//	}
//}
