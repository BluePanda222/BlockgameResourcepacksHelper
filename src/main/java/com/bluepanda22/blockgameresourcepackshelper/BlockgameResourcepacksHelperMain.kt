package com.bluepanda22.blockgameresourcepackshelper

import com.bluepanda22.blockgameresourcepackshelper.data.ItemData
import com.bluepanda22.blockgameresourcepackshelper.data.ItemIds
import com.bluepanda22.blockgameresourcepackshelper.data.ItemIds.registry
import com.bluepanda22.blockgameresourcepackshelper.resourcepack.ResourcepackPropertiesInitializer
import com.bluepanda22.blockgameresourcepackshelper.util.DirectoryUtil
import dev.bnjc.bglib.BGIField
import dev.bnjc.bglib.BGIWriter
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.ModifyEntries
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.equipment.EquipmentAsset
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.resource.ResourceReloader.Synchronizer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Consumer

class BlockgameResourcepacksHelperMain : ClientModInitializer, ResourceReloader {

	companion object {

		const val MOD_ID: String = "blockgameresourcepackshelper"

		lateinit var INSTANCE: BlockgameResourcepacksHelperMain

		@JvmField
		val LOGGER: Logger = LoggerFactory.getLogger("BlockgameResourcepacksHelper")

		@JvmField
		val LOADED_ITEM_IDS: MutableList<Identifier> = mutableListOf()


		private val BLOCKGAME_ITEMS: RegistryKey<ItemGroup> = RegistryKey.of<ItemGroup>(
			Registries.ITEM_GROUP.getKey(), Identifier.of(MOD_ID, "blockgame_items")
		)
		private val BLOCKGAME_ITEMS_GROUP: ItemGroup = FabricItemGroup.builder()
			.icon { makeBlockgameItem("GOLDEN_POTATO") }
			.displayName(Text.literal("Blockgame"))
			.build()

		private fun makeBlockgameItem(
			blockgameItemId: String,
		): ItemStack {
			val stack = ItemStack(Items.STICK)
			val writer = BGIWriter(1.toShort())

			stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(blockgameItemId))

			val itemId = Identifier.of("blockgame", blockgameItemId.lowercase())
			if (LOADED_ITEM_IDS.contains(itemId)) {
				stack.set(DataComponentTypes.ITEM_MODEL, itemId)
			}

			writer.addString(BGIField.ITEM_ID.name, blockgameItemId)

			writer.writeToCustomData(stack)
			return stack
		}


		// === OLD ===

		val convertableResourcepacks: MutableList<String> = mutableListOf<String>(
//			"Better-Blockgame-v0.4.2.zip",
//			"Cook's Additions - v0.5 Blockgame 1.20.2.zip",
//			"MineBlockCraftGame-Alpha2.zip",
//			"UnOfficial BlockGame Textures - 1.2.1.zip",
//			"GruMounts.zip",
//			"GruTatoes.zip",
		)

		@JvmField
		var MISSING_EQUIPMENT_ASSET_IDS: MutableSet<RegistryKey<EquipmentAsset>> = HashSet<RegistryKey<EquipmentAsset>>()
		@JvmField
		var MISSING_TEXTURE_IDS: MutableSet<Identifier> = HashSet<Identifier>()

		private fun makeBlockgameItemOld(
			blockgameItemId: String,
			isSpecial: Boolean = false,
			applyPatterns: Boolean = true
		): ItemStack {
			val stack = ItemStack(Items.STICK)
			val writer = BGIWriter(1.toShort())

//        var itemData = ItemData.Companion.getRegistry().get(blockgameItemId);
//        if (itemData != null) {
//            stack = new ItemStack(Registries.ITEM.get(Identifier.of(itemData.getMcItemId())));
//
//            if (itemData.getColor() != null) {
//                stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(itemData.getColor(), true));
//            }
//
//            if (applyPatterns && itemData.getMaterial() != null && itemData.getPattern() != null) {
//                var player = MinecraftClient.getInstance().player;
//                if (player != null) {
//                    var materialRegistry = player.getRegistryManager().getOrThrow(RegistryKeys.TRIM_MATERIAL);
//                    var patternRegistry = player.getRegistryManager().getOrThrow(RegistryKeys.TRIM_PATTERN);
//                    var material = materialRegistry.getEntry(Identifier.of(itemData.getMaterial())).orElseThrow();
//                    var pattern = patternRegistry.getEntry(Identifier.of(itemData.getPattern())).orElseThrow();
//                    stack.set(DataComponentTypes.TRIM, new ArmorTrim(material, pattern));
//                }
//            }
//
//            if (isSpecial) {
//                writer.addString(BGIField.NAME_PRE.name(), "TEST");
//                writer.addString(BGIField.NAME_SUF.name(), "TEST");
//            }
//
//            writer.addString(BGIField.ITEM_TYPE.name(), itemData.getItemType());
//            writer.addString(BGIField.SET.name(), itemData.getSet());
//        }

			stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(blockgameItemId))
			stack.set(
				DataComponentTypes.ITEM_MODEL,
				Identifier.of("blockgame", blockgameItemId.lowercase())
			)

			writer.addString(BGIField.ITEM_ID.name, blockgameItemId)

			writer.writeToCustomData(stack)
			return stack
		}

		lateinit var packsDir: Path
	}

	override fun onInitializeClient() {
		INSTANCE = this

		registerBuiltinResourcePack()
		ResourcepackPropertiesInitializer.initResourcepackProperties()

		DirectoryUtil 	// Init DirectoryUtil
		ItemIds 		// Init ItemIds
		ItemData 		// Init ItemData

		initCommands()
		initItemGroup()

		LOGGER.info("BlockgameResourcepacksHelper loaded!")
	}

	private fun initItemGroup() {
		Registry.register(Registries.ITEM_GROUP, BLOCKGAME_ITEMS, BLOCKGAME_ITEMS_GROUP)
		ItemGroupEvents.modifyEntriesEvent(BLOCKGAME_ITEMS)
			.register(ModifyEntries { itemGroup: FabricItemGroupEntries ->
				registry.forEach(
					Consumer { blockgameItemId: String ->
						itemGroup.add(
							makeBlockgameItem(blockgameItemId),
							ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS
						)
					})
			})
	}

	private fun initCommands() {
//		TestCommand				// Init TestCommand
//		TestArmorstandCommand	// Init TestArmorstandCommand
	}

	private fun registerBuiltinResourcePack() {
//        FabricLoader.getInstance().getModContainer("blockgameresourcepackshelper").ifPresent((container) -> {
//            ResourceManagerHelper.registerBuiltinResourcePack(
//                Identifier.of("blockgameresourcepackshelper", "blockgameresourcepackshelper"),
//                container,
//                Text.literal("Blockgame Resourcepacks Helper"),
//                ResourcePackActivationType.ALWAYS_ENABLED
//            );
//        });
	}

	override fun reload(
		synchronizer: Synchronizer,
		manager: ResourceManager,
		prepareExecutor: Executor,
		applyExecutor: Executor
	): CompletableFuture<Void> {
		return ReloadHelper.reload(synchronizer, manager, prepareExecutor, applyExecutor)
	}

}
