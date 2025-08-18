package com.bluepanda22.blockgameresourcepackshelper.migrator

import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain.Companion.LOGGER
import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain.Companion.MOD_ID
import java.io.File
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.writeText

object VersionHandler {

	const val VERSION = "0.1.6"
	const val FILE_NAME = "version.${MOD_ID}.txt"

	fun getVersionFromDirectoryResourcepack(resourcePackPath: Path): String? {
		val versionPath = resourcePackPath.resolve(FILE_NAME)
		if (versionPath.exists() && versionPath.isRegularFile()) {
			val versionFile = versionPath.toFile()
			val content = versionFile.readText()

			return content
		}

		return null
	}

	fun getVersionFromZipResourcepack(resourcePackPath: Path): String? {
		val zipResourcepackFile = resourcePackPath.toFile()
		try {
			ZipFile(zipResourcepackFile).use { zip ->
				val entry: ZipEntry = zip.getEntry(FILE_NAME)
					?: return null

				val inputStream = zip.getInputStream(entry)
				val content = inputStream.readAllBytes().decodeToString()

				return content
			}
		} catch (e: Exception) {
			LOGGER.error("Error checking zip file ${zipResourcepackFile.name}: ${e.message}", e)
			return null
		}
	}

	fun writeVersionToDirectoryResourcepack(resourcePackPath: Path) {
		val versionPath = resourcePackPath.resolve(FILE_NAME)
		versionPath.writeText(VERSION)
	}

	fun writeVersionToDirectoryResourcepack(resourcePackFile: File) {
		val versionPath = resourcePackFile.resolve(FILE_NAME)
		versionPath.writeText(VERSION)
	}

}