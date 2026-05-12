package com.datatool.photorecovery.view

import com.pdffox.adv.AdvertiseSdk
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.rememberNavController
import com.datatool.photorecovery.BuildConfig
import com.datatool.photorecovery.LocalInnerPadding
import com.datatool.photorecovery.LocalNavController
import com.datatool.photorecovery.R
import com.datatool.photorecovery.core.AreaKeyNative
import com.datatool.photorecovery.core.Config
import com.datatool.photorecovery.core.route.Routes
import com.datatool.photorecovery.ui.theme.Color_9E7BFB_25
import com.datatool.photorecovery.ui.theme.Gradient_FFF_to_F5F5F5
import com.datatool.photorecovery.ui.theme.TextStyle
import com.datatool.photorecovery.view.widget.DisplayNativeAd1
import com.datatool.photorecovery.view.widget.DisplayNativeAd4
import com.datatool.photorecovery.view.widget.NavigationWidget
import com.pdffox.adv.compose.rememberNativeAd
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun SettingsScreen() {
	val navController = LocalNavController.current
	val context = androidx.compose.ui.platform.LocalContext.current
	val activity = context as? Activity
	val versionName = try {
		val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
		packageInfo.versionName ?: "N/A"
	} catch (e: Exception) {
		"N/A"
	}
	val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current


	val nativeAd = rememberNativeAd(
		areaKey = AreaKeyNative.settingsNativeAdv,
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
			.padding(top = 15.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 24.dp)
		) {
			NavigationWidget(title = stringResource(R.string.settings), navController = navController, showBack = false)
			Spacer(modifier = Modifier.height(25.dp))
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.border(
						width = 1.dp,
						color = Color_9E7BFB_25,
						shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
					)
			) {
				Column (
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp, vertical = 12.dp)
				) {
					SettingsOptButton(
						icon = R.drawable.change_language,
						text = stringResource(R.string.change_language),
					) {
						navController.navigate("${Routes.LANGUAGES}/false") {
							popUpTo(Routes.SPLASH) { inclusive = true }
						}
					}
					Spacer(modifier = Modifier.height(16.dp))
					SettingsOptButton(
						icon = R.drawable.terms_of_service,
						text = stringResource(R.string.privacy_policy)
					) {
						val encodedUrl = URLEncoder.encode(Config.PrivacyUrl, StandardCharsets.UTF_8.toString())
						navController.navigate("${Routes.Website}/$encodedUrl")
					}
					if (AdvertiseSdk.isPrivacyOptionsRequired) {
						Spacer(modifier = Modifier.height(16.dp))
						SettingsOptButton(
							icon = R.drawable.privacy_policy,
							text = stringResource(R.string.privacy_settings)
						) {
							activity?.let {
								AdvertiseSdk.showPrivacyOptions(it)
							}
						}
					}
				}
			}
			Spacer(modifier = Modifier.height(8.dp))
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.border(
						width = 1.dp,
						color = Color_9E7BFB_25,
						shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
					)
			) {
				Column (
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp, vertical = 12.dp)
				) {
					Box(modifier = Modifier.fillMaxWidth()) {
						Row(
							verticalAlignment = Alignment.CenterVertically,
							modifier = Modifier.padding(
								start = 12.dp,
								top = 12.dp,
								bottom = 12.dp,
								end = 16.dp
							)) {
							Image(
								painter = painterResource(id = R.drawable.version),
								contentDescription = null,
								modifier = Modifier
									.size(24.dp)
							)
							Spacer(modifier = Modifier.width(20.dp))
							Text(
								text = stringResource(R.string.version),
								style = TextStyle.TextStyle_16sp_w600_252040
							)
							Spacer(modifier = Modifier.weight(1f))
							Text(
								text = versionName,
								style = TextStyle.TextStyle_16sp_w400_252040
							)
						}
					}
				}
			}
			if (BuildConfig.DEBUG) {
				Spacer(modifier = Modifier.height(8.dp))
				Button(
					modifier = Modifier.fillMaxWidth(),
					onClick = {
						navController.navigate(Routes.DEBUG)
					}
				) {
					Text(text = "DEBUG")
				}
			}
			Spacer(
				modifier = Modifier.weight(1f)
			)
			nativeAd.value?.let {
				DisplayNativeAd1(
					nativeAd = it
				)
			}
		}
	}
}

@Composable
fun SettingsOptButton(icon: Int, text: String, onClick: () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(
				interactionSource = remember { MutableInteractionSource() },
				indication = null,
				onClick = onClick
			),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(
				start = 12.dp,
				top = 12.dp,
				bottom = 12.dp,
				end = 16.dp
			)) {
			Image(
				painter = painterResource(id = icon),
				contentDescription = null,
				modifier = Modifier
					.size(24.dp)
			)
			Spacer(modifier = Modifier.width(20.dp))
			Text(
				text = text,
				style = TextStyle.TextStyle_16sp_w600_252040
			)
			Spacer(modifier = Modifier.weight(1f))
			Image(
				painter = painterResource(id = R.drawable.next),
				contentDescription = null,
				modifier = Modifier
					.size(16.dp)
			)
		}
	}
}

@Preview(showBackground = true)
@Composable
fun SettingsOptButtonPreview() {
	SettingsOptButton(icon = R.drawable.back, text = "Settings", onClick = {})
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(
		LocalNavController provides navController,
		LocalInnerPadding provides PaddingValues(0.dp)
	) {
		SettingsScreen()
	}
}
