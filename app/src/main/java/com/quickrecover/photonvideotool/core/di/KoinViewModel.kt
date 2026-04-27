package com.quickrecover.photonvideotool.core.di

import com.quickrecover.photonvideotool.viewmodel.HomeViewModel
import com.quickrecover.photonvideotool.viewmodel.RecoveriedViewModel
import com.quickrecover.photonvideotool.viewmodel.RecoveryViewModel
import com.quickrecover.photonvideotool.viewmodel.ScanViewModel
import com.quickrecover.photonvideotool.viewmodel.SplashViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val splashViewModel = module {
	viewModel { SplashViewModel(get()) }
}
val homeViewModel = module {
	viewModel { HomeViewModel(get(), get()) }
}

val scanViewModel = module {
	viewModel { ScanViewModel(get(), get()) }
}
val recoveryViewModel = module {
	viewModel { RecoveryViewModel( get()) }
}

val recoveriedViewModel = module {
	viewModel { RecoveriedViewModel(get()) }
}
