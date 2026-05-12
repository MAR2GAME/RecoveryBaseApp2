package com.pdffox.adv.adv.policy.data

import android.os.Parcelable
import kotlinx.serialization.Serializable

@Serializable
data class AdNativePolicy(
	val limited: Int,
	val limited_loadtime_seconds: Int,
	val totalClickMaxPerDay: Int,
	val totalRequestMaxPerDay: Int,
	var ad_units: List<AdUnit>
)