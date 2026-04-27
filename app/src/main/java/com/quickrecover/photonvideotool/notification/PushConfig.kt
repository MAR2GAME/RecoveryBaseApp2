//package com.quickrecover.photonvideotool.notification
//
//import kotlinx.serialization.Serializable
//
//@Serializable
//data class PushConfig(
//    var first_trigger_time: Long,
//    var last_updated: String,
//    var scene: Scene,
//    var version: String,
//)
//
//@Serializable
//data class Scene(
//    var app_installed: AppInstalled,
//    var app_uninstalled: AppUninstalled,
//    var charging_end: ChargingEnd,
//    var charging_start: ChargingStart,
//    var phone_unlock: PhoneUnlock,
//    var screen_on_5s: ScreenOn5s,
//    var delete_photos: DeletePhotos,
//	var delete_videos: DeleteVideos,
//	var delete_files: DeleteFiles,
//)
//
//@Serializable
//data class AppInstalled(
//    var enabled: Boolean,
//    var messages: List<Message>,
//    var trigger_interval: Long
//)
//
//@Serializable
//data class AppUninstalled(
//    var enabled: Boolean,
//    var messages: List<Message>,
//    var trigger_interval: Long
//)
//
//@Serializable
//data class ChargingEnd(
//    var enabled: Boolean,
//    var messages: List<Message>,
//    var trigger_interval: Long
//)
//
//@Serializable
//data class ChargingStart(
//    var enabled: Boolean,
//    var messages: List<Message>,
//    var trigger_interval: Long
//)
//
//@Serializable
//data class PhoneUnlock(
//    var enabled: Boolean,
//    var messages: List<Message>,
//    var trigger_interval: Long
//)
//
//@Serializable
//data class ScreenOn5s(
//    var enabled: Boolean,
//    var messages: List<Message>,
//    var trigger_interval: Long
//)
//
//@Serializable
//data class DeletePhotos(
//    var enabled: Boolean,
//    var messages: List<Message>,
//    var trigger_interval: Long
//)
//
//@Serializable
//data class DeleteVideos(
//    var enabled: Boolean,
//    var messages: List<Message>,
//    var trigger_interval: Long
//)
//
//@Serializable
//data class DeleteFiles(
//    var enabled: Boolean,
//    var messages: List<Message>,
//    var trigger_interval: Long
//)
//
//@Serializable
//data class Message(
//    var content: String,
//    var keys: List<Key>,
//    var route: String,
//    var title: String
//)
//
//@Serializable
//data class Key(
//    var content: String,
//    var language: String,
//    var title: String
//)