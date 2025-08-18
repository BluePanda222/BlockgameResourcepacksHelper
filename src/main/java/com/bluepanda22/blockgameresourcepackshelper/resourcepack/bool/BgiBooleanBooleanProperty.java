package com.bluepanda22.blockgameresourcepackshelper.resourcepack.bool;

import com.mojang.serialization.MapCodec;
import dev.bnjc.bglib.BGIData;
import dev.bnjc.bglib.BGIField;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BgiBooleanBooleanProperty<E extends Enum<BGIField>> extends BgiBooleanProperty {

    public final E bgiField;

    public BgiBooleanBooleanProperty(E bgiField) {
        this.bgiField = bgiField;
    }

    @Override
    boolean getValue(BGIData data) {
        var optional = data.getBoolean(bgiField.name());
        return optional.orElse(false);
    }

    public MapCodec<BgiBooleanBooleanProperty<E>> getCodec() {
        return ofCodec(bgiField);
    }

    public static <E extends Enum<BGIField>> MapCodec<BgiBooleanBooleanProperty<E>> ofCodec(E bgiField) {
        return MapCodec.unit(new BgiBooleanBooleanProperty<>(bgiField));
    }

}
