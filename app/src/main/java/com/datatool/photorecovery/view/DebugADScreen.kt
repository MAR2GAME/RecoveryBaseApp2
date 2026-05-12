package com.datatool.photorecovery.view

import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.rememberNavController
import com.datatool.photorecovery.BuildConfig
import com.datatool.photorecovery.LocalInnerPadding
import com.datatool.photorecovery.LocalNavController
import com.datatool.photorecovery.core.AreaKeyNative
import com.datatool.photorecovery.ui.theme.Gradient_FFF_to_F5F5F5
import com.datatool.photorecovery.ui.theme.TextStyle
import com.datatool.photorecovery.view.widget.DisplayNativeAd1
import com.datatool.photorecovery.view.widget.DisplayNativeAd2
import com.datatool.photorecovery.view.widget.DisplayNativeAd3
import com.datatool.photorecovery.view.widget.DisplayNativeAd4
import com.pdffox.adv.compose.rememberNativeAd
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

private const val TAG = "DebugScreen"
@Composable
fun DebugADScreen() {

	val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

	val nativeAd = rememberNativeAd(
		areaKey = "debug_page",
		shouldRefreshImmediately = {
			lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
		},
	)

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				brush = Gradient_FFF_to_F5F5F5,
			)
			.padding(top = 45.dp)
	) {
		val scrollState = rememberScrollState()
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 24.dp)
				.verticalScroll(scrollState)
		) {
			Spacer(modifier = Modifier.height(18.dp))
			Text(
				text = "测试展示原生广告",
				style = TextStyle.TextStyle_14sp_w500_252040
			)
			Spacer(
				modifier = Modifier
				.weight(1f)
			)
			nativeAd.value?.let {
				DisplayNativeAd4(
					nativeAd = it
				)
			}
			Spacer(modifier = Modifier.height(18.dp))
		}
	}
}

@Preview(showBackground = true)
@Composable
fun DebugADScreenPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(
		LocalNavController provides navController,
		LocalInnerPadding provides PaddingValues(0.dp)
	) {
		DebugADScreen()
	}
}
