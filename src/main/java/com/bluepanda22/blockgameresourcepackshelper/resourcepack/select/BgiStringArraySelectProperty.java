package com.bluepanda22.blockgameresourcepackshelper.resourcepack.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.bnjc.bglib.BGIData;
import dev.bnjc.bglib.BGIField;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BgiStringArraySelectProperty<E extends Enum<BGIField>> extends BgiSelectProperty<String> {

    public final E bgiField;

    public BgiStringArraySelectProperty(E bgiField) {
        this.bgiField = bgiField;
    }

    @Override
    @Nullable String getValue(BGIData data) {
        var optional = data.getStringArray(bgiField.name());

        if (optional.isPresent()) {
            var value = optional.get();
            if (value.length == 1) {
                return value[0].substring(2); // "&6Sturdy" -> "Sturdy"
            } else {
                // realistically, items don't have more than one prefix or suffix
                return null;
            }
        }

        return null;
    }

    public Type<BgiStringSelectProperty<E>, String> getType() {
        return ofType(bgiField);
    }

    public static <E extends Enum<BGIField>> Type<BgiStringSelectProperty<E>, String> ofType(E bgiField) {
        return Type.create(MapCodec.unit(new BgiStringSelectProperty<>(bgiField)), Codec.STRING);
    }

}
