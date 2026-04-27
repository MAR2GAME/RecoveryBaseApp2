package com.quickrecover.photonvideotool.view

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.quickrecover.photonvideotool.LocalInnerPadding
import com.quickrecover.photonvideotool.LocalNavController
import com.quickrecover.photonvideotool.MainActivity
import com.quickrecover.photonvideotool.R
import com.quickrecover.photonvideotool.core.AreaKey
import com.quickrecover.photonvideotool.core.LogConfig
import com.quickrecover.photonvideotool.core.route.Routes
import com.quickrecover.photonvideotool.ui.theme.Gradient_7BFBF7_to_4BE3F1
import com.quickrecover.photonvideotool.ui.theme.Gradient_9E7BFB_to_784BF1
import com.quickrecover.photonvideotool.ui.theme.TextStyle
import com.quickrecover.photonvideotool.view.widget.GradientProgressBar
import com.quickrecover.photonvideotool.view.widget.SetStatusBarLight
import com.quickrecover.photonvideotool.viewmodel.ScanViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// TODO: add
//import com.pdffox.adv.use.Ads
//import com.pdffox.adv.use.adv.AdConfig
//import com.pdffox.adv.use.log.LogUtil

@Composable
fun ScanningScreen(scanType: String) {
	val scanViewModel: ScanViewModel = koinViewModel()
	val navController = LocalNavController.current
	val context = LocalContext.current
	val activity = context as? MainActivity
	val coroutineScope = rememberCoroutineScope()

	val currentPath by scanViewModel.currentScanningPath.collectAsState()

	var dotCount by remember { mutableIntStateOf(0) }
	val progress = remember { Animatable(0f) }

	//旋转进度
	val infiniteTransition = rememberInfiniteTransition()
	val rotation by infiniteTransition.animateFloat(
		initialValue = 0f,
		targetValue = 360f,
		animationSpec = infiniteRepeatable(
			animation = tween(durationMillis = 600, easing = LinearEasing),
			repeatMode = RepeatMode.Restart
		)
	)

	var tips by remember { mutableStateOf("") }
	var tipTime by remember { mutableLongStateOf(0L) }
	var newTip by remember { mutableLongStateOf(0L) }

	LaunchedEffect(newTip) {
		val isExpired = System.currentTimeMillis() - tipTime > 5000
		val canToast = tips.isNotEmpty() && isExpired
		if (canToast) {
			Toast.makeText(navController.context, tips, Toast.LENGTH_SHORT).show()
			tipTime = System.currentTimeMillis()
		}
	}

	LaunchedEffect(Unit) {
		val remainingProgress = 1f - progress.value
		val duration = (6000 * remainingProgress).toInt().coerceAtLeast(300)
		progress.animateTo(
			targetValue = 1.0f,
			animationSpec = tween(durationMillis = duration)
		)

	}

	LaunchedEffect(Unit) {
		while (true) {
			dotCount = (dotCount + 1) % 3 // 0,1,2循环
			delay(500) // 每500毫秒更新一次
		}
	}

	LaunchedEffect(Unit) {
		// TODO: add
//		AdConfig.isOpenAppOpenHelper = true
		scanViewModel.scanFiles(navController.context, scanType)
	}

	LaunchedEffect(scanViewModel.scanFilesResult) {
		val startTime = System.currentTimeMillis()
		scanViewModel.scanFilesResult.collect {
			if (it.isNotEmpty()) {
				val elapsed = System.currentTimeMillis() - startTime
				val delayTime = 6000L - elapsed
				if (delayTime > 0) {
					delay(delayTime)
				}
				activity?.let {
					// TODO: change
//					Ads.showInterstitialAd(activity = it, AreaKey.scanLoadingEndAdv) {
						coroutineScope.launch(Dispatchers.Main) {
							navController.navigate("${Routes.ScanEnd}/${scanType}") {
								popUpTo("${Routes.Scanning}/${scanType}") { inclusive = true }
							}
						}
//					}
				}
			}
		}
	}

	LaunchedEffect(scanViewModel.scanFilesEmpty) {
		val startTime = System.currentTimeMillis()
		scanViewModel.scanFilesEmpty.collect {
			if (it) {
				val elapsed = System.currentTimeMillis() - startTime
				val delayTime = 6000L - elapsed
				if (delayTime > 0) {
					delay(delayTime)
				}
				activity?.let {
					// TODO: change
//					Ads.showInterstitialAd(activity = it, AreaKey.scanLoadingEndAdv) {
						coroutineScope.launch(Dispatchers.Main) {
							navController.navigate("${Routes.ScanEnd}/${scanType}") {
								popUpTo("${Routes.Scanning}/${scanType}") { inclusive = true }
							}
						}
//					}
				}
			}
		}
	}

	// TODO: add
//	LaunchedEffect(Unit) {
//		LogUtil.log(LogConfig.scan_loading_page, mapOf())
//	}

	SetStatusBarLight(false)
	Box(modifier = Modifier
		.fillMaxSize()
		.background(
			brush = Gradient_9E7BFB_to_784BF1,
		)
	) {
		val toastMsg = stringResource(R.string.scanning_please_wait)

		BackHandler {
			tips = toastMsg
			newTip = System.currentTimeMillis()
		}
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
							interactionSource = remember { MutableInteractionSource() },
							indication = null
						) {
							tips = toastMsg
							newTip = System.currentTimeMillis()
						}
				)
				Spacer(
					modifier = Modifier.height(120.dp)
				)
				Box(
					modifier = Modifier
						.align(Alignment.CenterHorizontally)
						.size(300.dp)
				) {
					// 背景
					Image(
						painter = painterResource(id = R.drawable.scanning_bg),
						contentDescription = null,
						modifier = Modifier
							.size(300.dp)
					)
					//旋转进度
					Image(
						painter = painterResource(id = R.drawable.scanning_run_bg),
						contentDescription = null,
						modifier = Modifier
							.width(170.dp)
							.height(170.dp)
							.align(Alignment.Center)
							.rotate(rotation)
					)
					//图标
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
							.height(100.dp)
							.align(Alignment.Center)
					)

				}
				Spacer(
					modifier = Modifier.height(54.dp)
				)
				ConstraintLayout (
					modifier = Modifier.fillMaxWidth()
				) {
					val (loadingText, dotsText) = createRefs()
					Text(
						text = stringResource(R.string.scanning),
						style = TextStyle.TextStyle_14sp_w500_FFF,
						modifier = Modifier.constrainAs(loadingText) {
							start.linkTo(parent.start)
							end.linkTo(parent.end)
							top.linkTo(parent.top)
						}
					)
					Text(
						text = ".".repeat(dotCount),
						style = TextStyle.TextStyle_14sp_w500_FFF,
						modifier = Modifier.constrainAs(dotsText) {
							start.linkTo(loadingText.end)
							bottom.linkTo(loadingText.bottom)
						}
					)
				}
				Spacer( modifier = Modifier.height(22.dp))
				GradientProgressBar(
					progress = progress.value,
					brush = Gradient_7BFBF7_to_4BE3F1,
					modifier = Modifier
						.fillMaxWidth(0.83f)
						.align(Alignment.CenterHorizontally),
				)
				Spacer( modifier = Modifier.height(20.dp))
				Text(
					text = currentPath,
					style = TextStyle.TextStyle_14sp_w500_FFF_70,
					modifier = Modifier.align(Alignment.CenterHorizontally)
				)
			}
		}
	}

}

//@Preview(showBackground = true)
//@Composable
//fun ScanningScreenPreview() {
//	val navController = rememberNavController()
//	CompositionLocalProvider(
//		LocalNavController provides navController,
//		LocalInnerPadding provides PaddingValues(0.dp)
//	) {
//		ScanningScreen("recovery_photos")
//	}
//}