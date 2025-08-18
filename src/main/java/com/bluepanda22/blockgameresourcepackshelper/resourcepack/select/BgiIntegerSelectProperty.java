package com.bluepanda22.blockgameresourcepackshelper.resourcepack.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.bnjc.bglib.BGIData;
import dev.bnjc.bglib.BGIField;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BgiIntegerSelectProperty<E extends Enum<BGIField>> extends BgiSelectProperty<Integer> {

    public final E bgiField;

    public BgiIntegerSelectProperty(E bgiField) {
        this.bgiField = bgiField;
    }

    @Override
    @Nullable Integer getValue(BGIData data) {
        var optional = data.getInt(bgiField.name());
        return optional.orElse(null);
    }

    public Type<BgiIntegerSelectProperty<E>, Integer> getType() {
        return ofType(bgiField);
    }

    public static <E extends Enum<BGIField>> Type<BgiIntegerSelectProperty<E>, Integer> ofType(E bgiField) {
        return Type.create(MapCodec.unit(new BgiIntegerSelectProperty<>(bgiField)), Codec.INT);
    }

}
