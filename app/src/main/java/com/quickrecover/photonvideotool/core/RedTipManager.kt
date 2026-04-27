package com.quickrecover.photonvideotool.core

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quickrecover.photonvideotool.BuildConfig
// TODO:
import com.quickrecover.photonvideotool.util.PreferenceUtil
// TODO:
//import com.pdffox.adv.BuildConfig
//import com.pdffox.adv.use.util.PreferenceUtil
import kotlinx.serialization.Serializable
import kotlin.collections.plus

@Serializable
data class RedTipRecord(
	val name: String,
	val timeStamp: Long,
)

object RedTipManager {

	private const val TAG = "RedTipManager"
	private const val PREF_KEY = "red_records_key"

	const val RECOVERY_PHOTOS_KEY = "recovery_photos_key"
	const val RECOVERY_VIDEOS_KEY = "recovery_videos_key"
	const val RECOVERY_AUDIO_KEY = "recovery_audio_key"
	const val RECOVERY_FILE_KEY = "recovery_file_key"

	const val RECOVERY_TOOLS_KEY = "recovery_tools_key"

	private val gson = Gson()

	var red_records: List<RedTipRecord> = emptyList()
		set(value) {
			field = value
			val json = gson.toJson(field)
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "set value: ad_records = $value")
			}
			PreferenceUtil.commitString(PREF_KEY, json)
		}
		get() {
			val strRecords = PreferenceUtil.getString(PREF_KEY, "")
			if (strRecords!!.isNotEmpty()) {
				val type = object : TypeToken<List<RedTipRecord>>() {}.type
				field = gson.fromJson(strRecords, type)
			}
			return field
		}

	init {
		loadFromPreference()
	}

	private fun loadFromPreference() {
		Log.e(TAG, "loadFromPreference: red_records = $red_records")
		val json = PreferenceUtil.getString(PREF_KEY, "")
		if (json!!.isNotEmpty()) {
			val type = object : TypeToken<List<RedTipRecord>>() {}.type
			red_records = gson.fromJson(json, type) ?: emptyList()
		}
	}

	fun addRecord(redTipRecord: RedTipRecord) {
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "addRecord: $redTipRecord")
		}
		red_records = red_records + redTipRecord
	}

	fun getLastRecord(name: String) : RedTipRecord? {
		return red_records
			.filter { it.name == name }
			.maxByOrNull { it.timeStamp }
	}

	fun isRedTip(name: String) : Boolean {
		val lastRecord = getLastRecord(name)
		return lastRecord == null || lastRecord.timeStamp < System.currentTimeMillis() - 24 * 60 * 60 * 1000
	}

	fun canShowRedTip(name: String) : Boolean {
		if (name == RECOVERY_TOOLS_KEY) {
			return isRedTip(name)
		}
		if (name != RECOVERY_PHOTOS_KEY && isRedTip(RECOVERY_PHOTOS_KEY)) {
			return false
		}
		return isRedTip(name)
	}

}