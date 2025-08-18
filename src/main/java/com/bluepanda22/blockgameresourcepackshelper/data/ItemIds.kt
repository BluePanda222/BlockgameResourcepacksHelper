package com.bluepanda22.blockgameresourcepackshelper.data

import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain
import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain.Companion.LOGGER
import com.bluepanda22.blockgameresourcepackshelper.util.DirectoryUtil
import com.bluepanda22.blockgameresourcepackshelper.util.FileUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

object ItemIds {

	val registry = mutableSetOf<String>()

	const val IDS_FILE = "${BlockgameResourcepacksHelperMain.MOD_ID}.ids.json"


	init {
//			if (FileUtil.checkIfFileExists(ModConfig.configDirectoryPath, IDS_FILE)) {
//				loadFile()
//			} else {
		FileUtil.getFileFromResourceAsStream("default/$IDS_FILE").use { inputStream ->
			val jsonStr = inputStream.bufferedReader().use { reader -> reader.readText() }
			FileUtil.writeToFile(jsonStr, DirectoryUtil.configDir, IDS_FILE)
			loadFile()
		}
//			}
	}

	fun saveFile() {
		val jsonStr = listToJsonString(registry)
		FileUtil.writeToFile(jsonStr, DirectoryUtil.configDir, IDS_FILE)
	}

	fun loadFile() {
		val jsonStr: String
		try {
			jsonStr = FileUtil.readFromFile(DirectoryUtil.configDir, IDS_FILE)
		} catch (e: Exception) {
			LOGGER.error("Failed to load models file.")
			e.printStackTrace()
			return
		}
		val list: List<String>
		try {
			list = listFromJsonString(jsonStr)
		} catch (e: Exception) {
			LOGGER.error("Failed to parse models file.")
			e.printStackTrace()
			return
		}
		list.forEach { register(it) }
	}

	private fun listToJsonString(items: Collection<String>): String {
		val gson: Gson = GsonBuilder().setPrettyPrinting().create()
		return gson.toJson(items)
	}

	private fun listFromJsonString(json: String): List<String> {
		val gson: Gson = GsonBuilder().create()
		val type = object : TypeToken<List<String>>() {}.type
		return gson.fromJson(json, type)
	}


	private fun register(value: String) {
		registry.add(value)
	}

}
