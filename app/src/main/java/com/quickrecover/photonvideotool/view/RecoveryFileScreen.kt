package com.quickrecover.photonvideotool.view

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.quickrecover.photonvideotool.LocalInnerPadding
import com.quickrecover.photonvideotool.LocalNavController
import com.quickrecover.photonvideotool.MainActivity
import com.quickrecover.photonvideotool.R
import com.quickrecover.photonvideotool.core.AreaKey
import com.quickrecover.photonvideotool.core.LogConfig
import com.quickrecover.photonvideotool.core.route.Routes
import com.quickrecover.photonvideotool.ui.theme.Gradient_9E7BFB_to_784BF1
import com.quickrecover.photonvideotool.ui.theme.TextStyle
import com.quickrecover.photonvideotool.view.widget.SetStatusBarLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// TODO: add
//import com.pdffox.adv.use.Ads
//import com.pdffox.adv.use.log.LogUtil

@Composable
fun RecoveryFileScreen(scanType: String = "recovery_photos") {
	val navController = LocalNavController.current
	val context = LocalContext.current
	val activity = context as? MainActivity
	val coroutineScope = rememberCoroutineScope()

	val recoveryMsg = stringResource(R.string.recoverying_please_wait)

	val composition by rememberLottieComposition(
		spec = LottieCompositionSpec.RawRes(when(scanType) {
			"recovery_photos" -> R.raw.photos
			"recovery_videos" -> R.raw.videos
			"recovery_audios" -> R.raw.music
			else -> R.raw.files
		}),
		imageAssetsFolder = "images/"
	)

	var tips by remember { mutableStateOf("") }
	var tipTime by remember { mutableLongStateOf(0L) }
	var newTip by remember { mutableLongStateOf(0L) }

	LaunchedEffect(newTip) {
		if (tips.isNotEmpty() && System.currentTimeMillis() - tipTime > 5000) {
			Toast.makeText(navController.context, tips, Toast.LENGTH_SHORT).show()
			tipTime = System.currentTimeMillis()
		}
	}

	LaunchedEffect(Unit) {
		delay(6000)
		activity?.let {
			// TODO: add
//			Ads.showInterstitialAd(activity = it, AreaKey.recoverLoadingEndAdv) {
				coroutineScope.launch(Dispatchers.Main) {
					navController.navigate("${Routes.RecoveryFileSuccess}/${scanType}") {
						popUpTo("${Routes.RecoveryFile}/${scanType}") { inclusive = true }
					}
				}
//			}
		}
	}

	// TODO: add
//	LaunchedEffect(Unit) {
//		LogUtil.log(LogConfig.recover_loading_page, mapOf())
//	}

	SetStatusBarLight(false)
	BackHandler {
		tips = recoveryMsg
		newTip = System.currentTimeMillis()
	}
	Box(modifier = Modifier
		.fillMaxSize()
		.background(
			brush = Gradient_9E7BFB_to_784BF1,
		)
	) {
		LottieAnimation(
			composition = composition,
			modifier = Modifier
				.fillMaxSize()
				.padding(bottom = 100.dp),
			iterations = LottieConstants.IterateForever,
			isPlaying = true
		)
		Text(
			stringResource(R.string.recovering),
			modifier = Modifier
				.align(androidx.compose.ui.Alignment.BottomCenter)
				.padding(bottom = 86.dp),
			style = TextStyle.TextStyle_20sp_w600_FFF
		)
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(LocalInnerPadding.current)
		) {
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
							tips = recoveryMsg
							newTip = System.currentTimeMillis()
						}
				)
			}

		}
	}
}

@Preview(showBackground = true)
@Composable
fun RecoveryFileScreenPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(
		LocalNavController provides navController,
		LocalInnerPadding provides PaddingValues(0.dp)
	) {
		RecoveryFileScreen()
	}
}