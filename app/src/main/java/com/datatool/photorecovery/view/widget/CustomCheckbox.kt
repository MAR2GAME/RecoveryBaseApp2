package com.datatool.photorecovery.view.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.datatool.photorecovery.R

@Composable
fun CustomCheckbox(
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier
) {
	val imageRes = if (checked) R.drawable.check_on else R.drawable.check_off
	Image(
		painter = painterResource(id = imageRes),
		contentDescription = if (checked) "Checked" else "Unchecked",
		modifier = modifier.clickable(
			indication = null,
			interactionSource = remember { MutableInteractionSource() }
		) {
			onCheckedChange(!checked)
		}
	)
}

@Composable
fun CustomCheckbox1(
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier
) {
	val imageRes = if (checked) R.drawable.check1_on else R.drawable.check1_off
	Image(
		painter = painterResource(id = imageRes),
		contentDescription = if (checked) "Checked" else "Unchecked",
		modifier = modifier.clickable(
			indication = null,
			interactionSource = remember { MutableInteractionSource() }
		) {
			onCheckedChange(!checked)
		}
	)
}

@Composable
fun CustomCheckbox2(
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier
) {
	val imageRes = if (checked) R.drawable.check2_on else R.drawable.check2_off
	Image(
		painter = painterResource(id = imageRes),
		contentDescription = if (checked) "Checked" else "Unchecked",
		modifier = modifier.clickable(
			indication = null,
			interactionSource = remember { MutableInteractionSource() }
		) {
			onCheckedChange(!checked)
		}
	)
}

@Composable
fun CustomCheckbox3(
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier
) {
	val imageRes = if (checked) R.drawable.check3_on else R.drawable.check3_off
	Image(
		painter = painterResource(id = imageRes),
		contentDescription = if (checked) "Checked" else "Unchecked",
		modifier = modifier.clickable(
			indication = null,
			interactionSource = remember { MutableInteractionSource() }
		) {
			onCheckedChange(!checked)
		}
	)
}