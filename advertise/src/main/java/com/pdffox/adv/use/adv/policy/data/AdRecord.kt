package com.pdffox.adv.use.adv.policy.data

import kotlinx.serialization.Serializable

@Serializable
data class AdRecord(
	val areakey: String,
	val adFormat: String,
	val showAdPlatform: String,
	val timestamp: Long,
)