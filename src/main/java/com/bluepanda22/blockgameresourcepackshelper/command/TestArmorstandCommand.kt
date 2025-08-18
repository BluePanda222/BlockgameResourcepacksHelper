package com.bluepanda22.blockgameresourcepackshelper.command

import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain
import com.bluepanda22.blockgameresourcepackshelper.data.ItemData
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.block.Blocks
import net.minecraft.block.WallSignBlock
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.block.entity.SignText
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.DyedColorComponent
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.equipment.trim.ArmorTrimMaterials
import net.minecraft.text.Text
import net.minecraft.util.BlockRotation
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box

object TestArmorstandCommand {

//	init {
//
//		ClientCommandRegistrationCallback.EVENT.register(
//			ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess? ->
//				dispatcher.register(
//					ClientCommandManager.literal("testarmorstand")
//						.executes { context: CommandContext<FabricClientCommandSource> ->
//							if (!MinecraftClient.getInstance().isInSingleplayer) {
//								context.source.sendFeedback(Text.literal("Only works in singleplayer!"))
//								return@executes Command.SINGLE_SUCCESS
//							}
////							val world = context.source.player.world
//							val world = MinecraftClient.getInstance().server?.overworld
//								?: return@executes Command.SINGLE_SUCCESS
//
//							val playerBlockPos = context.source.player.blockPos
//
//							fun setBlock(blockPos: BlockPos) {
//								world.setBlockState(blockPos, Blocks.SMOOTH_STONE.defaultState)
//							}
//							fun setSign(blockPos: BlockPos, text: Text) {
//								val sign = Blocks.BIRCH_WALL_SIGN as WallSignBlock
//								val signBlock = sign.createBlockEntity(blockPos, sign.defaultState.rotate(BlockRotation.COUNTERCLOCKWISE_90)) as SignBlockEntity
//								world.setBlockState(blockPos, signBlock.cachedState)
//								world.addBlockEntity(signBlock)
//								val signText = SignText().withColor(DyeColor.LIGHT_BLUE).withGlowing(true).withMessage(1, text)
//								signBlock.setText(signText, true)
//								signBlock.setWaxed(true)
//							}
//							fun spawnArmorStand(blockPos: BlockPos, set: String, name: String, isSpecial: Boolean = false) {
//								val armorStandEntity = ArmorStandEntity(world, blockPos.x.toDouble().plus(0.5) , blockPos.y.toDouble(), blockPos.z.toDouble().plus(0.5))
//								armorStandEntity.yaw = 90.0f
//								if (isSpecial) {
//									armorStandEntity.yaw = -90.0f
//								}
//								armorStandEntity.setNoGravity(true)
//								world.spawnEntity(armorStandEntity)
//
//								val armors = ItemData.registry
//									.filterValues { it.itemType == "ARMOR" }
//									.filterValues { it.set == set }
//									.filterKeys { it.contains(name.uppercase()) }
//									.mapKeys { BlockgameResourcepacksHelperMain.makeBlockgameItem(it.key, isSpecial, false) }
//
//								for (entry in armors) {
//									val eq = entry.key.get(DataComponentTypes.EQUIPPABLE)
//										?: return
////									entry.value.remove(DataComponentTypes.DYED_COLOR)
//									armorStandEntity.equipStack(eq.slot(), entry.key.copyWithCount(1))
////									armorStandEntity.equipStack(eq.slot(), entry.value)
//								}
////								armorStandEntity.customName = Text.literal(name)
////								armorStandEntity.isCustomNameVisible = true
//							}
//
//							fun fill6(blockPos: BlockPos) {
//								for (i in 0 .. 5) {
//									for (j in 0 .. 1) {
//										setBlock(blockPos.add(j, 0, i))
//									}
//								}
//							}
//							fun fill6ArmorStands(blockPos: BlockPos, set: String, names: List<String>) {
//								for (j in 0 .. 5) {
//									spawnArmorStand(blockPos.add(0, 1, j), set, names[j])
//									spawnArmorStand(blockPos.add(1, 1, j), set, names[j], true)
//								}
//							}
//							fun fill5(blockPos: BlockPos) {
//								for (i in 0 .. 4) {
//									for (j in 0 .. 1) {
//										setBlock(blockPos.add(j, 0, i))
//									}
//								}
//							}
//							fun fill5ArmorStands(blockPos: BlockPos, set: String, names: List<String>) {
//								for (j in 0 .. 4) {
//									spawnArmorStand(blockPos.add(0, 1, j), set, names[j])
//									spawnArmorStand(blockPos.add(1, 1, j), set, names[j], true)
//								}
//							}
//							fun clearArea(origin: BlockPos) {
//								val p1 = origin.add(-1, 0, 0)
//								val p2 = p1.add(2, 16, 19)
//
//								val armorStands = world.getEntitiesByClass(ArmorStandEntity::class.java, Box.enclosing(p1, p2.add(0, 2, 0)), ArmorStandEntity::isAlive)
//								for (armorStand in armorStands) {
//									armorStand.kill(world)
//								}
//
//								val box = Box.enclosing(p1, p2)
//								for (x in box.minX.toInt() .. box.maxX.toInt()) {
//									for (z in box.minZ.toInt() .. box.maxZ.toInt()) {
//										for (y in box.minY.toInt() .. box.maxY.toInt()) {
//											world.breakBlock(BlockPos(x, y, z), false)
//										}
//									}
//								}
//							}
//
//							val origin = playerBlockPos.add(3, 0, 0)
//							clearArea(origin)
//
//							setBlock(origin)
//							setSign(origin.add(-1, 0, 0), Text.literal("Onion"))
//							spawnArmorStand(origin.add(0, 1, 0), "ONION", "ONION")
//
//							val combatOrigin = origin.add(0, 0, 2)
//							val combatSigns = listOf("Ranger", "Guardian", "Warrior", "Wizard", "Thaumaturge")
//							val combatSets = listOf("SET_RANGER", "SET_GUARDIAN", "SET_WARRIOR", "SET_WIZARD", "SET_THAUMATURGE")
//							val combatNames = listOf(
//								listOf("SHOTCALLER", "RANGER", "SHARPSHOOTER", "BLOODSNOUT_RANGER", "SATET", "MARKSMAN"),
//								listOf("WOODEN", "COPPER", "STEEL", "BLOODSNOUT_BRUTE", "MAAHES", "GUARDIAN"),
//								listOf("CACTUS", "STONE", "MYTHRIL", "BLOODSNOUT_SLAYER", "MONTU", "WARRIORS"),
//								listOf("SILK", "SPARKING_SILK", "ACOLYTE", "BLOODSNOUT_MAGI", "DJEDI", "WIZARDS"),
//								listOf("WOOL", "BLESSED_WOOL", "SANCTIFIED", "BLOODSNOUT_HEALER", "HEKA", "THAUMATURGE"),
//							)
//							for (i in 0 .. 4) {
//								val pos = combatOrigin.add(0, i * 4, 0)
//								fill6(pos)
//								fill6ArmorStands(pos, combatSets[i], combatNames[i])
//								setSign(pos.add(-1, 0, 0), Text.literal(combatSigns[i]))
//							}
//
//							val professionOrigin = combatOrigin.add(0, 0, 7)
//							val professionSigns = listOf("Archaeologist", "Botanist", "Fisherman", "Lumberjack", "Miner")
//							val professionSets = listOf("SET_ARCHAEOLOGIST", "SET_BOTANIST", "SET_FISHERMAN", "SET_LUMBERJACK", "SET_MINER")
//							val professionNames = listOf(
//								listOf("_1", "_2", "_3", "_4", "_5"),
//								listOf("_1", "_2", "_3", "_4", "_5"),
//								listOf("_1", "_2", "_3", "_4", "_5"),
//								listOf("_1", "_2", "_3", "_4", "_5"),
//								listOf("_1", "_2", "_3", "_4", "_5"),
//							)
//							for (i in 0 .. 4) {
//								val pos = professionOrigin.add(0, i * 4, 0)
//								fill5(pos)
//								fill5ArmorStands(pos, professionSets[i], professionNames[i])
//								setSign(pos.add(-1, 0, 0), Text.literal(professionSigns[i]))
//							}
//
//							val craftingOrigin = professionOrigin.add(0, 0, 6)
//							val craftingSigns = listOf("Alchemist", "Chef", "Runecarver", "Hunter")
//							val craftingSets = listOf("SET_ALCHEMIST", "SET_CHEF", "SET_RUNECARVER", "SET_HUNTER")
//							val craftingNames = listOf(
//								listOf("_1", "_2", "_3", "_4", "_5"),
//								listOf("_1", "_2", "_3", "_4", "_5"),
//								listOf("_1", "_2", "_3", "_4", "_5"),
//								listOf("_1", "_2", "_3", "_4", "_5"),
//							)
//							for (i in 0 .. 3) {
//								val pos = craftingOrigin.add(0, i * 4, 0)
//								fill5(pos)
//								fill5ArmorStands(pos, craftingSets[i], craftingNames[i])
//								setSign(pos.add(-1, 0, 0), Text.literal(craftingSigns[i]))
//							}
//
//							Command.SINGLE_SUCCESS
//						}
//				)
//			}
//		)
//
//	}

}
