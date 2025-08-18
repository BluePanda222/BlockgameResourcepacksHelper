package com.bluepanda22.blockgameresourcepackshelper.data

import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain
import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain.Companion.LOGGER
import com.bluepanda22.blockgameresourcepackshelper.util.DirectoryUtil
import com.bluepanda22.blockgameresourcepackshelper.util.FileUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class ItemData(
	@SerializedName("SET")
	val set: String,
	@SerializedName("ITEM_TYPE")
	val itemType: String,
	@SerializedName("MC_ITEM_ID")
	val mcItemId: String,
	val color: Int? = null,
	val material: String? = null,
	val pattern: String? = null,
) {

	companion object {

		val registry = mutableMapOf<String, ItemData>()

		const val ITEM_DATA_FILE = "${BlockgameResourcepacksHelperMain.MOD_ID}.items.json"


		init {
//			if (FileUtil.checkIfFileExists(ModConfig.configDirectoryPath, MODELS_FILE)) {
//				loadFile()
//			} else {
			FileUtil.getFileFromResourceAsStream("default/$ITEM_DATA_FILE").use { inputStream ->
				val jsonStr = inputStream.bufferedReader().use { reader -> reader.readText() }
				FileUtil.writeToFile(jsonStr, DirectoryUtil.configDir, ITEM_DATA_FILE)
				loadFile()
			}
//			}
		}

		fun saveFile() {
			val jsonStr = mapToJsonString(registry)
			FileUtil.writeToFile(jsonStr, DirectoryUtil.configDir, ITEM_DATA_FILE)
		}

		fun loadFile() {
			val jsonStr: String
			try {
				jsonStr = FileUtil.readFromFile(DirectoryUtil.configDir, ITEM_DATA_FILE)
			} catch (e: Exception) {
				LOGGER.error("Failed to load item data file.")
				e.printStackTrace()
				return
			}
			val map: Map<String, ItemData>
			try {
				map = mapFromJsonString(jsonStr)
			} catch (e: Exception) {
				LOGGER.error("Failed to parse item data file.")
				e.printStackTrace()
				return
			}
			map.forEach { (key, value) -> register(key, value) }
		}

		private fun mapToJsonString(items: Map<String, ItemData>): String {
			val gson: Gson = GsonBuilder().setPrettyPrinting().create()
			return gson.toJson(items)
		}

		private fun mapFromJsonString(json: String): Map<String, ItemData> {
			val gson: Gson = GsonBuilder().create()
			val type = object : TypeToken<Map<String, ItemData>>() {}.type
			return gson.fromJson(json, type)
		}


		private fun register(key: String, value: ItemData) {
			registry[key] = value
		}

	}

}
