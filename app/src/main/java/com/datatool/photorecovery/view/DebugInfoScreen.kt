package com.datatool.photorecovery.view

import com.pdffox.adv.AdvertiseSdk
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
import androidx.navigation.compose.rememberNavController
import com.datatool.photorecovery.BuildConfig
import com.datatool.photorecovery.LocalInnerPadding
import com.datatool.photorecovery.LocalNavController
import com.datatool.photorecovery.ui.theme.Gradient_FFF_to_F5F5F5
import com.datatool.photorecovery.ui.theme.TextStyle
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

private const val TAG = "DebugScreen"
@Composable
fun DebugInfoScreen() {
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
				text = "设备ID: ${AdvertiseSdk.getThinkingDeviceId()}",
				style = TextStyle.TextStyle_14sp_w500_252040
			)
			Spacer(modifier = Modifier.height(18.dp))
			Text(
				text = "用户类型: ${if (AdvertiseSdk.isNature) "自然用户" else "广告用户"}",
				style = TextStyle.TextStyle_14sp_w500_252040
			)
			Spacer(modifier = Modifier.height(18.dp))
			Text(
				text = "FCM-TOPIC: ${AdvertiseSdk.topic}",
				style = TextStyle.TextStyle_14sp_w500_252040
			)
			Spacer(modifier = Modifier.height(18.dp))
			val locale = Locale.getDefault()
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "DebugScreen: locale = $locale" )
			}
			Text(
				text = "国家: ${locale.country} | ${locale.displayCountry}",
				style = TextStyle.TextStyle_14sp_w500_252040
			)
			Text(
				text = "语言: ${locale.language}",
				style = TextStyle.TextStyle_14sp_w500_252040
			)
			Spacer(modifier = Modifier.height(18.dp))
			Text(
				text = "手机型号: ${Build.BRAND}",
				style = TextStyle.TextStyle_14sp_w500_252040
			)

			Spacer(modifier = Modifier.height(28.dp))
			val mappingJson = AdvertiseSdk.getPreferenceString("ad_mapping", "ad_mapping")
			val mappingFormattedJson = try {
				JSONObject(mappingJson).toString(4) // 缩进4个空格格式化
			} catch (e: JSONException) {
				mappingJson // 如果不是合法JSON，直接显示原字符串
			}
			Text(
				text = "映射表: \n$mappingFormattedJson",
				style = TextStyle.TextStyle_14sp_w500_252040
			)

			Spacer(modifier = Modifier.height(28.dp))
			val rawJson = AdvertiseSdk.getPreferenceString("notification_config", "notification_config")
			val formattedJson = try {
				JSONObject(rawJson).toString(4) // 缩进4个空格格式化
			} catch (e: JSONException) {
				rawJson // 如果不是合法JSON，直接显示原字符串
			}
			Text(
				text = "通知策略: \n$formattedJson",
				style = TextStyle.TextStyle_14sp_w500_252040
			)

			Spacer(modifier = Modifier.height(28.dp))
			val rawAdPolicy = AdvertiseSdk.getPreferenceString("setPolicyFromJson", "setPolicyFromJson")
			val formattedAdPolicy = try {
				JSONObject(rawAdPolicy).toString(4) // 缩进4个空格格式化
			} catch (e: JSONException) {
				rawAdPolicy // 如果不是合法JSON，直接显示原字符串
			}
			Text(
				text = "广告策略: \n$formattedAdPolicy",
				style = TextStyle.TextStyle_14sp_w500_252040
			)

			Spacer(modifier = Modifier.height(28.dp))
			val rawAdloadConfig = AdvertiseSdk.getPreferenceString("updateConfigFromJson", "updateConfigFromJson")
			val formattedAdloadConfig = try {
				JSONObject(rawAdloadConfig).toString(4) // 缩进4个空格格式化
			} catch (e: JSONException) {
				rawAdloadConfig // 如果不是合法JSON，直接显示原字符串
			}
			Text(
				text = "广告加载策略: \n$formattedAdloadConfig",
				style = TextStyle.TextStyle_14sp_w500_252040
			)

			Spacer(modifier = Modifier.height(28.dp))
		}
	}
}

@Preview(showBackground = true)
@Composable
fun DebugInfoScreenPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(
		LocalNavController provides navController,
		LocalInnerPadding provides PaddingValues(0.dp)
	) {
		DebugInfoScreen()
	}
}