package com.quickrecover.photonvideotool.view.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quickrecover.photonvideotool.LocalNavController
import com.quickrecover.photonvideotool.MainActivity
import com.quickrecover.photonvideotool.R
import com.quickrecover.photonvideotool.core.AreaKey
import com.quickrecover.photonvideotool.core.bean.AllCapacityInfo
import com.quickrecover.photonvideotool.core.bean.CapacityBean
import com.quickrecover.photonvideotool.core.route.Routes
import com.quickrecover.photonvideotool.ui.theme.Color_68F8EF
import com.quickrecover.photonvideotool.ui.theme.Color_B5FF35
import com.quickrecover.photonvideotool.ui.theme.Color_FFE435
import com.quickrecover.photonvideotool.ui.theme.Color_FFF_25
import com.quickrecover.photonvideotool.ui.theme.Gradient_9E7BFB_to_784BF1
import com.quickrecover.photonvideotool.ui.theme.TextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: add
//import com.pdffox.adv.use.Ads

@Composable
fun CapacityCard(
	isHomeCard: Boolean = false,
	allCapacityInfo: AllCapacityInfo,
	items: List<CapacityBean>
) {
	val navController = LocalNavController.current
	val context = LocalContext.current
	val activity = context as? MainActivity
	val coroutineScope = rememberCoroutineScope()

	fun formatSize(size: Long): String {
		val gb = size.toDouble() / (1024 * 1024 * 1024)
		return String.format("%.1fGB", gb)
	}

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(
				brush = Gradient_9E7BFB_to_784BF1,
				shape = RoundedCornerShape(32.dp)
			)
			.clickable(
				interactionSource = remember { MutableInteractionSource() },
				indication = null
			) {
				if (isHomeCard) {
					activity?.let {
						// TODO: change
//						Ads.showInterstitialAd(it, AreaKey.enterFeatureAdv) {
							coroutineScope.launch(Dispatchers.Main) {
								navController.navigate(Routes.Capacity)
							}
//						}
					}
				}
			}
	) {
		Image(
			painter = painterResource(id = R.drawable.home_card_icon),
			contentDescription = null,
			modifier = Modifier
				.align(Alignment.TopEnd)
				.size(132.dp)
		)
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 32.dp, horizontal = 24.dp)
		) {
			Text(
				text = stringResource(R.string.capacity),
				style = TextStyle.TextStyle_28sp_w600_FFF,
			)
			Spacer(modifier = Modifier.height(12.dp))
			Row (verticalAlignment = Alignment.CenterVertically) {
				Text(
					text = formatSize(allCapacityInfo.usedSize),
					style = TextStyle.TextStyle_20sp_w700_EBF21F,
					modifier = Modifier
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = "/" + formatSize(allCapacityInfo.totalSize),
					style = TextStyle.TextStyle_12sp_w700_FFF_70,
					modifier = Modifier
				)

			}
			Spacer(modifier = Modifier.height(28.dp))
			CapacityInfo(items = items, allCapacityInfo = allCapacityInfo)
		}
	}
}

@Composable
fun CapacityInfo(
	items: List<CapacityBean> = emptyList(),
	allCapacityInfo: AllCapacityInfo
) {
	val unUsed = allCapacityInfo.totalSize - allCapacityInfo.usedSize
	val usedSum = items.sumOf { it.size }
	val otherSize = (allCapacityInfo.usedSize - usedSum).coerceAtLeast(0L) // 防止负数

	val displayItems = if (items.isEmpty()) {
		listOf(
			CapacityBean(name = "Used", size = allCapacityInfo.usedSize),
			CapacityBean(name = "UnUsed", size = unUsed)
		)
	} else {
		items + CapacityBean(name = "Other", size = otherSize) + CapacityBean(name = "UnUsed", size = unUsed)
	}

	val totalSize = displayItems.sumOf { it.size.toDouble() }
	val photosStr = stringResource(R.string.photos)
	val videosStr = stringResource(R.string.videos)
	val otherStr = stringResource(R.string.other)
	val usedStr = stringResource(R.string.used)
	val unusedStr = stringResource(R.string.unused)

	val segments = displayItems.map { item ->
		val ratio = if (totalSize > 0) (item.size.toFloat() / totalSize.toFloat()) else 0f
//		Log.e("TAG", "CapacityInfo: ${item.name} ratio = $ratio")
		val color = when (item.name) {
			"Photos" -> Color_FFE435
			"Videos" -> Color_B5FF35
			"Other" -> Color_68F8EF
			"Used" -> Color_68F8EF
			"UnUsed" -> Color_FFF_25
			else -> Color_FFF_25
		}
		ratio to color
	}

	MultiSegmentProgressBar(
		segments = segments,
		modifier = Modifier.fillMaxWidth()
	)
	Spacer(modifier = Modifier.height(12.dp))
	Row(verticalAlignment = Alignment.CenterVertically) {
		displayItems.forEach { label ->
			CapacityLabel(label.name)
		}
	}
}

@Composable
fun CapacityLabel(title: String) {
	Box(
		modifier = Modifier
			.size(5.dp)
			.background(
				color = when (title) {
					"Photos" -> Color_FFE435
					"Videos" -> Color_B5FF35
					"Other" -> Color_68F8EF
					"Used" -> Color_68F8EF
					"UnUsed" -> Color_FFF_25
					else -> Color_FFF_25
				}, shape = CircleShape
			)
	)
	Spacer(modifier = Modifier.width(6.dp))
	Text(
		text = title,
		style = TextStyle.TextStyle_10sp_w500_FFF,
		modifier = Modifier
	)
	Spacer(modifier = Modifier.width(24.dp))
}

@Composable
fun MultiSegmentProgressBar(
	segments: List<Pair<Float, androidx.compose.ui.graphics.Color>>, // List<比例, 颜色>
	modifier: Modifier = Modifier,
	height: Dp = 8.dp,
	cornerRadius: Dp = 4.dp
) {
//	Log.e("TAG", "MultiSegmentProgressBar: segments = $segments")
	Row(
		modifier = modifier
			.height(height)
			.clip(RoundedCornerShape(cornerRadius))
	) {
		segments.forEachIndexed { index, segment ->
			if (segment.first > 0) {
				Box(
					modifier = Modifier
						.weight(segment.first)
						.fillMaxHeight()
						.background(
							color = segment.second,
							shape = if (index == 0) {
								RoundedCornerShape(
									topStart = cornerRadius,
									bottomStart = cornerRadius
								)
							} else if (index == segments.lastIndex) {
								RoundedCornerShape(topEnd = cornerRadius, bottomEnd = cornerRadius)
							} else {
								RoundedCornerShape(0.dp)
							}
						)
				)
			}
		}
	}
}


//@Preview(showBackground = true)
//@Composable
//fun CapacityCardPreview() {
//	val navController = rememberNavController()
//	CompositionLocalProvider(
//		LocalNavController provides navController,
//		LocalInnerPadding provides PaddingValues(0.dp)
//	) {
//		CapacityCard()
//	}
//}