package com.pdffox.adv.notification

import java.util.Locale

internal data class PushMessageText(
	val title: String,
	val content: String,
	val route: String,
)

internal object PushSceneResolver {
	fun scene(config: PushConfig?, sceneKey: String?): PushScene? {
		val key = sceneKey?.takeIf { it.isNotBlank() } ?: return null
		return config?.scene?.get(key)
	}

	fun firstMessageText(
		config: PushConfig?,
		sceneKey: String?,
		locale: Locale = Locale.getDefault(),
	): PushMessageText? {
		val message = scene(config, sceneKey)?.messages?.firstOrNull() ?: return null
		val localized = message.keys.firstOrNull { it.language.equals(locale.language, ignoreCase = true) }
		return PushMessageText(
			title = localized?.title?.takeIf { it.isNotBlank() } ?: message.title,
			content = localized?.content?.takeIf { it.isNotBlank() } ?: message.content,
			route = message.route,
		)
	}
}
