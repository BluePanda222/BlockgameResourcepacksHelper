package com.bluepanda22.blockgameresourcepackshelper.mixin;

import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain;
import com.bluepanda22.blockgameresourcepackshelper.util.TextureUtil;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(EquipmentRenderer.class)
public class EquipmentRendererMixin {

//    @Shadow
//    @Final
//    private EquipmentModelLoader equipmentModelLoader;
//
//    @Inject(
//        method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;)V",
//        at = @At(
//            value = "INVOKE",
//            target = "Ljava/util/List;isEmpty()Z"
//        )
//    )
//    public void render(EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> assetKey, Model model, ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
//                       int light, @Nullable Identifier probablyNullTexture, CallbackInfo ci, @Local(ordinal = 0) LocalRef<List<EquipmentModel.Layer>> list) {
////        if (BlockgameResourcepacksHelperMain.MISSING_EQUIPMENT_ASSET_IDS.contains(assetKey)) {
////
////            if (TextureUtil.INSTANCE.isSpecial(assetKey)) {
////                var newAssetKey = TextureUtil.INSTANCE.makeNotSpecial(assetKey);
////                if (newAssetKey != null && !BlockgameResourcepacksHelperMain.MISSING_EQUIPMENT_ASSET_IDS.contains(newAssetKey)) {
////                    sanitiseList(layerType, list, newAssetKey);
////                    return;
////                }
////            }
////
////            var defaultEquipmentAssetId = getDefaultEquipmentAssetId(stack);
////            if (defaultEquipmentAssetId.isPresent()) {
////                var newAssetKey = defaultEquipmentAssetId.get();
////                sanitiseList(layerType, list, newAssetKey);
////            }
////        }
//    }
//
//    @Unique
//    private void sanitiseList(EquipmentModel.LayerType layerType, LocalRef<List<EquipmentModel.Layer>> list, RegistryKey<EquipmentAsset> newAssetKey) {
//        List<EquipmentModel.Layer> newList = this.equipmentModelLoader.get(newAssetKey).getLayers(layerType);
//        var newNewList = new ArrayList<EquipmentModel.Layer>();
//        newList.forEach(layer -> {
//            if (!BlockgameResourcepacksHelperMain.MISSING_TEXTURE_IDS.contains(layer.textureId())) {
//                newNewList.add(layer);
//            }
//        });
//        list.set(newList);
//    }
//
//
//    @Unique
//    private static Optional<RegistryKey<EquipmentAsset>> getDefaultEquipmentAssetId(ItemStack stack) {
//        var item = stack.getItem();
//        var id = Registries.ITEM.getId(item);
//
//        var newItem = Registries.ITEM.get(id);
//
//        var equipment = newItem.getComponents().get(DataComponentTypes.EQUIPPABLE);
//        if (equipment != null) {
//            return equipment.assetId();
//        }
//        return Optional.empty();
//    }

}
