package com.quickrecover.photonvideotool.view.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.quickrecover.photonvideotool.ui.theme.Color_F5F5F5
import com.quickrecover.photonvideotool.ui.theme.Gradient_9E7BFB_to_784BF1

@Composable
fun GradientProgressBar(progress: Float, modifier: Modifier = Modifier, brush: Brush = Gradient_9E7BFB_to_784BF1) {
	Canvas(modifier = modifier.height(5.dp)) {
		val strokeWidth = size.height
		val cornerRadius = CornerRadius(x = strokeWidth / 2, y = strokeWidth / 2)
		// 绘制背景轨道，带圆角
		drawRoundRect(
			color = Color_F5F5F5,
			size = size,
			cornerRadius = cornerRadius
		)
		// 绘制渐变进度条，带圆角
		drawRoundRect(
			brush = brush,
			size = Size(size.width * progress, strokeWidth),
			cornerRadius = cornerRadius
		)
	}
}