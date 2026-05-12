package com.datatool.photorecovery.view.widget

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.datatool.photorecovery.LocalInnerPadding
import com.datatool.photorecovery.LocalNavController
import com.datatool.photorecovery.R
import com.datatool.photorecovery.ui.theme.Gradient_9E7BFB_to_784BF1
import com.datatool.photorecovery.ui.theme.TextStyle
import kotlin.String

@Composable
fun NavigationWidget(
	title: String,
	navController: NavController,
	showBack: Boolean = true,
	hasDone: Boolean = false,
	doneTitle: String = stringResource(R.string.done),
	onClick: () -> Unit = {},
	onBack: (() -> Unit)? = null,
) {
	val interactionSource = remember { MutableInteractionSource() }

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxWidth()
	) {
		if (showBack) {
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
						onBack?.invoke() ?: navController.popBackStack()
					}
			)
		}
		Text(
			text = title,
			maxLines = 1,
			style = TextStyle.TextStyle_20sp_w600_252040,
			modifier = Modifier.padding(horizontal = 50.dp)
		)
		if (hasDone) {
			Box(
				modifier = Modifier
					.align(Alignment.CenterEnd)
					.height(28.dp)
					.background(
						brush = Gradient_9E7BFB_to_784BF1,
						shape = RoundedCornerShape(32.dp)
					)
					.clickable(
						interactionSource = interactionSource,
						indication = null
					) {
						onClick()
					},
				contentAlignment = Alignment.Center
			) {
				Text(
					text = doneTitle,
					style = TextStyle.TextStyle_14sp_w600_FFF,
					textAlign = TextAlign.Center,
					modifier = Modifier
						.wrapContentSize()
						.padding(horizontal = 12.dp)
				)
			}
		}
	}
}

@Composable
fun NavigationWidget1(
	title: String,
	navController: NavController,
	showBack: Boolean = true,
	hasDone: Boolean = false,
	doneTitle: String = stringResource(R.string.done),
	onClick: () -> Unit = {},
	onBack: (() -> Unit)? = null,
) {
	val interactionSource = remember { MutableInteractionSource() }

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxWidth()
	) {
		if (showBack) {
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
						onBack?.invoke() ?: navController.popBackStack()
					}
			)
		}
		Text(
			text = title,
			maxLines = 1,
			style = TextStyle.TextStyle_20sp_w600_252040,
			modifier = Modifier.padding(horizontal = 50.dp)
		)
		if (hasDone) {
			Box(
				modifier = Modifier
					.align(Alignment.CenterEnd)
					.height(36.dp)
					.background(
						brush = Gradient_9E7BFB_to_784BF1,
						shape = RoundedCornerShape(32.dp)
					)
					.clickable(
						interactionSource = interactionSource,
						indication = null
					) {
						onClick()
					},
				contentAlignment = Alignment.Center
			) {
				Text(
					text = doneTitle,
					style = TextStyle.TextStyle_14sp_w600_FFF,
					textAlign = TextAlign.Center,
					modifier = Modifier
						.wrapContentSize()
						.padding(horizontal = 12.dp)
				)
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun NavigationWidgetPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(
		LocalNavController provides navController,
		LocalInnerPadding provides PaddingValues(0.dp)
	) {
		NavigationWidget(
			title = stringResource(R.string.languages),
			navController = navController,
			showBack = true,
			hasDone = true,
			onClick = {
//				Toast.makeText(navController.context, "Language changed ", Toast.LENGTH_SHORT).show()
			}
		)
	}
}