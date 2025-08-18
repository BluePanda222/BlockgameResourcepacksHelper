package com.bluepanda22.blockgameresourcepackshelper.migrator

data class ResourcepackMigrateStatus(
	val resourcepackName: String,
	val migrateStatus: MigrateStatus,
	val throwable: Throwable? = null,
)
