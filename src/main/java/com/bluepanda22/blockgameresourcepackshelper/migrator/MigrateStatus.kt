package com.bluepanda22.blockgameresourcepackshelper.migrator

enum class MigrateStatus {
	SUCCESS,
	SUCCESS_REMIGRATED,
	SKIPPING,
	FAILED,
	FAILED_CURRENTLY_LOADED,
	FAILED_NOT_CONVERTABLE,
	;
}
