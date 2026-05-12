package com.datatool.photorecovery.core.di

import com.datatool.photorecovery.viewmodel.HomeViewModel
import com.datatool.photorecovery.viewmodel.RecoveriedViewModel
import com.datatool.photorecovery.viewmodel.RecoveryViewModel
import com.datatool.photorecovery.viewmodel.ScanViewModel
import com.datatool.photorecovery.viewmodel.SplashViewModel
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
