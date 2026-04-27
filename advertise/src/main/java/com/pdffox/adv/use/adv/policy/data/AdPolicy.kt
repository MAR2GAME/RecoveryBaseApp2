package com.pdffox.adv.use.adv.policy.data

import android.os.Parcelable
import kotlinx.serialization.Serializable

@Serializable
data class AdPolicy(
	val package_name: String,
	val platform: String,
	val global_ad_switch: Boolean,
	val limited: Int,
	val limited_loadtime_seconds: Int,
	val ad_network: AdNetwork,
	val first_open_enabled: Boolean,
	var ad_units: List<AdUnit>
)

@Serializable
data class AdNetwork(
	val aggregator: String
)

@Serializable
data class AdUnit(
	val areakey: String,
	val ad_format: String,
	val rate: Double,
	val frequency_caps: FrequencyCaps
)

@Serializable
data class FrequencyCaps(
	val max_per_hour: Int,
	val max_per_day: Int,
	val interval_seconds: Long
)
