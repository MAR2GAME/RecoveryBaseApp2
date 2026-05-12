package com.datatool.photorecovery.view

import com.pdffox.adv.AdvertiseSdk
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.datatool.photorecovery.BuildConfig
import com.datatool.photorecovery.LocalInnerPadding
import com.datatool.photorecovery.LocalNavController
import com.datatool.photorecovery.core.route.Routes
import com.datatool.photorecovery.ui.theme.Gradient_FFF_to_F5F5F5
import com.datatool.photorecovery.ui.theme.TextStyle
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

private const val TAG = "DebugScreen"
@Composable
fun DebugScreen() {
	val navController = LocalNavController.current
	val context = androidx.compose.ui.platform.LocalContext.current

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				brush = Gradient_FFF_to_F5F5F5,
			)
			.padding(top = 45.dp, bottom = 50.dp)
	) {
		val scrollState = rememberScrollState()
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 24.dp)
				.verticalScroll(scrollState)
		) {
			Spacer(modifier = Modifier.height(8.dp))
			Button(
				modifier = Modifier.fillMaxWidth(),
				onClick = {
					navController.navigate(Routes.DEBUGINFO)
				}
			) {
				Text(text = "DEBUG测试数据")
			}
			Spacer(modifier = Modifier.height(8.dp))
			Button(
				modifier = Modifier.fillMaxWidth(),
				onClick = {
					AdvertiseSdk.sendDebugNotification(context, "daily_notification", "daily_notification")
				}
			) {
				Text(text = "测试APP通知")
			}
			Spacer(modifier = Modifier.height(8.dp))
			Button(
				modifier = Modifier.fillMaxWidth(),
				onClick = {
					navController.navigate(Routes.DEBUGAD)
				}
			) {
				Text(text = "测试广告")
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun DebugScreenPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(
		LocalNavController provides navController,
		LocalInnerPadding provides PaddingValues(0.dp)
	) {
		DebugScreen()
	}
}