package com.bluepanda22.blockgameresourcepackshelper.migrator.conversion

import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain.Companion.LOGGER
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type

data class NewFormatJsonData(
	val model: BaseModel
) {

	companion object {

		fun readNewFormatJsonDataFromFile(file: File): NewFormatJsonData? {
			val gson = GsonBuilder()
				.registerTypeAdapter(BaseModel::class.java, BaseModelDeserializer())
				.create()

			try {
				FileReader(file).use {
					return gson.fromJson(it, NewFormatJsonData::class.java)
				}
			} catch (e: FileNotFoundException) {
				LOGGER.error("Error: File not found at ${file.path}")
				return null
			} catch (e: JsonSyntaxException) {
				LOGGER.error("Error: Invalid JSON syntax in file ${file.path}")
				return null
			} catch (e: Exception) {
				LOGGER.error("An error occurred while reading or parsing the file: ${file.path}", e)
				return null
			}
		}

		fun writeNewFormatJsonDataToFile(data: NewFormatJsonData, file: File) {
			val gson = GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(BaseModel::class.java, BaseModelDeserializer())
				.create()

			try {
				FileWriter(file).use {
					gson.toJson(data, it)
				}
			} catch (e: Exception) {
				println("An error occurred while writing the file: ${file.absolutePath}")
				e.printStackTrace()
			}
		}

	}

}

// Base interface for all potential model types
sealed interface BaseModel

data class SimpleModel(
	val type: String, // Should be "minecraft:model"
	val model: String
) : BaseModel

data class ConditionModel(
	val type: String, // Should be "minecraft:condition"
	val property: String, // minecraft:using_item -> pulling bow
	@SerializedName("on_true")
	val onTrue: BaseModel,
	@SerializedName("on_false")
	val onFalse: BaseModel,
) : BaseModel

data class SelectModel(
	val type: String, // Should be "minecraft:select"
	val property: String,
	val cases: List<SelectCase>,
	val fallback: BaseModel?,
) : BaseModel
data class SelectCase(
	val `when`: String,
	val model: BaseModel
)

data class RangeDispatchModel(
	val type: String, // Should be "minecraft:range_dispatch"
	val property: String,
	val scale: Double? = 1.0,
	val entries: List<RangeEntry>,
	val fallback: BaseModel?,
) : BaseModel
data class RangeEntry(
	val threshold: Double,
	val model: BaseModel,
)


class BaseModelDeserializer : JsonDeserializer<BaseModel> {
	override fun deserialize(
		json: JsonElement?,
		typeOfT: Type?,
		context: JsonDeserializationContext?
	): BaseModel? {
		val jsonObject = json?.asJsonObject ?: return null
		val type = jsonObject.get("type")?.asString ?: return null

		return when (type) {
			"minecraft:select" -> context?.deserialize(jsonObject, SelectModel::class.java)
			"minecraft:model" -> context?.deserialize(jsonObject, SimpleModel::class.java)
			"minecraft:range_dispatch" -> context?.deserialize(jsonObject, RangeDispatchModel::class.java)
			else -> null // Or throw an error for unknown types
		}
	}
}
