# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.datatool.photorecovery.core.bean.CapacityBean {
    *;
}

-keep class com.datatool.photorecovery.core.bean.FileData {
    *;
}

-keep class com.datatool.photorecovery.core.bean.FoldBean {
    *;
}

# 保留 Kotlin 反射相关
-keepclassmembers class kotlin.Metadata { *; }

# 保留所有注解
-keepattributes *Annotation*

# 保留所有实现了 Parcelable 的类
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# 保留 Gson 反射相关类
-keep class com.google.gson.** { *; }
-keepattributes Signature

# 保留 Koin Compose 相关类和接口
-keep class org.koin.androidx.compose.** { *; }
-keep interface org.koin.androidx.compose.** { *; }

# 保留 Koin 核心类和接口
-keep class org.koin.** { *; }
-keep interface org.koin.** { *; }

# 保留 Koin 注解相关方法
-keepclassmembers class * {
    @org.koin.core.annotation.* *;
}

# 保留所有注解
-keepattributes *Annotation*

# 保留 Coil 的核心类和接口
-keep class coil.** { *; }
-keep interface coil.** { *; }

# 保留 Coil 的 Kotlin 协程扩展相关类
-keep class coil.coroutines.** { *; }

# 保留 Coil 的注解和元数据，避免 Kotlin 反射出错
-keepclassmembers class kotlin.Metadata { *; }
-keepattributes *Annotation*

# 保留 Glide 核心类和接口
-keep class com.bumptech.glide.** { *; }
-keep interface com.bumptech.glide.** { *; }

# 保留 Glide 生成的 API 类
-keep class **$$GlideModuleImpl { *; }
-keep class **$$GlideOptions { *; }

# 保留所有实现了 GlideModule 的类
-keep class * implements com.bumptech.glide.module.GlideModule

# 保留所有实现了 AppGlideModule 的类
-keep class * implements com.bumptech.glide.module.AppGlideModule

# 保留所有实现了 LibraryGlideModule 的类
-keep class * implements com.bumptech.glide.module.LibraryGlideModule

# 保留 Kotlin 元数据，避免 Kotlin 反射出错
-keepclassmembers class kotlin.Metadata { *; }
-keepattributes *Annotation*

-keep class com.datatool.photorecovery.notification.PushConfig { *; }
-keep class com.datatool.photorecovery.notification.Scene { *; }
-keep class com.datatool.photorecovery.notification.AppInstalled { *; }
-keep class com.datatool.photorecovery.notification.AppUninstalled { *; }
-keep class com.datatool.photorecovery.notification.ChargingEnd { *; }
-keep class com.datatool.photorecovery.notification.ChargingStart { *; }
-keep class com.datatool.photorecovery.notification.PhoneUnlock { *; }
-keep class com.datatool.photorecovery.notification.ScreenOn5s { *; }
-keep class com.datatool.photorecovery.notification.DeletePhotos { *; }
-keep class com.datatool.photorecovery.notification.DeleteVideos { *; }
-keep class com.datatool.photorecovery.notification.DeleteFiles { *; }
-keep class com.datatool.photorecovery.notification.Message { *; }
-keep class com.datatool.photorecovery.notification.Key { *; }
-keep class com.datatool.photorecovery.notification.CommonService {
    *;
}