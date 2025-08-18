package com.bluepanda22.blockgameresourcepackshelper.resourcepack.bool;

import com.mojang.serialization.MapCodec;
import dev.bnjc.bglib.BGIData;
import dev.bnjc.bglib.BGIField;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BgiHasAttributeBooleanProperty<E extends Enum<BGIField>> extends BgiBooleanProperty {

    public final E bgiField;

    public BgiHasAttributeBooleanProperty(E bgiField) {
        this.bgiField = bgiField;
    }

    @Override
    boolean getValue(BGIData data) {
        var optional = data.getAttribute(bgiField.name());
        return optional.isPresent();
    }

    public MapCodec<BgiHasAttributeBooleanProperty<E>> getCodec() {
        return ofCodec(bgiField);
    }

    public static <E extends Enum<BGIField>> MapCodec<BgiHasAttributeBooleanProperty<E>> ofCodec(E bgiField) {
        return MapCodec.unit(new BgiHasAttributeBooleanProperty<>(bgiField));
    }

}
