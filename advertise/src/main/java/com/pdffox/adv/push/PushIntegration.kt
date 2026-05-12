package com.pdffox.adv.push

import android.content.Context
import android.content.Intent
import com.pdffox.adv.Ads
import com.pdffox.adv.Config
import com.pdffox.adv.notification.CommonService

object PushIntegration {
	fun commonServiceIntent(context: Context): Intent? {
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.persistentServiceEnabled) {
			return null
		}
		val className = Config.sdkConfig.push.commonServiceClassName?.takeIf { it.isNotBlank() }
		return if (className == null) {
			CommonService.intent(context)
		} else {
			Intent().apply {
				setClassName(context.packageName, className)
			}
		}
	}

	fun notificationDeletedAction(context: Context): String {
		return PushHostContract.notificationDeletedAction(
			packageName = context.packageName,
			configuredAction = Config.sdkConfig.push.notificationDeletedAction,
		)
	}

	fun fileProviderAuthority(context: Context): String {
		return PushHostContract.fileProviderAuthority(
			packageName = context.packageName,
			configuredAuthority = Config.sdkConfig.push.fileProviderAuthority,
		)
	}

	fun appLaunchIntent(
		context: Context,
		appOpenFrom: String,
		route: String = "",
		scene: String = "",
	): Intent? {
		return Ads.createLaunchIntent(context, appOpenFrom, route, scene)
	}
}
