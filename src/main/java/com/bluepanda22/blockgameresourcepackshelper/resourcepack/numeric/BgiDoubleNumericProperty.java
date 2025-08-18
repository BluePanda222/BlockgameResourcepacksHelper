package com.bluepanda22.blockgameresourcepackshelper.resourcepack.numeric;

import com.mojang.serialization.MapCodec;
import dev.bnjc.bglib.BGIData;
import dev.bnjc.bglib.BGIField;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BgiDoubleNumericProperty<E extends Enum<BGIField>> extends BgiNumericProperty {

    public final E bgiField;

    public BgiDoubleNumericProperty(E bgiField) {
        this.bgiField = bgiField;
    }

    @Override
    float getValue(BGIData data) {
        var optional = data.getDouble(bgiField.name());
        return optional.map(Double::floatValue).orElse(0.0F);
    }

    public MapCodec<BgiDoubleNumericProperty<E>> getCodec() {
        return ofCodec(bgiField);
    }

    public static <E extends Enum<BGIField>> MapCodec<BgiDoubleNumericProperty<E>> ofCodec(E bgiField) {
        return MapCodec.unit(new BgiDoubleNumericProperty<>(bgiField));
    }

}
