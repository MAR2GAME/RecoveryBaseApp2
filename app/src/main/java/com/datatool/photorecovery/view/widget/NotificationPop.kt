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
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.compose.rememberNavController
import com.datatool.photorecovery.LocalInnerPadding
import com.datatool.photorecovery.LocalNavController
import com.datatool.photorecovery.R
import com.datatool.photorecovery.core.AreaKeyNative
import com.datatool.photorecovery.core.route.Routes
import com.datatool.photorecovery.ui.theme.Color_9E7BFB_10
import com.datatool.photorecovery.ui.theme.Gradient_9E7BFB_to_784BF1
import com.datatool.photorecovery.ui.theme.TextStyle
import com.google.android.gms.ads.nativead.NativeAd

@Composable
fun NotificationPop(
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
			Box(
				modifier = Modifier
					.fillMaxWidth(0.9f)
					.height(490.dp)
					.background(
						color = Color.White,
						shape = RoundedCornerShape(16.dp)
					)
			) {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier.fillMaxWidth()
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.height(155.dp)
							.background(
								color = Color_9E7BFB_10,
								shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
							)
					) {
						Image(
							painter = painterResource(id = R.drawable.notification_bg),
							contentDescription = null,
							modifier = Modifier
								.fillMaxWidth()
								.padding(horizontal = 39.dp)
								.align(Alignment.Center)
						)
						Box(
							modifier = Modifier
							.width(107.dp)
							.padding(end = 51.dp, top = 24.dp)
							.align(Alignment.TopEnd)
						){
							Image(
								modifier = Modifier
									.width(56.dp),
								painter = painterResource(id = R.drawable.notification_icon),
								contentDescription = null,
							)
						}

					}
					Spacer(
						modifier = Modifier
							.height(24.dp)
					)
					Text(
						"Permission application",
						style = TextStyle.TextStyle_24sp_w600_252040,
						letterSpacing = 0.72.sp,
						lineHeight = 32.sp,
					)
					Spacer(
						modifier = Modifier
							.height(12.dp)
					)
					Text(
						"Don't miss important message notifications. You need to enable notification permissions firs",
						style = TextStyle.TextStyle_14sp_w600_252040_70,
						letterSpacing = 0.42.sp,
						lineHeight = 24.sp,
						modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
						textAlign = TextAlign.Center
					)
					Spacer(
						modifier = Modifier
							.height(24.dp)
					)
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.height(44.dp)
							.padding(horizontal = 32.dp)
							.background(
								brush = Gradient_9E7BFB_to_784BF1,
								shape = RoundedCornerShape(32.dp)
							)
							.clickable(
								interactionSource = interactionSource,
								indication = null
							) {
								showExitSheet = false
								onCallback(true)
							},
						contentAlignment = Alignment.Center
					) {
						Text(
							text = "I See",
							style = TextStyle.TextStyle_20sp_w600_FFF,
							textAlign = TextAlign.Center,
							modifier = Modifier.wrapContentSize()
						)
					}

					nativeAd?.let {
						Spacer(
							modifier = Modifier.height(10.dp)
						)
						Box(
							modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
						) {
							DisplayNativeAd3(
								nativeAd = it
							)
						}
					}
				}
			}
		}
	}

	showExitSheet = true
}

@Preview(showBackground = true)
@Composable
fun NotificationPopPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(
		LocalNavController provides navController,
		LocalInnerPadding provides PaddingValues(0.dp)
	) {
		NotificationPop(
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
