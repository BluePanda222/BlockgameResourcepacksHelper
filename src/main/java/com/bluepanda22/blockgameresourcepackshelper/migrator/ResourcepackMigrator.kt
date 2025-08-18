package com.bluepanda22.blockgameresourcepackshelper.migrator

import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain.Companion.LOGGER
import com.bluepanda22.blockgameresourcepackshelper.migrator.conversion.DataConverter
import com.bluepanda22.blockgameresourcepackshelper.migrator.conversion.NewFormatJsonData
import com.bluepanda22.blockgameresourcepackshelper.migrator.conversion.PredicateJsonData
import com.bluepanda22.blockgameresourcepackshelper.util.DirectoryUtil.tempDir
import com.bluepanda22.blockgameresourcepackshelper.util.ZipUtil
import java.io.File
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.nameWithoutExtension

object ResourcepackMigrator {

	// TODO - make extracting take less time -> only extract those files that are needed (-> overrides)
	//  -> That has the problem of adding the new files into the resourcepack
	//  -> Maybe when resourcepack is a zip file:
	//    -> extract needed files -> convert data -> copy original zip file to converted -> add files and version into zip
	//    -> else, honestly, just drop handling directories. It is never used anyways...
	// TODO - change minecraft resourcepack version to the correct version

	fun migrateResourcepack(resourcePackPath: Path): ResourcepackMigrateStatus {
		LOGGER.info("> Migrating resourcepack: ${resourcePackPath.nameWithoutExtension}...")
		return if (resourcePackPath.isDirectory()) {
//			migrateDirectoryResourcepack(resourcePackPath)
			ResourcepackMigrateStatus(resourcePackPath.nameWithoutExtension, MigrateStatus.FAILED)
		} else if (resourcePackPath.isRegularFile() && resourcePackPath.extension.lowercase() == "zip") {
			migrateZipResourcepack(resourcePackPath)
		} else {
			ResourcepackMigrateStatus(resourcePackPath.nameWithoutExtension, MigrateStatus.FAILED)
		}
	}

	private fun migrateDirectoryResourcepack(resourcePackPath: Path): ResourcepackMigrateStatus {
		val version = VersionHandler.getVersionFromDirectoryResourcepack(resourcePackPath)

		when (version) {
			null -> {
				LOGGER.info("> Migrating Resourcepack in BlockgameResourcepackHelper version ${VersionHandler.VERSION}: '${resourcePackPath.nameWithoutExtension}' ...")

				val resourcePackFile = resourcePackPath.toFile()
				val tempResourcepackFile = tempDir.resolve(resourcePackPath.nameWithoutExtension).toFile()
				val backupZipFile = tempDir.resolve("backup_${resourcePackPath.nameWithoutExtension}/backup.zip").toFile()
				val convertedZipFile = tempDir.resolve("converted_${resourcePackPath.nameWithoutExtension}/converted.zip").toFile()

				tempResourcepackFile.deleteRecursively() // delete potential leftover temp data

				// resourcepack dir -> copy -> temp resourcepack dir
				// resourcepack dir -> zip -> backup zip
				resourcePackFile.copyRecursively(tempResourcepackFile, overwrite = true)
				ZipUtil.zipDirectory(resourcePackFile, backupZipFile)

				// convert
				convertResourcepackInTempDirectory(resourcePackPath)

				// zip the converted files
				ZipUtil.zipDirectory(tempResourcepackFile, convertedZipFile)

				resourcePackFile.deleteRecursively() // delete original data
				ZipUtil.embedFileIntoZip(convertedZipFile, backupZipFile, "backups/backup.zip", resourcePackFile)

				tempResourcepackFile.deleteRecursively() // cleanup temp data
				backupZipFile.parentFile.deleteRecursively()
				convertedZipFile.parentFile.deleteRecursively()

				return ResourcepackMigrateStatus(resourcePackPath.nameWithoutExtension, MigrateStatus.SUCCESS)
			}

			VersionHandler.VERSION -> {
				LOGGER.info("> Skipping already migrated resourcepack: '${resourcePackPath.nameWithoutExtension}'")
				return ResourcepackMigrateStatus(resourcePackPath.nameWithoutExtension, MigrateStatus.SKIPPING)
			}

			else -> {
				LOGGER.info("> Resourcepack has been migrated in BlockgameResourcepackHelper version $version, re-migrating for BlockgameResourcepackHelper version ${VersionHandler.VERSION}: '${resourcePackPath.nameWithoutExtension}' ...")

				val resourcePackFile = resourcePackPath.toFile()
				val tempResourcepackFile = tempDir.resolve(resourcePackPath.nameWithoutExtension).toFile()
				val backupZipFile = tempDir.resolve("backup_${resourcePackPath.nameWithoutExtension}/backup.zip").toFile()
				val convertedZipFile = tempDir.resolve("converted_${resourcePackPath.nameWithoutExtension}/converted.zip").toFile()

				tempResourcepackFile.deleteRecursively() // delete potential leftover temp data

				// resourcepack dir -> backups/backup.zip -> unzip -> temp resourcepack dir
				// resourcepack dir -> backups/backup.zip -> copy -> backup zip
				val backupFile = resourcePackFile.resolve("backups/backup.zip")
				ZipUtil.unzipFile(backupFile, tempResourcepackFile)
				backupFile.copyTo(backupZipFile, overwrite = true)

				// convert
				convertResourcepackInTempDirectory(resourcePackPath)

				// zip the converted files
				ZipUtil.zipDirectory(tempResourcepackFile, convertedZipFile)

				resourcePackFile.deleteRecursively() // delete original data
				ZipUtil.embedFileIntoZip(convertedZipFile, backupZipFile, "backups/backup.zip", resourcePackFile)

				tempResourcepackFile.deleteRecursively() // cleanup temp data
				backupZipFile.parentFile.deleteRecursively()
				convertedZipFile.parentFile.deleteRecursively()

				return ResourcepackMigrateStatus(resourcePackPath.nameWithoutExtension, MigrateStatus.SUCCESS_REMIGRATED)
			}
		}
	}

	private fun migrateZipResourcepack(resourcePackPath: Path): ResourcepackMigrateStatus {
		val version = VersionHandler.getVersionFromZipResourcepack(resourcePackPath)

		when (version) {
			null -> {
				LOGGER.info("> Migrating Resourcepack in BlockgameResourcepackHelper version ${VersionHandler.VERSION}: '${resourcePackPath.nameWithoutExtension}' ...")

				val resourcePackFile = resourcePackPath.toFile()
				val tempResourcepackFile = tempDir.resolve(resourcePackPath.nameWithoutExtension).toFile()
				val backupZipFile = tempDir.resolve("backup_${resourcePackPath.nameWithoutExtension}/backup.zip").toFile()
				val convertedZipFile = tempDir.resolve("converted_${resourcePackPath.nameWithoutExtension}/converted.zip").toFile()

				tempResourcepackFile.deleteRecursively() // delete potential leftover temp data

				// resourcepack zip -> unzip -> temp resourcepack dir
				ZipUtil.unzipFile(resourcePackFile, tempResourcepackFile)
				// resourcepack zip -> copy -> backup zip
				resourcePackFile.copyTo(backupZipFile, overwrite = true)

				// convert
				convertResourcepackInTempDirectory(resourcePackPath)

				// zip the converted files
				ZipUtil.zipDirectory(tempResourcepackFile, convertedZipFile)

				resourcePackFile.deleteRecursively() // delete original data
				ZipUtil.embedFileIntoZip(convertedZipFile, backupZipFile, "backups/backup.zip", resourcePackFile)

				tempResourcepackFile.deleteRecursively() // cleanup temp data
				backupZipFile.parentFile.deleteRecursively()
				convertedZipFile.parentFile.deleteRecursively()

				return ResourcepackMigrateStatus(resourcePackPath.nameWithoutExtension, MigrateStatus.SUCCESS)
			}

			VersionHandler.VERSION -> {
				LOGGER.info("> Skipping already migrated resourcepack: '${resourcePackPath.nameWithoutExtension}'")
				return ResourcepackMigrateStatus(resourcePackPath.nameWithoutExtension, MigrateStatus.SKIPPING)
			}

			else -> {
				LOGGER.info("> Resourcepack has been migrated in BlockgameResourcepackHelper version $version, re-migrating for BlockgameResourcepackHelper version ${VersionHandler.VERSION}: '${resourcePackPath.nameWithoutExtension}' ...")

				val resourcePackFile = resourcePackPath.toFile()
				val tempResourcepackFile = tempDir.resolve(resourcePackPath.nameWithoutExtension).toFile()
				val backupZipFile = tempDir.resolve("backup_${resourcePackPath.nameWithoutExtension}/backup.zip").toFile()
				val convertedZipFile = tempDir.resolve("converted_${resourcePackPath.nameWithoutExtension}/converted.zip").toFile()

				tempResourcepackFile.deleteRecursively() // delete potential leftover temp data

				// resourcepack zip -> backups/backup.zip -> extract -> backup zip
//				ZipUtil.extractFileFromZip(resourcePackFile, "backups/backup.zip", tempDir.resolve("backup_${resourcePackPath.nameWithoutExtension}").toFile())
				ZipUtil.extractFileFromZip(resourcePackFile, "backups/backup.zip", backupZipFile)
				// backup zip -> unzip -> temp resourcepack dir
				ZipUtil.unzipFile(backupZipFile, tempResourcepackFile)

				// convert
				convertResourcepackInTempDirectory(resourcePackPath)

				// zip the converted files
				ZipUtil.zipDirectory(tempResourcepackFile, convertedZipFile)

				resourcePackFile.deleteRecursively() // delete original data
				ZipUtil.embedFileIntoZip(convertedZipFile, backupZipFile, "backups/backup.zip", resourcePackFile)

				tempResourcepackFile.deleteRecursively() // cleanup temp data
				backupZipFile.parentFile.deleteRecursively()
				convertedZipFile.parentFile.deleteRecursively()

				return ResourcepackMigrateStatus(resourcePackPath.nameWithoutExtension, MigrateStatus.SUCCESS_REMIGRATED)
			}
		}
	}

	private fun convertResourcepackInTempDirectory(resourcePackPath: Path) {
		val resourcePackFile = resourcePackPath.toFile()
		val tempResourcepackFile = tempDir.resolve(resourcePackPath.nameWithoutExtension).toFile()
		val backupZipFile = tempDir.resolve("backup_${resourcePackPath.nameWithoutExtension}/backup.zip").toFile()

		val mcAssetsDir = tempResourcepackFile.resolve("assets/minecraft")
		val overridesDir = mcAssetsDir.resolve("overrides")

		val blockgameAssetsDir = tempResourcepackFile.resolve("assets/blockgame")
		val blockgameItemsDir = blockgameAssetsDir.resolve("items")

		blockgameItemsDir.mkdirs()

		val overridesItemDir = overridesDir.resolve("item")

		overridesItemDir.listFiles()?.forEach { file: File ->
			if (file.extension.lowercase() == "json") {
				LOGGER.info("	> converting: ${file.nameWithoutExtension}...")
//				val newFileLocation = itemsDir.resolve("${file.nameWithoutExtension}.json")

				val json = PredicateJsonData.readPredicateJsonDataFromFile(file)
					?: return@forEach

				val newFormatJsonDataMap = DataConverter.convertPredicates2NewFormat(json, file.nameWithoutExtension)

				newFormatJsonDataMap.forEach { (blockgameItemId, newFormatJsonData) ->
					val newFileLocation = blockgameItemsDir.resolve("${blockgameItemId.lowercase()}.json")
					NewFormatJsonData.writeNewFormatJsonDataToFile(newFormatJsonData, newFileLocation)
				}

			}
		}

		overridesDir.deleteRecursively()

		when (resourcePackPath.nameWithoutExtension) {
			"Better-Blockgame-v0.4.2" -> {
				val dir = tempResourcepackFile.resolve("assets/minecraft/textures/item/nbt/armor")
				val newDir = tempResourcepackFile.resolve("assets/blockgame/textures/entity/equipment/humanoid")
				val newDir2 = tempResourcepackFile.resolve("assets/blockgame/textures/entity/equipment/humanoid_leggings")

				newDir.mkdirs()
				newDir2.mkdirs()

				dir.resolve("archer").let { archer ->
					newDir.resolve("archer").mkdir()
					newDir2.resolve("archer").mkdir()
					archer.resolve("archer_t1_layer_1.png").copyTo(newDir.resolve("ranger/shotcaller.png"))
					archer.resolve("archer_t1_layer_2.png").copyTo(newDir2.resolve("ranger/shotcaller.png"))
					archer.resolve("archer_t2_layer_1.png").copyTo(newDir.resolve("ranger/ranger.png"))
					archer.resolve("archer_t2_layer_2.png").copyTo(newDir2.resolve("ranger/ranger.png"))
					archer.resolve("archer_t3_layer_1.png").copyTo(newDir.resolve("ranger/sharpshooter.png"))
					archer.resolve("archer_t3_layer_2.png").copyTo(newDir2.resolve("ranger/sharpshooter.png"))
					archer.resolve("archer_t4_layer_1.png").copyTo(newDir.resolve("ranger/bloodsnout_ranger.png"))
					archer.resolve("archer_t4_layer_2.png").copyTo(newDir2.resolve("ranger/bloodsnout_ranger.png"))
//					archer.resolve("extra/archer_t4_layer_1.png").copyTo(newDir.resolve("ranger/special/bloodsnout_ranger.png"))
//					archer.resolve("extra/archer_t4_layer_2.png").copyTo(newDir2.resolve("ranger/special/bloodsnout_ranger.png"))
				}
				dir.resolve("guardian").let { guardian ->
					newDir.resolve("guardian").mkdir()
					newDir2.resolve("guardian").mkdir()
					guardian.resolve("guardian_t1_layer_1.png").copyTo(newDir.resolve("guardian/wooden.png"))
					guardian.resolve("guardian_t1_layer_2.png").copyTo(newDir2.resolve("guardian/wooden.png"))
					guardian.resolve("guardian_t2_layer_1.png").copyTo(newDir.resolve("guardian/copper.png"))
					guardian.resolve("guardian_t2_layer_2.png").copyTo(newDir2.resolve("guardian/copper.png"))
					guardian.resolve("guardian_t3_layer_1.png").copyTo(newDir.resolve("guardian/steel.png"))
					guardian.resolve("guardian_t3_layer_2.png").copyTo(newDir2.resolve("guardian/steel.png"))
					guardian.resolve("guardian_t4_layer_1.png").copyTo(newDir.resolve("guardian/bloodsnout_brute.png"))
					guardian.resolve("guardian_t4_layer_2.png").copyTo(newDir2.resolve("guardian/bloodsnout_brute.png"))
					guardian.resolve("guardian_t5_layer_1.png").copyTo(newDir.resolve("guardian/maahes.png"))
					guardian.resolve("guardian_t5_layer_2.png").copyTo(newDir2.resolve("guardian/maahes.png"))
				}
				dir.resolve("warrior").let { warrior ->
					newDir.resolve("warrior").mkdir()
					newDir2.resolve("warrior").mkdir()
					warrior.resolve("warrior_t1_layer_1.png").copyTo(newDir.resolve("warrior/cactus.png"))
					warrior.resolve("warrior_t1_layer_2.png").copyTo(newDir2.resolve("warrior/cactus.png"))
					warrior.resolve("warrior_t2_layer_1.png").copyTo(newDir.resolve("warrior/stone.png"))
					warrior.resolve("warrior_t2_layer_2.png").copyTo(newDir2.resolve("warrior/stone.png"))
					warrior.resolve("warrior_t3_layer_1.png").copyTo(newDir.resolve("warrior/mythril.png"))
					warrior.resolve("warrior_t3_layer_2.png").copyTo(newDir2.resolve("warrior/mythril.png"))
					warrior.resolve("warrior_t4_layer_1.png").copyTo(newDir.resolve("warrior/bloodsnout_slayer.png"))
					warrior.resolve("warrior_t4_layer_2.png").copyTo(newDir2.resolve("warrior/bloodsnout_slayer.png"))
				}
				dir.resolve("magic").let { magic ->
					newDir.resolve("magic").mkdir()
					newDir2.resolve("magic").mkdir()
					magic.resolve("magic_t1_layer_1.png").copyTo(newDir.resolve("wizard/silk.png"))
					magic.resolve("magic_t1_layer_2.png").copyTo(newDir2.resolve("wizard/silk.png"))
					magic.resolve("magic_t2_layer_1.png").copyTo(newDir.resolve("wizard/sparking_silk.png"))
					magic.resolve("magic_t2_layer_2.png").copyTo(newDir2.resolve("wizard/sparking_silk.png"))
					magic.resolve("magic_t3_layer_1.png").copyTo(newDir.resolve("wizard/acolyte.png"))
					magic.resolve("magic_t3_layer_2.png").copyTo(newDir2.resolve("wizard/acolyte.png"))
					magic.resolve("magic_t4_layer_1.png").copyTo(newDir.resolve("wizard/bloodsnout_magi.png"))
					magic.resolve("magic_t4_layer_2.png").copyTo(newDir2.resolve("wizard/bloodsnout_magi.png"))
//					magic.resolve("extra/magic_t4_layer_1.png").copyTo(newDir.resolve("wizard/special/bloodsnout_magi.png"))
//					magic.resolve("extra/magic_t4_layer_2.png").copyTo(newDir2.resolve("wizard/special/bloodsnout_magi.png"))
				}
				dir.resolve("thaum").let { thaum ->
					newDir.resolve("thaum").mkdir()
					newDir2.resolve("thaum").mkdir()
					thaum.resolve("thaum_t1_layer_1.png").copyTo(newDir.resolve("thaumaturge/wool.png"))
					thaum.resolve("thaum_t1_layer_2.png").copyTo(newDir2.resolve("thaumaturge/wool.png"))
					thaum.resolve("thaum_t2_layer_1.png").copyTo(newDir.resolve("thaumaturge/blessed_wool.png"))
					thaum.resolve("thaum_t2_layer_2.png").copyTo(newDir2.resolve("thaumaturge/blessed_wool.png"))
					thaum.resolve("thaum_t3_layer_1.png").copyTo(newDir.resolve("thaumaturge/sanctified.png"))
					thaum.resolve("thaum_t3_layer_2.png").copyTo(newDir2.resolve("thaumaturge/sanctified.png"))
					thaum.resolve("thaum_t4_layer_1.png").copyTo(newDir.resolve("thaumaturge/bloodsnout_healer.png"))
					thaum.resolve("thaum_t4_layer_2.png").copyTo(newDir2.resolve("thaumaturge/bloodsnout_healer.png"))
				}
				dir.resolve("onion").let { onion ->
					newDir.resolve("onion").mkdir()
					newDir2.resolve("onion").mkdir()
					onion.resolve("onion_layer_1.png").copyTo(newDir.resolve("onion/onion.png"))
					onion.resolve("onion_layer_2.png").copyTo(newDir2.resolve("onion/onion.png"))
				}

				dir.resolve("professions").let { professions ->
					newDir.resolve("professions").mkdir()
					newDir2.resolve("professions").mkdir()

					let {
						professions.resolve("archaeologist_armor_layer_1.png").copyTo(newDir.resolve("archaeologist/t1.png"))
						professions.resolve("archaeologist_armor_layer_2.png").copyTo(newDir2.resolve("archaeologist/t1.png"))
						professions.resolve("archaeologist_armor_layer_1.png").copyTo(newDir.resolve("archaeologist/t2.png"))
						professions.resolve("archaeologist_armor_layer_2.png").copyTo(newDir2.resolve("archaeologist/t2.png"))
						professions.resolve("archaeologist_armor_layer_1.png").copyTo(newDir.resolve("archaeologist/t3.png"))
						professions.resolve("archaeologist_armor_layer_2.png").copyTo(newDir2.resolve("archaeologist/t3.png"))
						professions.resolve("archaeologist_armor_layer_1.png").copyTo(newDir.resolve("archaeologist/t4.png"))
						professions.resolve("archaeologist_armor_layer_2.png").copyTo(newDir2.resolve("archaeologist/t4.png"))
						professions.resolve("archaeologist_armor_layer_1.png").copyTo(newDir.resolve("archaeologist/t5.png"))
						professions.resolve("archaeologist_armor_layer_2.png").copyTo(newDir2.resolve("archaeologist/t5.png"))
					}
					let {
						val botanist = professions.resolve("botanist")
						botanist.mkdir()
						botanist.resolve("t1").resolve("botanist_t1_layer_1.png").copyTo(newDir.resolve("botanist/t1.png"))
						botanist.resolve("t1").resolve("botanist_t1_layer_2.png").copyTo(newDir2.resolve("botanist/t1.png"))
						botanist.resolve("t2").resolve("botanist_t2_layer_1.png").copyTo(newDir.resolve("botanist/t2.png"))
						botanist.resolve("t2").resolve("botanist_t2_layer_2.png").copyTo(newDir2.resolve("botanist/t2.png"))
						botanist.resolve("t3").resolve("botanist_t3_layer_1.png").copyTo(newDir.resolve("botanist/t3.png"))
						botanist.resolve("t3").resolve("botanist_t3_layer_2.png").copyTo(newDir2.resolve("botanist/t3.png"))
						botanist.resolve("t4").resolve("botanist_t4_layer_1.png").copyTo(newDir.resolve("botanist/t4.png"))
						botanist.resolve("t4").resolve("botanist_t4_layer_2.png").copyTo(newDir2.resolve("botanist/t4.png"))
						botanist.resolve("t5").resolve("botanist_t5_layer_1.png").copyTo(newDir.resolve("botanist/t5.png"))
						botanist.resolve("t5").resolve("botanist_t5_layer_2.png").copyTo(newDir2.resolve("botanist/t5.png"))
					}
					let {
						professions.resolve("fisherman_armor_layer_1.png").copyTo(newDir.resolve("fisherman/t1.png"))
						professions.resolve("fisherman_armor_layer_2.png").copyTo(newDir2.resolve("fisherman/t1.png"))
						professions.resolve("fisherman_armor_layer_1.png").copyTo(newDir.resolve("fisherman/t2.png"))
						professions.resolve("fisherman_armor_layer_2.png").copyTo(newDir2.resolve("fisherman/t2.png"))
						professions.resolve("fisherman_armor_layer_1.png").copyTo(newDir.resolve("fisherman/t3.png"))
						professions.resolve("fisherman_armor_layer_2.png").copyTo(newDir2.resolve("fisherman/t3.png"))
						professions.resolve("fisherman_armor_layer_1.png").copyTo(newDir.resolve("fisherman/t4.png"))
						professions.resolve("fisherman_armor_layer_2.png").copyTo(newDir2.resolve("fisherman/t4.png"))
						professions.resolve("fisherman_armor_layer_1.png").copyTo(newDir.resolve("fisherman/t5.png"))
						professions.resolve("fisherman_armor_layer_2.png").copyTo(newDir2.resolve("fisherman/t5.png"))
					}
					let {
						professions.resolve("lumberjack_armor_layer_1.png").copyTo(newDir.resolve("lumberjack/t1.png"))
						professions.resolve("lumberjack_armor_layer_2.png").copyTo(newDir2.resolve("lumberjack/t1.png"))
						professions.resolve("lumberjack_armor_layer_1.png").copyTo(newDir.resolve("lumberjack/t2.png"))
						professions.resolve("lumberjack_armor_layer_2.png").copyTo(newDir2.resolve("lumberjack/t2.png"))
						professions.resolve("lumberjack_armor_layer_1.png").copyTo(newDir.resolve("lumberjack/t3.png"))
						professions.resolve("lumberjack_armor_layer_2.png").copyTo(newDir2.resolve("lumberjack/t3.png"))
						professions.resolve("lumberjack_armor_layer_1.png").copyTo(newDir.resolve("lumberjack/t4.png"))
						professions.resolve("lumberjack_armor_layer_2.png").copyTo(newDir2.resolve("lumberjack/t4.png"))
						professions.resolve("lumberjack_armor_layer_1.png").copyTo(newDir.resolve("lumberjack/t5.png"))
						professions.resolve("lumberjack_armor_layer_2.png").copyTo(newDir2.resolve("lumberjack/t5.png"))
					}
					let {
						val miner = professions.resolve("miner")
						miner.mkdir()
						miner.resolve("t1").resolve("miner_t1_layer_1.png").copyTo(newDir.resolve("miner/t1.png"))
						miner.resolve("t1").resolve("miner_t1_layer_2.png").copyTo(newDir2.resolve("miner/t1.png"))
						miner.resolve("t2").resolve("miner_t2_layer_1.png").copyTo(newDir.resolve("miner/t2.png"))
						miner.resolve("t2").resolve("miner_t2_layer_2.png").copyTo(newDir2.resolve("miner/t2.png"))
						miner.resolve("t3").resolve("miner_t3_layer_1.png").copyTo(newDir.resolve("miner/t3.png"))
						miner.resolve("t3").resolve("miner_t3_layer_2.png").copyTo(newDir2.resolve("miner/t3.png"))
						miner.resolve("t4").resolve("miner_t4_layer_1.png").copyTo(newDir.resolve("miner/t4.png"))
						miner.resolve("t4").resolve("miner_t4_layer_2.png").copyTo(newDir2.resolve("miner/t4.png"))
						miner.resolve("t5").resolve("miner_t5_layer_1.png").copyTo(newDir.resolve("miner/t5.png"))
						miner.resolve("t5").resolve("miner_t5_layer_2.png").copyTo(newDir2.resolve("miner/t5.png"))
					}

				}

				dir.deleteRecursively()
			}
			"MineBlockCraftGame-Alpha2" -> {
				val dir = tempResourcepackFile.resolve("assets/blockgame/textures/armor")
				val newDir = tempResourcepackFile.resolve("assets/blockgame/textures/entity/equipment/humanoid")
				val newDir2 = tempResourcepackFile.resolve("assets/blockgame/textures/entity/equipment/humanoid_leggings")

				newDir.mkdirs()
				newDir2.mkdirs()

				let {
					dir.resolve("wooden_armor_layer_1.png").copyTo(newDir.resolve("guardian/wooden.png"))
					dir.resolve("wooden_armor_layer_2.png").copyTo(newDir2.resolve("guardian/wooden.png"))
					dir.resolve("copper_armor_layer_1.png").copyTo(newDir.resolve("guardian/copper.png"))
					dir.resolve("copper_armor_layer_2.png").copyTo(newDir2.resolve("guardian/copper.png"))
					dir.resolve("steel_armor_layer_1.png").copyTo(newDir.resolve("guardian/steel.png"))
					dir.resolve("steel_armor_layer_2.png").copyTo(newDir2.resolve("guardian/steel.png"))

					dir.resolve("wooden_armor_layer_1_special.png").copyTo(newDir.resolve("guardian/special/wooden.png"))
					dir.resolve("wooden_armor_layer_2_special.png").copyTo(newDir2.resolve("guardian/special/wooden.png"))
					dir.resolve("copper_armor_layer_1_special.png").copyTo(newDir.resolve("guardian/special/copper.png"))
					dir.resolve("copper_armor_layer_2_special.png").copyTo(newDir2.resolve("guardian/special/copper.png"))
					dir.resolve("steel_armor_layer_1_special.png").copyTo(newDir.resolve("guardian/special/steel.png"))
					dir.resolve("steel_armor_layer_2_special.png").copyTo(newDir2.resolve("guardian/special/steel.png"))
				}
				let {
					dir.resolve("stone_armor_layer_1.png").copyTo(newDir.resolve("warrior/stone.png"))
					dir.resolve("stone_armor_layer_2.png").copyTo(newDir2.resolve("warrior/stone.png"))
					dir.resolve("mythril_armor_layer_1.png").copyTo(newDir.resolve("warrior/mythril.png"))
					dir.resolve("mythril_armor_layer_2.png").copyTo(newDir2.resolve("warrior/mythril.png"))

					dir.resolve("stone_armor_layer_1_special.png").copyTo(newDir.resolve("warrior/special/stone.png"))
					dir.resolve("stone_armor_layer_2_special.png").copyTo(newDir2.resolve("warrior/special/stone.png"))
					dir.resolve("mythril_armor_blinding_layer_1.png").copyTo(newDir.resolve("warrior/special/mythril.png"))
					dir.resolve("mythril_armor_blinding_layer_2.png").copyTo(newDir2.resolve("warrior/special/mythril.png"))
				}
				let {
					dir.resolve("onion_armor_layer_1.png").copyTo(newDir.resolve("onion/onion.png"))
					dir.resolve("onion_armor_layer_2.png").copyTo(newDir2.resolve("onion/onion.png"))
				}

				let {
					let {
						dir.resolve("archaeologist_armor_layer_1.png").copyTo(newDir.resolve("archaeologist/t1_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_2.png").copyTo(newDir2.resolve("archaeologist/t1_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_1.png").copyTo(newDir.resolve("archaeologist/t2_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_2.png").copyTo(newDir2.resolve("archaeologist/t2_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_1.png").copyTo(newDir.resolve("archaeologist/t3_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_2.png").copyTo(newDir2.resolve("archaeologist/t3_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_1.png").copyTo(newDir.resolve("archaeologist/t4_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_2.png").copyTo(newDir2.resolve("archaeologist/t4_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_1.png").copyTo(newDir.resolve("archaeologist/t5_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_2.png").copyTo(newDir2.resolve("archaeologist/t5_dyeable.png"))

						dir.resolve("archaeologist_armor_layer_1.png").copyTo(newDir.resolve("archaeologist/special/t1_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_2.png").copyTo(newDir2.resolve("archaeologist/special/t1_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_1.png").copyTo(newDir.resolve("archaeologist/special/t2_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_2.png").copyTo(newDir2.resolve("archaeologist/special/t2_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_1.png").copyTo(newDir.resolve("archaeologist/special/t3_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_2.png").copyTo(newDir2.resolve("archaeologist/special/t3_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_1.png").copyTo(newDir.resolve("archaeologist/special/t4_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_2.png").copyTo(newDir2.resolve("archaeologist/special/t4_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_1.png").copyTo(newDir.resolve("archaeologist/special/t5_dyeable.png"))
						dir.resolve("archaeologist_armor_layer_2.png").copyTo(newDir2.resolve("archaeologist/special/t5_dyeable.png"))

						dir.resolve("archaeologist_armor_layer_1_overlay.png").copyTo(newDir.resolve("archaeologist/t1.png"))
						dir.resolve("archaeologist_armor_layer_2_overlay.png").copyTo(newDir2.resolve("archaeologist/t1.png"))
						dir.resolve("archaeologist_armor_layer_1_overlay.png").copyTo(newDir.resolve("archaeologist/t2.png"))
						dir.resolve("archaeologist_armor_layer_2_overlay.png").copyTo(newDir2.resolve("archaeologist/t2.png"))
						dir.resolve("archaeologist_armor_layer_1_overlay.png").copyTo(newDir.resolve("archaeologist/t3.png"))
						dir.resolve("archaeologist_armor_layer_2_overlay.png").copyTo(newDir2.resolve("archaeologist/t3.png"))
						dir.resolve("archaeologist_armor_layer_1_overlay.png").copyTo(newDir.resolve("archaeologist/t4.png"))
						dir.resolve("archaeologist_armor_layer_2_overlay.png").copyTo(newDir2.resolve("archaeologist/t4.png"))
						dir.resolve("archaeologist_armor_layer_1_overlay.png").copyTo(newDir.resolve("archaeologist/t5.png"))
						dir.resolve("archaeologist_armor_layer_2_overlay.png").copyTo(newDir2.resolve("archaeologist/t5.png"))

						dir.resolve("archaeologist_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("archaeologist/special/t1.png"))
						dir.resolve("archaeologist_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("archaeologist/special/t1.png"))
						dir.resolve("archaeologist_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("archaeologist/special/t2.png"))
						dir.resolve("archaeologist_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("archaeologist/special/t2.png"))
						dir.resolve("archaeologist_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("archaeologist/special/t3.png"))
						dir.resolve("archaeologist_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("archaeologist/special/t3.png"))
						dir.resolve("archaeologist_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("archaeologist/special/t4.png"))
						dir.resolve("archaeologist_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("archaeologist/special/t4.png"))
						dir.resolve("archaeologist_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("archaeologist/special/t5.png"))
						dir.resolve("archaeologist_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("archaeologist/special/t5.png"))
					}
					let {
						dir.resolve("botanist_armor_layer_1.png").copyTo(newDir.resolve("botanist/t1_dyeable.png"))
						dir.resolve("botanist_armor_layer_2.png").copyTo(newDir2.resolve("botanist/t1_dyeable.png"))
						dir.resolve("botanist_armor_layer_1.png").copyTo(newDir.resolve("botanist/t2_dyeable.png"))
						dir.resolve("botanist_armor_layer_2.png").copyTo(newDir2.resolve("botanist/t2_dyeable.png"))
						dir.resolve("botanist_armor_layer_1.png").copyTo(newDir.resolve("botanist/t3_dyeable.png"))
						dir.resolve("botanist_armor_layer_2.png").copyTo(newDir2.resolve("botanist/t3_dyeable.png"))
						dir.resolve("botanist_armor_layer_1.png").copyTo(newDir.resolve("botanist/t4_dyeable.png"))
						dir.resolve("botanist_armor_layer_2.png").copyTo(newDir2.resolve("botanist/t4_dyeable.png"))
						dir.resolve("botanist_armor_layer_1.png").copyTo(newDir.resolve("botanist/t5_dyeable.png"))
						dir.resolve("botanist_armor_layer_2.png").copyTo(newDir2.resolve("botanist/t5_dyeable.png"))

						dir.resolve("botanist_armor_layer_1.png").copyTo(newDir.resolve("botanist/special/t1_dyeable.png"))
						dir.resolve("botanist_armor_layer_2.png").copyTo(newDir2.resolve("botanist/special/t1_dyeable.png"))
						dir.resolve("botanist_armor_layer_1.png").copyTo(newDir.resolve("botanist/special/t2_dyeable.png"))
						dir.resolve("botanist_armor_layer_2.png").copyTo(newDir2.resolve("botanist/special/t2_dyeable.png"))
						dir.resolve("botanist_armor_layer_1.png").copyTo(newDir.resolve("botanist/special/t3_dyeable.png"))
						dir.resolve("botanist_armor_layer_2.png").copyTo(newDir2.resolve("botanist/special/t3_dyeable.png"))
						dir.resolve("botanist_armor_layer_1.png").copyTo(newDir.resolve("botanist/special/t4_dyeable.png"))
						dir.resolve("botanist_armor_layer_2.png").copyTo(newDir2.resolve("botanist/special/t4_dyeable.png"))
						dir.resolve("botanist_armor_layer_1.png").copyTo(newDir.resolve("botanist/special/t5_dyeable.png"))
						dir.resolve("botanist_armor_layer_2.png").copyTo(newDir2.resolve("botanist/special/t5_dyeable.png"))

						dir.resolve("botanist_armor_layer_1_overlay.png").copyTo(newDir.resolve("botanist/t1.png"))
						dir.resolve("botanist_armor_layer_2_overlay.png").copyTo(newDir2.resolve("botanist/t1.png"))
						dir.resolve("botanist_armor_layer_1_overlay.png").copyTo(newDir.resolve("botanist/t2.png"))
						dir.resolve("botanist_armor_layer_2_overlay.png").copyTo(newDir2.resolve("botanist/t2.png"))
						dir.resolve("botanist_armor_layer_1_overlay.png").copyTo(newDir.resolve("botanist/t3.png"))
						dir.resolve("botanist_armor_layer_2_overlay.png").copyTo(newDir2.resolve("botanist/t3.png"))
						dir.resolve("botanist_armor_layer_1_overlay.png").copyTo(newDir.resolve("botanist/t4.png"))
						dir.resolve("botanist_armor_layer_2_overlay.png").copyTo(newDir2.resolve("botanist/t4.png"))
						dir.resolve("botanist_armor_layer_1_overlay.png").copyTo(newDir.resolve("botanist/t5.png"))
						dir.resolve("botanist_armor_layer_2_overlay.png").copyTo(newDir2.resolve("botanist/t5.png"))

						dir.resolve("botanist_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("botanist/special/t1.png"))
						dir.resolve("botanist_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("botanist/special/t1.png"))
						dir.resolve("botanist_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("botanist/special/t2.png"))
						dir.resolve("botanist_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("botanist/special/t2.png"))
						dir.resolve("botanist_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("botanist/special/t3.png"))
						dir.resolve("botanist_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("botanist/special/t3.png"))
						dir.resolve("botanist_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("botanist/special/t4.png"))
						dir.resolve("botanist_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("botanist/special/t4.png"))
						dir.resolve("botanist_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("botanist/special/t5.png"))
						dir.resolve("botanist_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("botanist/special/t5.png"))
					}
					let {
						dir.resolve("fisherman_armor_layer_1.png").copyTo(newDir.resolve("fisherman/t1_dyeable.png"))
						dir.resolve("fisherman_armor_layer_2.png").copyTo(newDir2.resolve("fisherman/t1_dyeable.png"))
						dir.resolve("fisherman_armor_layer_1.png").copyTo(newDir.resolve("fisherman/t2_dyeable.png"))
						dir.resolve("fisherman_armor_layer_2.png").copyTo(newDir2.resolve("fisherman/t2_dyeable.png"))
						dir.resolve("fisherman_armor_layer_1.png").copyTo(newDir.resolve("fisherman/t3_dyeable.png"))
						dir.resolve("fisherman_armor_layer_2.png").copyTo(newDir2.resolve("fisherman/t3_dyeable.png"))
						dir.resolve("fisherman_armor_layer_1.png").copyTo(newDir.resolve("fisherman/t4_dyeable.png"))
						dir.resolve("fisherman_armor_layer_2.png").copyTo(newDir2.resolve("fisherman/t4_dyeable.png"))
						dir.resolve("fisherman_armor_layer_1.png").copyTo(newDir.resolve("fisherman/t5_dyeable.png"))
						dir.resolve("fisherman_armor_layer_2.png").copyTo(newDir2.resolve("fisherman/t5_dyeable.png"))

						dir.resolve("fisherman_armor_layer_1.png").copyTo(newDir.resolve("fisherman/special/t1_dyeable.png"))
						dir.resolve("fisherman_armor_layer_2.png").copyTo(newDir2.resolve("fisherman/special/t1_dyeable.png"))
						dir.resolve("fisherman_armor_layer_1.png").copyTo(newDir.resolve("fisherman/special/t2_dyeable.png"))
						dir.resolve("fisherman_armor_layer_2.png").copyTo(newDir2.resolve("fisherman/special/t2_dyeable.png"))
						dir.resolve("fisherman_armor_layer_1.png").copyTo(newDir.resolve("fisherman/special/t3_dyeable.png"))
						dir.resolve("fisherman_armor_layer_2.png").copyTo(newDir2.resolve("fisherman/special/t3_dyeable.png"))
						dir.resolve("fisherman_armor_layer_1.png").copyTo(newDir.resolve("fisherman/special/t4_dyeable.png"))
						dir.resolve("fisherman_armor_layer_2.png").copyTo(newDir2.resolve("fisherman/special/t4_dyeable.png"))
						dir.resolve("fisherman_armor_layer_1.png").copyTo(newDir.resolve("fisherman/special/t5_dyeable.png"))
						dir.resolve("fisherman_armor_layer_2.png").copyTo(newDir2.resolve("fisherman/special/t5_dyeable.png"))

						dir.resolve("fisherman_armor_layer_1_overlay.png").copyTo(newDir.resolve("fisherman/t1.png"))
						dir.resolve("fisherman_armor_layer_2_overlay.png").copyTo(newDir2.resolve("fisherman/t1.png"))
						dir.resolve("fisherman_armor_layer_1_overlay.png").copyTo(newDir.resolve("fisherman/t2.png"))
						dir.resolve("fisherman_armor_layer_2_overlay.png").copyTo(newDir2.resolve("fisherman/t2.png"))
						dir.resolve("fisherman_armor_layer_1_overlay.png").copyTo(newDir.resolve("fisherman/t3.png"))
						dir.resolve("fisherman_armor_layer_2_overlay.png").copyTo(newDir2.resolve("fisherman/t3.png"))
						dir.resolve("fisherman_armor_layer_1_overlay.png").copyTo(newDir.resolve("fisherman/t4.png"))
						dir.resolve("fisherman_armor_layer_2_overlay.png").copyTo(newDir2.resolve("fisherman/t4.png"))
						dir.resolve("fisherman_armor_layer_1_overlay.png").copyTo(newDir.resolve("fisherman/t5.png"))
						dir.resolve("fisherman_armor_layer_2_overlay.png").copyTo(newDir2.resolve("fisherman/t5.png"))

						dir.resolve("fisherman_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("fisherman/special/t1.png"))
						dir.resolve("fisherman_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("fisherman/special/t1.png"))
						dir.resolve("fisherman_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("fisherman/special/t2.png"))
						dir.resolve("fisherman_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("fisherman/special/t2.png"))
						dir.resolve("fisherman_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("fisherman/special/t3.png"))
						dir.resolve("fisherman_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("fisherman/special/t3.png"))
						dir.resolve("fisherman_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("fisherman/special/t4.png"))
						dir.resolve("fisherman_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("fisherman/special/t4.png"))
						dir.resolve("fisherman_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("fisherman/special/t5.png"))
						dir.resolve("fisherman_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("fisherman/special/t5.png"))
					}
					let {
						dir.resolve("lumberjack_armor_layer_1.png").copyTo(newDir.resolve("lumberjack/t1_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_2.png").copyTo(newDir2.resolve("lumberjack/t1_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_1.png").copyTo(newDir.resolve("lumberjack/t2_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_2.png").copyTo(newDir2.resolve("lumberjack/t2_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_1.png").copyTo(newDir.resolve("lumberjack/t3_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_2.png").copyTo(newDir2.resolve("lumberjack/t3_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_1.png").copyTo(newDir.resolve("lumberjack/t4_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_2.png").copyTo(newDir2.resolve("lumberjack/t4_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_1.png").copyTo(newDir.resolve("lumberjack/t5_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_2.png").copyTo(newDir2.resolve("lumberjack/t5_dyeable.png"))

						dir.resolve("lumberjack_armor_layer_1.png").copyTo(newDir.resolve("lumberjack/special/t1_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_2.png").copyTo(newDir2.resolve("lumberjack/special/t1_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_1.png").copyTo(newDir.resolve("lumberjack/special/t2_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_2.png").copyTo(newDir2.resolve("lumberjack/special/t2_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_1.png").copyTo(newDir.resolve("lumberjack/special/t3_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_2.png").copyTo(newDir2.resolve("lumberjack/special/t3_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_1.png").copyTo(newDir.resolve("lumberjack/special/t4_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_2.png").copyTo(newDir2.resolve("lumberjack/special/t4_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_1.png").copyTo(newDir.resolve("lumberjack/special/t5_dyeable.png"))
						dir.resolve("lumberjack_armor_layer_2.png").copyTo(newDir2.resolve("lumberjack/special/t5_dyeable.png"))

						dir.resolve("lumberjack_armor_layer_1_overlay.png").copyTo(newDir.resolve("lumberjack/t1.png"))
						dir.resolve("lumberjack_armor_layer_2_overlay.png").copyTo(newDir2.resolve("lumberjack/t1.png"))
						dir.resolve("lumberjack_armor_layer_1_overlay.png").copyTo(newDir.resolve("lumberjack/t2.png"))
						dir.resolve("lumberjack_armor_layer_2_overlay.png").copyTo(newDir2.resolve("lumberjack/t2.png"))
						dir.resolve("lumberjack_armor_layer_1_overlay.png").copyTo(newDir.resolve("lumberjack/t3.png"))
						dir.resolve("lumberjack_armor_layer_2_overlay.png").copyTo(newDir2.resolve("lumberjack/t3.png"))
						dir.resolve("lumberjack_armor_layer_1_overlay.png").copyTo(newDir.resolve("lumberjack/t4.png"))
						dir.resolve("lumberjack_armor_layer_2_overlay.png").copyTo(newDir2.resolve("lumberjack/t4.png"))
						dir.resolve("lumberjack_armor_layer_1_overlay.png").copyTo(newDir.resolve("lumberjack/t5.png"))
						dir.resolve("lumberjack_armor_layer_2_overlay.png").copyTo(newDir2.resolve("lumberjack/t5.png"))

						dir.resolve("lumberjack_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("lumberjack/special/t1.png"))
						dir.resolve("lumberjack_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("lumberjack/special/t1.png"))
						dir.resolve("lumberjack_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("lumberjack/special/t2.png"))
						dir.resolve("lumberjack_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("lumberjack/special/t2.png"))
						dir.resolve("lumberjack_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("lumberjack/special/t3.png"))
						dir.resolve("lumberjack_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("lumberjack/special/t3.png"))
						dir.resolve("lumberjack_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("lumberjack/special/t4.png"))
						dir.resolve("lumberjack_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("lumberjack/special/t4.png"))
						dir.resolve("lumberjack_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("lumberjack/special/t5.png"))
						dir.resolve("lumberjack_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("lumberjack/special/t5.png"))
					}
					let {
						dir.resolve("miner_armor_layer_1.png").copyTo(newDir.resolve("miner/t1_dyeable.png"))
						dir.resolve("miner_armor_layer_2.png").copyTo(newDir2.resolve("miner/t1_dyeable.png"))
						dir.resolve("miner_armor_layer_1.png").copyTo(newDir.resolve("miner/t2_dyeable.png"))
						dir.resolve("miner_armor_layer_2.png").copyTo(newDir2.resolve("miner/t2_dyeable.png"))
						dir.resolve("miner_armor_layer_1.png").copyTo(newDir.resolve("miner/t3_dyeable.png"))
						dir.resolve("miner_armor_layer_2.png").copyTo(newDir2.resolve("miner/t3_dyeable.png"))
						dir.resolve("miner_armor_layer_1.png").copyTo(newDir.resolve("miner/t4_dyeable.png"))
						dir.resolve("miner_armor_layer_2.png").copyTo(newDir2.resolve("miner/t4_dyeable.png"))
						dir.resolve("miner_armor_layer_1.png").copyTo(newDir.resolve("miner/t5_dyeable.png"))
						dir.resolve("miner_armor_layer_2.png").copyTo(newDir2.resolve("miner/t5_dyeable.png"))

						dir.resolve("miner_armor_layer_1.png").copyTo(newDir.resolve("miner/special/t1_dyeable.png"))
						dir.resolve("miner_armor_layer_2.png").copyTo(newDir2.resolve("miner/special/t1_dyeable.png"))
						dir.resolve("miner_armor_layer_1.png").copyTo(newDir.resolve("miner/special/t2_dyeable.png"))
						dir.resolve("miner_armor_layer_2.png").copyTo(newDir2.resolve("miner/special/t2_dyeable.png"))
						dir.resolve("miner_armor_layer_1.png").copyTo(newDir.resolve("miner/special/t3_dyeable.png"))
						dir.resolve("miner_armor_layer_2.png").copyTo(newDir2.resolve("miner/special/t3_dyeable.png"))
						dir.resolve("miner_armor_layer_1.png").copyTo(newDir.resolve("miner/special/t4_dyeable.png"))
						dir.resolve("miner_armor_layer_2.png").copyTo(newDir2.resolve("miner/special/t4_dyeable.png"))
						dir.resolve("miner_armor_layer_1.png").copyTo(newDir.resolve("miner/special/t5_dyeable.png"))
						dir.resolve("miner_armor_layer_2.png").copyTo(newDir2.resolve("miner/special/t5_dyeable.png"))

						dir.resolve("miner_armor_layer_1_overlay.png").copyTo(newDir.resolve("miner/t1.png"))
						dir.resolve("miner_armor_layer_2_overlay.png").copyTo(newDir2.resolve("miner/t1.png"))
						dir.resolve("miner_armor_layer_1_overlay.png").copyTo(newDir.resolve("miner/t2.png"))
						dir.resolve("miner_armor_layer_2_overlay.png").copyTo(newDir2.resolve("miner/t2.png"))
						dir.resolve("miner_armor_layer_1_overlay.png").copyTo(newDir.resolve("miner/t3.png"))
						dir.resolve("miner_armor_layer_2_overlay.png").copyTo(newDir2.resolve("miner/t3.png"))
						dir.resolve("miner_armor_layer_1_overlay.png").copyTo(newDir.resolve("miner/t4.png"))
						dir.resolve("miner_armor_layer_2_overlay.png").copyTo(newDir2.resolve("miner/t4.png"))
						dir.resolve("miner_armor_layer_1_overlay.png").copyTo(newDir.resolve("miner/t5.png"))
						dir.resolve("miner_armor_layer_2_overlay.png").copyTo(newDir2.resolve("miner/t5.png"))

						dir.resolve("miner_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("miner/special/t1.png"))
						dir.resolve("miner_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("miner/special/t1.png"))
						dir.resolve("miner_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("miner/special/t2.png"))
						dir.resolve("miner_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("miner/special/t2.png"))
						dir.resolve("miner_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("miner/special/t3.png"))
						dir.resolve("miner_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("miner/special/t3.png"))
						dir.resolve("miner_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("miner/special/t4.png"))
						dir.resolve("miner_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("miner/special/t4.png"))
						dir.resolve("miner_armor_layer_1_overlay_special.png").copyTo(newDir.resolve("miner/special/t5.png"))
						dir.resolve("miner_armor_layer_2_overlay_special.png").copyTo(newDir2.resolve("miner/special/t5.png"))
					}

				}

				dir.deleteRecursively()
			}
			"UnOfficial BlockGame Textures - 1.2.1" -> {
				let {
					tempResourcepackFile.resolve("assets/minecraft/models/item/nbt/materials/turbo_encabulator.json").delete()
				}

				val dir = tempResourcepackFile.resolve("assets/minecraft/textures/item/nbt/armor")
				val newDir = tempResourcepackFile.resolve("assets/blockgame/textures/entity/equipment/humanoid")
				val newDir2 = tempResourcepackFile.resolve("assets/blockgame/textures/entity/equipment/humanoid_leggings")

				newDir.mkdirs()
				newDir2.mkdirs()

//				let {
//					dir.resolve("archer_t1_layer_1.png").copyTo(newDir.resolve("ranger/shotcaller.png"))
//					dir.resolve("archer_t1_layer_2.png").copyTo(newDir2.resolve("ranger/shotcaller.png"))
//					dir.resolve("archer_t2_layer_1.png").copyTo(newDir.resolve("ranger/ranger.png"))
//					dir.resolve("archer_t2_layer_2.png").copyTo(newDir2.resolve("ranger/ranger.png"))
//					dir.resolve("archer_t3_layer_1.png").copyTo(newDir.resolve("ranger/sharpshooter.png"))
//					dir.resolve("archer_t3_layer_2.png").copyTo(newDir2.resolve("ranger/sharpshooter.png"))
//					dir.resolve("archer_t4_layer_1.png").copyTo(newDir.resolve("ranger/bloodsnout_ranger.png"))
//					dir.resolve("archer_t4_layer_2.png").copyTo(newDir2.resolve("ranger/bloodsnout_ranger.png"))
//				}

				let {
					dir.resolve("wooden_armor_layer_1.png").copyTo(newDir.resolve("guardian/wooden.png"))
					dir.resolve("wooden_armor_layer_2.png").copyTo(newDir2.resolve("guardian/wooden.png"))
					dir.resolve("copper_armor_layer_1.png").copyTo(newDir.resolve("guardian/copper.png"))
					dir.resolve("copper_armor_layer_2.png").copyTo(newDir2.resolve("guardian/copper.png"))
					dir.resolve("steel_armor_layer_1.png").copyTo(newDir.resolve("guardian/steel.png"))
					dir.resolve("steel_armor_layer_2.png").copyTo(newDir2.resolve("guardian/steel.png"))
					dir.resolve("bloodsnout_brute_armor_layer_1.png").copyTo(newDir.resolve("guardian/bloodsnout_brute.png"))
					dir.resolve("bloodsnout_brute_armor_layer_2.png").copyTo(newDir2.resolve("guardian/bloodsnout_brute.png"))

					dir.resolve("sturdy").resolve("wooden_armor_layer_1.png").copyTo(newDir.resolve("guardian/special/wooden.png"))
					dir.resolve("sturdy").resolve("wooden_armor_layer_2.png").copyTo(newDir2.resolve("guardian/special/wooden.png"))
					dir.resolve("strong").resolve("copper_armor_layer_1.png").copyTo(newDir.resolve("guardian/special/copper.png"))
					dir.resolve("strong").resolve("copper_armor_layer_2.png").copyTo(newDir2.resolve("guardian/special/copper.png"))
					dir.resolve("unbreaking").resolve("steel_armor_layer_1.png").copyTo(newDir.resolve("guardian/special/steel.png"))
					dir.resolve("unbreaking").resolve("steel_armor_layer_2.png").copyTo(newDir2.resolve("guardian/special/steel.png"))
					dir.resolve("thick-cut").resolve("bloodsnout_brute_armor_layer_1.png").copyTo(newDir.resolve("guardian/special/bloodsnout_brute.png"))
					dir.resolve("thick-cut").resolve("bloodsnout_brute_armor_layer_2.png").copyTo(newDir2.resolve("guardian/special/bloodsnout_brute.png"))
				}
				let {
					dir.resolve("cactusarmor_layer_1.png").copyTo(newDir.resolve("warrior/cactus.png"))
					dir.resolve("cactusarmor_layer_2.png").copyTo(newDir2.resolve("warrior/cactus.png"))
					dir.resolve("stone_armor_layer_1.png").copyTo(newDir.resolve("warrior/stone.png"))
					dir.resolve("stone_armor_layer_2.png").copyTo(newDir2.resolve("warrior/stone.png"))
					dir.resolve("mythril_armor_layer_1.png").copyTo(newDir.resolve("warrior/mythril.png"))
					dir.resolve("mythril_armor_layer_2.png").copyTo(newDir2.resolve("warrior/mythril.png"))
					dir.resolve("bloodsnout_slayer_armor_layer_1.png").copyTo(newDir.resolve("warrior/bloodsnout_slayer.png"))
					dir.resolve("bloodsnout_slayer_armor_layer_2.png").copyTo(newDir2.resolve("warrior/bloodsnout_slayer.png"))

					dir.resolve("sturdy").resolve("cactusarmor_layer_1.png").copyTo(newDir.resolve("warrior/special/cactus.png"))
					dir.resolve("sturdy").resolve("cactusarmor_layer_2.png").copyTo(newDir2.resolve("warrior/special/cactus.png"))
					dir.resolve("strong").resolve("stone_armor_layer_1.png").copyTo(newDir.resolve("warrior/special/stone.png"))
					dir.resolve("strong").resolve("stone_armor_layer_2.png").copyTo(newDir2.resolve("warrior/special/stone.png"))
					dir.resolve("blinding").resolve("mythril_armor_layer_1.png").copyTo(newDir.resolve("warrior/special/mythril.png"))
					dir.resolve("blinding").resolve("mythril_armor_layer_2.png").copyTo(newDir2.resolve("warrior/special/mythril.png"))
					dir.resolve("brutal").resolve("bloodsnout_slayer_armor_layer_1.png").copyTo(newDir.resolve("warrior/special/bloodsnout_slayer.png"))
					dir.resolve("brutal").resolve("bloodsnout_slayer_armor_layer_2.png").copyTo(newDir2.resolve("warrior/special/bloodsnout_slayer.png"))
				}
				let {
					dir.resolve("silk_armor_layer_1.png").copyTo(newDir.resolve("wizard/silk.png"))
					dir.resolve("silk_armor_layer_2.png").copyTo(newDir2.resolve("wizard/silk.png"))
					dir.resolve("sparkling_silk_armor_layer_1.png").copyTo(newDir.resolve("wizard/sparking_silk.png"))
					dir.resolve("sparkling_silk_armor_layer_2.png").copyTo(newDir2.resolve("wizard/sparking_silk.png"))
					dir.resolve("acolyte_armor_layer_1.png").copyTo(newDir.resolve("wizard/acolyte.png"))
					dir.resolve("acolyte_armor_layer_2.png").copyTo(newDir2.resolve("wizard/acolyte.png"))
					dir.resolve("bloodsnout_magi_armor_layer_1.png").copyTo(newDir.resolve("wizard/bloodsnout_magi.png"))
					dir.resolve("bloodsnout_magi_armor_layer_2.png").copyTo(newDir2.resolve("wizard/bloodsnout_magi.png"))
//					dir.resolve("djedi_armor_layer_1.png").copyTo(newDir.resolve("wizard/djedi.png"))
//					dir.resolve("djedi_armor_layer_2.png").copyTo(newDir2.resolve("wizard/djedi.png"))
					dir.resolve("wizard_armor_layer_1.png").copyTo(newDir.resolve("wizard/wizards.png"))
					dir.resolve("wizard_armor_layer_2.png").copyTo(newDir2.resolve("wizard/wizards.png"))

					dir.resolve("sturdy").resolve("silk_armor_layer_1.png").copyTo(newDir.resolve("wizard/special/silk.png"))
					dir.resolve("sturdy").resolve("silk_armor_layer_2.png").copyTo(newDir2.resolve("wizard/special/silk.png"))
					dir.resolve("strong").resolve("sparkling_silk_armor_layer_1.png").copyTo(newDir.resolve("wizard/special/sparking_silk.png"))
					dir.resolve("strong").resolve("sparkling_silk_armor_layer_2.png").copyTo(newDir2.resolve("wizard/special/sparking_silk.png"))
					dir.resolve("haunted").resolve("acolyte_armor_layer_1.png").copyTo(newDir.resolve("wizard/special/acolyte.png"))
					dir.resolve("haunted").resolve("acolyte_armor_layer_2.png").copyTo(newDir2.resolve("wizard/special/acolyte.png"))
					dir.resolve("heated").resolve("bloodsnout_magi_armor_layer_1.png").copyTo(newDir.resolve("wizard/special/bloodsnout_magi.png"))
					dir.resolve("heated").resolve("bloodsnout_magi_armor_layer_2.png").copyTo(newDir2.resolve("wizard/special/bloodsnout_magi.png"))
//					dir.resolve("djedi_armor_layer_1.png").copyTo(newDir.resolve("wizard/special/djedi.png"))
//					dir.resolve("djedi_armor_layer_2.png").copyTo(newDir2.resolve("wizard/special/djedi.png"))
					dir.resolve("purified").resolve("wizard_armor_layer_1.png").copyTo(newDir.resolve("wizard/special/wizards.png"))
					dir.resolve("purified").resolve("wizard_armor_layer_2.png").copyTo(newDir2.resolve("wizard/special/wizards.png"))
				}
				let {
					dir.resolve("wool_armor_layer_1.png").copyTo(newDir.resolve("thaumaturge/wool.png"))
					dir.resolve("wool_armor_layer_2.png").copyTo(newDir2.resolve("thaumaturge/wool.png"))
					dir.resolve("blessed_wool_armor_layer_1.png").copyTo(newDir.resolve("thaumaturge/blessed_wool.png"))
					dir.resolve("blessed_wool_armor_layer_2.png").copyTo(newDir2.resolve("thaumaturge/blessed_wool.png"))
					dir.resolve("sanctified_armor_layer_1.png").copyTo(newDir.resolve("thaumaturge/sanctified.png"))
					dir.resolve("sanctified_armor_layer_2.png").copyTo(newDir2.resolve("thaumaturge/sanctified.png"))
					dir.resolve("bloodsnout_healer_armor_layer_1.png").copyTo(newDir.resolve("thaumaturge/bloodsnout_healer.png"))
					dir.resolve("bloodsnout_healer_armor_layer_2.png").copyTo(newDir2.resolve("thaumaturge/bloodsnout_healer.png"))
					dir.resolve("glowing_armor_layer_1.png").copyTo(newDir.resolve("thaumaturge/heka.png"))
					dir.resolve("glowing_armor_layer_2.png").copyTo(newDir2.resolve("thaumaturge/heka.png"))

					dir.resolve("sturdy").resolve("wool_armor_layer_1.png").copyTo(newDir.resolve("thaumaturge/special/wool.png"))
					dir.resolve("sturdy").resolve("wool_armor_layer_2.png").copyTo(newDir2.resolve("thaumaturge/special/wool.png"))
					dir.resolve("strong").resolve("blessed_wool_armor_layer_1.png").copyTo(newDir.resolve("thaumaturge/special/blessed_wool.png"))
					dir.resolve("strong").resolve("blessed_wool_armor_layer_2.png").copyTo(newDir2.resolve("thaumaturge/special/blessed_wool.png"))
					dir.resolve("blessed").resolve("sanctified_armor_layer_1.png").copyTo(newDir.resolve("thaumaturge/special/sanctified.png"))
					dir.resolve("blessed").resolve("sanctified_armor_layer_2.png").copyTo(newDir2.resolve("thaumaturge/special/sanctified.png"))
					dir.resolve("slick").resolve("bloodsnout_healer_armor_layer_1.png").copyTo(newDir.resolve("thaumaturge/special/bloodsnout_healer.png"))
					dir.resolve("slick").resolve("bloodsnout_healer_armor_layer_2.png").copyTo(newDir2.resolve("thaumaturge/special/bloodsnout_healer.png"))
					dir.resolve("sparkling").resolve("glowing_armor_layer_1.png").copyTo(newDir.resolve("thaumaturge/special/heka.png"))
					dir.resolve("sparkling").resolve("glowing_armor_layer_2.png").copyTo(newDir2.resolve("thaumaturge/special/heka.png"))
				}
				let {
					dir.resolve("onion_armor_layer_1.png").copyTo(newDir.resolve("onion/onion.png"))
					dir.resolve("onion_armor_layer_2.png").copyTo(newDir2.resolve("onion/onion.png"))
				}

				dir.resolve("professions/colored").let { professions ->
					let {
						professions.resolve("archaeologist_armor_layer_t1_1.png").copyTo(newDir.resolve("archaeologist/t1.png"))
						professions.resolve("archaeologist_armor_layer_t1_2.png").copyTo(newDir2.resolve("archaeologist/t1.png"))
						professions.resolve("archaeologist_armor_layer_t2_1.png").copyTo(newDir.resolve("archaeologist/t2.png"))
						professions.resolve("archaeologist_armor_layer_t2_2.png").copyTo(newDir2.resolve("archaeologist/t2.png"))
						professions.resolve("archaeologist_armor_layer_t3_1.png").copyTo(newDir.resolve("archaeologist/t3.png"))
						professions.resolve("archaeologist_armor_layer_t3_2.png").copyTo(newDir2.resolve("archaeologist/t3.png"))
						professions.resolve("archaeologist_armor_layer_t4_1.png").copyTo(newDir.resolve("archaeologist/t4.png"))
						professions.resolve("archaeologist_armor_layer_t4_2.png").copyTo(newDir2.resolve("archaeologist/t4.png"))
						professions.resolve("archaeologist_armor_layer_t5_1.png").copyTo(newDir.resolve("archaeologist/t5.png"))
						professions.resolve("archaeologist_armor_layer_t5_2.png").copyTo(newDir2.resolve("archaeologist/t5.png"))

						professions.resolve("archaeologist_armor_layer_modifier_t1_1.png").copyTo(newDir.resolve("archaeologist/special/t1.png"))
						professions.resolve("archaeologist_armor_layer_modifier_t1_2.png").copyTo(newDir2.resolve("archaeologist/special/t1.png"))
						professions.resolve("archaeologist_armor_layer_modifier_t2_1.png").copyTo(newDir.resolve("archaeologist/special/t2.png"))
						professions.resolve("archaeologist_armor_layer_modifier_t2_2.png").copyTo(newDir2.resolve("archaeologist/special/t2.png"))
						professions.resolve("archaeologist_armor_layer_modifier_t3_1.png").copyTo(newDir.resolve("archaeologist/special/t3.png"))
						professions.resolve("archaeologist_armor_layer_modifier_t3_2.png").copyTo(newDir2.resolve("archaeologist/special/t3.png"))
						professions.resolve("archaeologist_armor_layer_modifier_t4_1.png").copyTo(newDir.resolve("archaeologist/special/t4.png"))
						professions.resolve("archaeologist_armor_layer_modifier_t4_2.png").copyTo(newDir2.resolve("archaeologist/special/t4.png"))
						professions.resolve("archaeologist_armor_layer_modifier_t5_1.png").copyTo(newDir.resolve("archaeologist/special/t5.png"))
						professions.resolve("archaeologist_armor_layer_modifier_t5_2.png").copyTo(newDir2.resolve("archaeologist/special/t5.png"))
					}
					let {
						professions.resolve("botanist_armor_layer_t1_1.png").copyTo(newDir.resolve("botanist/t1.png"))
						professions.resolve("botanist_armor_layer_t1_2.png").copyTo(newDir2.resolve("botanist/t1.png"))
						professions.resolve("botanist_armor_layer_t2_1.png").copyTo(newDir.resolve("botanist/t2.png"))
						professions.resolve("botanist_armor_layer_t2_2.png").copyTo(newDir2.resolve("botanist/t2.png"))
						professions.resolve("botanist_armor_layer_t3_1.png").copyTo(newDir.resolve("botanist/t3.png"))
						professions.resolve("botanist_armor_layer_t3_2.png").copyTo(newDir2.resolve("botanist/t3.png"))
						professions.resolve("botanist_armor_layer_t4_1.png").copyTo(newDir.resolve("botanist/t4.png"))
						professions.resolve("botanist_armor_layer_t4_2.png").copyTo(newDir2.resolve("botanist/t4.png"))
						professions.resolve("botanist_armor_layer_t5_1.png").copyTo(newDir.resolve("botanist/t5.png"))
						professions.resolve("botanist_armor_layer_t5_2.png").copyTo(newDir2.resolve("botanist/t5.png"))

						professions.resolve("botanist_armor_layer_modifier_t1_1.png").copyTo(newDir.resolve("botanist/special/t1.png"))
						professions.resolve("botanist_armor_layer_modifier_t1_2.png").copyTo(newDir2.resolve("botanist/special/t1.png"))
						professions.resolve("botanist_armor_layer_modifier_t2_1.png").copyTo(newDir.resolve("botanist/special/t2.png"))
						professions.resolve("botanist_armor_layer_modifier_t2_2.png").copyTo(newDir2.resolve("botanist/special/t2.png"))
						professions.resolve("botanist_armor_layer_modifier_t3_1.png").copyTo(newDir.resolve("botanist/special/t3.png"))
						professions.resolve("botanist_armor_layer_modifier_t3_2.png").copyTo(newDir2.resolve("botanist/special/t3.png"))
						professions.resolve("botanist_armor_layer_modifier_t4_1.png").copyTo(newDir.resolve("botanist/special/t4.png"))
						professions.resolve("botanist_armor_layer_modifier_t4_2.png").copyTo(newDir2.resolve("botanist/special/t4.png"))
						professions.resolve("botanist_armor_layer_modifier_t5_1.png").copyTo(newDir.resolve("botanist/special/t5.png"))
						professions.resolve("botanist_armor_layer_modifier_t5_2.png").copyTo(newDir2.resolve("botanist/special/t5.png"))
					}
					let {
						professions.resolve("fisherman_armor_layer_t1_1.png").copyTo(newDir.resolve("fisherman/t1.png"))
						professions.resolve("fisherman_armor_layer_t1_2.png").copyTo(newDir2.resolve("fisherman/t1.png"))
						professions.resolve("fisherman_armor_layer_t2_1.png").copyTo(newDir.resolve("fisherman/t2.png"))
						professions.resolve("fisherman_armor_layer_t2_2.png").copyTo(newDir2.resolve("fisherman/t2.png"))
						professions.resolve("fisherman_armor_layer_t3_1.png").copyTo(newDir.resolve("fisherman/t3.png"))
						professions.resolve("fisherman_armor_layer_t3_2.png").copyTo(newDir2.resolve("fisherman/t3.png"))
						professions.resolve("fisherman_armor_layer_t4_1.png").copyTo(newDir.resolve("fisherman/t4.png"))
						professions.resolve("fisherman_armor_layer_t4_2.png").copyTo(newDir2.resolve("fisherman/t4.png"))
						professions.resolve("fisherman_armor_layer_t5_1.png").copyTo(newDir.resolve("fisherman/t5.png"))
						professions.resolve("fisherman_armor_layer_t5_2.png").copyTo(newDir2.resolve("fisherman/t5.png"))

						professions.resolve("fisherman_armor_layer_modifier_t1_1.png").copyTo(newDir.resolve("fisherman/special/t1.png"))
						professions.resolve("fisherman_armor_layer_modifier_t1_2.png").copyTo(newDir2.resolve("fisherman/special/t1.png"))
						professions.resolve("fisherman_armor_layer_modifier_t2_1.png").copyTo(newDir.resolve("fisherman/special/t2.png"))
						professions.resolve("fisherman_armor_layer_modifier_t2_2.png").copyTo(newDir2.resolve("fisherman/special/t2.png"))
						professions.resolve("fisherman_armor_layer_modifier_t3_1.png").copyTo(newDir.resolve("fisherman/special/t3.png"))
						professions.resolve("fisherman_armor_layer_modifier_t3_2.png").copyTo(newDir2.resolve("fisherman/special/t3.png"))
						professions.resolve("fisherman_armor_layer_modifier_t4_1.png").copyTo(newDir.resolve("fisherman/special/t4.png"))
						professions.resolve("fisherman_armor_layer_modifier_t4_2.png").copyTo(newDir2.resolve("fisherman/special/t4.png"))
						professions.resolve("fisherman_armor_layer_modifier_t5_1.png").copyTo(newDir.resolve("fisherman/special/t5.png"))
						professions.resolve("fisherman_armor_layer_modifier_t5_2.png").copyTo(newDir2.resolve("fisherman/special/t5.png"))
					}
					let {
						professions.resolve("lumberjack_armor_layer_t1_1.png").copyTo(newDir.resolve("lumberjack/t1.png"))
						professions.resolve("lumberjack_armor_layer_t1_2.png").copyTo(newDir2.resolve("lumberjack/t1.png"))
						professions.resolve("lumberjack_armor_layer_t2_1.png").copyTo(newDir.resolve("lumberjack/t2.png"))
						professions.resolve("lumberjack_armor_layer_t2_2.png").copyTo(newDir2.resolve("lumberjack/t2.png"))
						professions.resolve("lumberjack_armor_layer_t3_1.png").copyTo(newDir.resolve("lumberjack/t3.png"))
						professions.resolve("lumberjack_armor_layer_t3_2.png").copyTo(newDir2.resolve("lumberjack/t3.png"))
						professions.resolve("lumberjack_armor_layer_t4_1.png").copyTo(newDir.resolve("lumberjack/t4.png"))
						professions.resolve("lumberjack_armor_layer_t4_2.png").copyTo(newDir2.resolve("lumberjack/t4.png"))
						professions.resolve("lumberjack_armor_layer_t5_1.png").copyTo(newDir.resolve("lumberjack/t5.png"))
						professions.resolve("lumberjack_armor_layer_t5_2.png").copyTo(newDir2.resolve("lumberjack/t5.png"))

						professions.resolve("lumberjack_armor_layer_modifier_t1_1.png").copyTo(newDir.resolve("lumberjack/special/t1.png"))
						professions.resolve("lumberjack_armor_layer_modifier_t1_2.png").copyTo(newDir2.resolve("lumberjack/special/t1.png"))
						professions.resolve("lumberjack_armor_layer_modifier_t2_1.png").copyTo(newDir.resolve("lumberjack/special/t2.png"))
						professions.resolve("lumberjack_armor_layer_modifier_t2_2.png").copyTo(newDir2.resolve("lumberjack/special/t2.png"))
						professions.resolve("lumberjack_armor_layer_modifier_t3_1.png").copyTo(newDir.resolve("lumberjack/special/t3.png"))
						professions.resolve("lumberjack_armor_layer_modifier_t3_2.png").copyTo(newDir2.resolve("lumberjack/special/t3.png"))
						professions.resolve("lumberjack_armor_layer_modifier_t4_1.png").copyTo(newDir.resolve("lumberjack/special/t4.png"))
						professions.resolve("lumberjack_armor_layer_modifier_t4_2.png").copyTo(newDir2.resolve("lumberjack/special/t4.png"))
						professions.resolve("lumberjack_armor_layer_modifier_t5_1.png").copyTo(newDir.resolve("lumberjack/special/t5.png"))
						professions.resolve("lumberjack_armor_layer_modifier_t5_2.png").copyTo(newDir2.resolve("lumberjack/special/t5.png"))
					}
					let {
						professions.resolve("miner_armor_layer_t1_1.png").copyTo(newDir.resolve("miner/t1.png"))
						professions.resolve("miner_armor_layer_t1_2.png").copyTo(newDir2.resolve("miner/t1.png"))
						professions.resolve("miner_armor_layer_t2_1.png").copyTo(newDir.resolve("miner/t2.png"))
						professions.resolve("miner_armor_layer_t2_2.png").copyTo(newDir2.resolve("miner/t2.png"))
						professions.resolve("miner_armor_layer_t3_1.png").copyTo(newDir.resolve("miner/t3.png"))
						professions.resolve("miner_armor_layer_t3_2.png").copyTo(newDir2.resolve("miner/t3.png"))
						professions.resolve("miner_armor_layer_t4_1.png").copyTo(newDir.resolve("miner/t4.png"))
						professions.resolve("miner_armor_layer_t4_2.png").copyTo(newDir2.resolve("miner/t4.png"))
						professions.resolve("miner_armor_layer_t5_1.png").copyTo(newDir.resolve("miner/t5.png"))
						professions.resolve("miner_armor_layer_t5_2.png").copyTo(newDir2.resolve("miner/t5.png"))

						professions.resolve("miner_armor_layer_modifier_t1_1.png").copyTo(newDir.resolve("miner/special/t1.png"))
						professions.resolve("miner_armor_layer_modifier_t1_2.png").copyTo(newDir2.resolve("miner/special/t1.png"))
						professions.resolve("miner_armor_layer_modifier_t2_1.png").copyTo(newDir.resolve("miner/special/t2.png"))
						professions.resolve("miner_armor_layer_modifier_t2_2.png").copyTo(newDir2.resolve("miner/special/t2.png"))
						professions.resolve("miner_armor_layer_modifier_t3_1.png").copyTo(newDir.resolve("miner/special/t3.png"))
						professions.resolve("miner_armor_layer_modifier_t3_2.png").copyTo(newDir2.resolve("miner/special/t3.png"))
						professions.resolve("miner_armor_layer_modifier_t4_1.png").copyTo(newDir.resolve("miner/special/t4.png"))
						professions.resolve("miner_armor_layer_modifier_t4_2.png").copyTo(newDir2.resolve("miner/special/t4.png"))
						professions.resolve("miner_armor_layer_modifier_t5_1.png").copyTo(newDir.resolve("miner/special/t5.png"))
						professions.resolve("miner_armor_layer_modifier_t5_2.png").copyTo(newDir2.resolve("miner/special/t5.png"))
					}

				}

				dir.deleteRecursively()
			}
		}

//		mcAssetsDir.listFiles()?.forEach { dir ->
//			if (dir.isDirectory) {
//				dir.listFiles()?.forEach { subfolder ->
//					subfolder.copyTo(blockgameAssetsDir.resolve(dir.nameWithoutExtension).resolve(subfolder.nameWithoutExtension))
//					dir.deleteRecursively()
//				}
//			}
//		}

//		overridesArmorDir.listFiles()?.forEach { file: File ->
//			if (file.extension.lowercase() == "json") {
//				LOGGER.info("	> converting armor: ${file.nameWithoutExtension}...")
//
//				val json = PredicateTextureJsonData.readPredicateJsonDataFromFile(file)
//					?: return@forEach
//
////				val newFormatJsonDataMap = DataConverter.convertPredicates2NewFormat(json, file.nameWithoutExtension)
//
//
//			}
//		}

		VersionHandler.writeVersionToDirectoryResourcepack(tempResourcepackFile)
	}

}
