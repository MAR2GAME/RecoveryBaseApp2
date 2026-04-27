package com.pdffox.adv.use.push

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class ServiceWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

	override fun doWork(): Result {
		// 创建启动 CommonService 的 Intent
		val serviceIntent = Intent().apply {
			setClassName(applicationContext.packageName, "com.datatool.photorecovery.notification.CommonService")
		}

		try {
			ContextCompat.startForegroundService(applicationContext, serviceIntent)
			return Result.success()
		} catch (e: Exception) {
			e.printStackTrace()
			return Result.failure()
		}
	}
}