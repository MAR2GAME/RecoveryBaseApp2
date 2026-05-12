package com.pdffox.adv.notification

import kotlinx.serialization.Serializable

@Serializable
data class PushConfig(
	var first_trigger_time: Long = 0L,
	var last_updated: String = "",
	var scene: Map<String, PushScene> = emptyMap(),
	var version: String = "",
)

@Serializable
data class PushScene(
	var enabled: Boolean = false,
	var messages: List<Message> = emptyList(),
	var trigger_interval: Long = 0L
)

@Serializable
data class Message(
	var content: String = "",
	var keys: List<Key> = emptyList(),
	var route: String = "",
	var title: String = ""
)

@Serializable
data class Key(
	var content: String = "",
	var language: String = "",
	var title: String = ""
)
