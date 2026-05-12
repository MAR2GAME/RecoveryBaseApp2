package com.pdffox.adv

import android.app.Activity
import android.app.Application

open class AdvApplicaiton : Application() {
	companion object {
		val instance: Application
			get() = AdvRuntime.application

		val currentActivity: Activity?
			get() = AdvRuntime.currentActivity
	}

	val currentActivity: Activity?
		get() = AdvRuntime.currentActivity

	val startAppTime: Long
		get() = AdvRuntime.startAppTime

	override fun onCreate() {
		super.onCreate()
		AdvRuntime.init(this)
	}

	fun initConfiguredIntegrations() {
		AdvRuntime.initConfiguredIntegrations()
	}
}
