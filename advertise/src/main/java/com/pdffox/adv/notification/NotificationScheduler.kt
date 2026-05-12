package com.pdffox.adv.notification

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.pdffox.adv.BuildConfig
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

	private const val TAG = "NotificationScheduler"

	fun scheduleDailyNotification(context: Context, notificationType: String, hour: Int, minute: Int) {
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "scheduleDailyNotification: notificationType = $notificationType, hour = $hour, minute = $minute")
		}
		val delay = calculateInitialDelay(hour, minute)
		val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
			.setInitialDelay(delay, TimeUnit.MILLISECONDS)
			.setInputData(workDataOf("notificationType" to notificationType))
			.addTag("daily_notification")
			.build()
		WorkManager.getInstance(context).enqueue(workRequest)
	}

	fun cancelDailyNotification(context: Context) {
		WorkManager.getInstance(context).cancelAllWorkByTag("daily_notification")
	}

	private fun calculateInitialDelay(hour: Int, minute: Int): Long {
		val now = Calendar.getInstance()
		val target = Calendar.getInstance().apply {
			set(Calendar.HOUR_OF_DAY, hour)
			set(Calendar.MINUTE, minute)
			set(Calendar.SECOND, 0)
			set(Calendar.MILLISECOND, 0)
		}
		if (target.before(now)) {
			target.add(Calendar.DAY_OF_YEAR, 1)
		}
		return target.timeInMillis - now.timeInMillis
	}
}