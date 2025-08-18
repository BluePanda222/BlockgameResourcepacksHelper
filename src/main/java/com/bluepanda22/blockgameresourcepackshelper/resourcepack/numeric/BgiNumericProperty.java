package com.bluepanda22.blockgameresourcepackshelper.resourcepack.numeric;

import dev.bnjc.bglib.BGIData;
import dev.bnjc.bglib.BGIParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class BgiNumericProperty implements NumericProperty {

    @Override
    public float getValue(@NotNull ItemStack itemStack, @Nullable ClientWorld clientWorld, @Nullable LivingEntity livingEntity, int seed) {
        var result = BGIParser.parse(itemStack);

        if (result.isSuccess()) {
            var optionalData = result.result();
            if (optionalData.isPresent()) {
                var data = optionalData.get();
                return getValue(data);
            }
        }

        return 0.0F;
    }

    abstract float getValue(BGIData data);

}
