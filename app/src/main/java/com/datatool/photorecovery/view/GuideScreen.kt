package com.datatool.photorecovery.view

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.datatool.photorecovery.core.LogParams
import com.datatool.photorecovery.core.route.Routes
import com.datatool.photorecovery.ui.theme.Color_252040_10
import com.datatool.photorecovery.ui.theme.Color_252040_15
import com.datatool.photorecovery.ui.theme.Color_9E7BFB
import com.datatool.photorecovery.ui.theme.Color_9E7BFB_25
import com.datatool.photorecovery.ui.theme.Gradient_7BFBF7_to_4BE3F1
import com.datatool.photorecovery.ui.theme.Gradient_9E7BFB_to_784BF1
import com.datatool.photorecovery.ui.theme.TextStyle
import com.pdffox.adv.compose.BannerAd
import com.pdffox.adv.compose.rememberNativeAd
import com.datatool.photorecovery.view.widget.DisplayNativeAd2
import com.datatool.photorecovery.view.widget.DisplayNativeAd4
import com.datatool.photorecovery.view.widget.SetStatusBarLight
import com.datatool.photorecovery.viewmodel.SplashViewModel
import com.pdffox.adv.AdvertiseSdk
import com.datatool.photorecovery.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GuideScreen() {
	val TAG = "GuideScreen"

	val coroutineScope = rememberCoroutineScope()
	val context = LocalContext.current
	val navController = LocalNavController.current
	val activity = context as? MainActivity
	val splashViewModel: SplashViewModel = koinViewModel()
	val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

	val pagerState = rememberPagerState(pageCount = { 3 })
	val currentPage by remember { derivedStateOf { pagerState.currentPage } }
	val settledPage by remember { derivedStateOf { pagerState.settledPage } }

	val countdownActive = remember { mutableStateOf(true) }
	val countdown = remember { mutableStateOf(3) }

	val nativeAd = rememberNativeAd(
		areaKey = AreaKeyNative.appIntroNativeAdv,
		shouldRefreshImmediately = {
			lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
		},
	)

	// 自动翻页
	LaunchedEffect(settledPage) {
		if (settledPage != pagerState.pageCount - 1) {
			while (true) {
				Log.e("TAG", "currentPage: $currentPage" )
				kotlinx.coroutines.delay(AdvertiseSdk.guidePageSwapTime)
				if (currentPage == pagerState.pageCount - 1) {
					continue
				}
				val nextPage = currentPage + 1
				pagerState.animateScrollToPage(nextPage)
			}
		}
	}

	LaunchedEffect(currentPage, countdownActive.value) {
		if (currentPage == pagerState.pageCount - 1 && countdownActive.value) {
			countdown.value = 3
			while (countdown.value > 0 && countdownActive.value) {
				kotlinx.coroutines.delay(1000L)
				countdown.value -= 1
			}
			if (countdown.value == 0) {
				activity?.let {
					AdvertiseSdk.showInterstitialAd(activity = it, AreaKey.enterHomeFromAppIntroAdv) {
						coroutineScope.launch(Dispatchers.Main) {
							splashViewModel.setFirstLauncher("tag1/$currentPage/${countdownActive.value}")
							navController.navigate(Routes.HOME) {
								popUpTo(Routes.GUIDE) { inclusive = true }
							}
						}
					}
				}
			}
		} else {
			countdown.value = 3 // 非最后一页重置倒计?
		}
	}

	LaunchedEffect(currentPage) {
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "GuideScreen: $currentPage")
		}
		AdvertiseSdk.logEvent(LogConfig.show_guide, mapOf(LogParams.currentPage to currentPage))
	}

	LaunchedEffect(Unit) {
		AdvertiseSdk.logEvent(LogConfig.app_presentation, mapOf())
	}

	SetStatusBarLight(false)
	BackHandler {

	}
	Box(
		modifier = Modifier.fillMaxSize()
	) {
		HorizontalPager(
			state = pagerState,
			modifier = Modifier.fillMaxSize()
		) { page ->
			PageItem(page = page)
		}

		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.fillMaxWidth()
				.align(Alignment.BottomCenter)
//				.padding(bottom = 89.dp)
		) {
			Spacer(modifier = Modifier.height(31.dp))
			HorizontalPagerIndicator(currentPage)
			Spacer(modifier = Modifier.height(20.dp))
			if ((!BuildConfig.DEBUG) && (AdvertiseSdk.shouldSuppressAdsForCurrentUser)) {
				Box(
					modifier = Modifier
						.width(279.dp)
						.height(48.dp)
						.background(
							brush = Gradient_9E7BFB_to_784BF1,
							shape = RoundedCornerShape(32.dp)
						)
						.clickable(
							interactionSource = remember { MutableInteractionSource() },
							indication = null
						) {
							val current = pagerState.currentPage
							if (current < pagerState.pageCount - 1) {
								val nextPage = current + 1
								coroutineScope.launch {
									pagerState.animateScrollToPage(page = nextPage)
								}
							} else {
								countdownActive.value = false
								activity?.let {
									AdvertiseSdk.showInterstitialAd(
										activity = it,
										AreaKey.enterHomeFromAppIntroAdv
									) {
										coroutineScope.launch(Dispatchers.Main) {
											splashViewModel.setFirstLauncher("tag2")
											navController.navigate(Routes.HOME) {
												popUpTo(Routes.GUIDE) { inclusive = true }
											}
										}
									}
								}
							}
						},
					contentAlignment = Alignment.Center
				) {
					Text(
						text = when(currentPage) {
							0 -> stringResource(R.string.next)
							1 -> stringResource(R.string.next)
							else -> {
								if (countdown.value > 0) {
									stringResource(R.string.start_with_timer, countdown.value)
								} else {
									stringResource(R.string.start)
								}
							}
						},
						minLines = 1,
						maxLines = 1,
						style = TextStyle.TextStyle_20sp_w600_FFF,
						textAlign = TextAlign.Center
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
								val current = pagerState.currentPage
								if (current < pagerState.pageCount - 1) {
									val nextPage = current + 1
									coroutineScope.launch {
										pagerState.animateScrollToPage(page = nextPage)
									}
								} else {
									countdownActive.value = false
									activity?.let {
										AdvertiseSdk.showInterstitialAd(
											activity = it,
											AreaKey.enterHomeFromAppIntroAdv
										) {
											coroutineScope.launch(Dispatchers.Main) {
												splashViewModel.setFirstLauncher("tag3")
												navController.navigate(Routes.HOME) {
													popUpTo(Routes.GUIDE) { inclusive = true }
												}
											}
										}
									}
								}
							},
						contentAlignment = Alignment.Center
					) {
						Text(
							text = when(currentPage) {
								0 -> stringResource(R.string.next)
								1 -> stringResource(R.string.next)
								else -> {
									if (countdown.value > 0) {
										stringResource(R.string.start_with_timer, countdown.value)
									} else {
										stringResource(R.string.start)
									}
								}
							},
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

			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(220.dp)
					.padding(all = 10.dp)
			) {
				nativeAd.value?.let {
					DisplayNativeAd2(
						nativeAd = it
					)
				}
			}
		}

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.align(Alignment.TopCenter),
		) {
			Box(
				modifier = Modifier
					.height(87.dp)
					.fillMaxWidth()
			)
			Text(
				modifier = Modifier
					.fillMaxWidth(),
				text = when (currentPage) {
					0 -> stringResource(R.string.explore_your_files_with_photo_recovery_app)
					1 -> stringResource(R.string.easy_and_fast_recovery)
					else -> stringResource(R.string.explore_your_files_with_photo_recovery_app)
				},
				minLines = 2,
				maxLines = 2,
				style = TextStyle.TextStyle_20sp_w600_252040,
				textAlign = TextAlign.Center
			)
		}

	}
}

@Composable
fun PageItem(page: Int) {
	Box(
		modifier = Modifier.fillMaxSize(),
		contentAlignment = Alignment.TopCenter
	) {
		Image(
			painter = painterResource(id = when (page) {
				0 -> R.mipmap.guide_1
				1 -> R.mipmap.guide_2
				else -> R.mipmap.guide_3
			}),
			contentDescription = null,
			modifier = Modifier.fillMaxSize(),
			contentScale = ContentScale.Crop
		)
	}
}

@Composable
fun HorizontalPagerIndicator(page: Int) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center,
		modifier = Modifier.fillMaxWidth()
	) {
		repeat(3) { index ->
			Spacer(modifier = Modifier.width(8.dp))
			Box(
				modifier = Modifier
					.width(12.dp)
					.height(5.dp)
					.background(
						color = if (index == page) Color.White else Color.White.copy(alpha = 0.35f),
						shape = RoundedCornerShape(3.5.dp)
					)
			)
		}
		Spacer(modifier = Modifier.width(8.dp))
	}
}

@Preview(showBackground = true)
@Composable
fun GuideScreenPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(LocalNavController provides navController) {
		GuideScreen()
	}
}
