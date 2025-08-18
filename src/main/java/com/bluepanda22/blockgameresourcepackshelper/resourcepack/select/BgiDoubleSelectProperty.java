package com.bluepanda22.blockgameresourcepackshelper.resourcepack.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.bnjc.bglib.BGIData;
import dev.bnjc.bglib.BGIField;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BgiDoubleSelectProperty<E extends Enum<BGIField>> extends BgiSelectProperty<Double> {

    public final E bgiField;

    public BgiDoubleSelectProperty(E bgiField) {
        this.bgiField = bgiField;
    }

    @Override
    @Nullable Double getValue(BGIData data) {
        var optional = data.getDouble(bgiField.name());
        return optional.orElse(null);
    }

    public Type<BgiDoubleSelectProperty<E>, Double> getType() {
        return ofType(bgiField);
    }

    public static <E extends Enum<BGIField>> Type<BgiDoubleSelectProperty<E>, Double> ofType(E bgiField) {
        return Type.create(MapCodec.unit(new BgiDoubleSelectProperty<>(bgiField)), Codec.DOUBLE);
    }

}
