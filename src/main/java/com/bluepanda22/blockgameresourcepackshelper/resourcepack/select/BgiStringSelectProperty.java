package com.bluepanda22.blockgameresourcepackshelper.resourcepack.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.bnjc.bglib.BGIData;
import dev.bnjc.bglib.BGIField;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BgiStringSelectProperty<E extends Enum<BGIField>> extends BgiSelectProperty<String> {

    public final E bgiField;

    public BgiStringSelectProperty(E bgiField) {
        this.bgiField = bgiField;
    }

    @Override
    @Nullable String getValue(BGIData data) {
        var optional = data.getString(bgiField.name());

        if (optional.isPresent()) {
            var value = optional.get();
            if (value.startsWith("<tier-color>")) {
                // used for BGIField.NAME: "<tier-color>Bloodsnout Healers Hood"
                return value.substring("<tier-color>".length());
            } else {
                return value;
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
