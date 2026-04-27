package com.quickrecover.photonvideotool.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.quickrecover.photonvideotool.LocalInnerPadding
import com.quickrecover.photonvideotool.LocalNavController
import com.quickrecover.photonvideotool.MainActivity
import com.quickrecover.photonvideotool.R
import com.quickrecover.photonvideotool.core.AreaKey
import com.quickrecover.photonvideotool.core.LogConfig
import com.quickrecover.photonvideotool.core.route.Routes
import com.quickrecover.photonvideotool.ui.theme.Gradient_9E7BFB_to_784BF1
import com.quickrecover.photonvideotool.ui.theme.Gradient_FFF_to_F5F5F5
import com.quickrecover.photonvideotool.ui.theme.TextStyle
import com.quickrecover.photonvideotool.view.widget.BannerAd
import com.quickrecover.photonvideotool.view.widget.ExitPop
import com.quickrecover.photonvideotool.view.widget.SetStatusBarLight
import com.quickrecover.photonvideotool.viewmodel.ScanViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// TODO: add
//import com.pdffox.adv.use.Ads
//import com.pdffox.adv.use.log.LogUtil

@Composable
fun ScanEndScreen(scanType: String) {
	val scanViewModel: ScanViewModel = koinViewModel()
	val navController = LocalNavController.current
	val context = LocalContext.current
	val activity = context as? MainActivity
	val coroutineScope = rememberCoroutineScope()

	val scanResult by scanViewModel.scanFilesResult.collectAsState()
	var showExitPop by remember { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		scanViewModel.getScanResult(scanType)
	}

	// TODO: add
//	LaunchedEffect(Unit) {
//		LogUtil.log(LogConfig.scan_complete_page, mapOf())
//	}

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
									// TODO: change
//									Ads.showInterstitialAd(
//										activity = it,
//										AreaKey.beforeViewResultPageAdv
//									) {
									coroutineScope.launch(Dispatchers.Main) {
										navController.navigate("${Routes.RecoveryFolder}/${scanType}") {
											popUpTo("${Routes.ScanEnd}/${scanType}") {
												inclusive = true
											}
										}
									}
//									}
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
				Spacer(modifier = Modifier.height(16.dp))

			}
		}
		if (showExitPop) {
			// TODO: add
//			LogUtil.log(LogConfig.leave_popup, mapOf())
			ExitPop { flag ->
				showExitPop = false
				if (flag) {
					activity?.let {
						// TODO: change
//						Ads.showInterstitialAd(activity = it, AreaKey.returnHomeFromPopAdv) {
							coroutineScope.launch(Dispatchers.Main) {
								navController.navigate(Routes.HOME) {
									popUpTo(Routes.HOME) { inclusive = true }
								}
							}
//						}
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
