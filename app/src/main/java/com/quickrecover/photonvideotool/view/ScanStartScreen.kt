package com.quickrecover.photonvideotool.view

import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.rememberNavController
import com.quickrecover.photonvideotool.BuildConfig
import com.quickrecover.photonvideotool.LocalInnerPadding
import com.quickrecover.photonvideotool.LocalNavController
import com.quickrecover.photonvideotool.MainActivity
import com.quickrecover.photonvideotool.R
import com.quickrecover.photonvideotool.core.AreaKey
import com.quickrecover.photonvideotool.core.LogConfig
import com.quickrecover.photonvideotool.core.route.Routes
import com.quickrecover.photonvideotool.ui.theme.Gradient_9E7BFB_to_784BF1
import com.quickrecover.photonvideotool.ui.theme.TextStyle
import com.quickrecover.photonvideotool.view.widget.BannerAd
import com.quickrecover.photonvideotool.view.widget.FileManagePop
import com.quickrecover.photonvideotool.view.widget.SetStatusBarLight
import com.quickrecover.photonvideotool.viewmodel.ScanViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// TODO: add
//import com.pdffox.adv.use.Ads
//import com.pdffox.adv.use.adv.AdConfig
//import com.pdffox.adv.use.adv.AdLoader
//import com.pdffox.adv.use.log.LogUtil

/**
 * recovery_photos, recovery_videos, other_files
 */
@Composable
fun ScanStartScreen(scanType: String) {

	val scanViewModel: ScanViewModel = koinViewModel()
	val navController = LocalNavController.current
	val context = LocalContext.current
	val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
	val activity = context as? MainActivity
	val coroutineScope = rememberCoroutineScope()

	// 防止多次页面跳转
	var isRequestFileManagerPermission by remember { mutableStateOf(false) }

	var showFileManagePop by remember { mutableStateOf(false) }
	val hasPermission by scanViewModel.hasFileManagerPermission.collectAsState()
	val hasCheckedFileManagerPermission by scanViewModel.hasCheckedFileManagerPermission.collectAsState()
	val interactionSource = remember { MutableInteractionSource() }

	val fileManagerPermissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestMultiplePermissions(),
		onResult = { permissions ->
			val allGranted = permissions.all { it.value }
			if (allGranted) {
				// 授权成功
			}
			Log.e("TAG", "ScanStartScreen: fileManagerPermissionLauncher回调 $allGranted" )
			showFileManagePop = false
			scanViewModel.checkFileManagerPermission()
			// TODO: add
//			AdConfig.isOpenAppOpenHelper = true
		}
	)

	LaunchedEffect(hasPermission, isRequestFileManagerPermission) {
		if (isRequestFileManagerPermission && hasPermission) {
			isRequestFileManagerPermission = false
			// TODO: add
//			LogUtil.log(LogConfig.filemanage_permission_agree,mapOf())
			navController.navigate("${Routes.Scanning}/${scanType}") {
				popUpTo("${Routes.StartScan}/${scanType}") { inclusive = true }
			}
		}
	}

	DisposableEffect(lifecycleOwner) {
		val observer = LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_CREATE) {
				Log.e("TAG", "ScanStartScreen: 页面创建 ON_CREATE" )
			}
			if (event == Lifecycle.Event.ON_START) {
				Log.e("TAG", "ScanStartScreen: 页面启动 ON_START" )
				showFileManagePop = false
				scanViewModel.checkFileManagerPermission()
				// TODO: add
//				AdConfig.isOpenAppOpenHelper = true
			}
			if (event == Lifecycle.Event.ON_RESUME) {
				Log.e("TAG", "ScanStartScreen: 页面切换回前台 ON_RESUME 检查文件管理权限" )
				showFileManagePop = false
				scanViewModel.checkFileManagerPermission()
				// TODO: add
//				AdConfig.isOpenAppOpenHelper = true
			}
			if (event == Lifecycle.Event.ON_PAUSE) {
				Log.e("TAG", "ScanStartScreen: 页面切换到后台 ON_PAUSE" )
			}
			if (event == Lifecycle.Event.ON_DESTROY) {
				Log.e("TAG", "ScanStartScreen: 页面销毁 ON_DESTROY" )
			}
		}
		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose {
			lifecycleOwner.lifecycle.removeObserver(observer)
		}
	}

	// TODO: add
//	LaunchedEffect(Unit) {
//		when(scanType) {
//			"recovery_photos" -> LogUtil.log(LogConfig.enter_recover_photos, mapOf())
//			"recovery_videos" -> LogUtil.log(LogConfig.enter_recover_videos, mapOf())
//			"recovery_audios" -> LogUtil.log(LogConfig.enter_recover_audios, mapOf())
//			"other_files" -> LogUtil.log(LogConfig.enter_recover_files, mapOf())
//		}
//	}

	// TODO: add
//	LaunchedEffect(Unit) {
//		if (BuildConfig.DEBUG) {
//			Toast.makeText(context, "预加载插屏广告 ${AdConfig.canLoadInter(AdConfig.LOAD_TIME_ENTER_FEATURE)}", Toast.LENGTH_SHORT).show()
//		}
//		if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_ENTER_FEATURE)) {
//			AdLoader.loadInter(context)
//		}
//	}

	SetStatusBarLight(false)
	BackHandler {
		activity?.let {
			// TODO: change
//			Ads.showInterstitialAd(activity = it, AreaKey.returnHomeFromOtherAdv) {
				coroutineScope.launch(Dispatchers.Main) {
					navController.popBackStack()
				}
//			}
		}
	}
	Box(modifier = Modifier
		.fillMaxSize()
		.background(
			brush = Gradient_9E7BFB_to_784BF1,
		)
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(LocalInnerPadding.current)

		){
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 24.dp)
			) {
				Image(
					painter = painterResource(id = R.drawable.back_white),
					contentDescription = null,
					modifier = Modifier
						.size(44.dp)
						.clickable(
							interactionSource = interactionSource,
							indication = null
						) {
							activity?.let {
								// TODO: change
//								Ads.showInterstitialAd(
//									activity = it,
//									AreaKey.returnHomeFromOtherAdv
//								) {
								coroutineScope.launch(Dispatchers.Main) {
									navController.popBackStack()
								}
//								}
							}
						}
				)
				Spacer(
					modifier = Modifier.height(170.dp)
				)
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier
						.align(Alignment.CenterHorizontally)
						.clickable(
							interactionSource = interactionSource,
							indication = null
						) {
							Log.e("TAG", "ScanStartScreen: hasPermission = $hasPermission",)
							if (hasCheckedFileManagerPermission) {
								if (hasPermission) {
									navController.navigate("${Routes.Scanning}/${scanType}") {
										popUpTo("${Routes.StartScan}/${scanType}") {
											inclusive = true
										}
									}
								} else {
									isRequestFileManagerPermission = true
									showFileManagePop = true
								}
							}
						}
				) {
					Image(
						painter = painterResource(id = when(scanType){
							"recovery_photos" -> R.drawable.start_scan_photos
							"recovery_videos" -> R.drawable.start_scan_videos
							"recovery_audios" -> R.drawable.start_scan_audios
							else -> R.drawable.start_scan_other
						}),
						contentDescription = null,
						modifier = Modifier
							.width(100.dp)
							.height(88.dp)
					)
					Spacer(
						modifier = Modifier.height(35.dp)
					)
					Image(
						painter = painterResource(id = R.drawable.start_scan_bg),
						contentDescription = null,
						modifier = Modifier
							.width(242.dp)
							.height(131.dp)
					)
				}
				Spacer(
					modifier = Modifier.height(15.dp)
				)
				Box(
					modifier = Modifier.fillMaxWidth(),
					contentAlignment = Alignment.Center
				) {
					val infiniteTransition = rememberInfiniteTransition()
					val scale by infiniteTransition.animateFloat(
						initialValue = 1f,
						targetValue = 1.05f,
						animationSpec = infiniteRepeatable(
							animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
							repeatMode = RepeatMode.Reverse
						)
					)
					Image(
						painter = painterResource(id = R.drawable.guide_btn_bg),
						contentDescription = null,
						modifier = Modifier
							.fillMaxWidth(0.85f)
							.height(64.8.dp)
							.scale(scale),
						alignment = Alignment.Center,
						contentScale = ContentScale.FillBounds,
					)
					Box(modifier = Modifier
						.fillMaxWidth(0.8f)
						.height(54.dp)
						.padding(10.dp)
						.scale(scale)
						.clickable(
							interactionSource = interactionSource,
							indication = null
						) {
							coroutineScope.launch(Dispatchers.Main) {
								if (hasCheckedFileManagerPermission) {
									if (hasPermission) {
										navController.navigate("${Routes.Scanning}/${scanType}") {
											popUpTo("${Routes.StartScan}/${scanType}") {
												inclusive = true
											}
										}
									} else {
										isRequestFileManagerPermission = true
										showFileManagePop = true
									}
								}
							}
						},
						contentAlignment = Alignment.Center
					) {
						Text(
							text = stringResource(when(scanType){
								"recovery_photos" -> R.string.tap_to_scan_photos
								"recovery_videos" -> R.string.tap_to_scan_videos
								"recovery_audios" -> R.string.tap_to_scan_audios
								else -> R.string.tap_to_scan_other
							}),
							style = TextStyle.TextStyle_20sp_w600_9E7BFB,
							modifier = Modifier,
							textAlign = TextAlign.Center,
							maxLines = 1,
						)
					}
				}


			}
		}

		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.TopEnd
		) {
			val infiniteTransition = rememberInfiniteTransition()
			val scale by infiniteTransition.animateFloat(
				initialValue = 0f,
				targetValue = 1f,
				animationSpec = infiniteRepeatable(
					animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
					repeatMode = RepeatMode.Restart
				)
			)
			Image(
				painter = painterResource(id = R.drawable.scan_guide),
				contentDescription = null,
				modifier = Modifier
					.padding(top = 350.dp - (scale * 50).dp, end = (scale * 80).dp)
					.alpha(scale)
					.width(85.dp)
					.height(66.dp)
			)
		}


		if (showFileManagePop) {
			FileManagePop{ agree ->
				showFileManagePop = false
				// TODO: add
//				AdConfig.isOpenAppOpenHelper = false
				if (agree) {
					scanViewModel.requestFileManagerPermission(
						fileManagerPermissionLauncher
					)
				}
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun ScanStartScreenPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(
		LocalNavController provides navController,
		LocalInnerPadding provides PaddingValues(0.dp)
	) {
		ScanStartScreen("recovery_photos")
	}
}