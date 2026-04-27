package com.quickrecover.photonvideotool.view

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.quickrecover.photonvideotool.LocalInnerPadding
import com.quickrecover.photonvideotool.LocalNavController
import com.quickrecover.photonvideotool.R
import com.quickrecover.photonvideotool.core.Config
import com.quickrecover.photonvideotool.core.LogConfig
import com.quickrecover.photonvideotool.ui.theme.Gradient_FFF_to_F5F5F5
import com.quickrecover.photonvideotool.view.widget.NavigationWidget
import com.quickrecover.photonvideotool.view.widget.SetStatusBarLight

// TODO: add
//import com.pdffox.adv.use.log.LogUtil

@Composable
fun WebScreen(url: String) {
	val navController = LocalNavController.current
	var pageTitle by remember { mutableStateOf("Loading...") }

	LaunchedEffect(navController) {
		snapshotFlow { navController.currentBackStackEntry?.destination?.route }
			.collect { route ->
				// 这里可以打印或保存当前路由
				Log.e("NavTrace", "当前路由: $route")
			}
	}

	// TODO: add
//	LaunchedEffect(Unit) {
//		if (url == Config.PrivacyUrl) {
//			LogUtil.log(LogConfig.check_privacy, mapOf())
//		}
//		if (url == Config.TermUrl) {
//			LogUtil.log(LogConfig.check_terms, mapOf())
//		}
//	}

	SetStatusBarLight(true)
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				brush = Gradient_FFF_to_F5F5F5,
			)
			.padding(LocalInnerPadding.current)
	) {
		Column{
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 24.dp)
			) {
				NavigationWidget(title = pageTitle, navController = navController)
			}
			Spacer(modifier = Modifier.height(25.dp))
			AndroidView(
				factory = { context ->
					WebView(context).apply {
						webViewClient = object : WebViewClient() {
							override fun onPageFinished(view: WebView?, url: String?) {
								super.onPageFinished(view, url)
								pageTitle = view?.title ?: context.getString(R.string.no_title)
							}
						}
						loadUrl(url)
					}
				},
				modifier = Modifier.fillMaxSize()
			)
		}
	}

}