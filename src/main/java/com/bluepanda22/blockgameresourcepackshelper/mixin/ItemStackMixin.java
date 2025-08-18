package com.bluepanda22.blockgameresourcepackshelper.mixin;

import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain;
import dev.bnjc.bglib.BGIField;
import dev.bnjc.bglib.BGIParser;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static dev.bnjc.bglib.BGIParser.BGI_TAG;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Mutable
    @Shadow @Final
    MergedComponentMap components;

    @Inject(
        method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/MergedComponentMap;)V",
        at = @At(
            value = "TAIL"
        )
    )
    public void init(ItemConvertible item, int count, MergedComponentMap components, CallbackInfo ci) {
        NbtCompound nbtCompound = this.components.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        if (nbtCompound != null && nbtCompound.contains(BGI_TAG, NbtElement.BYTE_ARRAY_TYPE)) {
            var parseResult = BGIParser.parse(nbtCompound.getByteArray(BGI_TAG));
            if (parseResult.isSuccess() && parseResult.result().isPresent()) {
                var data = parseResult.result().get();

                var blockgameItemIdOptional = data.getString(BGIField.ITEM_ID);
                if (blockgameItemIdOptional.isPresent()) {
                    var blockgameItemId = blockgameItemIdOptional.get();
                    var itemId = Identifier.of("blockgame", blockgameItemId.toLowerCase());

                    if (BlockgameResourcepacksHelperMain.LOADED_ITEM_IDS.contains(itemId)) {
                        this.components.set(DataComponentTypes.ITEM_MODEL, itemId);
                    }
                }

            }
        }
    }

    @Inject(
        method = "getTooltip",
        at = @At(
            value = "TAIL"
        )
    )
    public void getTooltip(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        if (type.isAdvanced()) {
            NbtCompound nbtCompound = components.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
            if (nbtCompound != null && nbtCompound.contains(BGI_TAG, NbtElement.BYTE_ARRAY_TYPE)) {
                var parseResult = BGIParser.parse(nbtCompound.getByteArray(BGI_TAG));
                if (parseResult.isSuccess() && parseResult.result().isPresent()) {
                    var data = parseResult.result().get();

                    var itemId = data.getString(BGIField.ITEM_ID);
                    if (itemId.isPresent()) {
                        var list = cir.getReturnValue();
                        list.add(list.size() - 1, Text.literal("blockgame:" + itemId.get().toLowerCase()).formatted(Formatting.DARK_GRAY));
//                        cir.setReturnValue(list);
                    }
                }
            }
        }
    }


/*

                    var set = data.getString(BGIField.SET);
                    var itemType = data.getString(BGIField.ITEM_TYPE);
                    if (set.isPresent() && itemType.isPresent() && itemType.get().equals("ARMOR")) {

////                        if (!this.components.contains(DataComponentTypes.TRIM)) {
////                            var itemData = ItemData.Companion.getRegistry().get(blockgameItemId);
////                            var player = MinecraftClient.getInstance().player;
////                            if (player != null && itemData != null && itemData.getMaterial() != null && itemData.getPattern() != null) {
////                                var materialRegistry = player.getRegistryManager().getOrThrow(RegistryKeys.TRIM_MATERIAL);
////                                var patternRegistry = player.getRegistryManager().getOrThrow(RegistryKeys.TRIM_PATTERN);
////                                var material = materialRegistry.getEntry(Identifier.of(itemData.getMaterial())).orElseThrow();
////                                var pattern = patternRegistry.getEntry(Identifier.of(itemData.getPattern())).orElseThrow();
////                                this.components.set(DataComponentTypes.TRIM, new ArmorTrim(material, pattern));
////                            }
////                        }

                        var setName = set.get().replaceFirst("SET_", "").toLowerCase();
                        var armorTier = getArmorTier(blockgameItemId);
                        if (armorTier == null) {
                            return;
                        }

                        var newAssetId = RegistryKey.of(REGISTRY_KEY, Identifier.of("blockgame", setName + "/" + armorTier));
                        var namePre = data.getStringArray(BGIField.NAME_PRE);
                        var nameSuf = data.getStringArray(BGIField.NAME_SUF);
                        if (namePre.isPresent() || nameSuf.isPresent()) {
                            newAssetId = RegistryKey.of(REGISTRY_KEY, Identifier.of("blockgame", setName + "/special/" + armorTier));
                        }

                        var eq = this.components.get(DataComponentTypes.EQUIPPABLE);
                        if (eq == null) {
                            var mcItemId = Registries.ITEM.getId(item.asItem());
                            Item defaultMcItem = Registries.ITEM.get(mcItemId);
                            eq = defaultMcItem.getComponents().get(DataComponentTypes.EQUIPPABLE);
                        }

//                        if (eq == null) {
//                            EquipmentSlot equipmentSlot = getEquipmentSlot(blockgameItemId);
//                            if (equipmentSlot != null) {
//                                eq = new EquippableComponent(equipmentSlot, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, Optional.of(newAssetId), Optional.empty(), Optional.empty(), true, true, false);
//                            }
//                        }
//
                        if (eq != null) {
                            var newEq = new EquippableComponent(eq.slot(), eq.equipSound(), Optional.of(newAssetId), eq.cameraOverlay(), eq.allowedEntities(), eq.dispensable(), eq.swappable(), eq.damageOnHurt());
                            this.components.set(DataComponentTypes.EQUIPPABLE, newEq);
                        }
                    }

*/

/*
    @Unique
    @Nullable
    private static EquipmentSlot getEquipmentSlot(String blockgameItemId) {
        EquipmentSlot equipmentSlot = null;
        if (blockgameItemId.endsWith("_HELMET") || blockgameItemId.contains("_HELMET_")) {
            equipmentSlot = EquipmentSlot.HEAD;
        }
        if (blockgameItemId.endsWith("_CHESTPLATE") || blockgameItemId.contains("_CHESTPLATE_")) {
            equipmentSlot = EquipmentSlot.CHEST;
        }
        if (blockgameItemId.endsWith("_LEGGINGS") || blockgameItemId.contains("_LEGGINGS_")) {
            equipmentSlot = EquipmentSlot.LEGS;
        }
        if (blockgameItemId.endsWith("_BOOTS") || blockgameItemId.contains("_BOOTS_")) {
            equipmentSlot = EquipmentSlot.FEET;
        }
        return equipmentSlot;
    }

    @Unique
    @Nullable
    private static String getArmorTier(String blockgameItemId) {
        if (blockgameItemId.endsWith("_HELMET")) {
            return blockgameItemId.replace("_HELMET", "").toLowerCase();
        }
        if (blockgameItemId.endsWith("_CHESTPLATE")) {
            return blockgameItemId.replace("_CHESTPLATE", "").toLowerCase();
        }
        if (blockgameItemId.endsWith("_LEGGINGS")) {
            return blockgameItemId.replace("_LEGGINGS", "").toLowerCase();
        }
        if (blockgameItemId.endsWith("_BOOTS")) {
            return blockgameItemId.replace("_BOOTS", "").toLowerCase();
        }

        String tierString = "t" + blockgameItemId.substring(blockgameItemId.lastIndexOf("_") + 1);
        if (blockgameItemId.contains("_HELMET_")) {
            return tierString;
        }
        if (blockgameItemId.contains("_CHESTPLATE_")) {
            return tierString;
        }
        if (blockgameItemId.contains("_LEGGINGS_")) {
            return tierString;
        }
        if (blockgameItemId.contains("_BOOTS_")) {
            return tierString;
        }
        return null;
    }
*/

}
