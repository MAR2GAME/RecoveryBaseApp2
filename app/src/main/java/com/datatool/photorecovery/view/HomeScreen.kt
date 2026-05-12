package com.datatool.photorecovery.view

import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.datatool.photorecovery.LocalInnerPadding
import com.datatool.photorecovery.LocalNavController
import com.datatool.photorecovery.MainActivity
import com.datatool.photorecovery.R
import com.datatool.photorecovery.core.AreaKey
import com.datatool.photorecovery.core.Config
import com.datatool.photorecovery.core.LogConfig
import com.datatool.photorecovery.core.LogParams
import com.datatool.photorecovery.core.RedTipManager
import com.datatool.photorecovery.core.RedTipRecord
import com.datatool.photorecovery.core.route.Routes
import com.datatool.photorecovery.ui.theme.Color_9E7BFB
import com.datatool.photorecovery.ui.theme.Color_9E7BFB_25
import com.datatool.photorecovery.ui.theme.Gradient_9E7BFB_to_784BF1
import com.datatool.photorecovery.ui.theme.Gradient_FFF_to_F5F5F5
import com.datatool.photorecovery.ui.theme.Gradient_FFF_to_FFF
import com.datatool.photorecovery.ui.theme.TextStyle
import com.pdffox.adv.compose.BannerAd
import com.datatool.photorecovery.view.widget.FileManagePop
import com.datatool.photorecovery.view.widget.NavigationWidget
import com.datatool.photorecovery.view.widget.NotificationPop
import com.datatool.photorecovery.view.widget.NotificationTips
import com.datatool.photorecovery.view.widget.SetStatusBarLight
import com.datatool.photorecovery.viewmodel.HomeViewModel
import com.google.android.gms.ads.BaseAdView
import com.google.android.gms.ads.nativead.NativeAd
import com.pdffox.adv.AdvertiseSdk
import com.datatool.photorecovery.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
	nativeAd: NativeAd?,
) {
	val homeViewModel: HomeViewModel = koinViewModel()
	val navController = LocalNavController.current
	val context = LocalContext.current
	val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
	val activity = context as? MainActivity

	var isRequestFileManagerPermission by remember { mutableStateOf(false) }
	var showFileManagePop by remember { mutableStateOf(false) }
	var showNotificationPop by remember { mutableStateOf(false) }
	val hasPermission by homeViewModel.hasFileManagerPermission.collectAsState()
	val hasNotificationPermission by homeViewModel.hasNotificationPermission.collectAsState()
	var route by remember { mutableStateOf( "") }

	val coroutineScope = rememberCoroutineScope()

	var showRedTipPhoto by remember { mutableStateOf(false) }
	var showRedTipVideo by remember { mutableStateOf(false) }
	var showRedTipAudio by remember { mutableStateOf(false) }
	var showRedTipFile by remember { mutableStateOf(false) }

	val notificationPermissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission(),
		onResult = { granted ->
			// 处理权限结果
			val params = HashMap<String, Any>().apply {
				put(LogParams.ifOk, granted)
			}
			AdvertiseSdk.logEvent(LogConfig.push_permission_agree,params)
			if (granted) {
				homeViewModel.checkNotificationPermission()
			} else {
				val activity = context as? android.app.Activity
				if (activity != null) {
					val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
						activity,
						android.Manifest.permission.POST_NOTIFICATIONS
					)
					if (shouldShowRationale) {
						// 用户拒绝了权限，但没有勾选“不再询问�?
						showNotificationPop = true // 你可以提示用户重新请求权�?
					} else {
						// 用户拒绝了权限，并且勾选了“不再询问�?
						// 你可以引导用户去设置页手动开启权�?
						showNotificationPop = true // 也可以弹出提示，引导跳转设置
					}
				}
			}
		}
	)

	val fileManagerPermissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestMultiplePermissions(),
		onResult = { permissions ->
			val allGranted = permissions.all { it.value }
			homeViewModel.checkFileManagerPermission()
			AdvertiseSdk.isAppOpenAdEnabled = true
		}
	)

	LaunchedEffect(hasPermission, isRequestFileManagerPermission) {
		if (isRequestFileManagerPermission && hasPermission) {
			isRequestFileManagerPermission = false
			AdvertiseSdk.logEvent(LogConfig.filemanage_permission_agree,mapOf())
			navController.navigate("${Routes.RecoveredFiles}/${"recovery_photos"}")
		}
	}

	DisposableEffect(lifecycleOwner) {
		val observer = LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_RESUME) {
				homeViewModel.checkFileManagerPermission()
				AdvertiseSdk.isAppOpenAdEnabled = true
			}
			if (event == Lifecycle.Event.ON_START) {
				homeViewModel.checkFileManagerPermission()
				AdvertiseSdk.isAppOpenAdEnabled = true
			}

			showRedTipPhoto = RedTipManager.canShowRedTip(RedTipManager.RECOVERY_PHOTOS_KEY)
			showRedTipVideo = RedTipManager.canShowRedTip(RedTipManager.RECOVERY_VIDEOS_KEY)
			showRedTipAudio = RedTipManager.canShowRedTip(RedTipManager.RECOVERY_AUDIO_KEY)
			showRedTipFile = RedTipManager.canShowRedTip(RedTipManager.RECOVERY_FILE_KEY)
		}
		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose {
			lifecycleOwner.lifecycle.removeObserver(observer)
		}
	}

	LaunchedEffect(Unit) {
		// TODO:  记录渠道来源
		AdvertiseSdk.logEvent(LogConfig.enter_homepage, mapOf(LogParams.channel to ""))
	}

	homeViewModel.checkNotificationPermission()
	homeViewModel.getAllCapacityInfo()
	homeViewModel.getCapacityItems()

//	LaunchedEffect(route) {
//		if (route.isNotEmpty()) {
//			when (route) {
//				"Photos" -> {
//					navController.navigate("${Routes.StartScan}/${"recovery_photos"}")
//				}
//				"Videos" -> {
//					navController.navigate("${Routes.StartScan}/${"recovery_videos"}")
//				}
//				"Files" -> {
//					navController.navigate("${Routes.StartScan}/${"other_files"}")
//				}
//				"Recovered" -> {
//					delay(500)
//					if (hasPermission) {
//						navController.navigate("${Routes.RecoveredFiles}/${"recovery_photos"}")
//					} else {
//						isRequestFileManagerPermission = true
//						showFileManagePop = true
//					}
//				}
//			}
//			val activity = context as? MainActivity
//			activity?.route = ""
//			route = ""
//		}
//	}

	LaunchedEffect(Unit) {
		if (!Config.hasShowNotificationPermission) {
			homeViewModel.requestNotificationPermission(notificationPermissionLauncher)
			Config.hasShowNotificationPermission = true
		}
	}

	SetStatusBarLight(true)
	Box(
		Modifier.fillMaxSize()
	) {
		Box(modifier = Modifier
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
				NavigationWidget(title = stringResource(R.string.photo_recovery), navController = navController, showBack = false)
				Spacer(modifier = Modifier.height(16.dp))
				if (!hasNotificationPermission) {
					NotificationTips {
						homeViewModel.requestNotificationPermission(notificationPermissionLauncher)
					}
					Spacer(modifier = Modifier.height(26.dp))
				}
				Spacer(modifier = Modifier.height(24.dp))
				Row {
					LinkCard(
						modifier = Modifier.weight(1f),
						redTips = showRedTipPhoto,
						title = stringResource(R.string.photos),
						icon = R.drawable.recovery_photos_new
					) {
						RedTipManager.addRecord(
							RedTipRecord(
								RedTipManager.RECOVERY_PHOTOS_KEY,
								System.currentTimeMillis()
							)
						)
						activity?.let {
							AdvertiseSdk.showInterstitialAd(it, AreaKey.enterFeatureAdv) {
								coroutineScope.launch(Dispatchers.Main) {
									navController.navigate("${Routes.StartScan}/${"recovery_photos"}")
								}
							}
						}
					}
					Spacer(modifier = Modifier.width(15.dp))
					LinkCard(
						modifier = Modifier.weight(1f),
						redTips = showRedTipVideo,
						title = stringResource(R.string.videos),
						icon = R.drawable.recovery_videos_new
					) {
						RedTipManager.addRecord(
							RedTipRecord(
								RedTipManager.RECOVERY_VIDEOS_KEY,
								System.currentTimeMillis()
							)
						)
						activity?.let {
							AdvertiseSdk.showInterstitialAd(it, AreaKey.enterFeatureAdv) {
								coroutineScope.launch(Dispatchers.Main) {
									navController.navigate("${Routes.StartScan}/${"recovery_videos"}")
								}
							}
						}
					}
				}
				Spacer(modifier = Modifier.height(15.dp))
				Row {
					LinkCard(
						modifier = Modifier.weight(1f),
						redTips = showRedTipAudio,
						title = stringResource(R.string.audios),
						icon = R.drawable.recovery_audios_new
					) {
						RedTipManager.addRecord(
							RedTipRecord(
								RedTipManager.RECOVERY_AUDIO_KEY,
								System.currentTimeMillis()
							)
						)
						activity?.let {
							AdvertiseSdk.showInterstitialAd(it, AreaKey.enterFeatureAdv) {
								coroutineScope.launch(Dispatchers.Main) {
									navController.navigate("${Routes.StartScan}/${"recovery_audios"}")
								}
							}
						}
					}
					Spacer(modifier = Modifier.width(15.dp))
					LinkCard(
						modifier = Modifier.weight(1f),
						redTips = showRedTipFile,
						title = stringResource(R.string.other_files),
						icon = R.drawable.other_files_new
					) {
						RedTipManager.addRecord(
							RedTipRecord(
								RedTipManager.RECOVERY_FILE_KEY,
								System.currentTimeMillis()
							)
						)
						activity?.let {
							AdvertiseSdk.showInterstitialAd(it, AreaKey.enterFeatureAdv) {
								coroutineScope.launch(Dispatchers.Main) {
									navController.navigate("${Routes.StartScan}/${"other_files"}")
								}
							}
						}
					}
				}
				Spacer(modifier = Modifier.height(15.dp))
				LinkCard(
					modifier = Modifier.fillMaxWidth(),
					redTips = false,
					desc = stringResource(R.string.check_all_your_recovered_data),
					title = stringResource(R.string.recovered_files),
					icon = R.drawable.recovered_files_new
				) {
					activity?.let {
						AdvertiseSdk.showInterstitialAd(it, AreaKey.enterFeatureAdv) {
							coroutineScope.launch(Dispatchers.Main) {
								if (hasPermission) {
									navController.navigate("${Routes.RecoveredFiles}/${"recovery_photos"}")
								} else {
									isRequestFileManagerPermission = true
									showFileManagePop = true
								}
							}
						}
					}
				}
			}
		}

		if (showFileManagePop) {
			FileManagePop(nativeAd){ agree ->
				AdvertiseSdk.isAppOpenAdEnabled = false
				if (agree) {
					homeViewModel.requestFileManagerPermission(
						fileManagerPermissionLauncher
					)
				}
				showFileManagePop = false
			}
		}

		if (showNotificationPop) {
			NotificationPop(nativeAd){ agree ->
				if (agree) {
					val activity = context as? android.app.Activity
					if (activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.POST_NOTIFICATIONS)) {
						// 用户选择了“不再询问”，跳转设置�?
						val intent = Intent().apply {
							action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
							putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
						}
						context.startActivity(intent)
					}
				}
				showNotificationPop = false
			}
		}
	}

	route = activity?.route ?: ""

}

@Composable
fun LinkCard(modifier: Modifier, redTips: Boolean, desc: String = stringResource(R.string.recover), title: String, icon: Int, onClick: () -> Unit) {
	Box(
		modifier = modifier
			.height(122.dp)
			.background(
				brush = Gradient_FFF_to_FFF,
				shape = RoundedCornerShape(16.dp)
			)
			.border(
				width = 1.dp,
				color = Color_9E7BFB_25,
				shape = RoundedCornerShape(16.dp)
			)
			.clickable(
				onClick = onClick,
				interactionSource = remember { MutableInteractionSource() },
				indication = null
			),
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 18.dp),
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Box(modifier = Modifier
				.size(60.dp)
				.padding(top = 5.dp, end = 5.dp)) {
				Image(
					painter = painterResource(id = icon),
					contentDescription = null,
					modifier = Modifier.size(55.dp)
				)
				if (redTips) {
					Box(
						modifier = Modifier
							.size(10.dp)
							.align(Alignment.TopEnd)
							.background(
								color = Color.Red,
								shape = RoundedCornerShape(6.dp)
							)
					)
				}
			}
			Text(
				text = desc,
				style = TextStyle.TextStyle_11sp_w600_252040_65,
				maxLines = 1,
			)
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = title,
				style = TextStyle.TextStyle_18sp_w600_252040,
				maxLines = 1,
			)
		}
	}
}
