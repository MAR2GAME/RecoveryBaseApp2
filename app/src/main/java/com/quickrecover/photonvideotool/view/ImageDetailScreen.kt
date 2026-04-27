package com.quickrecover.photonvideotool.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.quickrecover.photonvideotool.LocalInnerPadding
import com.quickrecover.photonvideotool.LocalNavController
import com.quickrecover.photonvideotool.R

@Composable
fun ImageDetailScreen(filePath: String) {

	val interactionSource = remember { MutableInteractionSource() }
	val navController = LocalNavController.current

	var scale by remember { mutableFloatStateOf(1f) }
	var offsetX by remember { mutableFloatStateOf(0f) }
	var offsetY by remember { mutableFloatStateOf(0f) }

	Box(Modifier.fillMaxSize()) {
		AsyncImage(
			model = filePath,
			contentDescription = null,
			modifier = Modifier
				.fillMaxSize()
				.graphicsLayer(
					scaleX = scale,
					scaleY = scale,
					translationX = offsetX,
					translationY = offsetY
				)
				.pointerInput(Unit) {
					detectTransformGestures { _, pan, zoom, _ ->
						scale = (scale * zoom).coerceIn(0.5f, 5f)
						offsetX += pan.x
						offsetY += pan.y
					}
				},
			contentScale = ContentScale.Fit
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
