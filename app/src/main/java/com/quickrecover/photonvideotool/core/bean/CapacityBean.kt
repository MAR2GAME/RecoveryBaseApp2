package com.quickrecover.photonvideotool.core.bean

import kotlinx.serialization.Serializable

@Serializable
data class CapacityBean (
	val name: String,
	val size: Long,
	val fileCount: Int = 0
)