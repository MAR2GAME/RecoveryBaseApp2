package com.datatool.photorecovery.view

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
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
import com.datatool.photorecovery.view.widget.DisplayNativeAd3
import com.datatool.photorecovery.view.widget.DisplayNativeAd4
import com.datatool.photorecovery.view.widget.SetStatusBarLight
import com.pdffox.adv.AdvertiseSdk
import com.datatool.photorecovery.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryFileSuccessScreen(scanType: String) {
	val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
	val navController = LocalNavController.current
	val context = LocalContext.current
	val activity = context as? MainActivity

	var showExitSheet by remember { mutableStateOf(false) }

	val hasResult = true

	val interactionSource = remember { MutableInteractionSource() }
	val coroutineScope = rememberCoroutineScope()

	val nativeAd = rememberNativeAd(
		areaKey = AreaKeyNative.recoverCplNativeAdv,
		shouldRefreshImmediately = {
			lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
		},
	)

	LaunchedEffect(Unit) {
		AdvertiseSdk.logEvent(LogConfig.recover_complete_page, mapOf())
	}

	SetStatusBarLight(true)

	BackHandler {
		showExitSheet = true
	}
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
						showExitSheet = true
					}
			)
			Spacer(modifier = Modifier.height(18.dp))
			Image(
				painter = painterResource(id = R.drawable.scan_result_tips),
				contentDescription = null,
				modifier = Modifier
					.align(Alignment.CenterHorizontally)
					.width(139.dp)
					.height(162.dp)
			)
			Spacer(modifier = Modifier.height(32.dp))
			Text(
				text = stringResource(R.string.recovery_successful),
				style = TextStyle.TextStyle_24sp_w600_252040,
				modifier = Modifier.align(Alignment.CenterHorizontally)
			)
			Spacer(modifier = Modifier.height(16.dp))
			if (hasResult) {
				Row(
					modifier = Modifier
						.align(Alignment.CenterHorizontally),
					verticalAlignment = Alignment.Bottom
				) {
					Text(
						text = stringResource(R.string.photo_successfully_saved_to_your_device),
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
							navController.navigate("${Routes.RecoveredFiles}/${scanType}") {
								popUpTo("${Routes.RecoveryFileSuccess}/${scanType}") {
									inclusive = true
								}
							}
						}
				) {
					Text(
						text = stringResource(R.string.view),
						style = TextStyle.TextStyle_20sp_w600_FFF,
						modifier = Modifier.align(Alignment.Center)
					)
				}
				Spacer(modifier = Modifier.height(12.dp))
				Box(
					modifier = Modifier
						.width(311.dp)
						.height(44.dp)
						.align(Alignment.CenterHorizontally)
						.clickable(
							interactionSource = remember { MutableInteractionSource() },
							indication = null
						) {
							AdvertiseSdk.logEvent(LogConfig.click_continue_btn, mapOf())
							navController.popBackStack()
						}
				) {
					Text(
						text = stringResource(R.string._continue),
						style = TextStyle.TextStyle_20sp_w600_9E7BFB,
						modifier = Modifier.align(Alignment.Center)
					)
				}
			} else {
				Row(
					modifier = Modifier.fillMaxWidth()
				) {
					Spacer(modifier = Modifier.width(20.dp))
					Box(
						modifier = Modifier
							.height(44.dp)
							.align(Alignment.CenterVertically)
							.clickable(
								interactionSource = remember { MutableInteractionSource() },
								indication = null
							) {
								AdvertiseSdk.logEvent(LogConfig.click_continue_btn, mapOf())
								navController.popBackStack()
							}
					) {
						Text(
							text = stringResource(R.string._continue),
							style = TextStyle.TextStyle_20sp_w500_252040_35,
							modifier = Modifier.align(Alignment.Center)
						)
					}
					Spacer(modifier = Modifier.weight(1f))
					Box(
						modifier = Modifier
							.align(Alignment.CenterVertically)
							.border(
								width = 1.dp,
								color = Color_252040_15,
								shape = RoundedCornerShape(60.dp)
							)
							.padding(horizontal = 16.dp, vertical = 5.dp)
							.clickable(
								interactionSource = remember { MutableInteractionSource() },
								indication = null
							) {
								navController.navigate("${Routes.RecoveredFiles}/${scanType}") {
									popUpTo("${Routes.RecoveryFileSuccess}/${scanType}") {
										inclusive = true
									}
								}
							}
					) {
						Text(
							text = stringResource(R.string.view),
							style = TextStyle.TextStyle_20sp_w600_9E7BFB,
							modifier = Modifier.align(Alignment.Center)
						)
					}
					Spacer(modifier = Modifier.width(20.dp))
				}
			}
			Spacer(modifier = Modifier.height(16.dp))
			Spacer(
				modifier = Modifier
					.weight(1f)
			)
			nativeAd.value?.let {
				DisplayNativeAd1(
					nativeAd = it
				)
			}
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(20.dp)
			) {}
		}
	}

	if (showExitSheet) {
		AdvertiseSdk.logEvent(LogConfig.leave_popup, mapOf())
	}

	// 弹出确认返回的对话框
	AnimatedVisibility(
		visible = showExitSheet,
		enter = fadeIn(animationSpec = tween(durationMillis = 300)),
		exit = fadeOut(animationSpec = tween(durationMillis = 300))
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(Color.Black.copy(alpha = 0.5f)) // 半透明遮罩
				.clickable(
					indication = null,
					interactionSource = remember { MutableInteractionSource() }
				) {
					showExitSheet = false
				},
			contentAlignment = Alignment.Center
		) {
			ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
				val (tipImage, tipShadow, contentColumn) = createRefs()

				Box(
					modifier = Modifier
						.width(343.dp)
						.background(
							color = Color.White,
							shape = RoundedCornerShape(16.dp)
						)
						.constrainAs(contentColumn) {
							start.linkTo(parent.start)
							top.linkTo(tipImage.top, margin = 36.dp)
							end.linkTo(parent.end)
						}
						.clickable(enabled = false) {} // 防止点击穿?
						.padding(horizontal = 32.dp)
				) {
					Column(horizontalAlignment = Alignment.CenterHorizontally) {
						Spacer(modifier = Modifier.height(90.dp))
						Text(
							text = stringResource(R.string.lf_you_leave_the_scan_results_will_be_erased_sure_you_want_to_go),
							style = TextStyle.TextStyle_14sp_w600_252040_70,
							textAlign = TextAlign.Center
						)
						Spacer(modifier = Modifier.height(24.dp))
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.height(44.dp)
								.background(
									brush = Gradient_9E7BFB_to_784BF1,
									shape = RoundedCornerShape(32.dp)
								)
								.clickable(
									interactionSource = interactionSource,
									indication = null
								) {
									showExitSheet = false
								},
							contentAlignment = Alignment.Center
						) {
							Text(
								text = stringResource(R.string.cancel),
								style = TextStyle.TextStyle_20sp_w600_FFF,
								textAlign = TextAlign.Center,
								modifier = Modifier.wrapContentSize()
							)
						}
						Spacer(modifier = Modifier.height(12.dp))
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.height(44.dp)
								.clickable(
									interactionSource = interactionSource,
									indication = null
								) {
									activity?.let {
										AdvertiseSdk.showInterstitialAd(
											activity = it,
											AreaKey.returnHomeFromPopAdv
										) {
											coroutineScope.launch(Dispatchers.Main) {
												navController.navigate(Routes.HOME) {
													popUpTo(Routes.HOME) { inclusive = true }
												}
											}
										}
									}
								},
							contentAlignment = Alignment.Center
						) {
							Text(
								text = stringResource(R.string.exit),
								style = TextStyle.TextStyle_20sp_w500_252040_35,
								textAlign = TextAlign.Center,
								modifier = Modifier.wrapContentSize()
							)
						}
						nativeAd.value?.let {
							DisplayNativeAd3(
								nativeAd = it
							)
						}
						Spacer(modifier = Modifier.height(20.dp))
					}
				}

				Image(
					painter = painterResource(id = R.drawable.tip),
					contentDescription = null,
					modifier = Modifier
						.height(72.dp)
						.constrainAs(tipImage) {
							start.linkTo(parent.start)
							end.linkTo(parent.end)
							top.linkTo(parent.top)
						}
				)

				Image(
					painter = painterResource(id = R.drawable.tip_shadow),
					contentDescription = null,
					modifier = Modifier
						.constrainAs(tipShadow) {
							start.linkTo(parent.start)
							end.linkTo(parent.end)
							top.linkTo(tipImage.bottom, margin = 6.dp)
						}
				)
			}

		}
	}
}

@Preview(showBackground = true)
@Composable
fun RecoveryFileSuccessScreenPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(
		LocalNavController provides navController,
		LocalInnerPadding provides PaddingValues(0.dp)
	) {
		RecoveryFileSuccessScreen(scanType = "recovery_photos")
	}
}
