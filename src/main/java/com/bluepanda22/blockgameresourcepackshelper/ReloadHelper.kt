package com.bluepanda22.blockgameresourcepackshelper

import com.bluepanda22.blockgameresourcepackshelper.migrator.MigrateStatus
import com.bluepanda22.blockgameresourcepackshelper.migrator.ResourcepackMigrateStatus
import com.bluepanda22.blockgameresourcepackshelper.migrator.ResourcepackMigrator.migrateResourcepack
import com.bluepanda22.blockgameresourcepackshelper.migrator.VersionHandler
import com.bluepanda22.blockgameresourcepackshelper.util.DirectoryUtil.cleanupTempDir
import com.bluepanda22.blockgameresourcepackshelper.util.DirectoryUtil.initTempDir
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.ItemAssetsLoader
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Consumer
import kotlin.io.path.nameWithoutExtension

object ReloadHelper {

	private fun reloadResourcepacks(prepareExecutor: Executor): List<ResourcepackMigrateStatus> {
		val futures = mutableListOf<CompletableFuture<ResourcepackMigrateStatus>>()

		initTempDir()

		try {
			Files.newDirectoryStream(BlockgameResourcepacksHelperMain.packsDir).use { directoryStream ->
				for (resourcePackPath in directoryStream) {
					val resourcepackMigrateStatus = CompletableFuture.supplyAsync({
						try {
							if (!isConvertable(resourcePackPath)) {
								val failedNotConvertableStatus = ResourcepackMigrateStatus(
									resourcePackPath.fileName.toString(),
									MigrateStatus.FAILED_NOT_CONVERTABLE,
									null
								)
								return@supplyAsync failedNotConvertableStatus
							} else if (isEnabled(resourcePackPath)) {
								val failedCurrentlyLoadedStatus = ResourcepackMigrateStatus(
									resourcePackPath.fileName.toString(),
									MigrateStatus.FAILED_CURRENTLY_LOADED,
									null
								)
								return@supplyAsync failedCurrentlyLoadedStatus
							} else {
								val migrateStatus = migrateResourcepack(resourcePackPath)
								return@supplyAsync migrateStatus
							}
						} catch (throwable: Throwable) {
							val errorMigrateStatus = ResourcepackMigrateStatus(
								resourcePackPath.fileName.toString(),
								MigrateStatus.FAILED,
								throwable
							)
							return@supplyAsync errorMigrateStatus
						}
					}, prepareExecutor)
					futures.add(resourcepackMigrateStatus)
				}
			}
		} catch (ex: IOException) {
			BlockgameResourcepacksHelperMain.LOGGER.warn(
				"Failed to list packs in {}",
				BlockgameResourcepacksHelperMain.packsDir,
				ex
			)
		}

		val allFutures = CompletableFuture.allOf(*futures.toTypedArray())
		val migrateStatusesFuture = allFutures.thenApplyAsync({ futures.map { it.join() } }, prepareExecutor)
		val migrateStatus = migrateStatusesFuture.get()

		cleanupTempDir()

		return migrateStatus
	}

	private fun isEnabled(resourcePackPath: Path): Boolean {
		val fileName = resourcePackPath.nameWithoutExtension
		val enabledIds = MinecraftClient.getInstance().resourcePackManager.enabledIds.stream().map { id -> id.replace("file/", "") }

		return enabledIds.anyMatch { id -> id.equals(fileName) }
	}

	private fun isConvertable(resourcePackPath: Path): Boolean {
		val fileName = resourcePackPath.nameWithoutExtension
		val convertableIds = BlockgameResourcepacksHelperMain.convertableResourcepacks.stream()

		return convertableIds.anyMatch { id: String -> id == fileName }
	}

	private data class PreparedData(
		val itemIdsData: ItemAssetsLoader.Result,
		val resourcepackMigrateStatusData: List<ResourcepackMigrateStatus>,
	)

	fun reload(
		synchronizer: ResourceReloader.Synchronizer,
		manager: ResourceManager,
		prepareExecutor: Executor,
		applyExecutor: Executor
	): CompletableFuture<Void> {

		// === CLEAR ===

		BlockgameResourcepacksHelperMain.LOADED_ITEM_IDS.clear()
		BlockgameResourcepacksHelperMain.MISSING_EQUIPMENT_ASSET_IDS.clear()
		BlockgameResourcepacksHelperMain.MISSING_TEXTURE_IDS.clear()


		// === PREPARE ===

		val loadIds = ItemAssetsLoader.load(manager, prepareExecutor)

		val loadResourcePackMigrateStatusData = CompletableFuture.supplyAsync({
//			reloadResourcepacks(prepareExecutor)
			listOf<ResourcepackMigrateStatus>()
		}, prepareExecutor)


		// === COMBINE ===

		val preparedDataFuture = CompletableFuture.allOf(
			loadIds,
			loadResourcePackMigrateStatusData
		).thenApplyAsync({ void ->
			PreparedData(
				loadIds.join(),
				loadResourcePackMigrateStatusData.join(),
			)
		}, prepareExecutor)


		// === SYNCHRONIZER ===

		val applyStart = preparedDataFuture.thenCompose { preparedData ->
			MinecraftClient.getInstance().textureManager.reload(synchronizer, manager, prepareExecutor, applyExecutor).thenCompose {
				synchronizer.whenPrepared(preparedData)
			}
		}


		// === APPLY ===

		return applyStart.thenAcceptAsync({ preparedData ->

			preparedData.itemIdsData.contents.forEach { identifier, _ ->
				if (identifier.namespace == "blockgame") {
					BlockgameResourcepacksHelperMain.LOADED_ITEM_IDS.add(identifier)
				}
			}

			BlockgameResourcepacksHelperMain.LOGGER.debug("=".repeat(10) + " LOADED ITEM IDS " + "=".repeat(10))
			BlockgameResourcepacksHelperMain.LOADED_ITEM_IDS.sortedBy { it.path }.forEach {
				BlockgameResourcepacksHelperMain.LOGGER.debug(it.toString())
			}
			BlockgameResourcepacksHelperMain.LOGGER.debug("=".repeat(10) + " LOADED ITEM IDS " + "=".repeat(10))

//			preparedData.resourcepackMigrateStatusData.forEach(
//				Consumer { resourcepackMigrateStatus: ResourcepackMigrateStatus ->
//				when (resourcepackMigrateStatus.migrateStatus) {
//					MigrateStatus.SUCCESS -> {
//						BlockgameResourcepacksHelperMain.LOGGER.info(
//							"Successfully migrated Resourcepack '{}'!",
//							resourcepackMigrateStatus.resourcepackName
//						)
//					}
//
//					MigrateStatus.SUCCESS_REMIGRATED -> {
//						BlockgameResourcepacksHelperMain.LOGGER.info(
//							"Successfully re-migrated Resourcepack '{}' for version {}!",
//							resourcepackMigrateStatus.resourcepackName,
//							VersionHandler.VERSION
//						)
//					}
//
//					MigrateStatus.SKIPPING -> {
//						BlockgameResourcepacksHelperMain.LOGGER.info(
//							"Skipped already migrated Resourcepack '{}'!",
//							resourcepackMigrateStatus.resourcepackName
//						)
//					}
//
//					MigrateStatus.FAILED_CURRENTLY_LOADED -> {
//						BlockgameResourcepacksHelperMain.LOGGER.warn(
//							"Can't migrate Resourcepack that is currently loaded '{}'!",
//							resourcepackMigrateStatus.resourcepackName
//						)
//					}
//
//					MigrateStatus.FAILED_NOT_CONVERTABLE -> {
//						BlockgameResourcepacksHelperMain.LOGGER.warn(
//							"Can't migrate Resourcepack that isn't supported by this Mod '{}'!",
//							resourcepackMigrateStatus.resourcepackName
//						)
//					}
//
//					MigrateStatus.FAILED -> {
//						BlockgameResourcepacksHelperMain.LOGGER.error(
//							"Error in migrating Resourcepack '{}'!",
//							resourcepackMigrateStatus.resourcepackName,
//							resourcepackMigrateStatus.throwable
//						)
//					}
//				}
//			})
		}, applyExecutor)
	}

}