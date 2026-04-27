package com.pdffox.adv.use.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pdffox.adv.use.AdvApplicaiton
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.R
import com.pdffox.adv.use.adv.AdConfig
import com.pdffox.adv.use.adv.AdLoader
import com.pdffox.adv.use.log.LogUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

	override fun onCreate() {
		super.onCreate()
		createNotificationChannel()
	}

	override fun onNewToken(token: String) {
		super.onNewToken(token)
		PushManager.pushToken = token
		Log.e(TAG, "onNewToken: $token")
	}

	override fun onMessageReceived(message: RemoteMessage) {
		super.onMessageReceived(message)

		if (BuildConfig.DEBUG) {
			Toast.makeText(AdvApplicaiton.instance, "收到FCM", Toast.LENGTH_SHORT).show()
		}

		val appOpenFrom = message.data["AppOpenFrom"] ?: "AppOpenFrom"
		val fCMSendTime = message.data["FCMSendTime"] ?: "FCMSendTime"
		val fcmId = message.data["Id"] ?: "fcmId"
		val appPackage = message.data["AppPackage"] ?: "AppPackage"
		val fcmContent = message.data["FcmContent"] ?: "FcmContent"
		val fcmType = message.data["FcmType"] ?: "FcmType"
		val fcmTitle = message.data["FcmTitle"] ?: "FcmTitle"

		if (BuildConfig.DEBUG) {
			Log.e(TAG, "onMessageReceived: " +
					"appOpenFrom = $appOpenFrom, \n " +
					"fCMSendTime = $fCMSendTime, \n " +
					"fcmId = $fcmId, \n " +
					"appPackage = $appPackage, \n " +
					"fcmContent = $fcmContent, \n " +
					"fcmType = $fcmType, \n " +
					"fcmTitle = $fcmTitle, \n "
			)
		}

		LogUtil.log(LogPushData.notification_shown,mapOf(
			"fCMSendTime" to fCMSendTime,
			"fcmId" to fcmId,
			"appPackage" to appPackage,
			"fcmContent" to fcmContent,
			"fcmType" to fcmType,
			"fcmTitle" to fcmTitle
		))

		Handler(Looper.getMainLooper()).post {
			if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
				AdLoader.loadOpen(this@MyFirebaseMessagingService)
			}
			if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
				AdLoader.loadInter(this@MyFirebaseMessagingService)
			}
		}

		// 广播启动前台服务
		sendBroadcast(Intent("com.datatool.photorecovery.ACTION_NOTIFICATION_DELETED"))

		// 直接Intent启动前台服务
		try {
			val serviceIntent = Intent().apply {
				setClassName(packageName, "com.datatool.photorecovery.notification.CommonService")
			}
			ContextCompat.startForegroundService(this, serviceIntent)
		} catch (e: Exception) {
			e.printStackTrace()
		}

		// desc:  10s后通过 JobService 启动 CommonService
		try {
			val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as android.app.job.JobScheduler
			val componentName = android.content.ComponentName(this, ServiceStarterJobService::class.java)
			val jobInfo = android.app.job.JobInfo.Builder(1001, componentName)
				.setMinimumLatency(10 * 1000) // 延迟 10 秒
				.setOverrideDeadline(15 * 1000) // 最晚 15 秒内必须执行
				.setRequiredNetworkType(android.app.job.JobInfo.NETWORK_TYPE_NONE) // 不需要网络
				.build()
			jobScheduler.schedule(jobInfo)
		} catch (e: Exception) {
			e.printStackTrace()
		}

		// desc: 使用wordmanager 启动 CommonService
		try {
			val workRequest = androidx.work.OneTimeWorkRequestBuilder<ServiceWorker>()
				// 设置为加急任务，Android 12+ 会优先执行
				.setExpedited(androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
				.build()
			androidx.work.WorkManager.getInstance(this).enqueue(workRequest)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	@OptIn(DelicateCoroutinesApi::class)
	private fun showNotification(remoteMessage: RemoteMessage) {
		val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		val imageUrl = remoteMessage.notification?.imageUrl.toString()
		val intent = Intent("OPEN_MAIN_ACTIVITY").apply {
			addCategory(Intent.CATEGORY_DEFAULT)
		}
		val pendingIntent = PendingIntent.getActivity(
			this,
			0,
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)
		// 处理接收到的消息
		val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
			.setContentTitle(remoteMessage.notification?.title)
			.setContentText(remoteMessage.notification?.body)
                .setSmallIcon(R.drawable.a_nlogo)
			.setContentIntent(pendingIntent)
			.setVibrate(longArrayOf(1000, 1000, 1000))
			.setLights(Color.RED, 3000, 3000)
			.setAutoCancel(true)

		// 下载并设置大图
		if (imageUrl.isNotEmpty()) {
			GlobalScope.launch(Dispatchers.IO) {
				try {
					val bitmap = Glide.with(applicationContext)
						.asBitmap()
						.load(imageUrl)
						.submit()
						.get()

					if (imageUrl.endsWith("gif") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
						Log.e(TAG, "showNotification: 渲染Gif图", )
						val bigPictureStyle = NotificationCompat.BigPictureStyle()
						val dataImage = imageUrl.trim().replace(" ", "%20")
						val url = URL(dataImage)
						saveTempAnimatedImage(url)?.let { filePath ->
							Log.e(TAG, "showNotification: filePath = $filePath")
							val icon = Icon.createWithContentUri(filePath)
							bigPictureStyle.bigPicture(icon)
							bigPictureStyle.showBigPictureWhenCollapsed(true)
							notificationBuilder.setStyle(bigPictureStyle)
						} ?: {
							bigPictureStyle.bigPicture(bitmap)
							notificationBuilder.setStyle(bigPictureStyle)
						}
					} else {
						val bigPictureStyle = NotificationCompat
							.BigPictureStyle()
							.bigPicture(bitmap)
						notificationBuilder.setStyle(bigPictureStyle)
					}
					notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
				} catch (e: Exception) {
					e.printStackTrace()
					// 如果图片加载失败，显示没有图片的通知
					withContext(Dispatchers.Main) {
						val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
						notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
					}
				}
			}
		} else {
			// 如果没有图片 URL，直接显示通知
			val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
		}
	}

	private suspend fun saveTempAnimatedImage(url: URL): Uri? = withContext(Dispatchers.IO) {
		Log.e(TAG, "saveTempAnimatedImage: $url" )
		try {
			val file = File(cacheDir, "temp_animated_notification.gif")
			val connection = url.openConnection() as HttpURLConnection
			connection.doInput = true
			connection.connect()

			val input = connection.inputStream
			val output = FileOutputStream(file)
			input.copyTo(output)
			output.close()
			input.close()

			Log.e(TAG, "saveTempAnimatedImage: file.name = ${file.name}, file.size = ${file.length()} $packageName" )
			val mUri = FileProvider.getUriForFile(this@MyFirebaseMessagingService, "${packageName}.fileprovider", file)
			Log.e(TAG, "saveTempAnimatedImage: fileUri = $mUri" )
			mUri
		} catch (e: Exception) {
			e.printStackTrace()
			Log.e(TAG, "saveTempAnimatedImage: ", e)
			null
		}
	}

	private fun createNotificationChannel() {
		val channel = NotificationChannel(
			CHANNEL_ID,
			"FCM Notifications",
			NotificationManager.IMPORTANCE_DEFAULT
		).apply {
			description = "Receive FCM notifications"
			enableLights(true)
			lightColor = Color.RED
			enableVibration(true)
			vibrationPattern = longArrayOf(100, 200, 300, 400, 500)
		}
		val notificationManager = getSystemService(NotificationManager::class.java)
		notificationManager.createNotificationChannel(channel)
	}

	companion object {
		private const val TAG = "MyFirebaseMessagingServ"
		private const val CHANNEL_ID = "MyFirebaseMessagingService_CHANNEL"
		private const val NOTIFICATION_ID = 789012
	}

}
