package com.bluepanda22.blockgameresourcepackshelper.resourcepack.select;

import dev.bnjc.bglib.BGIData;
import dev.bnjc.bglib.BGIParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class BgiSelectProperty<T> implements SelectProperty<T> {

    @Override
    @Nullable
    public T getValue(ItemStack itemStack, @Nullable ClientWorld clientWorld, @Nullable LivingEntity livingEntity, int i, ModelTransformationMode modelTransformationMode) {
        var result = BGIParser.parse(itemStack);

        if (result.isSuccess()) {
            var optionalData = result.result();
            if (optionalData.isPresent()) {
                var data = optionalData.get();
                return getValue(data);
            }
        }

        return null;
    }

    @Nullable
    abstract T getValue(BGIData data);

}
