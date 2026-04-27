package com.quickrecover.photonvideotool.view.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quickrecover.photonvideotool.R
import com.quickrecover.photonvideotool.ui.theme.Color_9E7BFB_25
import com.quickrecover.photonvideotool.ui.theme.TextStyle


@Composable
fun NotificationTips(onRequestNotificationPermission: () -> Unit = {}) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.border(
				width = 1.dp,
				color = Color_9E7BFB_25,
				shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
			).clickable(
				interactionSource = remember { MutableInteractionSource() },
				indication = null
			) {
				onRequestNotificationPermission()
			}
	) {
		Row (
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp)
		) {
			Image(
				painter = painterResource(id = R.drawable.notification),
				contentDescription = null,
				modifier = Modifier
					.size(36.dp)
			)
			Spacer(modifier = Modifier.width(12.dp))
			Text(
				text = stringResource(R.string.never_miss_important_notifications),
				style = TextStyle.TextStyle_12sp_w500_252040_35,
				modifier = Modifier.align(Alignment.CenterVertically)
			)
		}
	}
}

@Preview
@Composable
fun NotificationPreview() {
    NotificationTips()
}
