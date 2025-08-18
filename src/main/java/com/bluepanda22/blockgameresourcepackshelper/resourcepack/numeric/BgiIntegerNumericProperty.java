package com.bluepanda22.blockgameresourcepackshelper.resourcepack.numeric;

import com.mojang.serialization.MapCodec;
import dev.bnjc.bglib.BGIData;
import dev.bnjc.bglib.BGIField;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BgiIntegerNumericProperty<E extends Enum<BGIField>> extends BgiNumericProperty {

    public final E bgiField;

    public BgiIntegerNumericProperty(E bgiField) {
        this.bgiField = bgiField;
    }

    @Override
    float getValue(BGIData data) {
        var optional = data.getInt(bgiField.name());
        return optional.map(Integer::floatValue).orElse(0.0F);
    }

    public MapCodec<BgiIntegerNumericProperty<E>> getCodec() {
        return ofCodec(bgiField);
    }

    public static <E extends Enum<BGIField>> MapCodec<BgiIntegerNumericProperty<E>> ofCodec(E bgiField) {
        return MapCodec.unit(new BgiIntegerNumericProperty<>(bgiField));
    }

}
