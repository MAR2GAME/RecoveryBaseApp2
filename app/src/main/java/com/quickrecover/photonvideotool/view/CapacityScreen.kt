package com.quickrecover.photonvideotool.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.quickrecover.photonvideotool.LocalInnerPadding
import com.quickrecover.photonvideotool.LocalNavController
import com.quickrecover.photonvideotool.MainActivity
import com.quickrecover.photonvideotool.R
import com.quickrecover.photonvideotool.core.AreaKey
import com.quickrecover.photonvideotool.core.LogConfig
import com.quickrecover.photonvideotool.core.bean.CapacityBean
import com.quickrecover.photonvideotool.ui.theme.Color_9E7BFB_25
import com.quickrecover.photonvideotool.ui.theme.Gradient_FFF_to_FFF
import com.quickrecover.photonvideotool.ui.theme.TextStyle
import com.quickrecover.photonvideotool.view.widget.CapacityCard
import com.quickrecover.photonvideotool.view.widget.NavigationWidget
import com.quickrecover.photonvideotool.viewmodel.HomeViewModel
// TODO: add
//import com.pdffox.adv.use.Ads
//import com.pdffox.adv.use.log.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CapacityScreen(showScanBanner: (CapacityBean) -> Unit = {}) {
	val navController = LocalNavController.current
	val homeViewModel: HomeViewModel = koinViewModel()
	val coroutineScope = rememberCoroutineScope()
	val context = LocalContext.current
	val activity = context as? MainActivity

	val capacityDetailItems by homeViewModel.capacityDetailItems.collectAsState()

	homeViewModel.getAllCapacityInfo()
	homeViewModel.getCapacityItems()
	homeViewModel.getCapacityDetailItems()

// TODO: add
//	LaunchedEffect(Unit) {
//		LogUtil.log(LogConfig.enter_capacity, mapOf())
//	}

//	LaunchedEffect(Unit) {
//		if (BuildConfig.DEBUG) {
//			Toast.makeText(context, "预加载插屏广告 ${AdConfig.canLoadInter(AdConfig.LOAD_TIME_ENTER_FEATURE)}", Toast.LENGTH_SHORT).show()
//		}
//		if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_ENTER_FEATURE)) {
//			AdLoader.loadInter(context)
//		}
//	}

	Box(
		modifier = Modifier
			.fillMaxSize()
			.padding(top = 15.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 24.dp)
		) {
			NavigationWidget(title = stringResource(R.string.capacity), navController = navController, showBack = false, onBack = {
				// TODO: change
//				activity?.let {
//					Ads.showInterstitialAd(activity = it, AreaKey.returnHomeFromOtherAdv) {
						coroutineScope.launch(Dispatchers.Main) {
							navController.popBackStack()
						}
//					}
//				}
			})
			Spacer(modifier = Modifier.height(32.dp))
			CapacityCard(
				allCapacityInfo = homeViewModel.allCapacityInfo.collectAsState().value,
				items = homeViewModel.capacityItems.collectAsState().value
			)
			Spacer(modifier = Modifier.height(22.dp))
			if (capacityDetailItems.isNotEmpty()) {
				LazyColumn(modifier = Modifier.fillMaxSize()) {
					itemsIndexed(capacityDetailItems) { _, item ->
						CapacityItem(item) {
							showScanBanner.invoke(item)
						}
					}
				}
			} else {
				val items = listOf(
					CapacityBean("Photos", 0, 0),
					CapacityBean("Videos", 0, 0),
					CapacityBean("Audios", 0, 0),
					CapacityBean("Downloads", 0, 0),
					CapacityBean("Document", 0, 0),
				)
				LazyColumn(modifier = Modifier.fillMaxSize()) {
					itemsIndexed(items) { _, item ->
						CapacityItem(item) {
							showScanBanner.invoke(item)
						}
					}
				}
			}
		}
	}
}

@Composable
fun CapacityItem(info: CapacityBean, onClick: () -> Unit) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Spacer(modifier = Modifier
			.fillMaxWidth()
			.height(12.dp))
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.border(
					width = 1.dp,
					color = Color_9E7BFB_25,
					shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
				)
				.background(
					brush = Gradient_FFF_to_FFF,
					shape = RoundedCornerShape(24.dp)
				)
				.clickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = null,
					onClick = onClick
				)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp)
			) {
				Image(
					painter = painterResource(id =
					when(info.name) {
						"Photos" -> R.drawable.capacity_photos
						"Videos" -> R.drawable.capacity_videos
						"Audios" -> R.drawable.capacity_audios
						"Document" -> R.drawable.capacity_document
						"Other" -> R.drawable.capacity_other
						else -> R.drawable.capacity_other
					}),
					contentDescription = null,
					modifier = Modifier
						.size(30.dp)
				)
				Spacer(modifier = Modifier.width(12.dp))
				Column(
					modifier = Modifier.weight(1f),
					verticalArrangement = Arrangement.SpaceBetween
				) {
					Text(
						text = info.name,
						maxLines = 1,
						style = TextStyle.TextStyle_18sp_w700_252040
					)
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = stringResource(R.string.files, info.fileCount),
						maxLines = 1,
						style = TextStyle.TextStyle_12sp_w500_252040_35
					)
				}
				Text(
					text = formatFileSize(info.size),
					maxLines = 1,
					style = TextStyle.TextStyle_16sp_w500_252040
				)
			}
		}
	}
}

fun formatFileSize(size: Long): String {
	val kb = 1024L
	val mb = kb * 1024
	val gb = mb * 1024
	val tb = gb * 1024

	return when {
		size >= tb -> String.format("%.2f TB", size.toDouble() / tb)
		size >= gb -> String.format("%.2f GB", size.toDouble() / gb)
		size >= mb -> String.format("%.2f MB", size.toDouble() / mb)
		size >= kb -> String.format("%.2f KB", size.toDouble() / kb)
		else -> "$size B"
	}
}

@Preview(showBackground = true)
@Composable
fun CapacityScreenPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(
		LocalNavController provides navController,
		LocalInnerPadding provides PaddingValues(0.dp)
	) {
		CapacityScreen()
	}
}