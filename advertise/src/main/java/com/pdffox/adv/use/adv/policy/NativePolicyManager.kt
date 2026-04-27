package com.pdffox.adv.use.adv.policy

import android.util.Log
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.use.adv.policy.data.AdNativePolicy
import com.pdffox.adv.use.adv.policy.data.AdUnit
import kotlinx.serialization.json.Json

object NativePolicyManager {

	private const val TAG = "NativePolicyManager"
	var adPolicy: AdNativePolicy? = null
	
	fun setPolicyFromJson(jsonString: String) {
		if (jsonString == "") {
			return
		}
		if (jsonString.isBlank()) {
			return
		}
		val adPolicy = Json.decodeFromString<AdNativePolicy>(jsonString)
		this.adPolicy = adPolicy
	}

	fun getAdUnit(areakey: String): AdUnit? {
		return adPolicy?.ad_units?.find { it.areakey == areakey }
	}

	fun checkAdUnit(areakey: String): Boolean {
		if (areakey == "debug_page") {
			return true
		}
		val adUnit = getAdUnit(areakey)
		val result = checkAdUnit(adUnit)
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "checkAdUnit: $areakey adUnit = $adUnit result = $result" )
		}
		return result
	}

	fun checkAdUnit(adUnit: AdUnit?): Boolean {
		if (adUnit == null) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "checkAdUnit: adUnit is null" )
			}
			return false
		}
		return checkLimit() &&
				checkAdUnitsFrequency(adUnit) &&
				checkCheckAdUnitRate(adUnit)
	}

	// 全局播放广告数限制
	fun checkLimit(): Boolean {
		val limited = adPolicy?.limited ?: 50
		val limitedLoadtimeSeconds = adPolicy?.limited_loadtime_seconds ?: 86400
		if (System.currentTimeMillis() - NativeAdPlayRecordManager.lastLimitedTime < limitedLoadtimeSeconds * 1000) {
			return false
		}
		val playCount = NativeAdPlayRecordManager.getAllPlayTime(NativeAdPlayRecordManager.lastLimitedTime)
		if (playCount >= limited) {
			NativeAdPlayRecordManager.lastLimitedTime = System.currentTimeMillis()
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "checkLimit: playCount = $playCount limited = $limited")
			}
			return false
		}
		return true
	}

	// 广告位频率限制
	fun checkAdUnitsFrequency(adUnit: AdUnit): Boolean {
		val frequencyCaps = adUnit.frequency_caps
		val maxPerHour = frequencyCaps.max_per_hour
		val maxPerDay = frequencyCaps.max_per_day
		val intervalSeconds = frequencyCaps.interval_seconds

		val lastTime = NativeAdPlayRecordManager.getAdLastPlayTime(adUnit.areakey)
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "checkAdUnitsFrequency: areakey = ${adUnit.areakey} lastTime = $lastTime")
		}
		// 检查广告位是否在最近的时间间隔内播放过
		if (lastTime > 0 && System.currentTimeMillis() - lastTime < intervalSeconds * 1000) {
			return false
		}

		// 检查广告位在最近的时间间隔内是否播放次数超过限制
		val playCount = NativeAdPlayRecordManager.getAdPlayCount(adUnit.areakey, 60 * 60)
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "checkAdUnitsFrequency: areakey = ${adUnit.areakey} playCount = $playCount maxPerHour = $maxPerHour")
		}
		if (playCount >= maxPerHour) {
			return false
		}

		// 检查广告位在最近的24小时内是否播放次数超过限制
		val playCount24h = NativeAdPlayRecordManager.getAdPlayCount(adUnit.areakey, 24 * 60 * 60)
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "checkAdUnitsFrequency: areakey = ${adUnit.areakey} playCount24h = $playCount24h maxPerDay = $maxPerDay")
		}
		if (playCount24h >= maxPerDay) {
			return false
		}

		return true
	}

	// 广告位概率限制
	fun checkCheckAdUnitRate(adUnit: AdUnit): Boolean {
		val rate = adUnit.rate
		val randomValue = kotlin.random.Random.nextDouble(0.0, 1.0)
		return randomValue <= rate
	}

}