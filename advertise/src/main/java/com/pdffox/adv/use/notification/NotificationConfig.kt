package com.pdffox.adv.use.notification

import kotlinx.serialization.Serializable

@Serializable
data class NotificationConfig(
	val `24HMax`: Int,
	val each_trigger_sent: Int,
	val NMax: Int,
	val has_sound_alert: Boolean,
	val has_vibration: Boolean,
	val is_foreground_send: Boolean,
	val content: String,
	val triggers: List<Trigger>,
	val timer: List<Timer>
)

@Serializable
data class Trigger(
	val id: String,
	val name: String,
	val offset_second: Long? = null,
	val delay: Long? = null,
	val interval_second: Long? = null,
	val configs: List<Trigger>? = null
)

@Serializable
data class Timer(
	val id: String,
	val name: String,
	val HH: Int,
	val MM: Int
)

@Serializable
data class Notice(
	val Id: Int,
	val AppName: String,
	val AppPackage: String,
	val Policy: Int,
	val NoticeId: String,
	val Title: String,
	val Content: String,
	val Button: String,
	val Icon: String,
	val Img: String,
	val Languages: String, // 这里先用String，后面可以再解析
	val Route: String
)

@Serializable
data class LanguageKey(
	val language: String,
	val title: String,
	val content: String,
	val img: String,
	val button: String
)

@Serializable
data class Languages(
	val keys: List<LanguageKey>
)