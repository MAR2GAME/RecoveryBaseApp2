package com.quickrecover.photonvideotool.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.core.net.toUri
import com.quickrecover.photonvideotool.LocalInnerPadding
import com.quickrecover.photonvideotool.LocalNavController
import com.quickrecover.photonvideotool.R

@Composable
fun VideoDetailScreen(videoPath: String) {
	val interactionSource = remember { MutableInteractionSource() }
	val navController = LocalNavController.current
	val context = LocalContext.current
	val exoPlayer = remember {
		ExoPlayer.Builder(context).build().apply {
			setMediaItem(MediaItem.fromUri(videoPath.toUri()))
			prepare()
			playWhenReady = true
		}
	}
	DisposableEffect(exoPlayer) {
		onDispose {
			exoPlayer.release()
		}
	}

	Box(Modifier.fillMaxSize()) {
		AndroidView(
			modifier = Modifier.fillMaxSize(),
			factory = {
				PlayerView(it).apply {
					player = exoPlayer
					useController = true
				}
			}
		)
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(LocalInnerPadding.current)
		) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 24.dp)
			) {
				Image(
					painter = painterResource(id = R.drawable.back),
					contentDescription = null,
					modifier = Modifier
						.size(44.dp)
						.align(Alignment.CenterStart)
						.clickable(
							interactionSource = interactionSource,
							indication = null
						) {
							navController.popBackStack()
						}
				)
			}
		}
	}

}