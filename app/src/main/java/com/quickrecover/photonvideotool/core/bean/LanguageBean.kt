package com.quickrecover.photonvideotool.core.bean

import java.util.Locale

data class LanguageBean(
	val language: String,
	val locale: Locale,
	var selected: Boolean
)