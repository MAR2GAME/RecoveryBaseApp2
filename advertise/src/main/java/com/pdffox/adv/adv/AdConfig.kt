package com.pdffox.adv.adv

import android.util.Log
import com.google.gson.Gson
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.util.PreferenceUtil
import org.json.JSONObject

object AdConfig {
	private const val TAG = "AdConfig"
	var isOpenAppOpenHelper = true

	var isNewAdPolicy = true
	var isNewPush = true

	var ignoreGuide = false
	var hasOpenLaungPage = false

	var adload_cache_time: Long = 60 * 60 * 1000
	var adload_retry_num: Int = 1
	var adload_max_time: Long = 6 * 1000
	var adload_poolsize_open: Int = 1
	var adload_poolsize_inter: Int = 1
	var adload_poolsize_native: Int = 3

	val LOAD_TIME_OPEN_APP = "open_app"
	val LOAD_TIME_PLAY_FINISH = "play_finish"
	val LOAD_TIME_ENTER_BACKGROUND = "enter_background"
	val LOAD_TIME_RECEIVE_NOTIFICATION = "receive_notification"

	val LOAD_TIME_ENTER_FEATURE = "enter_features"

	var adload_trigger_timing_open: Map<String, Boolean> = mapOf(
		LOAD_TIME_OPEN_APP to false,
		LOAD_TIME_PLAY_FINISH to true,
		LOAD_TIME_ENTER_BACKGROUND to false,
		LOAD_TIME_RECEIVE_NOTIFICATION to false
	)
	var adload_trigger_timing_inter: Map<String, Boolean> = mapOf(
		LOAD_TIME_OPEN_APP to false,
		LOAD_TIME_PLAY_FINISH to true,
		LOAD_TIME_ENTER_BACKGROUND to false,
		LOAD_TIME_RECEIVE_NOTIFICATION to false,
		LOAD_TIME_ENTER_FEATURE to false
	)

	var adload_trigger_timing_native: Map<String, Boolean> = mapOf(
		LOAD_TIME_OPEN_APP to false,
		LOAD_TIME_PLAY_FINISH to true,
		LOAD_TIME_ENTER_BACKGROUND to false,
		LOAD_TIME_RECEIVE_NOTIFICATION to false,
		LOAD_TIME_ENTER_FEATURE to false
	)

	fun updateConfigFromJson(strJson: String) {
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "updateConfigFromJson: $strJson")
			PreferenceUtil.commitString("updateConfigFromJson", strJson)
		}
		try {
			val jb = JSONObject(strJson)
			adload_cache_time = jb.optLong("adload_cache_time", adload_cache_time) * 1000
			adload_retry_num = jb.optInt("adload_retry_num", adload_retry_num)
			adload_max_time = jb.optLong("adload_max_time", adload_max_time) * 1000
			adload_poolsize_open = jb.optInt("adload_poolsize_open", adload_poolsize_open)
			adload_poolsize_inter = jb.optInt("adload_poolsize_inter", adload_poolsize_inter)
			adload_poolsize_native = jb.optInt("adload_poolsize_native",adload_poolsize_native)
			adload_trigger_timing_open = jb.optJSONObject("adload_trigger_timing_open")?.let {
				Gson().fromJson(it.toString(), Map::class.java) as Map<String, Boolean>
			} ?: emptyMap()
			adload_trigger_timing_inter = jb.optJSONObject("adload_trigger_timing_inter")?.let {
				Gson().fromJson(it.toString(), Map::class.java) as Map<String, Boolean>
			} ?: emptyMap()
			adload_trigger_timing_native = jb.optJSONObject("adload_trigger_timing_native")?.let {
				Gson().fromJson(it.toString(), Map::class.java) as Map<String, Boolean>
			} ?: emptyMap()

		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun canLoadOpen(loadTimeKey: String): Boolean {
		val canLoadOpen = adload_trigger_timing_open[loadTimeKey] ?: false
		Log.e(TAG, "canLoadOpen:$canLoadOpen  $loadTimeKey, ${adload_trigger_timing_open.keys}")
		return canLoadOpen
	}

	fun canLoadInter(loadTimeKey: String): Boolean {
		val canLoadInter = adload_trigger_timing_inter[loadTimeKey] ?: false
		Log.e(TAG, "canLoadInter:$canLoadInter  $loadTimeKey, ${adload_trigger_timing_inter.keys}")
		return canLoadInter
	}

	fun canLoadNative(loadTimeKey: String): Boolean {
		val canLoadNative = adload_trigger_timing_native[loadTimeKey] ?: false
		Log.e(TAG, "canLoadNative:$canLoadNative  $loadTimeKey, ${adload_trigger_timing_native.keys}")
		return canLoadNative
	}
}