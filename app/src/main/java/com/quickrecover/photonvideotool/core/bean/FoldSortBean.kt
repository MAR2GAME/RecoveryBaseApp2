package com.quickrecover.photonvideotool.core.bean

data class FoldSortBean(
	val name: String,
	val files: List<FileData> = emptyList(),
	val checked: Boolean = false,
)
