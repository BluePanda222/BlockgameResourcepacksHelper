package com.bluepanda22.blockgameresourcepackshelper.migrator.conversion

import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain.Companion.LOGGER
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.Strictness
import com.google.gson.annotations.SerializedName
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader

data class PredicateJsonData(
	val overrides: List<OverrideEntry>
) {

	companion object {

		fun readPredicateJsonDataFromFile(file: File): PredicateJsonData? {
			val gson = Gson().newBuilder().setStrictness(Strictness.STRICT).create()
			try {
				val fileReader = FileReader(file)
				try {
					fileReader.use {
						return gson.fromJson(it, PredicateJsonData::class.java)
					}
				} catch (e: JsonSyntaxException) {
					fileReader.close()
					LOGGER.error("Error: Invalid JSON syntax in file ${file.path}")
					return null
				} catch (e: Exception) {
					fileReader.close()
					LOGGER.error("An error occurred while reading or parsing the file: ${file.path}", e)
					return null
				}
			} catch (e: FileNotFoundException) {
				LOGGER.error("Error: File not found at ${file.path}")
				return null
			}
		}

	}

}

data class PredicateTextureJsonData(
	val overrides: List<OverrideTextureEntry>
) {

	companion object {

		fun readPredicateJsonDataFromFile(file: File): PredicateTextureJsonData? {
			val gson = Gson().newBuilder().setStrictness(Strictness.STRICT).create()
			try {
				val fileReader = FileReader(file)
				try {
					fileReader.use {
						return gson.fromJson(it, PredicateTextureJsonData::class.java)
					}
				} catch (e: JsonSyntaxException) {
					fileReader.close()
					LOGGER.error("Error: Invalid JSON syntax in file ${file.path}")
					return null
				} catch (e: Exception) {
					fileReader.close()
					LOGGER.error("An error occurred while reading or parsing the file: ${file.path}", e)
					return null
				}
			} catch (e: FileNotFoundException) {
				LOGGER.error("Error: File not found at ${file.path}")
				return null
			}
		}

	}

}

data class OverrideEntry(
	val predicate: Predicate,
	val model: String,
)

data class OverrideTextureEntry(
	val predicate: Predicate,
	val texture: String,
)

data class Predicate(
	val nbt: NbtPredicate? = null,
	val name: String? = null, // /.*Battleship.*/"
	val count: String? = null,
	val pulling: Int? = null,
	val pull: Double? = null,
	val cast: Int? = null,
) {

	private fun deepCopy(
		name: String? = this.name,
		count: String? = this.count,
		pulling: Int? = this.pulling,
		pull: Double? = this.pull,
		cast: Int? = this.cast,

		itemId: String? = this.nbt?.itemId,
		prefix: String? = this.nbt?.prefix,
		suffix: String? = this.nbt?.suffix,
		maxConsume: String? = this.nbt?.maxConsume,
		itemType: String? = this.nbt?.itemType,
		itemName: String? = this.nbt?.itemName,
	): Predicate {
		val nbt: NbtPredicate? = this.nbt?.copy(
			itemId = itemId,
			prefix = prefix,
			suffix = suffix,
			maxConsume = maxConsume,
			itemType = itemType,
			itemName = itemName,
		)

		return Predicate(
			nbt,
			name,
			count,
			pulling,
			pull,
			cast,
		)
	}


	fun hasOnlyItemId(): Boolean {
		return name == null && count == null && pulling == null && pull == null && cast == null
				&& nbt != null && nbt.hasOnlyItemId()
	}

	fun hasOnlyCount(): Boolean {
		return name == null && count != null && pulling == null && pull == null && cast == null
				&& nbt != null && nbt.prefix == null && nbt.suffix == null && nbt.maxConsume == null && nbt.itemType == null && nbt.itemName == null
	}

	fun hasOnlyPulling(): Boolean {
		return name == null && count == null && pulling != null && cast == null
				&& nbt != null && nbt.prefix == null && nbt.suffix == null && nbt.maxConsume == null && nbt.itemType == null && nbt.itemName == null
	}

	fun hasOnlyCast(): Boolean {
		return name == null && count == null && pulling == null && pull == null && cast != null
				&& nbt != null && nbt.prefix == null && nbt.suffix == null && nbt.maxConsume == null && nbt.itemType == null && nbt.itemName == null
	}

	fun hasOnlyPrefix(): Boolean {
		return name == null && count == null && pulling == null && pull == null && cast == null
				&& nbt != null && nbt.prefix != null && nbt.suffix == null && nbt.maxConsume == null && nbt.itemType == null && nbt.itemName == null
	}

	fun hasOnlySuffix(): Boolean {
		return name == null && count == null && pulling == null && pull == null && cast == null
				&& nbt != null && nbt.prefix == null && nbt.suffix != null && nbt.maxConsume == null && nbt.itemType == null && nbt.itemName == null
	}

	fun hasOnlyMaxConsume(): Boolean {
		return name == null && count == null && pulling == null && pull == null && cast == null
				&& nbt != null && nbt.prefix == null && nbt.suffix == null && nbt.maxConsume != null && nbt.itemType == null && nbt.itemName == null
	}

	fun hasOnlyItemType(): Boolean {
		return name == null && count == null && pulling == null && pull == null && cast == null
				&& nbt != null && nbt.prefix == null && nbt.suffix == null && nbt.maxConsume == null && nbt.itemType != null && nbt.itemName == null
	}

	fun hasOnlyItemName(): Boolean {
		return name == null && count == null && pulling == null && pull == null && cast == null
				&& nbt != null && nbt.prefix == null && nbt.suffix == null && nbt.maxConsume == null && nbt.itemType != null && nbt.itemName != null
	}


	fun hasCount(): Boolean {
		return count != null
	}

	fun hasPulling(): Boolean {
		return pulling != null
	}

	fun hasCast(): Boolean {
		return cast != null
	}

	fun hasPrefix(): Boolean {
		return nbt?.prefix != null
	}

	fun hasSuffix(): Boolean {
		return nbt?.suffix != null
	}

	fun hasMaxConsume(): Boolean {
		return nbt?.maxConsume != null
	}

	fun hasItemType(): Boolean {
		return nbt?.itemType != null
	}

	fun hasItemName(): Boolean {
		return nbt?.itemName != null
	}


	fun eraseCount(): Predicate {
		return this.deepCopy(count = null)
	}

	fun erasePulling(): Predicate {
		return this.deepCopy(pulling = null, pull = null)
	}

	fun erasePrefix(): Predicate {
		return this.deepCopy(prefix = null)
	}

	fun eraseSuffix(): Predicate {
		return this.deepCopy(suffix = null)
	}

	fun eraseMaxConsume(): Predicate {
		return this.deepCopy(maxConsume = null)
	}

	fun eraseItemType(): Predicate {
		return this.deepCopy(itemType = null)
	}

	fun eraseItemName(): Predicate {
		return this.deepCopy(itemName = null)
	}

}

data class NbtPredicate(
	@SerializedName("MMOITEMS_ITEM_ID") 		// string
	val itemId: String? = null,
	@SerializedName("MMOITEMS_NAME_PRE") 		// list with only up to one entry
	val prefix: String? = null,
	@SerializedName("MMOITEMS_NAME_SUF") 		// list with only up to one entry
	val suffix: String? = null,
	@SerializedName("MMOITEMS_MAX_CONSUME") 	// double
	var maxConsume: String? = null,
	@SerializedName("MMOITEMS_ITEM_TYPE") 		// string
	val itemType: String? = null, 				// -> CORRUPTED_ARMOR
	@SerializedName("MMOITEMS_NAME")			// string
	val itemName: String? = null,				// "MMOITEMS_NAME": "<tier-color>Backstab - Rank 1",

	// only handled for armors
	@SerializedName("MMOITEMS_ITEM_SET")
	val itemSet: String? = null,

//	@SerializedName("recipeId")
//	val recipeId: String? = null,				// -> '/.*LAPIS/', MineBlockCraftGame, redstone.json, sugar.json
) {

	fun hasOnlyItemId(): Boolean {
		return prefix == null && suffix == null && maxConsume == null && itemType == null && itemName == null
	}

}
