package com.quickrecover.photonvideotool.core.di


val repositoryModules = listOf(
	permissionRepository,
	dataStoreModule,
	recoveryRepository
)
val viewModelModules = listOf(
	splashViewModel,
	homeViewModel,
	scanViewModel,
	recoveryViewModel,
	recoveriedViewModel
)

val allModules = repositoryModules + viewModelModules
