package com.bluepanda22.blockgameresourcepackshelper.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.bnjc.bglib.BGIField
import dev.bnjc.bglib.BGIParser
import dev.bnjc.bglib.BGIType
import dev.bnjc.bglib.BGIWriter
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.CommandSource
import net.minecraft.text.Text
import java.util.concurrent.CompletableFuture
import kotlin.jvm.optionals.getOrDefault

object TestCommand {

//	class BGIFieldSuggestionProvider : SuggestionProvider<FabricClientCommandSource> {
//		@Throws(CommandSyntaxException::class)
//		override fun getSuggestions(
//			context: CommandContext<FabricClientCommandSource>,
//			builder: SuggestionsBuilder
//		): CompletableFuture<Suggestions> {
//			return CommandSource.suggestMatching(BGIField.entries
//				.map { it.name }, builder
//			)
//		}
//	}
//
//	init {
//
//		ClientCommandRegistrationCallback.EVENT.register(
//			ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess? ->
//				dispatcher.register(
//					ClientCommandManager.literal("bgi")
//						.then(ClientCommandManager.argument("field type", StringArgumentType.word())
//							.suggests(BGIFieldSuggestionProvider())
//							.executes { context: CommandContext<FabricClientCommandSource> ->
//								val fieldType = StringArgumentType.getString(context, "field type")
//
//								val mainHandStack = context.source.player.inventory.mainHandStack
//								if (!mainHandStack.isEmpty) {
//									val result = BGIParser.parse(mainHandStack)
//									result.ifSuccess { data ->
//										val attribute = data.getAttribute(fieldType)
//										var attributeData = attribute.getOrDefault("not set")
//										if (attributeData is Array<*>) {
//											attributeData = attributeData.joinToString(",")
//										}
//										context.source.sendFeedback(Text.literal("item in main hand has $fieldType: $attributeData"))
//									}
//								}
//
//								Command.SINGLE_SUCCESS
//							}
//							.then(
//								ClientCommandManager.argument("field value", StringArgumentType.greedyString())
//									.executes { context: CommandContext<FabricClientCommandSource> ->
//										val fieldType = StringArgumentType.getString(context, "field type")
//										val fieldValue = StringArgumentType.getString(context, "field value")
//
//										val mainHandStack = context.source.player.inventory.mainHandStack
//										if (!mainHandStack.isEmpty) {
//											val result = BGIParser.parse(mainHandStack)
//											result.ifSuccess { data ->
//												val writer = BGIWriter(1)
//												BGIField.entries.forEach { bgiField ->
//													when (bgiField.type) {
//														BGIType.INTEGER -> {
//															val attribute = data.getInt(bgiField)
//															if (attribute.isPresent) {
//																writer.addInt(bgiField.name, attribute.get())
//															}
//														}
//														BGIType.STRING -> {
//															val attribute = data.getString(bgiField)
//															if (attribute.isPresent) {
//																writer.addString(bgiField.name, attribute.get())
//															}
//														}
//														BGIType.STRING_ARRAY -> {
//															val attribute = data.getStringArray(bgiField)
//															if (attribute.isPresent) {
//																writer.addStringArray(bgiField.name, attribute.get())
//															}
//														}
//														BGIType.DOUBLE -> {
//															val attribute = data.getDouble(bgiField)
//															if (attribute.isPresent) {
//																writer.addDouble(bgiField.name, attribute.get())
//															}
//														}
//														BGIType.STREAM -> {
//															val attribute = data.getStream(bgiField)
//															if (attribute.isPresent) {
//																writer.addStream(bgiField.name, attribute.get())
//															}
//														}
//														BGIType.BOOLEAN -> {
//															val attribute = data.getBoolean(bgiField)
//															if (attribute.isPresent) {
//																writer.addBoolean(bgiField.name, attribute.get())
//															}
//														}
//														else -> {
//															// nothing
//														}
//													}
//												}
//
//												val field = BGIField.valueOf(fieldType.uppercase())
//												when (field.type) {
//													BGIType.INTEGER -> {
//														writer.addInt(field.name, fieldValue.toInt())
//													}
//													BGIType.STRING -> {
//														writer.addString(field.name, fieldValue.toString())
//													}
//													BGIType.STRING_ARRAY -> {
//														writer.addStringArray(field.name, fieldValue.split(", ").toTypedArray())
//													}
//													BGIType.DOUBLE -> {
//														writer.addDouble(field.name, fieldValue.toDouble())
//													}
//													BGIType.STREAM -> {
//														writer.addStream(field.name, fieldValue.encodeToByteArray())
//													}
//													BGIType.BOOLEAN -> {
//														writer.addBoolean(field.name, fieldValue.toBoolean())
//													}
//													else -> {
//														// nothing
//													}
//												}
//												writer.writeToCustomData(mainHandStack)
//											}.ifError {
//												BGIWriter(1)
//													.addString(fieldType.uppercase(), fieldValue)
//													.writeToCustomData(mainHandStack)
//											}
//										}
//
//										context.source.sendFeedback(Text.literal("written $fieldType of $fieldValue onto item in main hand!"))
//										Command.SINGLE_SUCCESS
//									}
//							)
//						)
//				)
//			}
//		)
//
//	}

}