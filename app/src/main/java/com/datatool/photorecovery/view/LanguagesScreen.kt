package com.datatool.photorecovery.view

import com.pdffox.adv.AdvertiseSdk
import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.datatool.photorecovery.LocalInnerPadding
import com.datatool.photorecovery.LocalNavController
import com.datatool.photorecovery.MainActivity
import com.datatool.photorecovery.R
import com.datatool.photorecovery.core.AreaKeyNative
import com.datatool.photorecovery.core.LogConfig
import com.datatool.photorecovery.core.LogParams
import com.datatool.photorecovery.core.bean.LanguageBean
import com.datatool.photorecovery.ui.theme.Color_9E7BFB
import com.datatool.photorecovery.ui.theme.Gradient_FFF_to_F5F5F5
import com.datatool.photorecovery.ui.theme.TextStyle
import com.datatool.photorecovery.view.widget.DisplayNativeAd1
import com.datatool.photorecovery.view.widget.NavigationWidget
import com.datatool.photorecovery.view.widget.SetLanguagePop
import com.datatool.photorecovery.view.widget.SetStatusBarLight
import com.datatool.photorecovery.viewmodel.LanguagesViewModel
import com.pdffox.adv.compose.rememberNativeAd
import kotlinx.coroutines.launch

@Composable
fun LanguagesScreen(isFromAppOpen: Boolean = false) {

	var showPopAd by remember { mutableStateOf(false) }

	val navController = LocalNavController.current
	val languagesViewModel: LanguagesViewModel = viewModel()
	val scope = rememberCoroutineScope()
	val context = LocalContext.current
	val activity = context as? Activity
	val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

	val selectedLanguage by languagesViewModel.selectedLanguage.collectAsState()

	val languages = remember {
		languagesViewModel.LANGUAGE_MAP.map { (name, locale) ->
			LanguageBean(name, locale, false)
		}
	}

	val nativeAd = rememberNativeAd(
		areaKey = AreaKeyNative.languageNativeAdv,
		shouldRefreshImmediately = {
			lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
		},
	)

	LaunchedEffect(Unit) {
		languagesViewModel.loadSelectedLanguage()
	}

	var launchedTime = System.currentTimeMillis()
	LaunchedEffect(Unit) {
		launchedTime = System.currentTimeMillis()
	}

	DisposableEffect(Unit) {
		onDispose {
			val params = HashMap<String, Any>().apply {
				put(LogParams.duration_time, System.currentTimeMillis() - launchedTime)
				put(LogParams.language_value, selectedLanguage ?: "")
			}
			AdvertiseSdk.logEvent(LogConfig.settings_language, params)
		}
	}

	SetStatusBarLight(true)
	if (isFromAppOpen) {
		BackHandler {
		}
	}
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(brush = Gradient_FFF_to_F5F5F5)
			.padding(LocalInnerPadding.current),
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 24.dp),
		) {
			NavigationWidget(
				title = stringResource(R.string.languages),
				navController = navController,
				showBack = !isFromAppOpen,
				hasDone = true,
				doneTitle = stringResource(R.string.next),
				onClick = {
					showPopAd = true
				},
			)
			Spacer(modifier = Modifier.height(9.dp))
			LazyColumn(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f),
			) {
				items(languages.size) { index ->
					val language = languages[index]
					LanguageItem(
						language = language.copy(selected = language.language == selectedLanguage),
						onSelected = { selected ->
							languagesViewModel.selectLanguage(selected.language)
						},
					)
				}
				item {
					Spacer(
						modifier = Modifier
							.fillMaxWidth()
							.height(20.dp),
					)
				}
			}
			nativeAd.value?.let {
				Spacer(modifier = Modifier.height(10.dp))
				DisplayNativeAd1(nativeAd = it)
			}
		}
	}

	if (showPopAd) {
		nativeAd.value?.let {
			SetLanguagePop(
				it,
				onCallback = { agree ->
					showPopAd = false
					if (agree) {
						scope.launch {
							languagesViewModel.setLocale(selectedLanguage)
							restartApp(activity)
						}
					}
				},
			)
		}
	}
}

private fun restartApp(activity: Activity?) {
	if (activity == null) return
	val restartIntent = Intent(activity, MainActivity::class.java).apply {
		addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
	}
	activity.startActivity(restartIntent)
	activity.overridePendingTransition(0, 0)
	activity.finish()
	activity.overridePendingTransition(0, 0)
}

@Composable
fun LanguageItem(language: LanguageBean, onSelected: (LanguageBean) -> Unit) {
	Box(
		modifier = Modifier
			.height(70.dp)
			.padding(top = 16.dp),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxSize()
				.border(
					1.dp,
					Color_9E7BFB.copy(alpha = 0.25f),
					shape = RoundedCornerShape(16.dp),
				)
				.then(
					if (language.selected) {
						Modifier.background(
							shape = RoundedCornerShape(16.dp),
							color = Color_9E7BFB.copy(alpha = 0.06f),
						)
					} else {
						Modifier
					},
				)
				.clickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = null,
				) {
					onSelected(language)
				}
				.padding(8.dp),
		) {
			Text(
				text = language.language,
				style = TextStyle.TextStyle_18sp_w600_252040,
				modifier = Modifier.padding(start = 8.dp),
			)
			Spacer(modifier = Modifier.weight(1f))
			if (language.selected) {
				Image(
					painter = painterResource(id = R.drawable.language_selected),
					contentDescription = "Checked",
					modifier = Modifier.size(22.dp),
				)
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun LanguagesScreenPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(
		LocalNavController provides navController,
		LocalInnerPadding provides PaddingValues(0.dp),
	) {
		LanguagesScreen(false)
	}
}
