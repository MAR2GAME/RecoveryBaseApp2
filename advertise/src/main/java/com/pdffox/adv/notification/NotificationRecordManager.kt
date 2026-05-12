package com.pdffox.adv.notification

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.adv.policy.AdPlayRecordManager
import com.pdffox.adv.adv.policy.AdPlayRecordManager.ad_records
import com.pdffox.adv.adv.policy.data.AdRecord
import com.pdffox.adv.util.PreferenceUtil
import kotlinx.serialization.Serializable

@Serializable
data class NotificationRecord(
	val name: String,
	val timestamp: Long,
)
object NotificationRecordManager {

	private const val TAG = "NotificationRecordManager"
	private const val PREF_KEY = "notification_records_key"

	private val gson = Gson()

	var notification_records: List<NotificationRecord> = emptyList()
		set(value) {
			field = value
			val json = gson.toJson(field)
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "set value: ad_records = $value")
			}
			PreferenceUtil.commitString(PREF_KEY, json)
		}
		get() {
			val strRecords = PreferenceUtil.getString(PREF_KEY, "")
			if (strRecords!!.isNotEmpty()) {
				val type = object : TypeToken<List<NotificationRecord>>() {}.type
				field = gson.fromJson(strRecords, type)
			}
			return field
		}

	init {
		loadFromPreference()
	}

	private fun loadFromPreference() {
		Log.e(TAG, "loadFromPreference: notification_records = $notification_records")
		val json = PreferenceUtil.getString(PREF_KEY, "")
		if (json!!.isNotEmpty()) {
			val type = object : TypeToken<List<NotificationRecord>>() {}.type
			notification_records = gson.fromJson(json, type) ?: emptyList()
		}
	}

	fun addRecord(notificationRecord: NotificationRecord) {
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "addRecord: $notificationRecord")
		}
		notification_records = notification_records + notificationRecord
	}


	fun get24HRecordsCount() : Int {
		return notification_records.count {
			it.timestamp > System.currentTimeMillis() - 24 * 60 * 60 * 1000
		}
	}

	fun getLastRecord(name: String) : NotificationRecord? {
		return notification_records
			.filter { it.name == name }
			.maxByOrNull { it.timestamp }
	}
}