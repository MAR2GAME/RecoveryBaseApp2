package com.quickrecover.photonvideotool.core.bean

import android.os.Parcelable
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize

@Parcelize
@Serializable
data class FileData(
	val name: String,
	val path: String,
	val size: Long,
	val lastModified: Long,
	var isSelect: Boolean = false
): Parcelable