package com.quickrecover.photonvideotool.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController
import com.quickrecover.photonvideotool.FileCache
import com.quickrecover.photonvideotool.LocalInnerPadding
import com.quickrecover.photonvideotool.LocalNavController
import com.quickrecover.photonvideotool.MainActivity
import com.quickrecover.photonvideotool.R
import com.quickrecover.photonvideotool.core.AreaKey
import com.quickrecover.photonvideotool.core.LogConfig
import com.quickrecover.photonvideotool.core.route.Routes
import com.quickrecover.photonvideotool.ui.theme.Color_9E7BFB
import com.quickrecover.photonvideotool.ui.theme.Gradient_9E7BFB_to_784BF1
import com.quickrecover.photonvideotool.ui.theme.TextStyle
import com.quickrecover.photonvideotool.view.widget.BannerAd
import com.quickrecover.photonvideotool.view.widget.DelPop
import com.quickrecover.photonvideotool.viewmodel.RecoveryViewModel
import org.koin.compose.viewmodel.koinViewModel
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: add
//import com.pdffox.adv.use.log.LogUtil

@Composable
fun FileDetailScreen(filePath: String, recovered: Boolean = false) {
	val navController = LocalNavController.current
	val context = LocalContext.current
	val recoveryViewModel: RecoveryViewModel = koinViewModel()
	val activity = context as? MainActivity

	val interactionSource = remember { MutableInteractionSource() }
	var showDelPop by remember { mutableStateOf(false) }

	val file = File(filePath)
	val fileName = filePath.split("/").last()
	val fileType = fileName.split(".").last()
	val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic")
	val videoExtensions = listOf("mp4", "avi", "mov", "mkv", "flv", "wmv", "webm")
	val fileTypeCategory = remember(fileType) {
		when (fileType.lowercase(Locale.getDefault())) {
			in imageExtensions -> "Image"
			in videoExtensions -> "Video"
			else -> "File"
		}
	}

	val unknown_resolution = stringResource(R.string.unknown_resolution)
	val _00_00_00 = stringResource(R.string._00_00_00)
	val unknown_size = stringResource(R.string.unknown_size)
	val unknown_date = stringResource(R.string.unknown_date)

	val imageSize = remember(filePath) {
		if (file.exists() && fileTypeCategory == "Image") {
			val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
			BitmapFactory.decodeFile(filePath, options)
			"${options.outWidth}x${options.outHeight}"
		} else {
			unknown_resolution
		}
	}

	val videoSize = remember(filePath) {
		if (file.exists() && fileTypeCategory == "Video") {
			val retriever = MediaMetadataRetriever()
			return@remember try {
				retriever.setDataSource(filePath)
				val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
				val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
				if (width != null && height != null) {
					"${width}x${height}"
				} else {
					unknown_resolution
				}
			} catch (e: Exception) {
				unknown_resolution
			} finally {
				retriever.release()
			}
		} else {
			unknown_resolution
		}
	}

	val videoDuration = remember(filePath) {
		if (file.exists() && fileTypeCategory == "Video") {
			val retriever = MediaMetadataRetriever()
			try {
				retriever.setDataSource(filePath)
				val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
				val durationMs = durationStr?.toLongOrNull() ?: 0L
				val totalSeconds = durationMs / 1000
				val hours = totalSeconds / 3600
				val minutes = (totalSeconds % 3600) / 60
				val seconds = totalSeconds % 60
				String.format("%02d:%02d:%02d", hours, minutes, seconds)
			} catch (e: Exception) {
				_00_00_00
			} finally {
				retriever.release()
			}
		} else {
			_00_00_00
		}
	}

	val fileSize = remember(filePath) {
		if (file.exists()) {
			formatFileSize(file.length())
		} else {
				unknown_size
		}
	}
	val lastModifier = remember(filePath) {
		if (file.exists()) {
			val lastModifiedMillis = file.lastModified()
			val sdf = SimpleDateFormat("MMM dd.yyyy")
			sdf.format(Date(lastModifiedMillis))
		} else {
			unknown_date
		}
	}

	LaunchedEffect(filePath) {
		val scanType = when (fileTypeCategory) {
			"Image" -> "recovery_photos"
			"Video" -> "recovery_videos"
			else -> "other_files"
		}
		recoveryViewModel.getCurrentFold(scanType)
	}

	// TODO: add
//	LaunchedEffect(Unit) {
//		LogUtil.log(LogConfig.detail_page, mapOf())
//	}

	BackHandler {
		navController.popBackStack()
	}

	Box(Modifier.fillMaxSize()) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(LocalInnerPadding.current)
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
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
								showDelPop = true
							}
					)
				}
				Spacer(modifier = Modifier.size(18.dp))
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(220.dp),
				) {
					when (fileTypeCategory) {
						"Image" -> {
							ImageThumbnailNoClip(
								path = filePath,
								modifier = Modifier
									.fillMaxSize()
									.clickable(
										interactionSource = interactionSource,
										indication = null
									) {
										navController.navigate(
											"${Routes.ViewImage}/${
												URLEncoder.encode(
													filePath,
													StandardCharsets.UTF_8.toString()
												)
											}"
										)
									}
							)
						}
						"Video" -> {
							ImageThumbnailNoClip(
								path = filePath,
								modifier = Modifier
									.fillMaxSize()
									.clickable(
										interactionSource = interactionSource,
										indication = null
									) {
										// 点击查看视频
										navController.navigate(
											"${Routes.ViewVideo}/${
												URLEncoder.encode(
													filePath,
													StandardCharsets.UTF_8.toString()
												)
											}"
										)
									}
							)
							Image(
								painter = painterResource(id = R.drawable.video_play),
								contentDescription = null,
								contentScale = ContentScale.Crop,
								modifier = Modifier
									.align(Alignment.Center)
									.size(24.dp),
							)
						}
						else -> {
							Image(
								painter = painterResource(id = when(getFileType(fileName)) {
									"PDF" -> R.drawable.item_pdf
									"Word" -> R.drawable.item_word
									"Excel" -> R.drawable.item_excel
									"PPT" -> R.drawable.item_ppt
									"TXT" -> R.drawable.item_txt
									else -> R.drawable.item_other
								}),
								contentDescription = null,
								modifier = Modifier
									.size(70.dp)
									.align(Alignment.Center)
							)
						}
					}
					if (fileTypeCategory == "Image" || fileTypeCategory == "Video") {
						Image(
							painter = painterResource(id = R.drawable.expand),
							contentDescription = null,
							contentScale = ContentScale.Crop,
							modifier = Modifier
								.align(Alignment.BottomEnd)
								.size(60.dp)
								.padding(16.dp)
								.clickable(
									interactionSource = interactionSource,
									indication = null
								) {
									if (fileTypeCategory == "Image") {
										navController.navigate(
											"${Routes.ViewImage}/${
												URLEncoder.encode(
													filePath,
													StandardCharsets.UTF_8.toString()
												)
											}"
										)
									}
									if (fileTypeCategory == "Video") {
										// 视频播放
										navController.navigate(
											"${Routes.ViewVideo}/${
												URLEncoder.encode(
													filePath,
													StandardCharsets.UTF_8.toString()
												)
											}"
										)
									}
								},
						)
					}
				}

				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 24.dp)
				) {
					Spacer(modifier = Modifier.size(16.dp))
					InfoItem(stringResource(R.string.name), fileName)
					InfoItem(stringResource(R.string.path), filePath)
					if (fileTypeCategory == "Image") {
						InfoItem(stringResource(R.string.resolution), imageSize)
					}
					if (fileTypeCategory == "Video") {
						InfoItem(stringResource(R.string.resolution), videoSize)
					}
					InfoItem(stringResource(R.string.size), fileSize)
					if (fileTypeCategory == "Video") {
						InfoItem(stringResource(R.string.duration), videoDuration)
					}
//					InfoItem(stringResource(R.string.date), "Jun 24.2025")
					InfoItem(stringResource(R.string.date), lastModifier)
					Spacer(modifier = Modifier.weight(1f))
					if (!recovered) {
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
									val scanType = when (fileTypeCategory) {
										"Image" -> "recovery_photos"
										"Video" -> "recovery_videos"
										else -> "other_files"
									}
									recoveryViewModel.recoveryFile(context, scanType, filePath)
									navController.navigate("${Routes.RecoveryFile}/${scanType}") {
										val encodedPath = URLEncoder.encode(
											filePath,
											StandardCharsets.UTF_8.toString()
										)
										popUpTo("${Routes.FileDetail}/$encodedPath/$recovered") {
											inclusive = true
										}
									}
								},
							contentAlignment = Alignment.Center
						) {
							Text(
								text = stringResource(R.string.recovery),
								style = TextStyle.TextStyle_20sp_w600_FFF,
								textAlign = TextAlign.Center,
								modifier = Modifier.wrapContentSize()
							)
						}
					}
					Spacer(modifier = Modifier.height(12.dp))
					if (fileTypeCategory == "File") {
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.height(44.dp)
								.border(
									1.dp,
									Color_9E7BFB.copy(alpha = 0.25f),
									shape = RoundedCornerShape(16.dp)
								)
								.clickable(
									interactionSource = interactionSource,
									indication = null
								) {
									val mimeType = when (getFileType(fileName)) {
										"PDF" -> "application/pdf"
										"Word" -> "application/msword"
										"Excel" -> "application/vnd.ms-excel"
										"PPT" -> "application/vnd.ms-powerpoint"
										"TXT" -> "text/plain"
										"OTHER" -> "*/*"
										else -> "*/*"
									}
									val intent = Intent(Intent.ACTION_VIEW).apply {
										setDataAndType(filePath.toUri(), mimeType)
										flags = Intent.FLAG_ACTIVITY_NEW_TASK
									}
									context.startActivity(intent)
								},
							contentAlignment = Alignment.Center
						) {
							Text(
								text = stringResource(R.string.open),
								style = TextStyle.TextStyle_20sp_w600_9E7BFB,
								textAlign = TextAlign.Center,
								modifier = Modifier.wrapContentSize()
							)
						}
					}
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.height(150.dp)
					) {}
				}
			}
		}
		if (showDelPop) {
			DelPop { flag ->
				showDelPop = false
				if (flag) {
					val scanType = when (fileTypeCategory) {
						"Image" -> "recovery_photos"
						"Video" -> "recovery_videos"
						else -> "other_files"
					}
					recoveryViewModel.delFile(scanType, filePath)
					FileCache.tmpToDelCacheFilePath = filePath
					navController.popBackStack()
				}
			}
		}
	}
}

@Composable
fun InfoItem(title: String, content: String){
	Column {
		Spacer(modifier = Modifier.size(16.dp))
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = title,
				style = TextStyle.TextStyle_14sp_w500_252040
			)
			Spacer(modifier = Modifier.width(12.dp))
			Text(
				text = content,
				textAlign = TextAlign.End,
				modifier = Modifier.width(200.dp),
				lineHeight = 24.sp,
				overflow = TextOverflow.Ellipsis,
				style = TextStyle.TextStyle_14sp_w500_252040_35
			)
		}
	}
}

@Preview(showBackground = true)
@Composable
fun FileDetailScreenPreview() {
	val navController = rememberNavController()
	CompositionLocalProvider(
		LocalNavController provides navController,
		LocalInnerPadding provides PaddingValues(0.dp)
	) {
		FileDetailScreen(filePath = "C:/Users/admin/Desktop/project/PhotoRecovery/app/src/main/java/com/datatool/photorecovery/view/FileDetailScreen.kt", recovered = false)
	}
}