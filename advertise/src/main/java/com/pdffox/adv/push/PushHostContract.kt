package com.pdffox.adv.push

object PushHostContract {
	private const val DEFAULT_FILE_PROVIDER_SUFFIX = ".pdffox.adv.fileprovider"
	const val DEFAULT_COMMON_SERVICE_CLASS_NAME = "com.pdffox.adv.notification.CommonService"

	fun notificationDeletedAction(packageName: String, configuredAction: String?): String {
		return configuredAction?.takeIf { it.isNotBlank() }
			?: "$packageName.ACTION_NOTIFICATION_DELETED"
	}

	fun fileProviderAuthority(packageName: String, configuredAuthority: String?): String {
		return configuredAuthority?.takeIf { it.isNotBlank() }
			?: packageName + DEFAULT_FILE_PROVIDER_SUFFIX
	}
}
