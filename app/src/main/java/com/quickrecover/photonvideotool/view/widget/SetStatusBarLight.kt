package com.quickrecover.photonvideotool.view.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

@Composable
fun SetStatusBarLight(isLightPage: Boolean) {
	val view = LocalView.current
	if (!view.isInEditMode) {
		val window = androidx.core.view.ViewCompat.getWindowInsetsController(view)
		if (window != null) {
			window.isAppearanceLightStatusBars = isLightPage
		}
	}
}