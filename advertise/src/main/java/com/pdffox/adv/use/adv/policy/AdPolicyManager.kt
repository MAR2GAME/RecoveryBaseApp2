package com.pdffox.adv.use.adv.policy

import android.app.Activity
import android.content.Context
import android.os.Process
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pdffox.adv.use.AdvApplicaiton
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.R
import com.pdffox.adv.use.adv.policy.data.AdPolicy
import com.pdffox.adv.use.adv.policy.data.AdUnit
import com.pdffox.adv.use.util.PreferenceUtil
import kotlinx.serialization.json.Json
import kotlin.system.exitProcess

/**
 * 广告策略管理类
 */
object AdPolicyManager {

	private const val TAG = "AdPolicyManager"

	var adPolicy: AdPolicy? = null

	fun loadPolicyFromLocal(context: Context) {
		val inputStream = context.resources.openRawResource(R.raw.ad_policy)
		val strPolicy = inputStream.bufferedReader().use { it.readText() }
		setPolicyFromJson(strPolicy)
	}

	fun setPolicyFromJson(jsonString: String) {
		if (jsonString.isBlank()) {
			return
		}
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "setPolicyFromJson: $jsonString" )
			PreferenceUtil.commitString("setPolicyFromJson", jsonString)
		}
//		val gson = Gson()
//		val adPolicy = gson.fromJson(jsonString, AdPolicy::class.java)
		val adPolicy = Json.decodeFromString<AdPolicy>(jsonString)

		if (BuildConfig.DEBUG) {
			Log.e(TAG, "setPolicyFromJson: packageName = ${AdvApplicaiton.instance.packageName} adPolicy = $adPolicy")
		}
		val packageName = AdvApplicaiton.instance.packageName
		if (packageName != adPolicy.package_name) {
			AdvApplicaiton.instance.currentActivity?.finishAffinity()
			Process.killProcess(Process.myPid())
			exitProcess(0)
		}
		setPolicy(adPolicy)
	}

	fun setPolicy(adPolicy: AdPolicy) {
		this.adPolicy = adPolicy
	}

	fun getAdUnit(areakey: String): AdUnit? {
		return adPolicy?.ad_units?.find { it.areakey == areakey }
	}

	fun checkAdUnit(areakey: String): Boolean {
		val adUnit = getAdUnit(areakey)
		val result = checkAdUnit(adUnit)
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "checkAdUnit: $areakey ${adUnit?.rate} ${adUnit?.frequency_caps} result = $result" )
		}
		return result
	}

	fun checkAdUnit(adUnit: AdUnit?): Boolean {
		if (adUnit == null) {
			return false
		}
		val checkGlobalAdSwitch = checkGlobalAdSwitch()
		val checkLimit = checkLimit()
		val checkAdUnitsFrequency = checkAdUnitsFrequency(adUnit)
		val checkCheckAdUnitRate = checkCheckAdUnitRate(adUnit)
		Log.e(TAG, "checkAdUnit: " +
				"checkGlobalAdSwitch = $checkGlobalAdSwitch, " +
				"checkLimit = $checkLimit, " +
				"checkAdUnitsFrequency = $checkAdUnitsFrequency, " +
				"checkCheckAdUnitRate = $checkCheckAdUnitRate"
		)
		return checkGlobalAdSwitch &&
				checkLimit &&
				checkAdUnitsFrequency &&
				checkCheckAdUnitRate
	}

	// 全局广告开关
	fun checkGlobalAdSwitch(): Boolean {
		return adPolicy?.global_ad_switch ?: false
	}

	// 全局播放广告数限制
	fun checkLimit(): Boolean {
		val limited = adPolicy?.limited ?: 50
		val limitedLoadtimeSeconds = adPolicy?.limited_loadtime_seconds ?: 86400
		if (System.currentTimeMillis() - AdPlayRecordManager.lastLimitedTime < limitedLoadtimeSeconds * 1000) {
			return false
		}
		val playCount = AdPlayRecordManager.getAllPlayTime(AdPlayRecordManager.lastLimitedTime)
		if (playCount >= limited) {
			AdPlayRecordManager.lastLimitedTime = System.currentTimeMillis()
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

		val lastTime = AdPlayRecordManager.getAdLastPlayTime(adUnit.areakey)
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "checkAdUnitsFrequency: areakey = ${adUnit.areakey} lastTime = $lastTime")
		}
		// 检查广告位是否在最近的时间间隔内播放过
		if (lastTime > 0 && System.currentTimeMillis() - lastTime < intervalSeconds * 1000) {
			Log.e(TAG, "checkAdUnitsFrequency: 在最近的时间间隔内播放过" )
			return false
		}

		// 检查广告位在最近的时间间隔内是否播放次数超过限制
		val playCount = AdPlayRecordManager.getAdPlayCount(adUnit.areakey, 60 * 60)
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "checkAdUnitsFrequency: areakey = ${adUnit.areakey} playCount = $playCount maxPerHour = $maxPerHour")
		}
		if (playCount >= maxPerHour) {
			Log.e(TAG, "checkAdUnitsFrequency: 超过每日播放次数最大限制" )
			return false
		}

		// 检查广告位在最近的24小时内是否播放次数超过限制
		val playCount24h = AdPlayRecordManager.getAdPlayCount(adUnit.areakey, 24 * 60 * 60)
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "checkAdUnitsFrequency: areakey = ${adUnit.areakey} playCount24h = $playCount24h maxPerDay = $maxPerDay")
		}
		if (playCount24h >= maxPerDay) {
			Log.e(TAG, "checkAdUnitsFrequency: 最近的24小时内播放次数超过限制", )
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