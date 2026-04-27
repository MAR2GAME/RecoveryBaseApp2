package com.quickrecover.photonvideotool.view

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.bumptech.glide.Glide
import com.quickrecover.photonvideotool.LocalInnerPadding
import com.quickrecover.photonvideotool.LocalNavController
import com.quickrecover.photonvideotool.MainActivity
import com.quickrecover.photonvideotool.R
import com.quickrecover.photonvideotool.core.AreaKey
import com.quickrecover.photonvideotool.core.LogConfig
import com.quickrecover.photonvideotool.core.bean.FoldBean
import com.quickrecover.photonvideotool.core.route.Routes
import com.quickrecover.photonvideotool.ui.theme.Color_9E7BFB
import com.quickrecover.photonvideotool.ui.theme.Gradient_FFF_to_F5F5F5
import com.quickrecover.photonvideotool.ui.theme.TextStyle
import com.quickrecover.photonvideotool.view.widget.BannerAd
import com.quickrecover.photonvideotool.view.widget.ExitPop
import com.quickrecover.photonvideotool.view.widget.NavigationWidget
import com.quickrecover.photonvideotool.viewmodel.RecoveryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel

// TODO: add
//import com.pdffox.adv.use.Ads
//import com.pdffox.adv.use.log.LogUtil

@Composable
fun RecoveryFolderListScreen(scanType: String) {

	val recoveryViewModel: RecoveryViewModel = koinViewModel()

	val navController = LocalNavController.current
	val context = LocalContext.current
	val activity = context as? MainActivity

	val folders by recoveryViewModel.recoveryFolds.collectAsState()
	var showExitPop by remember { mutableStateOf(false) }
	val coroutineScope = rememberCoroutineScope()

	val lifecycleOwner = LocalLifecycleOwner.current
	DisposableEffect(lifecycleOwner) {
		val observer = LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_RESUME) {
				recoveryViewModel.getRecoveryFiles(scanType)
			}
		}
		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose {
			lifecycleOwner.lifecycle.removeObserver(observer)
		}
	}

	LaunchedEffect(Unit) {
		recoveryViewModel.clearCurrentFold(scanType)
	}

	// TODO: add
//	LaunchedEffect(Unit) {
//		LogUtil.log(LogConfig.view_scan_result, mapOf())
//	}
	BackHandler {
		showExitPop = true
	}
	Box(Modifier.fillMaxSize()) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(
					brush = Gradient_FFF_to_F5F5F5,
				)
				.padding(LocalInnerPadding.current)
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 24.dp)
			) {
				NavigationWidget(title = when(scanType) {
					"recovery_photos" -> stringResource(R.string.recovery_photos)
					"recovery_videos" -> stringResource(R.string.recovery_videos)
					"recovery_audios" -> stringResource(R.string.recovery_audios)
					else -> stringResource(R.string.recovery_files)
				}, navController = navController, onBack = {
					showExitPop = true
				})
				Spacer(modifier = Modifier.height(10.dp))
				LazyColumn(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
				) {
					items(folders.size) { index ->
						when(scanType) {
							"recovery_photos" -> PhotoItem(scanType,folders[index])
							"recovery_videos" -> VideoItem(scanType,folders[index])
							"recovery_audios" -> FileItem(scanType,folders[index])
							else -> FileItem(scanType,folders[index])
						}
					}
				}
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(150.dp)
				) {

				}
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

@Composable
fun PhotoItem(scanType: String, foldBean: FoldBean) {
	val navController = LocalNavController.current
	val context = LocalContext.current
	val activity = context as? MainActivity
	val coroutineScope = rememberCoroutineScope()
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(
				interactionSource = remember { MutableInteractionSource() },
				indication = null
			) {
				activity?.let {
					// TODO: change
//					Ads.showInterstitialAd(activity = it, AreaKey.beforeViewResultFolderAdv) {
					coroutineScope.launch(Dispatchers.Main) {
						navController.currentBackStackEntry?.savedStateHandle?.set(
							"foldBean",
							foldBean
						)
						navController.navigate("${Routes.RecoveryFolderItem}/$scanType")
					}
//					}
				}
			}
	) {
		Spacer(modifier = Modifier.height(22.dp))
		Row(verticalAlignment = Alignment.CenterVertically) {
			Text(
				text = foldBean.name,
				style = TextStyle.TextStyle_16sp_w500_252040
			)
			Spacer(modifier = Modifier.width(12.dp))
			Text(
				text = "(${foldBean.files.size})",
				style = TextStyle.TextStyle_16sp_w500_252040_35
			)
			Spacer(modifier = Modifier.weight(1f))
			Image(
				painter = painterResource(id = R.drawable.next),
				contentDescription = null,
				modifier = Modifier
					.size(12.dp)
			)
		}
		Spacer(modifier = Modifier.height(16.dp))
		Row(modifier = Modifier.fillMaxWidth()) {
			val images = foldBean.files.take(3)
			images.forEachIndexed { index, fileData ->
				AsyncImage(
					model = fileData.path,
					contentDescription = fileData.name,
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.size(106.dp)
						.clip(RoundedCornerShape(8.dp))
				)
				if (index != images.lastIndex) {
					Spacer(modifier = Modifier.width(5.dp))
				}
			}
		}
	}
}

@Composable
fun VideoItem(scanType: String, foldBean: FoldBean) {
	val navController = LocalNavController.current
	val context = LocalContext.current
	val activity = context as? MainActivity
	val coroutineScope = rememberCoroutineScope()

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(
				interactionSource = remember { MutableInteractionSource() },
				indication = null
			) {
				activity?.let {
					// TODO: change
//					Ads.showInterstitialAd(activity = it, AreaKey.beforeViewResultFolderAdv) {
					coroutineScope.launch(Dispatchers.Main) {
						navController.currentBackStackEntry?.savedStateHandle?.set(
							"foldBean",
							foldBean
						)
						navController.navigate("${Routes.RecoveryFolderItem}/$scanType")
					}
//					}
				}
			}
	) {
		Spacer(modifier = Modifier.height(22.dp))
		Row(verticalAlignment = Alignment.CenterVertically) {
			Text(
				text = foldBean.name,
				style = TextStyle.TextStyle_16sp_w500_252040
			)
			Spacer(modifier = Modifier.width(12.dp))
			Text(
				text = "(${foldBean.files.size})",
				style = TextStyle.TextStyle_16sp_w500_252040_35
			)
			Spacer(modifier = Modifier.weight(1f))
			Image(
				painter = painterResource(id = R.drawable.next),
				contentDescription = null,
				modifier = Modifier
					.size(12.dp)
			)
		}
		Spacer(modifier = Modifier.height(16.dp))
		Row(modifier = Modifier.fillMaxWidth()) {
			val images = foldBean.files.take(3)
			images.forEachIndexed { index, fileData ->
				VideoThumbnail(
					path = fileData.path,
					modifier = Modifier
						.size(106.dp)
						.clip(RoundedCornerShape(8.dp)),
				)
				if (index != images.lastIndex) {
					Spacer(modifier = Modifier.width(5.dp))
				}
			}
		}
	}
}

@Composable
fun FileItem(scanType: String, foldBean: FoldBean) {
	val navController = LocalNavController.current
	val context = LocalContext.current
	val activity = context as? MainActivity
	val coroutineScope = rememberCoroutineScope()

	Column(modifier = Modifier.fillMaxWidth()) {
		Spacer(modifier = Modifier.height(8.dp))
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.border(
					1.dp,
					Color_9E7BFB.copy(alpha = 0.25f),
					shape = RoundedCornerShape(16.dp)
				)
				.clickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = null
				) {
					activity?.let {
						// TODO: change
//						Ads.showInterstitialAd(activity = it, AreaKey.beforeViewResultFolderAdv) {
						coroutineScope.launch(Dispatchers.Main) {
							navController.currentBackStackEntry?.savedStateHandle?.set(
								"foldBean",
								foldBean
							)
							navController.navigate("${Routes.RecoveryFolderItem}/$scanType")
						}
//						}
					}
				}
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.padding(start = 22.dp, top = 18.dp, bottom = 18.dp, end = 16.dp)
			) {
				Image(
					painter = painterResource(id = R.drawable.icon_files),
					contentDescription = null,
					modifier = Modifier
						.size(36.dp)
				)
				Spacer(modifier = Modifier.width(20.dp))
				Text(
					text = "${foldBean.name}(${foldBean.files.size})",
					style = TextStyle.TextStyle_16sp_w500_252040
				)
				Spacer(modifier = Modifier.weight(1f))
				Image(
					painter = painterResource(id = R.drawable.next),
					contentDescription = null,
					modifier = Modifier
						.size(16.dp)
				)
			}
		}
	}
}

@Composable
fun VideoThumbnail(path: String, modifier: Modifier = Modifier) {
	val context = LocalContext.current
	var bitmap by remember { mutableStateOf<Bitmap?>(null) }

	LaunchedEffect(path) {
		withContext(Dispatchers.IO) {
			try {
				val thumbnailRequest = Glide.with(context)
					.asBitmap()
					.load(path)
					.override(50)
				val loadedBitmap = Glide.with(context)
					.asBitmap()
					.load(path)
					.thumbnail(thumbnailRequest)
					.submit()
					.get()
				bitmap = loadedBitmap
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}

	bitmap?.let {
		Box(
			modifier = modifier
		) {
			Image(
				bitmap = it.asImageBitmap(),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = modifier.clip(RoundedCornerShape(8.dp))
			)
			Image(
				painter = painterResource(id = R.drawable.video_play),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.align(Alignment.Center)
					.size(24.dp),
			)
		}
	}
}

//@Preview
//@Composable
//fun RecoveryFolderScreenPreview() {
//	val navController = rememberNavController()
//	CompositionLocalProvider(
//		LocalNavController provides navController,
//		LocalInnerPadding provides PaddingValues(0.dp)
//	) {
//		RecoveryFolderListScreen("recovery_photos")
//	}
//}