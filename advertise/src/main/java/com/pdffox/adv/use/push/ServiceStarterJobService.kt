package com.pdffox.adv.use.push

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat

@SuppressLint("SpecifyJobSchedulerIdRange")
class ServiceStarterJobService : JobService() {
	override fun onStartJob(params: JobParameters?): Boolean {
		// 这里的代码会在 10s 后执行
		val serviceIntent = Intent().apply {
			// 注意：如果 CommonService 在另一个模块，需要使用全路径类名
			setClassName(packageName, "com.datatool.photorecovery.notification.CommonService")
		}

		try {
			ContextCompat.startForegroundService(this, serviceIntent)
		} catch (e: Exception) {
			e.printStackTrace()
		}

		// 返回 false 表示任务执行完毕
		return false
	}

	override fun onStopJob(params: JobParameters?): Boolean {
		// 如果任务被系统意外终止，返回 true 表示愿意再次尝试
		return true
	}
}