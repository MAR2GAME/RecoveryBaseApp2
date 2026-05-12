package com.datatool.photorecovery.view.widget

import com.pdffox.adv.AdvertiseSdk
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.compose.rememberNavController
import com.datatool.photorecovery.LocalInnerPadding
import com.datatool.photorecovery.LocalNavController
import com.datatool.photorecovery.R
import com.datatool.photorecovery.core.AreaKeyNative
import com.datatool.photorecovery.core.route.Routes
import com.datatool.photorecovery.ui.theme.Gradient_9E7BFB_to_784BF1
import com.datatool.photorecovery.ui.theme.TextStyle
import com.google.android.gms.ads.nativead.NativeAd

@Composable
fun ExitAppPop(
	nativeAd: NativeAd?,
	onCallback: (agree: Boolean) -> Unit = {}
) {
	val interactionSource = remember { MutableInteractionSource() }
	var showExitSheet by remember { mutableStateOf(false) }

	AnimatedVisibility(
		visible = showExitSheet,
		enter = fadeIn(animationSpec = tween(durationMillis = 300)),
		exit = fadeOut(animationSpec = tween(durationMillis = 300))
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(Color.Black.copy(alpha = 0.5f)) // 半透明遮罩
				.clickable(
					indication = null,
					interactionSource = remember { MutableInteractionSource() }
				) {
					showExitSheet = false
					onCallback(false)
				},
			contentAlignment = Alignment.Center
		) {
			ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
				val (tipImage, tipShadow, contentColumn) = createRefs()

				Box(
					modifier = Modifier
						.padding(horizontal = 20.dp)
						.fillMaxWidth()
						.background(
							color = Color.White,
							shape = RoundedCornerShape(16.dp)
						)
						.constrainAs(contentColumn) {
							start.linkTo(parent.start)
							top.linkTo(tipImage.top, margin = 36.dp)
							end.linkTo(parent.end)
						}
						.clickable(enabled = false) {} // 防止点击穿透
						.padding(horizontal = 32.dp)
				) {
					Column(horizontalAlignment = Alignment.CenterHorizontally) {
						Spacer(modifier = Modifier.height(90.dp))
						Text(
							text = stringResource(R.string.exit_app),
							style = TextStyle.TextStyle_24sp_w600_252040,
							textAlign = TextAlign.Center
						)
						Spacer(modifier = Modifier.height(12.dp))
						Text(
							text = stringResource(R.string.are_you_sure_you_want_to_exit),
							style = TextStyle.TextStyle_14sp_w600_252040_70,
							textAlign = TextAlign.Center
						)
						Spacer(modifier = Modifier.height(24.dp))
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.height(44.dp)
								.background(
									brush = Gradient_9E7BFB_to_784BF1,
									shape = RoundedCornerShape(32.dp)
								)
								.clickable(
									interactionSource = interactionSource,
									indication = null
								) {
									showExitSheet = false
									onCallback(false)
								},
							contentAlignment = Alignment.Center
						) {
							Text(
								text = stringResource(R.string.cancel),
								style = TextStyle.TextStyle_20sp_w600_FFF,
								textAlign = TextAlign.Center,
								modifier = Modifier.wrapContentSize()
							)
						}
						Spacer(modifier = Modifier.height(12.dp))
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.height(44.dp)
								.clickable(
									interactionSource = interactionSource,
									indication = null
								) {
									onCallback(true)
								},
							contentAlignment = Alignment.Center
						) {
							Text(
								text = stringResource(R.string.exit),
								style = TextStyle.TextStyle_20sp_w500_252040_35,
								textAlign = TextAlign.Center,
								modifier = Modifier.wrapContentSize()
							)
						}
						nativeAd?.let {
							DisplayNativeAd1(
								nativeAd = it
							)
						}
						Spacer(modifier = Modifier.height(20.dp))
					}
				}

				Image(
					painter = painterResource(id = R.drawable.tip),
					contentDescription = null,
					modifier = Modifier
						.height(72.dp)
						.constrainAs(tipImage) {
							start.linkTo(parent.start)
							end.linkTo(parent.end)
							top.linkTo(parent.top)
						}
				)

				Image(
					painter = painterResource(id = R.drawable.tip_shadow),
					contentDescription = null,
					modifier = Modifier
						.constrainAs(tipShadow) {
							start.linkTo(parent.start)
							end.linkTo(parent.end)
							top.linkTo(tipImage.bottom, margin = 6.dp)
						}
				)
			}

		}
	}

	showExitSheet = true
}

@Preview(showBackground = true)
@Composable
fun ExitAppPopPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(
		LocalNavController provides navController,
		LocalInnerPadding provides PaddingValues(0.dp)
	) {
		ExitAppPop(
			null,
			onCallback = { agree ->
				if (agree) {
					// : Agree
				} else {
					// : Cancel
				}
			}
		)
	}
}
