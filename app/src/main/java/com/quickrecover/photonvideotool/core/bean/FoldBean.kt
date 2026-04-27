package com.quickrecover.photonvideotool.core.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class FoldBean(
	val name: String = "",
	var files: List<FileData> = emptyList(),
): Parcelable
