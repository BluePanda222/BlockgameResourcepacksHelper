package com.bluepanda22.blockgameresourcepackshelper.migrator.conversion

import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain.Companion.LOGGER
import com.bluepanda22.blockgameresourcepackshelper.data.ItemIds

object DataConverter {

//	private val defaultModels: Map<String, String>
//		get() = ModelData.registry

	private val itemIds: Set<String>
		get() = ItemIds.registry


	private fun parsePrefixOrSuffix(string: String): String {
		return string
			.replace("/", "")
			.replace(".*", "")
	}

	private fun getItemIdsThatMatchRegex(regex: Regex): List<String> {
		return itemIds.filter { regex.matches(it) }
	}

	private fun convertModel(model: String): String {
		return model
//		return model.replace("minecraft:", "blockgame:")
	}

	private fun groupHasOnlyItemId(groupOverrides: List<OverrideEntry>): Boolean {
		return groupOverrides.all { it.predicate.hasOnlyItemId() }
	}

	private fun groupHasPulling(groupOverrides: List<OverrideEntry>, jsonFileName: String): Boolean {
		val pullingList = groupOverrides.filter { it.predicate.hasOnlyPulling() }
		val itemIdList = groupOverrides.filter { it.predicate.hasOnlyItemId() }

		if (itemIdList.size > 1) {
			LOGGER.debug("too many item ids for pulling! $jsonFileName")
			return false
		}

		if (groupOverrides.size != (pullingList.size + itemIdList.size)) {
			LOGGER.debug("size mismatch for pulling! $jsonFileName")
			return false
		} else {
			return true
		}
	}

	private fun groupHasCount(groupOverrides: List<OverrideEntry>, jsonFileName: String): Boolean {
		val countList = groupOverrides.filter { it.predicate.hasOnlyCount() }
		val itemIdList = groupOverrides.filter { it.predicate.hasOnlyItemId() }

		if (itemIdList.size > 1) {
			LOGGER.debug("too many item ids for count! $jsonFileName")
			return false
		}

		if (groupOverrides.size != (countList.size + itemIdList.size)) {
			LOGGER.debug("size mismatch for count! $jsonFileName")
			return false
		} else {
			return true
		}
	}

	private fun groupHasCast(groupOverrides: List<OverrideEntry>, jsonFileName: String): Boolean {
		val castList = groupOverrides.filter { it.predicate.hasOnlyCast() }
		val itemIdList = groupOverrides.filter { it.predicate.hasOnlyItemId() }

		if (itemIdList.size > 1) {
			LOGGER.debug("too many item ids for cast! $jsonFileName")
			return false
		}

		if (castList.size > 1) {
			LOGGER.debug("too many overrides with cast for cast! $jsonFileName")
			return false
		}

		if (groupOverrides.size != (castList.size + itemIdList.size)) {
			LOGGER.debug("size mismatch for cast! $jsonFileName")
			return false
		} else {
			return true
		}
	}

	private fun groupHasPrefix(groupOverrides: List<OverrideEntry>, jsonFileName: String): Boolean {
		val prefixList = groupOverrides.filter { it.predicate.hasOnlyPrefix() }
		val itemIdList = groupOverrides.filter { it.predicate.hasOnlyItemId() }

		if (itemIdList.size > 1) {
			LOGGER.debug("too many item ids for prefix! $jsonFileName")
			return false
		}

		if (groupOverrides.size != (prefixList.size + itemIdList.size)) {
			LOGGER.debug("size mismatch for prefix! $jsonFileName")
			return false
		} else {
			return true
		}
	}

	private fun groupHasSuffix(groupOverrides: List<OverrideEntry>, jsonFileName: String): Boolean {
		val suffixList = groupOverrides.filter { it.predicate.hasOnlySuffix() }
		val itemIdList = groupOverrides.filter { it.predicate.hasOnlyItemId() }

		if (itemIdList.size > 1) {
			LOGGER.debug("too many item ids for suffix! $jsonFileName")
			return false
		}

		if (groupOverrides.size != (suffixList.size + itemIdList.size)) {
			LOGGER.debug("size mismatch for suffix! $jsonFileName")
			return false
		} else {
			return true
		}
	}

	private fun groupHasMaxConsume(groupOverrides: List<OverrideEntry>, jsonFileName: String): Boolean {
		val maxConsumeList = groupOverrides.filter { it.predicate.hasOnlyMaxConsume() }
		val itemIdList = groupOverrides.filter { it.predicate.hasOnlyItemId() }

		if (itemIdList.size > 1) {
			LOGGER.debug("too many item ids for maxConsume! $jsonFileName")
			return false
		}

		if (maxConsumeList.any { it.predicate.nbt!!.maxConsume!!.startsWith(">=") }) {
			LOGGER.error("started with '>=' for maxConsume!!! $jsonFileName")
			return false
		}

		if (groupOverrides.size != (maxConsumeList.size + itemIdList.size)) {
			LOGGER.debug("size mismatch for maxConsume! $jsonFileName")
			return false
		} else {
			return true
		}
	}

	private fun groupHasItemType(groupOverrides: List<OverrideEntry>, jsonFileName: String): Boolean {
		val itemTypeList = groupOverrides.filter { it.predicate.hasOnlyItemType() }
		val itemIdList = groupOverrides.filter { it.predicate.hasOnlyItemId() }

		if (itemIdList.size > 1) {
			LOGGER.debug("too many item ids for itemType! $jsonFileName")
			return false
		}

		if (groupOverrides.size != (itemTypeList.size + itemIdList.size)) {
			LOGGER.debug("size mismatch for itemType! $jsonFileName")
			return false
		} else {
			return true
		}
	}

	private fun groupHasItemName(groupOverrides: List<OverrideEntry>, jsonFileName: String): Boolean {
		val itemNameList = groupOverrides.filter { it.predicate.hasOnlyItemName() }
		val itemIdList = groupOverrides.filter { it.predicate.hasOnlyItemId() }

		if (itemIdList.size > 1) {
			LOGGER.debug("too many item ids for itemName! $jsonFileName")
			return false
		}

		if (groupOverrides.size != (itemNameList.size + itemIdList.size)) {
			LOGGER.debug("size mismatch for itemName! $jsonFileName")
			return false
		} else {
			return true
		}
	}


	private fun handleOverridesWithOnlyItemId(itemId: String, groupOverrides: List<OverrideEntry>): BaseModel? {
		if (groupOverrides.size > 1) {
			LOGGER.info("Size should only be 1.")
			if (!groupOverrides.all { it == groupOverrides[0] }) {
				LOGGER.info("Error, item has probably unhandled keys!!! $itemId")
				return null
			}
		}

		val overrideEntry = groupOverrides[0]
		val model = SimpleModel(
			type = "minecraft:model",
			model = convertModel(overrideEntry.model)
		)

		return model
	}

	private fun handleOverridesWithPulling(itemId: String, groupOverrides: List<OverrideEntry>): BaseModel? {
		val pullingList = groupOverrides.filter { it.predicate.hasOnlyPulling() }
		val itemIdEntry = groupOverrides.firstOrNull { it.predicate.hasOnlyItemId() }
			?: return null

		val fallbackModel = SimpleModel(
			type = "minecraft:model",
			model = convertModel(itemIdEntry.model),
		)

		val basePullingOverrideEntry = pullingList.firstOrNull { it.predicate.pulling == 1 && it.predicate.pull == null }
			?: return null

		val basePullingModel = SimpleModel(
			type = "minecraft:model",
			model = convertModel(basePullingOverrideEntry.model),
		)

		val entries = mutableListOf<RangeEntry>()
		pullingList
			.filter { it.predicate.pulling == 1 && it.predicate.pull != null }
			.sortedBy { it.predicate.pull!! }
			.forEach {
				val rangeEntry = RangeEntry(
					threshold = it.predicate.pull!!,
					model = SimpleModel(
						type = "minecraft:model",
						model = convertModel(it.model),
					),
				)
				entries.add(rangeEntry)
			}

		val rangeDispatchModel = RangeDispatchModel(
			type = "minecraft:range_dispatch",
			property = "minecraft:use_duration",
			scale = 0.05,
			entries = entries,
			fallback = basePullingModel,
		)

		val conditionModel = ConditionModel(
			type = "minecraft:condition",
			property = "minecraft:using_item",
			onTrue = rangeDispatchModel,
			onFalse = fallbackModel,
		)
		return conditionModel
	}

	private fun handleOverridesWithCount(itemId: String, groupOverrides: List<OverrideEntry>): BaseModel? {
		val countList = groupOverrides.filter { it.predicate.hasOnlyCount() }
		val itemIdEntry = groupOverrides.firstOrNull { it.predicate.hasOnlyItemId() }
			?: return null

		val fallbackModel = SimpleModel(
			type = "minecraft:model",
			model = convertModel(itemIdEntry.model),
		)

		val entries = mutableListOf<RangeEntry>()
		countList
			.sortedBy { it.predicate.count!! }
			.forEach {
				val countParsedString = it.predicate.count!!.replace(">=", "")
				val countParsed: Double
				try {
					countParsed = countParsedString.toInt().toDouble()
				} catch (e: NumberFormatException) {
					LOGGER.info("failed to format count to int!")
					e.printStackTrace()
					return@forEach
				}

				val rangeEntry = RangeEntry(
					threshold = countParsed,
					model = SimpleModel(
						type = "minecraft:model",
						model = convertModel(it.model),
					),
				)
				entries.add(rangeEntry)
			}

		val rangeDispatchModel = RangeDispatchModel(
			type = "minecraft:range_dispatch",
			property = "minecraft:count",
			scale = 1.0,
			entries = entries,
			fallback = fallbackModel,
		)
		return rangeDispatchModel
	}

	private fun handleOverridesWithCast(itemId: String, groupOverrides: List<OverrideEntry>): BaseModel? {
		val castEntry = groupOverrides.firstOrNull { it.predicate.hasOnlyCast() }
			?: return null
		val itemIdEntry = groupOverrides.firstOrNull { it.predicate.hasOnlyItemId() }
			?: return null

		val fallbackModel = SimpleModel(
			type = "minecraft:model",
			model = convertModel(itemIdEntry.model),
		)

		val castModel = SimpleModel(
			type = "minecraft:model",
			model = convertModel(castEntry.model),
		)

		val conditionModel = ConditionModel(
			type = "minecraft:condition",
			property = "minecraft:fishing_rod/cast",
			onTrue = castModel,
			onFalse = fallbackModel,
		)
		return conditionModel
	}

	private fun handleOverridesWithPrefix(itemId: String, groupOverrides: List<OverrideEntry>): BaseModel? {
		val prefixList = groupOverrides.filter { it.predicate.hasOnlyPrefix() }
		val itemIdEntry = groupOverrides.firstOrNull { it.predicate.hasOnlyItemId() }
			?: return null

		if (prefixList.isEmpty()) {
			return null
		}

		val fallbackModel = SimpleModel(
			type = "minecraft:model",
			model = convertModel(itemIdEntry.model),
		)

		val prefixModel = SimpleModel(
			type = "minecraft:model",
			model = convertModel(prefixList.first().model),
		)

//		val cases = mutableListOf<SelectCase>()
//		prefixList
//			.forEach {
//				val prefixParsed = parsePrefixOrSuffix(it.predicate.nbt!!.prefix!!)
//
//				val case = SelectCase(
//					`when` = prefixParsed,
//					model = SimpleModel(
//						type = "minecraft:model",
//						model = convertModel(it.model),
//					),
//				)
//				cases.add(case)
//			}
//
//		val prefixSelectModel = SelectModel(
//			type = "minecraft:select",
//			property = "blockgame:bgi_name_pre",
//			cases = cases,
//			fallback = fallbackModel,
//		)

		val isPrefixedModel = ConditionModel(
			type = "minecraft:condition",
			property = "blockgame:bgi_has_name_pre",
			onTrue = prefixModel,
			onFalse = fallbackModel,
		)
		return isPrefixedModel
	}

	private fun handleOverridesWithSuffix(itemId: String, groupOverrides: List<OverrideEntry>): BaseModel? {
		val suffixList = groupOverrides.filter { it.predicate.hasOnlySuffix() }
		val itemIdEntry = groupOverrides.firstOrNull { it.predicate.hasOnlyItemId() }
			?: return null

		if (suffixList.isEmpty()) {
			return null
		}

		val fallbackModel = SimpleModel(
			type = "minecraft:model",
			model = convertModel(itemIdEntry.model),
		)

		val suffixModel = SimpleModel(
			type = "minecraft:model",
			model = convertModel(suffixList.first().model),
		)

//		val cases = mutableListOf<SelectCase>()
//		suffixList
//			.forEach {
//				val suffixParsed = parsePrefixOrSuffix(it.predicate.nbt!!.suffix!!)
//
//				val case = SelectCase(
//					`when` = suffixParsed,
//					model = SimpleModel(
//						type = "minecraft:model",
//						model = convertModel(it.model),
//					),
//				)
//				cases.add(case)
//			}
//
//		val suffixSelectModel = SelectModel(
//			type = "minecraft:select",
//			property = "blockgame:bgi_name_suf",
//			cases = cases,
//			fallback = fallbackModel,
//		)

		val isSuffixedModel = ConditionModel(
			type = "minecraft:condition",
			property = "blockgame:bgi_has_name_suf",
			onTrue = suffixModel,
			onFalse = fallbackModel,
		)
		return isSuffixedModel
	}

	private fun handleOverridesWithMaxConsume(itemId: String, groupOverrides: List<OverrideEntry>): BaseModel? {
		val maxConsumeList = groupOverrides.filter { it.predicate.hasOnlyMaxConsume() }
		val itemIdEntry = groupOverrides.firstOrNull { it.predicate.hasOnlyItemId() }
			?: return null

		if (maxConsumeList.isEmpty()) {
			return null
		}

		val fallbackModel: BaseModel

		val entries = mutableListOf<RangeEntry>()

		if (maxConsumeList.none { it.predicate.nbt!!.maxConsume!!.startsWith("<=") } && maxConsumeList.none { it.predicate.nbt!!.maxConsume!!.startsWith(">=") }) {
			// handle as: '>='

			fallbackModel = SimpleModel(
				type = "minecraft:model",
				model = convertModel(itemIdEntry.model),
			)

			maxConsumeList
				.sortedBy { it.predicate.nbt!!.maxConsume!! }
				.forEachIndexed { index, overrideEntry ->
					val countParsedString = overrideEntry.predicate.nbt!!.maxConsume!!
					val countParsed: Double
					try {
						countParsed = countParsedString.toInt().toDouble()
					} catch (e: NumberFormatException) {
						LOGGER.info("failed to format count to int! in fun handleOverridesWithMaxConsume()!")
						e.printStackTrace()
						return@forEachIndexed
					}

					val rangeEntry = RangeEntry(
						threshold = countParsed,
						model = SimpleModel(
							type = "minecraft:model",
							model = convertModel(overrideEntry.model),
						),
					)
					entries.add(rangeEntry)
				}

		} else if (maxConsumeList.all { it.predicate.nbt!!.maxConsume!!.startsWith("<=") }) {
			// handle: '<='

			fallbackModel = SimpleModel(
				type = "minecraft:model",
				model = convertModel(maxConsumeList.first().model),
			)

			maxConsumeList
				.sortedBy { it.predicate.nbt!!.maxConsume!! }
				.forEachIndexed { index, overrideEntry ->
					val countParsedString = overrideEntry.predicate.nbt!!.maxConsume!!.replace("<=", "")
					val countParsed: Double
					try {
						countParsed = countParsedString.toInt().toDouble() + 1
					} catch (e: NumberFormatException) {
						LOGGER.info("failed to format count to int! in fun handleOverridesWithMaxConsume()!")
						e.printStackTrace()
						return@forEachIndexed
					}

					val nextModelName = if (maxConsumeList.size < index + 1 + 1) {
						itemIdEntry.model
					} else {
						maxConsumeList[index + 1].model
					}

					val rangeEntry = RangeEntry(
						threshold = countParsed,
						model = SimpleModel(
							type = "minecraft:model",
							model = convertModel(nextModelName),
						),
					)
					entries.add(rangeEntry)
				}
		} else {
			LOGGER.warn("couldn't handle this for maxConsumeList.")
			return null
		}

		val rangeDispatchModel = RangeDispatchModel(
			type = "minecraft:range_dispatch",
			property = "blockgame:bgi_max_consume",
			scale = 1.0,
			entries = entries,
			fallback = fallbackModel,
		)
		return rangeDispatchModel
	}

	private fun handleOverridesWithItemType(itemId: String, groupOverrides: List<OverrideEntry>): BaseModel? {
		val itemTypeList = groupOverrides.filter { it.predicate.hasOnlyItemType() }
		val itemIdEntry = groupOverrides.firstOrNull { it.predicate.hasOnlyItemId() }
			?: return null

		val fallbackModel = SimpleModel(
			type = "minecraft:model",
			model = convertModel(itemIdEntry.model),
		)

		val cases = mutableListOf<SelectCase>()
		itemTypeList
			.forEach {
				val itemTypeParsed = parsePrefixOrSuffix(it.predicate.nbt!!.itemType!!)

				val case = SelectCase(
					`when` = itemTypeParsed,
					model = SimpleModel(
						type = "minecraft:model",
						model = convertModel(it.model),
					),
				)
				cases.add(case)
			}

		val prefixSelectModel = SelectModel(
			type = "minecraft:select",
			property = "blockgame:bgi_item_type",
			cases = cases,
			fallback = fallbackModel,
		)
		return prefixSelectModel
	}

	private fun handleOverridesWithItemName(itemId: String, groupOverrides: List<OverrideEntry>): BaseModel? {
		val itemNameList = groupOverrides.filter { it.predicate.hasOnlyItemName() }
		val itemIdEntry = groupOverrides.firstOrNull { it.predicate.hasOnlyItemId() }
			?: return null

		val fallbackModel = SimpleModel(
			type = "minecraft:model",
			model = convertModel(itemIdEntry.model),
		)

		val cases = mutableListOf<SelectCase>()
		itemNameList
			.forEach {
				val itemNameParsed = parsePrefixOrSuffix(it.predicate.nbt!!.itemName!!)

				val case = SelectCase(
					`when` = itemNameParsed,
					model = SimpleModel(
						type = "minecraft:model",
						model = convertModel(it.model),
					),
				)
				cases.add(case)
			}

		val prefixSelectModel = SelectModel(
			type = "minecraft:select",
			property = "blockgame:bgi_name",
			cases = cases,
			fallback = fallbackModel,
		)
		return prefixSelectModel
	}


	fun convertPredicates2NewFormat(predicateJsonData: PredicateJsonData, jsonFileName: String): Map<String, NewFormatJsonData> {
//		val rootCases: MutableList<SelectCase> = mutableListOf()

//		val fallbackModelName = defaultModels[jsonFileName]
//			?: run {
//				LOGGER.warn("couldn't find default fallback model for $jsonFileName!")
//				"minecraft:item/barrier"
//			}
//
//		val fallbackModel = SimpleModel(
//			type = "minecraft:model",
//			model = convertModel(fallbackModelName)
//		)

		val grouped = predicateJsonData.overrides
			.filter { it.predicate.nbt?.itemId != null }
			.groupBy { it.predicate.nbt?.itemId!! }

		val ungrouped = predicateJsonData.overrides
			.filter { it.predicate.nbt == null }


		fun forEachGrouped(groups2: Map<String, List<OverrideEntry>>): MutableMap<String, BaseModel> {
			val resultingModels = mutableMapOf<String, BaseModel>()

			val groups = mutableMapOf<String, List<OverrideEntry>>()

			groups2.forEach { (itemId, groupOverrides) ->
				if (itemId.startsWith("/") && itemId.endsWith("/")) {
					val regex = itemId.substring(1, itemId.length - 1).toRegex()
					val itemIds = getItemIdsThatMatchRegex(regex)

					LOGGER.warn("=".repeat(10))
					LOGGER.warn("regex -> $regex")
					LOGGER.warn("itemIds -> $itemIds")
					LOGGER.warn("=".repeat(10))

					if (itemIds.size == 1) {
						groups[itemIds[0]] = groupOverrides
					} else {
						itemIds.forEach {
							groups[it] = groupOverrides
						}
					}
				} else {
					groups[itemId] = groupOverrides
				}
			}

			groups.forEach grouped@{ (itemId, groupOverrides) ->
				if (groupOverrides.isEmpty()) {
					LOGGER.info("groupOverrides is empty! $itemId")
					return@grouped
				}

				when {

					groupHasOnlyItemId(groupOverrides) -> {
						val model = handleOverridesWithOnlyItemId(itemId, groupOverrides)
							?: return@grouped
						resultingModels[itemId] = model
					}

					groupHasPulling(groupOverrides, jsonFileName) -> {
						val model = handleOverridesWithPulling(itemId, groupOverrides)
							?: return@grouped
						resultingModels[itemId] = model
					}

					groupHasCount(groupOverrides, jsonFileName) -> {
						val model = handleOverridesWithCount(itemId, groupOverrides)
							?: return@grouped
						resultingModels[itemId] = model
					}

					groupHasCast(groupOverrides, jsonFileName) -> {
						val model = handleOverridesWithCast(itemId, groupOverrides)
							?: return@grouped
						resultingModels[itemId] = model
					}

					groupHasPrefix(groupOverrides, jsonFileName) -> {
						val model = handleOverridesWithPrefix(itemId, groupOverrides)
							?: return@grouped
						resultingModels[itemId] = model
					}

					groupHasSuffix(groupOverrides, jsonFileName) -> {
						val model = handleOverridesWithSuffix(itemId, groupOverrides)
							?: return@grouped
						resultingModels[itemId] = model
					}

					groupHasMaxConsume(groupOverrides, jsonFileName) -> {
						val model = handleOverridesWithMaxConsume(itemId, groupOverrides)
							?: return@grouped
						resultingModels[itemId] = model
					}

					groupHasItemType(groupOverrides, jsonFileName) -> {
						val model = handleOverridesWithItemType(itemId, groupOverrides)
							?: return@grouped
						resultingModels[itemId] = model
					}

					groupHasItemName(groupOverrides, jsonFileName) -> {
						val model = handleOverridesWithItemName(itemId, groupOverrides)
							?: return@grouped
						resultingModels[itemId] = model
					}

					else -> {
						LOGGER.info("\t\t> trying inner grouping! $jsonFileName, $itemId")

						val innerCases: MutableList<SelectCase> = mutableListOf()
						val innerFallbackModel: BaseModel?

						val innerGroups: Map<String, List<OverrideEntry>>
						val innerLeftover: Map<String, List<OverrideEntry>>
						val groupingKey: String

						when {

							groupOverrides.any { it.predicate.hasPrefix() } -> {
								groupingKey = "blockgame:bgi_name_pre"

								innerGroups = groupOverrides
									.filter { it.predicate.hasPrefix() }
									.groupBy { parsePrefixOrSuffix(it.predicate.nbt!!.prefix!!) }
									.mapValues { (_, values) -> values.map { it.copy(predicate = it.predicate.erasePrefix()) } }

								val innerLeftoverList = groupOverrides
									.filter { !it.predicate.hasPrefix() }
								innerLeftover = mutableMapOf(itemId to innerLeftoverList)
							}

							groupOverrides.any { it.predicate.hasItemType() } -> {
								groupingKey = "blockgame:bgi_item_type"

								innerGroups = groupOverrides
									.filter { it.predicate.hasItemType() }
									.groupBy { parsePrefixOrSuffix(it.predicate.nbt!!.itemType!!) }
									.mapValues { (_, values) -> values.map { it.copy(predicate = it.predicate.eraseItemType()) } }

								val innerLeftoverList = groupOverrides
									.filter { !it.predicate.hasItemType() }
								innerLeftover = mutableMapOf(itemId to innerLeftoverList)
							}

							groupOverrides.any { it.predicate.hasItemName() } -> {
								groupingKey = "blockgame:bgi_name"

								innerGroups = groupOverrides
									.filter { it.predicate.hasItemName() }
									.groupBy { parsePrefixOrSuffix(it.predicate.nbt!!.itemName!!) }
									.mapValues { (_, values) -> values.map { it.copy(predicate = it.predicate.eraseItemName()) } }

								val innerLeftoverList = groupOverrides
									.filter { !it.predicate.hasItemName() }
								innerLeftover = mutableMapOf(itemId to innerLeftoverList)
							}

							else -> {
								LOGGER.info("couldn't handle this inner grouping case! $jsonFileName, $itemId, $groupOverrides")
								return@grouped
							}

						}

						if (innerGroups[itemId] == groupOverrides) {
							LOGGER.info("recursion in inner grouping! $jsonFileName, $itemId, $groupOverrides")
							return@grouped
						}

						val resultingInnerGroupModels = forEachGrouped(innerGroups)
						resultingInnerGroupModels.forEach { (itemId, model) ->
							innerCases.add(SelectCase(
								`when` = itemId,
								model = model,
							))
						}

						val resultingInnerLeftoverModels = forEachGrouped(innerLeftover)
						innerFallbackModel = resultingInnerLeftoverModels.entries.firstOrNull()?.value
//							?: fallbackModel

						val innerModel = SelectModel(
							type = "minecraft:select",
							property = groupingKey,
							cases = innerCases,
							fallback = innerFallbackModel,
						)
						resultingModels[itemId] = innerModel
					}

				}

			}

			return resultingModels
		}

		val resultingModels = forEachGrouped(grouped)
		return resultingModels.mapValues { NewFormatJsonData(model = it.value) }

//		resultingModels.forEach { (itemId, model) ->
//			rootCases.add(SelectCase(
//				`when` = itemId,
//				model = model,
//			))
//		}
//
//		val newFormatJsonData = NewFormatJsonData(
//			model = SelectModel(
//				type = "minecraft:select",
//				property = "blockgame:bgi_item_id",
//				cases = rootCases,
//				fallback = fallbackModel,
//			)
//		)
//
//		return newFormatJsonData
	}


//	fun convertPredicateTextures2NewFormat(predicateJsonData: PredicateTextureJsonData, jsonFileName: String): Map<String, NewFormatJsonData> {
//
//
//		predicateJsonData.overrides.forEach { overrideTextureEntry ->
//			val predicate = overrideTextureEntry.predicate
//			val nbt = predicate.nbt
//				?: return@forEach
//			val texture = overrideTextureEntry.texture
//
//			val itemId = nbt.itemId
//			val itemSet = nbt.itemSet
//			val hasPrefixOrSuffix = nbt.prefix != null || nbt.suffix != null
//
//			val fileName = jsonFileName.replace(".json", "")
//
//
//			val equipmentType = if (fileName.endsWith("layer_1") || fileName.endsWith("layer_1_overlay")) {
//				"humanoid"
//			} else if (fileName.endsWith("layer_2") || fileName.endsWith("layer_2_overlay")) {
//				"humanoid_leggings"
//			} else {
//				return@forEach
//			}
//
//
//			if (itemId != null) {
//				if (itemId.startsWith("/") && itemId.endsWith("/")) {
//					val regex = itemId.substring(1, itemId.length - 1).toRegex()
//					val itemDataList = ItemData.registry.filterKeys { regex.matches(it) }
//
//				} else {
//					val itemDataList = ItemData.registry.filterKeys { it == itemId }
//
//				}
//			} else if (itemSet != null) {
//				val itemDataList = ItemData.registry.filterValues { it.set == itemSet || it.set == "SET_$itemSet" }
//
//
//
//			}
//
//			ItemData.registry.values.first().itemType
//
//			var SET: String? = null // cactus
//
//			if (itemSet != null) {
//				SET = itemSet.replace("SET_", "").lowercase()
//			}
//
//			if (fileName.endsWith("_overlay")) {
//				if (hasPrefixOrSuffix) {
//					val newDir = "assets/blockgame/textures/entity/equipment/$equipmentType/warrior/${SET}_overlay.png"
//				} else {
//					val newDir = "assets/blockgame/textures/entity/equipment/$equipmentType/warrior/special/${SET}_overlay.png"
//				}
//			} else {
//				if (hasPrefixOrSuffix) {
//					val newDir = "assets/blockgame/textures/entity/equipment/$equipmentType/warrior/cactus/${SET}.png"
//				} else {
//					val newDir = "assets/blockgame/textures/entity/equipment/$equipmentType/warrior/special/cactus/${SET}.png"
//				}
//			}
//
//
//
//
//
//		}
//
//
//
//	}

}
