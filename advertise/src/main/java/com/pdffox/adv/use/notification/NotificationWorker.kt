package com.pdffox.adv.use.notification
import androidx.work.*
import java.util.concurrent.TimeUnit
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationManagerCompat
import com.pdffox.adv.use.AdvApplicaiton
import com.pdffox.adv.use.log.LogUtil
import com.pdffox.adv.use.notification.NotificationManager.notificationConfig

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
	override fun doWork(): Result {
		val notificationType = inputData.getString("notificationType") ?: return Result.failure()
		// 这里调用你的通知发送逻辑
		val logParams = mutableMapOf<String, Any>()
		logParams["scene"] = notificationType
		if (NotificationManagerCompat.from(AdvApplicaiton.instance).areNotificationsEnabled()) {
			LogUtil.log("notification_app_shown",logParams)
		}
		repeat(notificationConfig?.each_trigger_sent ?: 1) { index ->
			Handler(Looper.getMainLooper()).postDelayed({
				NotificationManager.sendNotificationDetail(notificationType, notificationType)
			}, index * 3000L) // 每次延迟3秒，index从0开始
		}
		// 重新调度明天同一时间的任务
		scheduleNextWork(notificationType)
		return Result.success()
	}

	private fun scheduleNextWork(notificationType: String) {
		val nextDelay = 24 * 60L // 24小时后执行
		val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
			.setInitialDelay(nextDelay, TimeUnit.MINUTES)
			.setInputData(workDataOf("notificationType" to notificationType))
			.build()
		WorkManager.getInstance(applicationContext).enqueue(workRequest)
	}
}