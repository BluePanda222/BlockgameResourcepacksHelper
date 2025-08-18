package com.bluepanda22.blockgameresourcepackshelper.util

import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain.Companion.MOD_ID
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path
import kotlin.io.path.exists

object DirectoryUtil {

	private val minecraftConfigDir: Path
		get() = FabricLoader.getInstance().configDir

	val configDir: Path
		get() = minecraftConfigDir.resolve(MOD_ID)

	val tempDir: Path
		get() = configDir.resolve("temp")


	init {
		initTempDir()
	}

	fun mkDirs(path: Path) {
		if (!path.exists()) {
			path.toFile().mkdirs()
		}
	}

	fun cleanupTempDir() {
		tempDir.toFile().deleteRecursively()
	}

	fun initTempDir() {
		mkDirs(configDir)
		mkDirs(tempDir)
	}

}
