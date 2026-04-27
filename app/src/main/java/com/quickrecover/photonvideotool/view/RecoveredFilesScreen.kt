package com.quickrecover.photonvideotool.view

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.quickrecover.photonvideotool.BuildConfig
import com.quickrecover.photonvideotool.LocalInnerPadding
import com.quickrecover.photonvideotool.LocalNavController
import com.quickrecover.photonvideotool.MainActivity
import com.quickrecover.photonvideotool.R
import com.quickrecover.photonvideotool.core.AreaKey
import com.quickrecover.photonvideotool.core.LogConfig
import com.quickrecover.photonvideotool.core.route.Routes
import com.quickrecover.photonvideotool.ui.theme.Color_9E7BFB
import com.quickrecover.photonvideotool.ui.theme.Gradient_9E7BFB_to_784BF1
import com.quickrecover.photonvideotool.ui.theme.Gradient_FFF_to_F5F5F5
import com.quickrecover.photonvideotool.ui.theme.TextStyle
import com.quickrecover.photonvideotool.view.widget.CustomCheckbox1
import com.quickrecover.photonvideotool.view.widget.CustomCheckbox3
import com.quickrecover.photonvideotool.view.widget.SetStatusBarLight
import com.quickrecover.photonvideotool.viewmodel.RecoveriedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// TODO: add
//import com.pdffox.adv.use.Ads
//import com.pdffox.adv.use.adv.AdConfig
//import com.pdffox.adv.use.adv.AdLoader
//import com.pdffox.adv.use.log.LogUtil

@Composable
fun RecoveredFilesScreen(scanType: String) {

	Log.e("TAG", "RecoveredFilesScreen: $scanType")
	val navController = LocalNavController.current
	val recoveriedViewModel: RecoveriedViewModel = koinViewModel()
	val context = LocalContext.current
	val activity = context as? MainActivity
	val coroutineScope = rememberCoroutineScope()
	val interactionSource = remember { MutableInteractionSource() }

	val currentScanType = recoveriedViewModel.currentScanType.collectAsState()
	val recoveredFiles = recoveriedViewModel.recoveredFiles.collectAsState()

	LaunchedEffect(scanType) {
		recoveriedViewModel.setCurrentScanType(if (scanType == "recovery_files") "other_files" else scanType)
	}

	// TODO: add
//	LaunchedEffect(Unit) {
//		LogUtil.log(LogConfig.enter_recovered, mapOf())
//	}
//
//	LaunchedEffect(Unit) {
//		if (BuildConfig.DEBUG) {
//			Toast.makeText(context, "预加载插屏广告 ${AdConfig.canLoadInter(AdConfig.LOAD_TIME_ENTER_FEATURE)}", Toast.LENGTH_SHORT).show()
//		}
//		if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_ENTER_FEATURE)) {
//			AdLoader.loadInter(context)
//		}
//	}

	BackHandler {
		activity?.let {
			// TODO: change
//			Ads.showInterstitialAd(activity = it, AreaKey.returnHomeFromOtherAdv) {
				coroutineScope.launch(Dispatchers.Main) {
					navController.popBackStack(Routes.HOME, false)
				}
//			}
		}
	}
	SetStatusBarLight(true)
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				brush = Gradient_FFF_to_F5F5F5,
			)
			.padding(LocalInnerPadding.current)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 24.dp)
		) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.fillMaxWidth()
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
							activity?.let {
								// TODO: change
//								Ads.showInterstitialAd(
//									activity = it,
//									AreaKey.returnHomeFromOtherAdv
//								) {
								coroutineScope.launch(Dispatchers.Main) {
									navController.popBackStack(Routes.HOME, false)
								}
//								}
							}
						}
				)
				Text(
					text = stringResource(id = R.string.recovered_files),
					maxLines = 1,
					style = TextStyle.TextStyle_20sp_w600_252040,
					modifier = Modifier.padding(horizontal = 50.dp)
				)
				Image(
					painter = painterResource(id = R.drawable.del_icon),
					contentDescription = null,
					modifier = Modifier
						.size(44.dp)
						.align(Alignment.CenterEnd)
						.clickable(
							interactionSource = interactionSource,
							indication = null
						) {
							recoveriedViewModel.delRecoveredFiles()
						}
				)
			}
			Spacer(modifier = Modifier.height(16.dp))
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.height(52.dp)
					.padding(3.dp)
					.background(
						color = Color.White,
						shape = RoundedCornerShape(32.dp)
					)
			) {
				Box(
					modifier = Modifier
						.fillMaxHeight()
						.weight(1f)
						.background(
							color = when (currentScanType.value) {
								"recovery_photos" -> Color_9E7BFB
								"recovery_videos" -> Color.Transparent
								else -> Color.Transparent
							},
							shape = RoundedCornerShape(32.dp)
						)
						.clickable(
							interactionSource = interactionSource,
							indication = null
						) {
							recoveriedViewModel.setCurrentScanType("recovery_photos")
						}
				) {
					Text(
						text = stringResource(id = R.string.photos),
						maxLines = 1,
						style = when(currentScanType.value) {
							"recovery_photos" -> TextStyle.TextStyle_14sp_w600_FFF
							"recovery_videos" -> TextStyle.TextStyle_14sp_w600_252040
							else -> TextStyle.TextStyle_14sp_w600_252040
						},
						modifier = Modifier.align(Alignment.Center)
					)
				}

				Box(
					modifier = Modifier
						.fillMaxHeight()
						.weight(1f)
						.background(
							color = when (currentScanType.value) {
								"recovery_photos" -> Color.Transparent
								"recovery_videos" -> Color_9E7BFB
								else -> Color.Transparent
							},
							shape = RoundedCornerShape(32.dp)
						)
						.clickable(
							interactionSource = interactionSource,
							indication = null
						) {
							recoveriedViewModel.setCurrentScanType("recovery_videos")
						}
				) {
					Text(
						text = stringResource(id = R.string.videos),
						maxLines = 1,
						style = when(currentScanType.value) {
							"recovery_photos" -> TextStyle.TextStyle_14sp_w600_252040
							"recovery_videos" -> TextStyle.TextStyle_14sp_w600_FFF
							else -> TextStyle.TextStyle_14sp_w600_252040
						},
						modifier = Modifier.align(Alignment.Center)
					)
				}

				Box(
					modifier = Modifier
						.fillMaxHeight()
						.weight(1f)
						.background(
							color = when (currentScanType.value) {
								"recovery_photos" -> Color.Transparent
								"recovery_videos" -> Color.Transparent
								else -> Color_9E7BFB
							},
							shape = RoundedCornerShape(32.dp)
						)
						.clickable(
							interactionSource = interactionSource,
							indication = null
						) {
							recoveriedViewModel.setCurrentScanType("other_files")
						}
				) {
					Text(
						text = stringResource(id = R.string.other_files),
						maxLines = 1,
						style = when(currentScanType.value) {
							"recovery_photos" -> TextStyle.TextStyle_14sp_w600_252040
							"recovery_videos" -> TextStyle.TextStyle_14sp_w600_252040
							else -> TextStyle.TextStyle_14sp_w600_FFF
						},
						modifier = Modifier.align(Alignment.Center)
					)
				}
			}
			Spacer(modifier = Modifier.height(12.dp))
			Box(
				modifier = Modifier
					.fillMaxSize()
			) {
				if(recoveredFiles.value.isEmpty()) {
					EmptyScreen(currentScanType.value, scanType)
				} else {
					when(currentScanType.value) {
						"recovery_photos", "recovery_videos" -> {
							LazyVerticalGrid(
								columns = GridCells.Fixed(3), // 设置列数
								modifier = Modifier.fillMaxSize(),
								horizontalArrangement = Arrangement.spacedBy(5.dp),
								verticalArrangement = Arrangement.spacedBy(5.dp)
							) {
								items(recoveredFiles.value.size) { index ->
									Box(
										modifier = Modifier
											.size(106.dp)
									) {
										ImageThumbnail(
											path = recoveredFiles.value[index].path,
											modifier = Modifier
												.size(106.dp)
												.clickable(
													interactionSource = remember { MutableInteractionSource() },
													indication = null
												) {
													//  点击查看图片
													val encodedPath = URLEncoder.encode(
														recoveredFiles.value[index].path,
														StandardCharsets.UTF_8.toString()
													)
													navController.navigate("${Routes.FileDetail}/$encodedPath/true")
												}
										)
										CustomCheckbox3(
											checked = recoveredFiles.value[index].isSelect,
											modifier = Modifier
												.align(Alignment.TopEnd)
												.padding(8.dp)
												.size(16.dp),
											onCheckedChange = { checked ->
												recoveriedViewModel.setFileSelect(checked, recoveredFiles.value[index])
											}
										)
									}
								}
							}
						}
						else -> {
							LazyColumn(
								modifier = Modifier.fillMaxSize(),
								verticalArrangement = Arrangement.spacedBy(16.dp)
							) {
								items(recoveredFiles.value.size) { index ->
									val item = recoveredFiles.value[index]
									Box(
										contentAlignment = Alignment.Center,
										modifier = Modifier
											.fillMaxWidth()
											.border(
												1.dp,
												Color_9E7BFB.copy(alpha = 0.25f),
												shape = RoundedCornerShape(16.dp)
											)
											.padding(
												start = 16.dp,
												top = 12.dp,
												end = 50.dp,
												bottom = 12.dp
											)
											.clickable(
												interactionSource = remember { MutableInteractionSource() },
												indication = null
											) {
												//  点击查看文件
												val encodedPath = URLEncoder.encode(
													recoveredFiles.value[index].path,
													StandardCharsets.UTF_8.toString()
												)
												navController.navigate("${Routes.FileDetail}/$encodedPath/true")
											},
									) {
										Row (
											modifier = Modifier.fillMaxWidth(),
											verticalAlignment = Alignment.CenterVertically,
											horizontalArrangement = Arrangement.Start
										) {
											CustomCheckbox1(
												checked = recoveredFiles.value[index].isSelect,
												modifier = Modifier
													.size(22.dp),
												onCheckedChange = { checked ->
													recoveriedViewModel.setFileSelect(checked, item)
												}
											)
											Spacer(modifier = Modifier.width(22.4.dp))
											Image(
												painter = painterResource(id = when(getFileType(item.name)) {
													"PDF" -> R.drawable.item_pdf
													"Word" -> R.drawable.item_word
													"Excel" -> R.drawable.item_excel
													"PPT" -> R.drawable.item_ppt
													"TXT" -> R.drawable.item_txt
													else -> R.drawable.item_other
												}),
												contentDescription = null,
												modifier = Modifier
													.size(24.dp)
											)
											Spacer(modifier = Modifier.width(20.dp))
											Column(
												modifier = Modifier.fillMaxWidth()
											) {
												Row(
													verticalAlignment = Alignment.CenterVertically,
													horizontalArrangement = Arrangement.Start
												) {
													//显示文件名前面部分
													Text(
														item.name,
														maxLines = 1,
														overflow = TextOverflow.Ellipsis,
														modifier = Modifier.weight(1f),
														style = TextStyle.TextStyle_16sp_w600_252040
													)
												}
												Spacer(modifier = Modifier.height(5.dp))
												Text(
													formatFileSize(item.size),
													maxLines = 1,
													modifier = Modifier.fillMaxWidth(),
													style = TextStyle.TextStyle_10sp_w500_252040_35
												)
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

@Composable
fun EmptyScreen(scanType: String, oriScanType: String) {
	val navController = LocalNavController.current

	data class EmptyScreenData(
		val image: Int,
		val text: String,
		val btn: String,
	)

	val emptyScreenData = when(scanType){
		"recovery_photos" -> {
			EmptyScreenData(
				R.drawable.no_photos,
				stringResource(R.string.no_photos_have_been_retrieved_yet),
				stringResource(R.string.scan_deleted_photos)
			)
		}
		"recovery_videos" -> EmptyScreenData(
			R.drawable.no_videos,
			stringResource(R.string.no_videos_have_been_retrieved_yet),
			stringResource(R.string.scan_deleted_videos)
		)
		else -> EmptyScreenData(
			R.drawable.no_files,
			stringResource(R.string.no_files_have_been_retrieved_yet),
			stringResource(R.string.scan_deleted_files)
		)
	}

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = Modifier.fillMaxSize()
	) {
		Image(
			painter = painterResource(id = emptyScreenData.image),
			contentDescription = null,
			modifier = Modifier
				.width(140.dp)
				.height(138.dp)
		)
		Spacer(modifier = Modifier.height(11.dp))
		Text(
			text = emptyScreenData.text,
			maxLines = 1,
			style = TextStyle.TextStyle_14sp_w500_252040_35
		)
		Spacer(modifier = Modifier.height(40.dp))
		Box(
			modifier = Modifier
				.width(279.dp)
				.height(44.dp)
				.align(Alignment.CenterHorizontally)
				.background(
					brush = Gradient_9E7BFB_to_784BF1,
					shape = RoundedCornerShape(32.dp)
				)
				.clickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = null
				) {
					navController.navigate("${Routes.StartScan}/${scanType}") {
						popUpTo("${Routes.RecoveredFiles}/${oriScanType}") { inclusive = true }
					}
				}
		) {
			Text(
				text = emptyScreenData.btn,
				style = TextStyle.TextStyle_20sp_w600_FFF,
				modifier = Modifier.align(Alignment.Center)
			)
		}
	}
}

@Preview(showBackground = true)
@Composable
fun RecoveredFilesScreenPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(
		LocalNavController provides navController,
		LocalInnerPadding provides PaddingValues(0.dp)
	) {
		RecoveredFilesScreen(scanType = "recovery_photos")
	}
}