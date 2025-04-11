package com.bluepanda22.blockgameresourcepacks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.bnjc.bglib.BGIField;
import dev.bnjc.bglib.BGIParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
public record BlockgameItemStringProperty() implements SelectProperty<String> {
    public static final SelectProperty.Type<BlockgameItemStringProperty, String> TYPE;

    @Nullable
    public String getValue(ItemStack itemStack, @Nullable ClientWorld clientWorld, @Nullable LivingEntity livingEntity, int i, ModelTransformationMode modelTransformationMode) {
        var maybeResult = BGIParser.parse(itemStack);

        if (maybeResult.isError()) {
            var maybeErr = maybeResult.error();
            if (maybeErr.isPresent()) {
                var err = maybeErr.get();
                System.err.println(err.getErrorCode());
                System.err.println(err.getMessage());
                System.err.println(Arrays.toString(err.getStackTrace()));
            }
            return null;
        } else {
           var result = maybeResult.result();
            if (result.isEmpty()) {
                return null;
            } else {
                var data = result.get();
                var itemId = data.getString(BGIField.ITEM_ID);
                return itemId.orElse(null);
            }
        }
    }

    public SelectProperty.Type<BlockgameItemStringProperty, String> getType() {
        return TYPE;
    }

    static {
        TYPE = SelectProperty.Type.create(MapCodec.unit(new BlockgameItemStringProperty()), Codec.STRING);
    }
}
