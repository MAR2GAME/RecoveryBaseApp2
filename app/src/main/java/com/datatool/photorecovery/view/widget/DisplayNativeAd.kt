package com.datatool.photorecovery.view.widget

import com.pdffox.adv.AdvertiseSdk
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.datatool.photorecovery.R
import com.datatool.photorecovery.ui.theme.Color_9E7BFB
import com.datatool.photorecovery.ui.theme.Color_C1C5C7
import com.datatool.photorecovery.ui.theme.Color_ECEFF3
import com.datatool.photorecovery.ui.theme.TextStyle.TextStyle_13sp_w400_303C54
import com.datatool.photorecovery.ui.theme.TextStyle.TextStyle_16sp_w500_303C54
import com.google.android.gms.ads.nativead.NativeAd

@Composable
fun DisplayNativeAd1(nativeAd: NativeAd) {
	// 1. 确保 NativeAdView 是最外层，且 padding 在其内部处理，防止资源超出边界
	NativeAdView(
		nativeAd = nativeAd,
		modifier = Modifier
			.height(320.dp)
			.fillMaxWidth()
			.background(
				Color_ECEFF3,
				shape = RoundedCornerShape(10.5.dp)
			)
			.padding(horizontal = 16.dp, vertical = 11.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 10.dp),
			horizontalAlignment = Alignment.Start
		) {
			// 头部：图标 + 标题 + 评分 + 广告标识
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.Start
			) {
				// 图标展示
				nativeAd.icon?.let { icon ->
					NativeAdIconView(Modifier.size(34.dp)) { // 给图标固定大小，防止撑开
						icon.drawable?.toBitmap()?.let { bitmap ->
							Image(
								bitmap = bitmap.asImageBitmap(),
								contentDescription = "Ad Icon",
								modifier = Modifier
									.fillMaxSize()
									.clip(RoundedCornerShape(5.dp))
							)

						}
					}
				}

				Column(modifier = Modifier
					.padding(start = 8.dp)
					.weight(1f)) {
					Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
						// 2. 广告标识：置于右上角，避免覆盖图标导致 "Outside boundary" 报错
						NativeAdAttribution(
							modifier = Modifier,
							containerColor = Color_C1C5C7,
							padding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
							contentColor = Color.White,
							shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 6.dp),
							text = stringResource(R.string.attribution),
						)
						Spacer(modifier = Modifier.width(4.dp))
						// 标题
						nativeAd.headline?.let {
							NativeAdHeadlineView {
								Text(
									text = it,
									style = TextStyle_16sp_w500_303C54,
									maxLines = 1,
									overflow = TextOverflow.Ellipsis
								)
							}
						}
					}
					Spacer(modifier = Modifier.height(6.dp))
					// 正文内容
					nativeAd.body?.let {
						NativeAdBodyView(modifier = Modifier.fillMaxWidth()) {
							Text(
								text = it,
								style = TextStyle_13sp_w400_303C54,
								maxLines = 2,
								overflow = TextOverflow.Ellipsis
							)
						}
					}
				}
			}

			Spacer(modifier = Modifier.height(8.dp))

			NativeAdMediaView(
				modifier = Modifier
					.fillMaxWidth()
					.height(180.dp) // 增加高度
					.padding(vertical = 8.dp)
			)

			// 底部操作区：价格/店铺 + 按钮
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween
			) {
//				Row {
//					nativeAd.price?.let {
//						NativeAdPriceView {
//							Text(text = it, style = MaterialTheme.typography.labelSmall)
//						}
//					}
//					nativeAd.store?.let {
//						NativeAdStoreView(Modifier.padding(start = 4.dp)) {
//							Text(text = it, style = MaterialTheme.typography.labelSmall)
//						}
//					}
//				}
				// 行动呼吁按钮 (Call to Action)
				nativeAd.callToAction?.let { callToAction ->
					NativeAdCallToActionView(modifier = Modifier.fillMaxWidth()) {
						// 确保你的 NativeAdButton 组件内部也是一个可点击的 Button
						NativeAdButton(text = callToAction, modifier = Modifier.fillMaxWidth().height(44.dp), containerColor = Color_9E7BFB, contentColor = Color.White)
					}
				}
			}
		}
	}
}

@Composable
fun DisplayNativeAd2(nativeAd: NativeAd) {
	// 1. 确保 NativeAdView 是最外层，且 padding 在其内部处理，防止资源超出边界
	NativeAdView(
		nativeAd = nativeAd,
		modifier = Modifier
			.fillMaxWidth()
			.height(200.dp)
			.background(
				Color_ECEFF3,
				shape = RoundedCornerShape(10.5.dp)
			)
			.padding(horizontal = 16.dp, vertical = 11.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 10.dp),
			horizontalAlignment = Alignment.Start
		) {
			// 头部：图标 + 标题 + 评分 + 广告标识
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.Start,
				verticalAlignment = Alignment.CenterVertically
			) {
				NativeAdMediaView(
					modifier = Modifier
						.weight(1f)
						.height(120.dp)
				)

				Spacer(modifier = Modifier.width(8.dp))

				Column(modifier = Modifier
					.padding(start = 8.dp)
					.weight(1f)) {
					Row(modifier = Modifier.fillMaxWidth()) {
						// 2. 广告标识：置于右上角，避免覆盖图标导致 "Outside boundary" 报错
						NativeAdAttribution(
							modifier = Modifier,
							containerColor = Color_C1C5C7,
							padding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
							contentColor = Color.White,
							shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 6.dp),
							text = stringResource(R.string.attribution),
						)
						Spacer(modifier = Modifier.width(4.dp))
						// 标题
						nativeAd.headline?.let {
							NativeAdHeadlineView {
								Text(
									text = it,
									style = TextStyle_16sp_w500_303C54,
									maxLines = 2,
									overflow = TextOverflow.Ellipsis
								)
							}
						}
					}
					Spacer(modifier = Modifier.height(6.dp))
					// 正文内容
					nativeAd.body?.let {
						NativeAdBodyView(modifier = Modifier.fillMaxWidth()) {
							Text(
								text = it,
								style = TextStyle_13sp_w400_303C54,
								overflow = TextOverflow.Ellipsis
							)
						}
					}
				}
			}

			// 底部操作区：价格/店铺 + 按钮
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween
			) {
//				Row {
//					nativeAd.price?.let {
//						NativeAdPriceView {
//							Text(text = it, style = MaterialTheme.typography.labelSmall)
//						}
//					}
//					nativeAd.store?.let {
//						NativeAdStoreView(Modifier.padding(start = 4.dp)) {
//							Text(text = it, style = MaterialTheme.typography.labelSmall)
//						}
//					}
//				}
				// 行动呼吁按钮 (Call to Action)
				nativeAd.callToAction?.let { callToAction ->
					NativeAdCallToActionView(modifier = Modifier.fillMaxWidth()) {
						// 确保你的 NativeAdButton 组件内部也是一个可点击的 Button
						NativeAdButton(text = callToAction, modifier = Modifier.fillMaxWidth().height(44.dp), containerColor = Color_9E7BFB, contentColor = Color.White)
					}
				}
			}
		}
	}
}

@Composable
fun DisplayNativeAd3(nativeAd: NativeAd) {
	// 1. 确保 NativeAdView 是最外层，且 padding 在其内部处理，防止资源超出边界
	NativeAdView(
		nativeAd = nativeAd,
		modifier = Modifier
			.fillMaxWidth()
			.height(130.dp)
			.background(
				Color_ECEFF3,
				shape = RoundedCornerShape(10.5.dp)
			)
			.padding(horizontal = 16.dp, vertical = 11.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 10.dp),
			horizontalAlignment = Alignment.Start
		) {
			// 头部：图标 + 标题 + 评分 + 广告标识
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.Start
			) {
				// 图标展示
				nativeAd.icon?.let { icon ->
					NativeAdIconView(Modifier.size(34.dp)) { // 给图标固定大小，防止撑开
						icon.drawable?.toBitmap()?.let { bitmap ->
							Image(
								bitmap = bitmap.asImageBitmap(),
								contentDescription = "Ad Icon",
								modifier = Modifier
									.fillMaxSize()
									.clip(RoundedCornerShape(5.dp))
							)

						}
					}
				}

				Column(modifier = Modifier
					.padding(start = 8.dp)
					.weight(1f)) {
					Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
						// 2. 广告标识：置于右上角，避免覆盖图标导致 "Outside boundary" 报错
						NativeAdAttribution(
							modifier = Modifier,
							containerColor = Color_C1C5C7,
							padding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
							contentColor = Color.White,
							shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 6.dp),
							text = stringResource(R.string.attribution),
						)
						Spacer(modifier = Modifier.width(4.dp))
						// 标题
						nativeAd.headline?.let {
							NativeAdHeadlineView {
								Text(
									text = it,
									style = TextStyle_16sp_w500_303C54,
									maxLines = 1,
									overflow = TextOverflow.Ellipsis
								)
							}
						}
					}
					Spacer(modifier = Modifier.height(6.dp))
					// 正文内容
					nativeAd.body?.let {
						NativeAdBodyView(modifier = Modifier.fillMaxWidth()) {
							Text(
								text = it,
								style = TextStyle_13sp_w400_303C54,
								maxLines = 2,
								overflow = TextOverflow.Ellipsis
							)
						}
					}
				}
			}

			// 底部操作区：价格/店铺 + 按钮
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween
			) {
//				Row {
//					nativeAd.price?.let {
//						NativeAdPriceView {
//							Text(text = it, style = MaterialTheme.typography.labelSmall)
//						}
//					}
//					nativeAd.store?.let {
//						NativeAdStoreView(Modifier.padding(start = 4.dp)) {
//							Text(text = it, style = MaterialTheme.typography.labelSmall)
//						}
//					}
//				}
				// 行动呼吁按钮 (Call to Action)
				nativeAd.callToAction?.let { callToAction ->
					NativeAdCallToActionView(modifier = Modifier.fillMaxWidth()) {
						// 确保你的 NativeAdButton 组件内部也是一个可点击的 Button
						NativeAdButton(text = callToAction, modifier = Modifier.fillMaxWidth().height(44.dp), containerColor = Color_9E7BFB, contentColor = Color.White)
					}
				}
			}
		}
	}
}

@Composable
fun DisplayNativeAd4(nativeAd: NativeAd) {
	// 1. 确保 NativeAdView 是最外层，且 padding 在其内部处理，防止资源超出边界
	NativeAdView(
		nativeAd = nativeAd,
		modifier = Modifier
			.fillMaxWidth()
			.height(170.dp)
			.background(
				Color_ECEFF3,
				shape = RoundedCornerShape(10.5.dp)
			)
			.padding(horizontal = 16.dp, vertical = 11.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 10.dp),
			horizontalAlignment = Alignment.Start
		) {
			// 头部：图标 + 标题 + 评分 + 广告标识
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.Start,
				verticalAlignment = Alignment.CenterVertically
			) {
				NativeAdMediaView(
					modifier = Modifier
						.weight(1f)
						.height(120.dp)
				)

				Spacer(modifier = Modifier.width(8.dp))

				Column(modifier = Modifier
					.padding(start = 8.dp)
					.weight(1f)) {
					Row(modifier = Modifier.fillMaxWidth()) {
						// 2. 广告标识：置于右上角，避免覆盖图标导致 "Outside boundary" 报错
						NativeAdAttribution(
							modifier = Modifier,
							containerColor = Color_C1C5C7,
							padding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
							contentColor = Color.White,
							shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 6.dp),
							text = stringResource(R.string.attribution),
						)
						Spacer(modifier = Modifier.width(4.dp))
						// 标题
						nativeAd.headline?.let {
							NativeAdHeadlineView {
								Text(
									text = it,
									style = TextStyle_16sp_w500_303C54,
									maxLines = 2,
									overflow = TextOverflow.Ellipsis
								)
							}
						}
					}
					Spacer(modifier = Modifier.height(6.dp))
					// 正文内容
					nativeAd.body?.let {
						NativeAdBodyView(modifier = Modifier.fillMaxWidth()) {
							Text(
								text = it,
								style = TextStyle_13sp_w400_303C54,
								overflow = TextOverflow.Ellipsis
							)
						}
					}
					Spacer(modifier = Modifier.height(10.dp))
					nativeAd.callToAction?.let { callToAction ->
						NativeAdCallToActionView(modifier = Modifier.fillMaxWidth()) {
							// 确保你的 NativeAdButton 组件内部也是一个可点击的 Button
							NativeAdButton(text = callToAction, modifier = Modifier.fillMaxWidth().height(44.dp), containerColor = Color_9E7BFB, contentColor = Color.White)
						}
					}
				}
			}

		}
	}
}
