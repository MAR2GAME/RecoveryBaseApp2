package com.datatool.photorecovery.view

import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.rememberNavController
import com.datatool.photorecovery.LocalInnerPadding
import com.datatool.photorecovery.LocalNavController
import com.datatool.photorecovery.MainActivity
import com.datatool.photorecovery.R
import com.datatool.photorecovery.core.AreaKey
import com.datatool.photorecovery.core.AreaKeyNative
import com.datatool.photorecovery.core.LogConfig
import com.datatool.photorecovery.core.route.Routes
import com.datatool.photorecovery.ui.theme.Color_252040_15
import com.datatool.photorecovery.ui.theme.Color_9E7BFB
import com.datatool.photorecovery.ui.theme.Gradient_9E7BFB_to_784BF1
import com.datatool.photorecovery.ui.theme.Gradient_FFF_to_F5F5F5
import com.datatool.photorecovery.ui.theme.TextStyle
import com.pdffox.adv.compose.BannerAd
import com.pdffox.adv.compose.rememberNativeAd
import com.datatool.photorecovery.view.widget.DisplayNativeAd1
import com.datatool.photorecovery.view.widget.DisplayNativeAd2
import com.datatool.photorecovery.view.widget.ExitPop
import com.datatool.photorecovery.view.widget.SetStatusBarLight
import com.datatool.photorecovery.viewmodel.ScanViewModel
import com.pdffox.adv.AdvertiseSdk
import com.datatool.photorecovery.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScanEndScreen(scanType: String) {
	val scanViewModel: ScanViewModel = koinViewModel()
	val navController = LocalNavController.current
	val context = LocalContext.current
	val activity = context as? MainActivity
	val coroutineScope = rememberCoroutineScope()
	val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

	val scanResult by scanViewModel.scanFilesResult.collectAsState()
	var showExitPop by remember { mutableStateOf(false) }

	val nativeAd = rememberNativeAd(
		areaKey = AreaKeyNative.scanCplNativeAdv,
		shouldRefreshImmediately = {
			lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
		},
	)

	LaunchedEffect(Unit) {
		scanViewModel.getScanResult(scanType)
	}

	LaunchedEffect(Unit) {
		AdvertiseSdk.logEvent(LogConfig.scan_complete_page, mapOf())
	}

	SetStatusBarLight(true)
	BackHandler {
		showExitPop = true
	}
	Box(Modifier.fillMaxSize()) {
		Box(modifier = Modifier
			.fillMaxSize()
			.background(
				brush = Gradient_FFF_to_F5F5F5,
			)
			.padding(LocalInnerPadding.current)) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.align(Alignment.Center)
					.padding(horizontal = 24.dp)
			) {
				Image(
					painter = painterResource(id = R.drawable.back),
					contentDescription = null,
					modifier = Modifier
						.size(44.dp)
						.clickable(
							interactionSource = remember { MutableInteractionSource() },
							indication = null
						) {
							showExitPop = true
						}
				)
				Spacer(modifier = Modifier.height(18.dp))
				Image(
					painter = painterResource(id = R.drawable.scan_result_tips),
					contentDescription = null,
					modifier = Modifier
						.align(androidx.compose.ui.Alignment.CenterHorizontally)
						.width(139.dp)
						.height(162.dp)
				)
				Spacer(modifier = Modifier.height(32.dp))
				Text(
					text = stringResource(R.string.scan_completed),
					style = TextStyle.TextStyle_24sp_w600_252040,
					modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
				)
				Spacer(modifier = Modifier.height(16.dp))
				if (scanResult.isNotEmpty()) {
					Row(
						modifier = Modifier
							.align(androidx.compose.ui.Alignment.CenterHorizontally),
						verticalAlignment = androidx.compose.ui.Alignment.Bottom
					) {
						Text(
							text = stringResource(R.string.found),
							style = TextStyle.TextStyle_16sp_w500_252040,
						)
						Spacer(modifier = Modifier.width(6.dp))
						Text(
							text = scanResult.size.toString(),
							style = TextStyle.TextStyle_24sp_w600_9E7BFB,
						)
						Spacer(modifier = Modifier.width(6.dp))
						Text(
							text = when(scanType) {
								"recovery_photos" -> stringResource(R.string.photos_for_recovery)
								"recovery_videos" -> stringResource(R.string.videos_for_recovery)
								"recovery_audios" -> stringResource(R.string.audios_for_recovery)
								else -> stringResource(R.string.files_for_recovery)
							},
							style = TextStyle.TextStyle_16sp_w500_252040,
						)
					}
				} else {
					Text(
						text = when(scanType) {
							"recovery_photos" -> stringResource(R.string.no_photos_found_for_recovery)
							"recovery_videos" -> stringResource(R.string.no_videos_found_for_recovery)
							"recovery_audios" -> stringResource(R.string.no_audios_found_for_recovery)
							else -> stringResource(R.string.no_files_found_for_recovery)
						},
						style = TextStyle.TextStyle_16sp_w500_252040,
						modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
					)
				}
				Spacer(modifier = Modifier.height(44.dp))
				if ((!BuildConfig.DEBUG) && (AdvertiseSdk.shouldSuppressAdsForCurrentUser)) {
					Box(
						modifier = Modifier
							.width(311.dp)
							.height(44.dp)
							.align(Alignment.CenterHorizontally)
							.background(
								brush = Gradient_9E7BFB_to_784BF1,
								shape = RoundedCornerShape(32.dp)
							)
							.clickable(
								interactionSource = remember { MutableInteractionSource() },
								indication = null
							) {
								if (scanResult.isNotEmpty()) {
									activity?.let {
										AdvertiseSdk.showInterstitialAd(activity = it, AreaKey.beforeViewResultPageAdv) {
											coroutineScope.launch(Dispatchers.Main) {
												navController.navigate("${Routes.RecoveryFolder}/${scanType}") {
													popUpTo("${Routes.ScanEnd}/${scanType}") { inclusive = true }
												}
											}
										}
									}
								} else {
									navController.popBackStack()
								}
							}
					) {
						Text(
							text = if (scanResult.isNotEmpty()) stringResource(R.string.view) else stringResource(R.string.ok),
							style = TextStyle.TextStyle_20sp_w600_FFF,
							modifier = Modifier.align(Alignment.Center)
						)
					}
				} else {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.height(34.dp)
					) {
						Spacer(
							modifier = Modifier.weight(1f)
						)
						Box(
							modifier = Modifier
								.height(40.dp)
								.wrapContentWidth()
								.border(
									width = 1.dp,
									color = Color_252040_15,
									shape = RoundedCornerShape(60.dp)
								)
								.padding(horizontal = 16.dp)
								.clickable(
									interactionSource = remember { MutableInteractionSource() },
									indication = null
								) {
									if (scanResult.isNotEmpty()) {
										activity?.let {
											AdvertiseSdk.showInterstitialAd(activity = it, AreaKey.beforeViewResultPageAdv) {
												coroutineScope.launch(Dispatchers.Main) {
													navController.navigate("${Routes.RecoveryFolder}/${scanType}") {
														popUpTo("${Routes.ScanEnd}/${scanType}") { inclusive = true }
													}
												}
											}
										}
									} else {
										navController.popBackStack()
									}
								},
							contentAlignment = Alignment.Center
						) {
							Text(
								modifier = Modifier.align(Alignment.Center),
								text = if (scanResult.isNotEmpty()) stringResource(R.string.view) else stringResource(R.string.ok),
								minLines = 1,
								maxLines = 1,
								style = TextStyle.TextStyle_20sp_w600_9E7BFB,
								textAlign = TextAlign.Center
							)
						}
						Spacer(
							modifier = Modifier.width(50.dp)
						)
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
		if (showExitPop) {
			AdvertiseSdk.logEvent(LogConfig.leave_popup, mapOf())
			ExitPop(nativeAd.value) { flag ->
				showExitPop = false
				if (flag) {
					activity?.let {
						AdvertiseSdk.showInterstitialAd(activity = it, AreaKey.returnHomeFromPopAdv) {
							coroutineScope.launch(Dispatchers.Main) {
								navController.navigate(Routes.HOME) {
									popUpTo(Routes.HOME) { inclusive = true }
								}
							}
						}
					}
				}
			}
		}
	}

}

//@Preview
//@Composable
//fun ScanEndScreenPreview() {
//	val navController = rememberNavController()
//	CompositionLocalProvider(
//		LocalNavController provides navController,
//		LocalInnerPadding provides PaddingValues(0.dp)
//	) {
//		ScanEndScreen("recovery_photos")
//	}
//}
