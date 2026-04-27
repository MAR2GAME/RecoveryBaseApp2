package com.quickrecover.photonvideotool.view

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.quickrecover.photonvideotool.core.RedTipManager
import com.quickrecover.photonvideotool.core.RedTipRecord
import com.quickrecover.photonvideotool.core.bean.CapacityBean
import com.quickrecover.photonvideotool.core.route.Routes
import com.quickrecover.photonvideotool.ui.theme.Gradient_FFF_to_F5F5F5
import com.quickrecover.photonvideotool.ui.theme.TextStyle
import com.quickrecover.photonvideotool.view.widget.ExitAppPop
import com.quickrecover.photonvideotool.view.widget.ScanFilePop
import com.quickrecover.photonvideotool.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// TODO: add
//import com.pdffox.adv.use.Ads

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

	val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

	val currentPage = remember { mutableIntStateOf(0) }
	val interactionSource = remember { MutableInteractionSource() }
	var showExitAppPop by remember { mutableStateOf(false) }

	val context = LocalContext.current
	val activity = context as? MainActivity

	var showScanPop by remember { mutableStateOf(false) }
	var currentItem by remember { mutableStateOf<CapacityBean?>(null) }
	val coroutineScope = rememberCoroutineScope()
	val navController = LocalNavController.current

	var recoveryTipPhoto by remember { mutableStateOf(false) }
	var toolsTipPhoto by remember { mutableStateOf(false) }

	var route by remember { mutableStateOf( "") }
	val homeViewModel: HomeViewModel = koinViewModel()
	val hasPermission by homeViewModel.hasFileManagerPermission.collectAsState()

	DisposableEffect(lifecycleOwner) {
		val observer = LifecycleEventObserver { _, event ->
			toolsTipPhoto = RedTipManager.canShowRedTip(RedTipManager.RECOVERY_TOOLS_KEY)
		 	val showRedTipPhoto = RedTipManager.canShowRedTip(RedTipManager.RECOVERY_PHOTOS_KEY)
			val showRedTipVideo = RedTipManager.canShowRedTip(RedTipManager.RECOVERY_VIDEOS_KEY)
			val showRedTipAudio = RedTipManager.canShowRedTip(RedTipManager.RECOVERY_AUDIO_KEY)
			val showRedTipFile = RedTipManager.canShowRedTip(RedTipManager.RECOVERY_FILE_KEY)
			recoveryTipPhoto = showRedTipPhoto || showRedTipVideo || showRedTipAudio || showRedTipFile
		}
		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose {
			lifecycleOwner.lifecycle.removeObserver(observer)
		}
	}

	LaunchedEffect(route) {
		if (BuildConfig.DEBUG) {
			Log.e("TAG", "MainScreen: 跳转逻辑 $route" )
		}
		if (route.isNotEmpty()) {
			when (route) {
				"Photos" -> {
					navController.navigate("${Routes.StartScan}/${"recovery_photos"}")
				}
				"Videos" -> {
					navController.navigate("${Routes.StartScan}/${"recovery_videos"}")
				}
				"Files" -> {
					navController.navigate("${Routes.StartScan}/${"other_files"}")
				}
				"Recovered" -> {
//					delay(500)
//					if (hasPermission) {
						navController.navigate("${Routes.RecoveredFiles}/${"recovery_photos"}")
//					} else {
//						isRequestFileManagerPermission = true
//						showFileManagePop = true
//					}
				}
			}
			val activity = context as? MainActivity
			activity?.route = ""
			route = ""
		}
	}

	BackHandler{
		showExitAppPop = true
	}
	Column(modifier = Modifier
		.fillMaxSize()
		.background(
			brush = Gradient_FFF_to_F5F5F5,
		)
		.padding(LocalInnerPadding.current)
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
		) {
			when(currentPage.intValue) {
				0 -> HomeScreen()
				1 -> CapacityScreen { item ->
					showScanPop = true
					currentItem = item
				}
				2-> SettingsScreen()
			}
		}
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.wrapContentHeight()
				.padding(top = 11.dp, bottom = 20.dp)
		) {
			val modifier = Modifier
				.weight(1f)
				.wrapContentHeight()
			TabItem(
				modifier.clickable(
					interactionSource = interactionSource,
					indication = null
				) {
					if (currentPage.intValue == 0) return@clickable
					activity?.let {
						// TODO: add
//						Ads.showInterstitialAd(activity = it, AreaKey.tabNavigationAdv) {
							coroutineScope.launch(Dispatchers.Main) {
								currentPage.intValue = 0
							}
//						}
					}
				 },
				redTips = recoveryTipPhoto,
				TabItemInfo(
					stringResource(R.string.recovery),
					R.drawable.main_ic_recovery_unselecte,
					R.drawable.main_ic_recovery_selected
				),
				isSelected = currentPage.intValue == 0
			)
			TabItem(
				modifier.clickable(
					interactionSource = interactionSource,
					indication = null
				) {
					if (currentPage.value == 1) return@clickable
					activity?.let {
						// TODO: add
//						Ads.showInterstitialAd(activity = it, AreaKey.tabNavigationAdv) {
							coroutineScope.launch(Dispatchers.Main) {
								// TODO: 申请文件管理权限
								currentPage.intValue = 1
								RedTipManager.addRecord(RedTipRecord(RedTipManager.RECOVERY_TOOLS_KEY, System.currentTimeMillis()))
								toolsTipPhoto = false
							}
//						}
					}
				  },
				redTips = toolsTipPhoto,
				TabItemInfo(
					stringResource(R.string.tools),
					R.drawable.main_ic_tools_unselect,
					R.drawable.main_ic_tools_selected
				),
				isSelected = currentPage.intValue == 1
			)
			TabItem(
				modifier.clickable(
					interactionSource = interactionSource,
					indication = null
				) {
					if (currentPage.value == 2) return@clickable
					activity?.let {
						// TODO: add
//						Ads.showInterstitialAd(activity = it, AreaKey.tabNavigationAdv) {
							coroutineScope.launch(Dispatchers.Main) {
								currentPage.intValue = 2
							}
//						}
					}
				},
				redTips = false,
				TabItemInfo(
					stringResource(R.string.settings),
					R.drawable.main_ic_settings_unselect,
					R.drawable.main_ic_settings_selected
				),
				isSelected = currentPage.intValue == 2
			)
		}
//		Spacer(modifier = Modifier
//			.fillMaxWidth()
//			.height(24.dp))
	}

	if (showScanPop) {
		if (BuildConfig.DEBUG) {
			Log.e("TAG", "CapacityScreen: $currentItem", )
		}
		currentItem?.let { item ->
			ScanFilePop(when(item.name) {
				"Photos" -> "recovery_photos"
				"Videos" -> "recovery_videos"
				"Audios" -> "recovery_audios"
				else -> "other_files"
			}, item.fileCount) { ok, scanType ->
				if (ok) {
					Log.e("TAG", "MainScreen: $ok $scanType $item" )
					activity?.let {
						// TODO: add
//						Ads.showInterstitialAd(it, AreaKey.enterFeatureAdv) {
							coroutineScope.launch(Dispatchers.Main) {
								navController.navigate("${Routes.StartScan}/$scanType")
							}
//						}
					}
				}
				showScanPop = false
			}
		}
	}

	if (showExitAppPop) {
		ExitAppPop{ agree ->
			if (agree) {
				activity?.finish()
			}
			showExitAppPop = false
		}
	}

	route = activity?.route ?: ""

}

data class TabItemInfo(
	val name: String,
	val icon: Int,
	val iconSelected: Int
)

@Composable
fun TabItem(modifier: Modifier, redTips: Boolean, info: TabItemInfo, isSelected: Boolean) {
	Box(modifier = modifier) {
		Column (
			modifier = Modifier.fillMaxWidth(),
			horizontalAlignment =Alignment.CenterHorizontally,
			verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
		) {
			Image(
				painter = painterResource(id = if (isSelected) info.iconSelected else info.icon),
				contentDescription = null,
				modifier = Modifier
					.size(32.dp)
			)
			Text(
				text = info.name,
				style = if (isSelected) TextStyle.TextStyle_16sp_w600_9E7BFB else TextStyle.TextStyle_16sp_w600_252040,
				textAlign = TextAlign.Center,
				modifier = Modifier.wrapContentSize()
			)
		}
		if (redTips && !isSelected) {
			Box(
				modifier = Modifier
					.size(height = 12.dp, width = 52.dp)
					.padding(start = 40.dp)
					.align(Alignment.TopCenter)
					.background(
						color = androidx.compose.ui.graphics.Color.Red,
						shape = RoundedCornerShape(6.dp)
					)
			)
		}
	}
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(
		LocalNavController provides navController,
		LocalInnerPadding provides PaddingValues(0.dp)
	) {
		MainScreen()
	}
}