//package com.quickrecover.photonvideotool.notification
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//
//class BootReceiver : BroadcastReceiver() {
//	companion object {
//		private const val TAG = "BootReceiver"
//	}
//
//	override fun onReceive(context: Context, intent: Intent) {
//		if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
//			Log.e(TAG, "onReceive: 启动完成")
//			android.os.Handler(context.mainLooper).postDelayed({
//				try {
//					context.let {
//						val serviceIntent = Intent(it, CommonService::class.java)
//						it.startForegroundService(serviceIntent)
//					}
//				} catch (e: Exception) {
//					Log.e(TAG, "onReceive: 启动服务失败", e)
//				}
//			}, 2000)
//		}
//	}
//
//}