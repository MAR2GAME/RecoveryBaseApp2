package com.quickrecover.photonvideotool.view

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bumptech.glide.Glide
import com.quickrecover.photonvideotool.FileCache
import com.quickrecover.photonvideotool.LocalInnerPadding
import com.quickrecover.photonvideotool.LocalNavController
import com.quickrecover.photonvideotool.MainActivity
import com.quickrecover.photonvideotool.R
import com.quickrecover.photonvideotool.core.AreaKey
import com.quickrecover.photonvideotool.core.bean.FileData
import com.quickrecover.photonvideotool.core.bean.FoldBean
import com.quickrecover.photonvideotool.core.bean.FoldSortBean
import com.quickrecover.photonvideotool.core.route.Routes
import com.quickrecover.photonvideotool.ui.theme.Color_252040_10
import com.quickrecover.photonvideotool.ui.theme.Color_9E7BFB
import com.quickrecover.photonvideotool.ui.theme.Color_F3EEFF
import com.quickrecover.photonvideotool.ui.theme.Gradient_9E7BFB_to_784BF1
import com.quickrecover.photonvideotool.ui.theme.Gradient_FFF_to_F5F5F5
import com.quickrecover.photonvideotool.ui.theme.TextStyle
import com.quickrecover.photonvideotool.view.widget.BannerAd
import com.quickrecover.photonvideotool.view.widget.CustomCheckbox
import com.quickrecover.photonvideotool.view.widget.CustomCheckbox1
import com.quickrecover.photonvideotool.view.widget.CustomCheckbox2
import com.quickrecover.photonvideotool.view.widget.CustomCheckbox3
import com.quickrecover.photonvideotool.view.widget.DelPop
import com.quickrecover.photonvideotool.view.widget.NavigationWidget1
import com.quickrecover.photonvideotool.view.widget.SetStatusBarLight
import com.quickrecover.photonvideotool.viewmodel.RecoveryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val TAG = "RecoveryFolderItemScree"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryFolderItemScreen(scanType: String, foldBean: FoldBean) {

	val recoveryViewModel: RecoveryViewModel = koinViewModel()

	val navController = LocalNavController.current
	val context = LocalContext.current
	val activity = context as? MainActivity

	val interactionSource = remember { MutableInteractionSource() }

	val folderSorts by recoveryViewModel.foldSortBeans.collectAsState()

	val currentFold by recoveryViewModel.currentFold.collectAsState()
	val foldFilter by recoveryViewModel.foldFilter.collectAsState()
	val tmpFoldFilter by recoveryViewModel.tmpFoldFilter.collectAsState()
	val sortBy by recoveryViewModel.sortBy.collectAsState()
	val tmpSortBy by recoveryViewModel.tmpSortBy.collectAsState()
	val orderType by recoveryViewModel.orderType.collectAsState()
	val tmpOrderType by recoveryViewModel.tmpOrderType.collectAsState()
	val hideLowQualityPhotos by recoveryViewModel.hideLowQualityPhotos.collectAsState()
	val hideLowQualityPhotosSize by recoveryViewModel.hideLowQualityPhotosSize.collectAsState()
	val isSelectAll by recoveryViewModel.isSelectAll.collectAsState()
	val hasRecovery by recoveryViewModel.hasRecovery.collectAsState()
	val tips by recoveryViewModel.tips.collectAsState()
	val tipsTime by recoveryViewModel.tipsTime.collectAsState()

	var showFilterSheet by remember { mutableStateOf(false) }
	val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

	var showSortSheet by remember { mutableStateOf(false) }
	val sortSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

	var showDelPop by remember { mutableStateOf(false) }

	val lifecycleOwner = LocalLifecycleOwner.current
	DisposableEffect(lifecycleOwner) {
		val observer = LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_CREATE) {
				Log.e(TAG, "RecoveryFolderItemScreen: ON_CREATE" )
			}
			if (event == Lifecycle.Event.ON_START) {
				Log.e(TAG, "RecoveryFolderItemScreen: ON_START" )
			}
			if (event == Lifecycle.Event.ON_RESUME) {
				Log.e(TAG, "RecoveryFolderItemScreen: ON_RESUME" )
				Log.e(TAG, "RecoveryFolderItemScreen: tmpToDelCacheFilePath = ${FileCache.tmpToDelCacheFilePath}" )
				runBlocking {
					val localFoldBean = recoveryViewModel.getCurrentFold(scanType)
					if (localFoldBean.name == foldBean.name && localFoldBean != null && localFoldBean.name.isNotEmpty() && localFoldBean.files.isNotEmpty()) {
						foldBean.files = localFoldBean.files
					}
					if (foldBean.name.isNotEmpty()) {
						Log.e(TAG, "RecoveryFolderItemScreen: foldBean is not null.")
						if (FileCache.tmpToDelCacheFilePath != "") {
							foldBean.files = foldBean.files.filter { it.path != FileCache.tmpToDelCacheFilePath }
							FileCache.tmpToDelCacheFilePath = ""
						}
						recoveryViewModel.setCurrenntFold(scanType, foldBean)
					}
				}
			}
			if (event == Lifecycle.Event.ON_PAUSE) {
				Log.e(TAG, "RecoveryFolderItemScreen: ON_PAUSE", )
			}
			if (event == Lifecycle.Event.ON_STOP) {
				Log.e(TAG, "RecoveryFolderItemScreen: ON_STOP", )
			}
			if (event == Lifecycle.Event.ON_DESTROY) {
				Log.e(TAG, "RecoveryFolderItemScreen: ON_DESTROY", )
			}
		}
		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose {
			lifecycleOwner.lifecycle.removeObserver(observer)
		}
	}

	LaunchedEffect(hasRecovery) {
		if (hasRecovery) {
			navController.navigate("${Routes.RecoveryFile}/${scanType}")
			recoveryViewModel.resetHasRecovery()
		}
	}

	LaunchedEffect(tipsTime) {
		if (tips.isNotEmpty()) {
			Toast.makeText(context, tips, Toast.LENGTH_SHORT).show()
		}
	}

	SetStatusBarLight(true)

	Box(Modifier.fillMaxSize()) {
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
				NavigationWidget1(
					foldBean.name,
					navController,
					hasDone = true,
					doneTitle = stringResource(
						R.string.recovery_count,
						currentFold
							.files.filter { file ->
								if (hideLowQualityPhotos) {
									file.size > 1_048_576
								} else {
									true
								}
							}.count {
								it.isSelect
							}
					),
					onClick =  {
						recoveryViewModel.recoveryFiles(context, scanType)
					}
				)
				Spacer(modifier = Modifier.height(32.dp))
				Row {
					FilterView(
						Modifier.clickable(
							interactionSource = interactionSource,
							indication = null
						) {
							showFilterSheet = true
						}
					)
					Spacer(modifier = Modifier.width(19.dp))
					SortView(
						sortBy,
						Modifier.clickable(
							interactionSource = interactionSource,
							indication = null
						) {
							showSortSheet = true
						}
					)
					Spacer(modifier = Modifier.weight(1f))
					Image(
						painter = painterResource(id = R.drawable.del),
						contentDescription = null,
						modifier = Modifier
							.size(24.dp)
							.clickable(
								interactionSource = interactionSource,
								indication = null
							) {
								showDelPop = true
							}
					)
				}
				Spacer(modifier = Modifier.height(20.dp))
				SelectAll(checked = isSelectAll, onCheckedChange = {
					recoveryViewModel.selectAll(it)
				})
				Spacer(modifier = Modifier.height(10.dp))
				if (scanType == "recovery_photos") {
					HideLowQualityPhotos(
						checked = hideLowQualityPhotos,
						hideLowQualityPhotosSize = hideLowQualityPhotosSize,
						onCheckedChange = {
							recoveryViewModel.setHideLowQualityPhotos(it)
						}
					)
				}
				LazyColumn(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
				) {
					val filteredFolderSorts = if (hideLowQualityPhotos) {
						folderSorts.map { foldSortBean ->
							foldSortBean.copy(
								files = foldSortBean.files.filter { file ->
									file.size > 1_048_576
								}
							)
						}.filter { it.files.isNotEmpty() } // 过滤掉没有文件的日期组
					} else {
						folderSorts
					}
					items(filteredFolderSorts.size) { index ->
						FolderDateItem(
							modifier = Modifier.fillMaxWidth(),
							scanType = scanType,
							showTitle = sortBy == 0,
							date = filteredFolderSorts[index],
							onCheckedChange = {
								recoveryViewModel.selectSort(it, filteredFolderSorts[index])
							},
							onImgCheckedChange = { checked, fileData ->
								recoveryViewModel.selectFile(checked, fileData)
							}
						)
					}
				}
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(150.dp)
				) {

				}
			}

		}

		if (showDelPop) {
			DelPop { flag ->
				showDelPop = false
				if (flag) {
					recoveryViewModel.delFiles(scanType)
				}
			}
		}
	}

	// Filter底部弹框
	if (showFilterSheet) {
		recoveryViewModel.setTmpFilter()
		ModalBottomSheet(
			onDismissRequest = { showFilterSheet = false },
			sheetState = filterSheetState,
			modifier = Modifier
				.fillMaxWidth()
		) {
			Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 32.dp)) {
				Text(stringResource(R.string.filters), style = TextStyle.TextStyle_18sp_w600_252040)
				Spacer(modifier = Modifier.height(23.dp))
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.Start,
				) {
					Text(
						text = stringResource(R.string.all_days),
						style = TextStyle.TextStyle_14sp_w500_252040
					)
					Spacer(modifier = Modifier.weight(1f))
					CustomCheckbox1(
						checked = tmpFoldFilter == 0,
						onCheckedChange = { recoveryViewModel.setTmpFilter(0) },
						modifier = Modifier.size(24.dp)
					)
				}
				Spacer(modifier = Modifier.height(23.dp))
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.Start,
				) {
					Text(
						text = stringResource(R.string.within_7_days),
						style = TextStyle.TextStyle_14sp_w500_252040
					)
					Spacer(modifier = Modifier.weight(1f))
					CustomCheckbox1(
						checked = tmpFoldFilter == 1,
						onCheckedChange = { recoveryViewModel.setTmpFilter(1) },
						modifier = Modifier.size(24.dp)
					)
				}
				Spacer(modifier = Modifier.height(23.dp))
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.Start,
				) {
					Text(
						text = stringResource(R.string.within_1_months),
						style = TextStyle.TextStyle_14sp_w500_252040
					)
					Spacer(modifier = Modifier.weight(1f))
					CustomCheckbox1(
						checked = tmpFoldFilter == 2,
						onCheckedChange = { recoveryViewModel.setTmpFilter(2) },
						modifier = Modifier.size(24.dp)
					)
				}
				Spacer(modifier = Modifier.height(23.dp))
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.Start,
				) {
					Text(
						text = stringResource(R.string.within_6_months),
						style = TextStyle.TextStyle_14sp_w500_252040
					)
					Spacer(modifier = Modifier.weight(1f))
					CustomCheckbox1(
						checked = tmpFoldFilter == 3,
						onCheckedChange = { recoveryViewModel.setTmpFilter(3) },
						modifier = Modifier.size(24.dp)
					)
				}
				Spacer(modifier = Modifier.height(28.dp))
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(44.dp)
						.background(
							brush = Gradient_9E7BFB_to_784BF1,
							shape = RoundedCornerShape(32.dp)
						)
						.clickable(
							interactionSource = interactionSource,
							indication = null
						) {
							showFilterSheet = false
							recoveryViewModel.setFilter()
						},
					contentAlignment = Alignment.Center
				) {
					Text(
						text = stringResource(R.string.ok),
						style = TextStyle.TextStyle_20sp_w600_FFF,
						textAlign = TextAlign.Center,
						modifier = Modifier.wrapContentSize()
					)
				}
				Spacer(modifier = Modifier.height(12.dp))
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(44.dp)
						.clickable(
							interactionSource = interactionSource,
							indication = null
						) {
							recoveryViewModel.resetFilter()
							showFilterSheet = false
						},
					contentAlignment = Alignment.Center
				) {
					Text(
						text = stringResource(R.string.reset),
						style = TextStyle.TextStyle_20sp_w500_252040_35,
						textAlign = TextAlign.Center,
						modifier = Modifier.wrapContentSize()
					)
				}
				Spacer(modifier = Modifier.height(20.dp))
			}
		}
	}

	//排序底部弹框
	if (showSortSheet) {
		recoveryViewModel.setTmpSortAndOrder()
		ModalBottomSheet(
			onDismissRequest = { showSortSheet = false },
			sheetState = sortSheetState,
			modifier = Modifier.fillMaxWidth()
		) {
			Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 32.dp)) {
				Text(stringResource(R.string.sort_by), style = TextStyle.TextStyle_18sp_w600_252040)
				Spacer(modifier = Modifier.height(23.dp))
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.Start,
				) {
					Text(
						text = stringResource(R.string.date),
						style = TextStyle.TextStyle_14sp_w500_252040
					)
					Spacer(modifier = Modifier.weight(1f))
					CustomCheckbox1(
						checked = tmpSortBy == 0,
						onCheckedChange = { recoveryViewModel.setTmpSort(0) },
						modifier = Modifier.size(24.dp)
					)
				}
				Spacer(modifier = Modifier.height(23.dp))
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.Start,
				) {
					Text(
						text = stringResource(R.string.file_size),
						style = TextStyle.TextStyle_14sp_w500_252040
					)
					Spacer(modifier = Modifier.weight(1f))
					CustomCheckbox1(
						checked = tmpSortBy == 1,
						onCheckedChange = { recoveryViewModel.setTmpSort(1) },
						modifier = Modifier.size(24.dp)
					)
				}
				Spacer(modifier = Modifier.height(23.dp))
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(
							color = Color_252040_10,
							shape = RoundedCornerShape(1.dp)
						)
				)
				Spacer(modifier = Modifier.height(23.dp))
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.Start,
				) {
					Text(
						text = stringResource(R.string.ascending),
						style = TextStyle.TextStyle_14sp_w500_252040
					)
					Spacer(modifier = Modifier.weight(1f))
					CustomCheckbox1(
						checked = tmpOrderType == 0,
						onCheckedChange = { recoveryViewModel.setTmpOrder(0) },
						modifier = Modifier.size(24.dp)
					)
				}
				Spacer(modifier = Modifier.height(23.dp))
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.Start,
				) {
					Text(
						text = stringResource(R.string.descending),
						style = TextStyle.TextStyle_14sp_w500_252040
					)
					Spacer(modifier = Modifier.weight(1f))
					CustomCheckbox1(
						checked = tmpOrderType == 1,
						onCheckedChange = { recoveryViewModel.setTmpOrder(1) },
						modifier = Modifier.size(24.dp)
					)
				}
				Spacer(modifier = Modifier.height(28.dp))
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(44.dp)
						.background(
							brush = Gradient_9E7BFB_to_784BF1,
							shape = RoundedCornerShape(32.dp)
						)
						.clickable(
							interactionSource = interactionSource,
							indication = null
						) {
							recoveryViewModel.setSortAndOrder()
							showSortSheet = false
						},
					contentAlignment = Alignment.Center
				) {
					Text(
						text = stringResource(R.string.ok),
						style = TextStyle.TextStyle_20sp_w600_FFF,
						textAlign = TextAlign.Center,
						modifier = Modifier.wrapContentSize()
					)
				}
				Spacer(modifier = Modifier.height(12.dp))
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(44.dp)
						.clickable(
							interactionSource = interactionSource,
							indication = null
						) {
							recoveryViewModel.resetSortAndOrder()
							showSortSheet = false
						},
					contentAlignment = Alignment.Center
				) {
					Text(
						text = stringResource(R.string.reset),
						style = TextStyle.TextStyle_20sp_w500_252040_35,
						textAlign = TextAlign.Center,
						modifier = Modifier.wrapContentSize()
					)
				}
				Spacer(modifier = Modifier.height(20.dp))
			}
		}
	}
}

@Composable
fun FilterView(modifier: Modifier = Modifier) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Start,
		modifier = modifier
	) {
		Text(
			text = stringResource(R.string.filters),
			style = TextStyle.TextStyle_16sp_w600_252040
		)
		Spacer(modifier = Modifier.width(4.dp))
		Image(
			painter = painterResource(id = R.drawable.arrow_down),
			contentDescription = null,
			modifier = Modifier
				.size(24.dp)
		)
	}
}

@Composable
fun SortView(sortBy: Int, modifier: Modifier = Modifier) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Start,
		modifier = modifier
	) {
		Text(
			text = stringResource(
				R.string.sort_by_type,
				if (sortBy == 0) stringResource(R.string.date) else stringResource(R.string.file_size)
			),
			style = TextStyle.TextStyle_16sp_w600_252040
		)
		Spacer(modifier = Modifier.width(4.dp))
		Image(
			painter = painterResource(id = R.drawable.arrow_down),
			contentDescription = null,
			modifier = Modifier
				.size(24.dp)
		)
	}
}

//Hide low quality photos (259)
@Composable
fun HideLowQualityPhotos(modifier: Modifier = Modifier, checked: Boolean, hideLowQualityPhotosSize: Int, onCheckedChange: (Boolean) -> Unit) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Start,
		modifier = modifier
	) {
		Text(
			text = stringResource(R.string.hide_low_quality_photos),
			style = TextStyle.TextStyle_12sp_w500_252040_60
		)
		Text(
			text = " (${hideLowQualityPhotosSize})",
			style = TextStyle.TextStyle_12sp_w500_252040_35
		)
		Spacer(modifier = Modifier.weight(1f))
		Switch(
			modifier = Modifier.scale(0.7f),
			checked = checked,
			onCheckedChange = onCheckedChange,
			colors = SwitchDefaults.colors(
				checkedThumbColor = Color.White,
				checkedTrackColor = Color_9E7BFB,
				uncheckedThumbColor = Color_9E7BFB,
				uncheckedTrackColor = Color_F3EEFF,
				uncheckedBorderColor = Color_F3EEFF
			)
		)
	}
}

@Composable
fun SelectAll(modifier: Modifier = Modifier, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Start,
		modifier = modifier
	) {
		CustomCheckbox(
			checked = checked,
			modifier = Modifier.size(22.dp),
			onCheckedChange = onCheckedChange
		)
		Spacer(modifier = Modifier.width(4.dp))
		Text(
			text = "Select All",
			style = TextStyle.TextStyle_16sp_w600_252040
		)
	}
}

@Composable
fun FolderDateItem(
	modifier: Modifier = Modifier,
	showTitle: Boolean,
	date: FoldSortBean,
	scanType: String,
	onCheckedChange: (Boolean) -> Unit,
	onImgCheckedChange: (Boolean, FileData) -> Unit
) {
	val navController = LocalNavController.current

	val context = LocalContext.current

	val columns = 3
	val itemSize = 106.dp
	val itemSpacing = 4.dp
	val rows = (date.files.size + columns - 1) / columns

	Column(
		verticalArrangement = Arrangement.Center,
		modifier = modifier
	) {
		if (showTitle) {
			Spacer(modifier = Modifier.height(20.dp))
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.Start,
				modifier = modifier
			) {
				CustomCheckbox2(
					checked = date.checked,
					modifier = Modifier.size(22.dp),
					onCheckedChange = onCheckedChange
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = date.name,
					style = TextStyle.TextStyle_16sp_w600_252040
				)
			}
		}

		Spacer(modifier = Modifier.height(20.dp))
		when(scanType) {
			"recovery_photos", "recovery_videos" -> {
				Column {
					for (rowIndex in 0 until rows) {
						Row(
							horizontalArrangement = Arrangement.spacedBy(itemSpacing),
							modifier = Modifier.padding(bottom = itemSpacing)
						) {
							for (colIndex in 0 until columns) {
								val itemIndex = rowIndex * columns + colIndex
								if (itemIndex < date.files.size) {
									Box(
										modifier = Modifier
											.size(itemSize)
									) {
										ImageThumbnail(
											path = date.files[itemIndex].path,
											modifier = Modifier
												.size(itemSize)
												.clickable(
													interactionSource = remember { MutableInteractionSource() },
													indication = null
												) {
													//  点击查看图片
													val encodedPath = URLEncoder.encode(
														date.files[itemIndex].path,
														StandardCharsets.UTF_8.toString()
													)
													navController.navigate("${Routes.FileDetail}/$encodedPath/false")
												}
										)
										if (scanType == "recovery_videos") {
											Image(
												painter = painterResource(id = R.drawable.video_play),
												contentDescription = null,
												contentScale = ContentScale.Crop,
												modifier = Modifier
													.align(Alignment.Center)
													.size(24.dp),
											)
										}
										CustomCheckbox3(
											checked = date.files[itemIndex].isSelect,
											modifier = Modifier
												.align(Alignment.TopEnd)
												.padding(8.dp)
												.size(16.dp),
											onCheckedChange = { checked ->
												onImgCheckedChange(checked, date.files[itemIndex])
											}
										)
									}
								} else {
									Spacer(modifier = Modifier.size(itemSize))
								}
							}
						}
					}
				}
			}
			else -> {
				Column(modifier = Modifier.fillMaxWidth()) {
					for ((index, item) in date.files.withIndex()) {
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.border(
									1.dp,
									Color_9E7BFB.copy(alpha = 0.25f),
									shape = RoundedCornerShape(16.dp)
								)
								.padding(start = 16.dp, top = 12.dp, end = 50.dp, bottom = 12.dp)
								.clickable(
									interactionSource = remember { MutableInteractionSource() },
									indication = null
								) {
									val encodedPath = URLEncoder.encode(
										date.files[index].path,
										StandardCharsets.UTF_8.toString()
									)
									navController.navigate("${Routes.FileDetail}/$encodedPath/false")
//									val mimeType = when (getFileType(item.name)) {
//										"PDF" -> "application/pdf"
//										"Word" -> "application/msword"
//										"Excel" -> "application/vnd.ms-excel"
//										"PPT" -> "application/vnd.ms-powerpoint"
//										"TXT" -> "text/plain"
//										"OTHER" -> "*/*"
//										else -> "*/*"
//									}
//									val intent = Intent(Intent.ACTION_VIEW).apply {
//										setDataAndType(item.path.toUri(), mimeType)
//										flags = Intent.FLAG_ACTIVITY_NEW_TASK
//									}
//									context.startActivity(intent)
								}
						) {
							Row (
								modifier = Modifier.fillMaxWidth(),
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.Start
							) {
								CustomCheckbox1(
									checked = item.isSelect,
									modifier = Modifier
										.size(22.dp),
									onCheckedChange = { checked ->
										onImgCheckedChange(checked, item)
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
										.size(40.dp)
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
//										//显示文件名后缀
//										Text(
//											item.name.substringAfterLast('.'),
//											maxLines = 1,
//											overflow = TextOverflow.Ellipsis,
//											modifier = Modifier,
//											style = TextStyle.TextStyle_16sp_w600_252040
//										)
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
						if (index < date.files.size - 1) {
							Spacer(modifier = Modifier.height(16.dp))
						}
					}
				}
			}
		}

	}
}

fun getFileType(fileName: String): String {
	val extension = fileName.substringAfterLast('.', "").lowercase()
	return when (extension) {
		"pdf" -> "PDF"
		"doc", "docx" -> "Word"
		"xls", "xlsx" -> "Excel"
		"ppt", "pptx" -> "PPT"
		"txt", "json" -> "TXT"
		"" -> "OTHER"
		else -> "OTHER"
	}
}

@Composable
fun ImageThumbnail(path: String, modifier: Modifier = Modifier) {
	val context = LocalContext.current
	var bitmap by remember { mutableStateOf<Bitmap?>(null) }

	LaunchedEffect(path) {
		withContext(Dispatchers.IO) {
			try {
				val thumbnailRequest = Glide.with(context)
					.asBitmap()
					.load(path)
					.override(300)
				val loadedBitmap = Glide.with(context)
					.asBitmap()
					.load(path)
					.thumbnail(thumbnailRequest)
					.submit()
					.get()
				bitmap = loadedBitmap
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}

	bitmap?.let {
		Image(
			bitmap = it.asImageBitmap(),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = modifier.clip(RoundedCornerShape(8.dp))
		)
	}
}

@Composable
fun ImageThumbnailNoClip(path: String, modifier: Modifier = Modifier) {
	val context = LocalContext.current
	var bitmap by remember { mutableStateOf<Bitmap?>(null) }

	LaunchedEffect(path) {
		withContext(Dispatchers.IO) {
			try {
				val thumbnailRequest = Glide.with(context)
					.asBitmap()
					.load(path)
					.override(300)
				val loadedBitmap = Glide.with(context)
					.asBitmap()
					.load(path)
					.thumbnail(thumbnailRequest)
					.submit()
					.get()
				bitmap = loadedBitmap
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}

	bitmap?.let {
		Image(
			bitmap = it.asImageBitmap(),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = modifier
		)
	}
}

//@Preview
//@Composable
//fun RecoveryFolderItemScreenPreview() {
//	val navController = rememberNavController()
//	CompositionLocalProvider(
//		LocalNavController provides navController,
//		LocalInnerPadding provides PaddingValues(0.dp)
//	) {
//		RecoveryFolderItemScreen("recovery_photos", FoldBean("All", listOf()))
//	}
//}