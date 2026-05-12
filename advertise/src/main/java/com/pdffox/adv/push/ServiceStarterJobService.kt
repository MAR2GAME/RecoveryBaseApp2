package com.pdffox.adv.push

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import androidx.core.content.ContextCompat
import com.pdffox.adv.Config

@SuppressLint("SpecifyJobSchedulerIdRange")
class ServiceStarterJobService : JobService() {
	override fun onStartJob(params: JobParameters?): Boolean {
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.serviceStarterJobEnabled) {
			return false
		}
		// 这里的代码会在 10s 后执行
		val serviceIntent = PushIntegration.commonServiceIntent(this) ?: return false

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
