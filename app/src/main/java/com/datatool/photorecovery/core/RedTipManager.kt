package com.datatool.photorecovery.core

import com.pdffox.adv.AdvertiseSdk
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.datatool.photorecovery.BuildConfig
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
			AdvertiseSdk.putPreferenceString(PREF_KEY, json)
		}
		get() {
			val strRecords = AdvertiseSdk.getPreferenceString(PREF_KEY, "")
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
		val json = AdvertiseSdk.getPreferenceString(PREF_KEY, "")
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