package com.datatool.photorecovery.view

import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.datatool.photorecovery.BuildConfig
import com.datatool.photorecovery.LocalNavController
import com.datatool.photorecovery.R
import com.datatool.photorecovery.core.AreaKey
import com.datatool.photorecovery.core.Config
import com.datatool.photorecovery.core.LogConfig
import com.datatool.photorecovery.core.LogParams
import com.datatool.photorecovery.core.route.Routes
import com.datatool.photorecovery.ui.theme.TextStyle
import com.datatool.photorecovery.view.widget.CustomCheckbox
import com.datatool.photorecovery.view.widget.GradientProgressBar
import com.datatool.photorecovery.view.widget.SetStatusBarLight
import com.datatool.photorecovery.viewmodel.SplashViewModel
import com.pdffox.adv.AdvertiseSdk
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun SplashScreen() {

	val navController = LocalNavController.current
	val splashViewModel: SplashViewModel = koinViewModel()
	val context = LocalContext.current
	val activity = context as? Activity

	val navigateToLanguageSetting by splashViewModel.navigateToLanguageSetting.collectAsState()
	val navigateToGuide by splashViewModel.navigateToGuide.collectAsState()
	val navigateToHome by splashViewModel.navigateToHome.collectAsState()
	val isFirstShow by splashViewModel.isFirstShow.collectAsState()

	val progress = remember { Animatable(0f) }
	var dotCount by remember { mutableIntStateOf(0) }
	var checked by remember { mutableStateOf(true) }
	var hasRequestNotification by remember { mutableStateOf(false) }
	var hasRequestUMP by remember { mutableStateOf(false) }
	var hasShowRequestUMP by remember { mutableStateOf(false) }
	var hasUMPCache = false

	val scope = rememberCoroutineScope()
	var animationJob1 by remember { mutableStateOf<Job?>(null) }
	var animationJob2 by remember { mutableStateOf<Job?>(null) }

	val notificationPermissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission(),
		onResult = { granted ->
			Log.e("SplashScreen", "SplashScreen: notificationPermissionLauncher $granted" )
			hasRequestNotification = true
			val params = HashMap<String, Any>().apply {
				put(LogParams.ifOk, granted)
			}
			AdvertiseSdk.logEvent(LogConfig.push_permission_agree,params)
		}
	)

	LaunchedEffect(hasRequestNotification) {
		if (hasRequestNotification) {
			activity?.let {
				val isCacheUMP = AdvertiseSdk.initConsent(it) { onSuccess ->
					if (!onSuccess) {
						hasUMPCache = true
					}
					hasRequestUMP = true
				}
				if (isCacheUMP) {
					// 命中缓存
					hasUMPCache = true
					hasRequestUMP = true
				}
				if (AdvertiseSdk.canPreloadOpen(AdvertiseSdk.LOAD_TIME_OPEN_APP)) {
					AdvertiseSdk.preloadOpen(context)
				}
				if (AdvertiseSdk.canPreloadInterstitial(AdvertiseSdk.LOAD_TIME_OPEN_APP)) {
					AdvertiseSdk.preloadInterstitial(context)
				}
				if (!AdvertiseSdk.isGoogleIp && AdvertiseSdk.canPreloadNative(AdvertiseSdk.LOAD_TIME_OPEN_APP)) {
					AdvertiseSdk.preloadNative(context)
				}
			}
		}
	}

	LaunchedEffect(Unit) {
		splashViewModel.checkFirstShow()
	}

	LaunchedEffect(checked, hasRequestNotification) {
		animationJob1?.cancelAndJoin()
		if (checked && hasRequestNotification) {
			val remainingProgress = 1f - progress.value
			val duration = (3000 * remainingProgress).toInt().coerceAtLeast(300)
			animationJob1 = scope.launch {
				progress.animateTo(
					targetValue = 0.6f,
					animationSpec = tween(durationMillis = duration)
				)
			}
		}
	}

	LaunchedEffect(checked, hasRequestNotification, hasRequestUMP, hasShowRequestUMP) {
		animationJob2?.cancelAndJoin()
		if (checked && hasRequestNotification && hasRequestUMP && hasShowRequestUMP) {
//			val remainingProgress = 1f - progress.value
//			val duration = (6000 * remainingProgress).toInt().coerceAtLeast(300)
			animationJob2 = scope.launch {
				progress.animateTo(
					targetValue = 1.0f,
					animationSpec = tween(durationMillis = 6000)
				)
				Log.e("TAG", "SplashScreen: 超时未播广告进入主页")
				splashViewModel.checkFirstLaunch()
			}
		}
	}

	LaunchedEffect(checked, hasRequestNotification, hasRequestUMP) {
		if (checked && hasRequestNotification && hasRequestUMP) {
			while (true) {
				dotCount = (dotCount + 1) % 3 // 0,1,2循环
				delay(500) // 每500毫秒更新一次
			}
		}
	}

	LaunchedEffect(navigateToLanguageSetting) {
		if (navigateToLanguageSetting) {
			splashViewModel.setFirstShow()
			navController.navigate("${Routes.LANGUAGES}/true") {
				popUpTo(Routes.SPLASH) { inclusive = true }
			}
		}
	}

	LaunchedEffect(navigateToGuide) {
		if (navigateToGuide) {
			splashViewModel.setFirstShow()
			navController.navigate(Routes.GUIDE) {
				popUpTo("${Routes.LANGUAGES}/true") { inclusive = true }
			}
		}
	}

	LaunchedEffect(navigateToHome) {
		if (navigateToHome) {
			navController.navigate(Routes.HOME) {
				popUpTo(Routes.SPLASH) { inclusive = true }
			}
		}
	}

	LaunchedEffect(Unit) {
		Log.e("SplashScreen", "SplashScreen: requestNotificationPermission" )
		val hasPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
		if (hasPermission) {
			hasRequestNotification = true
		} else {
			splashViewModel.requestNotificationPermission(notificationPermissionLauncher)
		}
	}

	LaunchedEffect(hasShowRequestUMP) {
		if (hasShowRequestUMP) {
			fun showOpenAdWithCallbacks() {
				activity?.let {
					AdvertiseSdk.showOpenAd(
						activity,
						AreaKey.openPageAdv,
						onCloseListener = {
							scope.launch {
								Log.e("TAG", "SplashScreen: 播完广告进入主页")
								splashViewModel.checkFirstLaunch()
							}
						},
						onLoadedListener = {
							checked = false
						},
						onPaidListener = { paid ->
//							paid0HasResult = true
//							isPaidUser = paid == 0L
						},
					)
				}
			}
			if (BuildConfig.DEBUG) {
				Log.e("TAG", "SplashScreen: first_open_enabled = ${AdvertiseSdk.isFirstOpenAdEnabled}")
			}
			if (AdvertiseSdk.isFirstOpenAdEnabled != false) {
				showOpenAdWithCallbacks()
			} else {
				scope.launch {
					Log.e("TAG", "SplashScreen: 根据广告策略未播广告进入主页", )
					splashViewModel.checkFirstLaunch()
				}
			}
		}
	}

	LaunchedEffect(hasRequestNotification, hasRequestUMP) {
		if (hasRequestNotification && hasRequestUMP) {
			if (!hasUMPCache) {
				activity?.let {
					AdvertiseSdk.showSplashConsent(it) {
						hasShowRequestUMP = true
					}
				}
			} else {
				hasShowRequestUMP = true
			}
		}
	}
	
	var launchedTime = System.currentTimeMillis()
	LaunchedEffect(Unit) {
		launchedTime = System.currentTimeMillis()
	}

	DisposableEffect(Unit) {
		onDispose {
			AdvertiseSdk.logEvent(LogConfig.splash_display, mapOf(LogParams.duration_time to System.currentTimeMillis() - launchedTime))
		}
	}

	SetStatusBarLight(false)

	BackHandler {

	}

	Box(
		modifier = Modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Image(
			painter = painterResource(id = R.mipmap.splash_bg),
			contentDescription = null,
			modifier = Modifier.fillMaxSize(),
			contentScale = ContentScale.Crop
		)
		Column(
			modifier = Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally) {
			Spacer(modifier = Modifier.height(100.dp))
			Image(
				painter = painterResource(id = R.drawable.aplash_logo),
				contentDescription = null,
				modifier = Modifier
					.width(138.dp)
					.height(138.dp)
					.clip(RoundedCornerShape(16.dp)),
				contentScale = ContentScale.Crop
			)
			Spacer(modifier = Modifier.height(12.dp))
			Text(
				stringResource(R.string.app_name),
				style = TextStyle.TextStyle_20sp_w700_FFF
			)
			Spacer(modifier = Modifier.height(254.dp))
			GradientProgressBar(progress = progress.value, modifier = Modifier.fillMaxWidth(0.83f))
			Spacer(modifier = Modifier.height(24.dp))
			ConstraintLayout (
				modifier = Modifier.fillMaxWidth()
			) {
				val (loadingText, dotsText) = createRefs()
				Text(
					text = stringResource(id = R.string.loading),
					style = TextStyle.TextStyle_14sp_w700_252040,
					modifier = Modifier.constrainAs(loadingText) {
						start.linkTo(parent.start)
						end.linkTo(parent.end)
						top.linkTo(parent.top)
					}
				)
				Text(
					text = ".".repeat(dotCount),
					style = TextStyle.TextStyle_14sp_w700_252040,
					modifier = Modifier.constrainAs(dotsText) {
						start.linkTo(loadingText.end)
						bottom.linkTo(loadingText.bottom)
					}
				)
			}
			Spacer(modifier = Modifier.weight(1f))
			if (isFirstShow) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.Center
				){
					CustomCheckbox(
						checked = checked,
						modifier = Modifier.size(22.dp),
						onCheckedChange = { checked = it }
					)
					Spacer(modifier = Modifier.width(8.dp))
					Text(
						text = "Agree ",
						style = TextStyle.TextStyle_14sp_w500_252040_35
					)
					Text(
						text = stringResource(R.string.privacy_policy),
						style = TextStyle.TextStyle_14sp_w600_252040,
						modifier = Modifier
							.clickable(
								indication = null,
								interactionSource = remember { MutableInteractionSource() }
							) {
								checked = false
								val encodedUrl = URLEncoder.encode(Config.PrivacyUrl, StandardCharsets.UTF_8.toString())
								navController.navigate("${Routes.Website}/$encodedUrl")
							}
					)
					Text(
						text = " & ",
						style = TextStyle.TextStyle_14sp_w500_252040_35
					)
					Text(
						text = stringResource(R.string.terms_of_service),
						style = TextStyle.TextStyle_14sp_w600_252040,
						modifier = Modifier
							.clickable(
								indication = null,
								interactionSource = remember { MutableInteractionSource() }
							) {
								checked = false
								val encodedUrl = URLEncoder.encode(Config.TermUrl, StandardCharsets.UTF_8.toString())
								navController.navigate("${Routes.Website}/$encodedUrl")
							}
					)
				}
			}
			Spacer(modifier = Modifier.height(50.dp))
		}
	}

}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(LocalNavController provides navController) {
		SplashScreen()
	}
}
