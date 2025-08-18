package com.bluepanda22.blockgameresourcepackshelper.resourcepack.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.bnjc.bglib.BGIData;
import dev.bnjc.bglib.BGIField;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BgiBooleanSelectProperty<E extends Enum<BGIField>> extends BgiSelectProperty<Boolean> {

    public final E bgiField;

    public BgiBooleanSelectProperty(E bgiField) {
        this.bgiField = bgiField;
    }

    @Override
    @Nullable Boolean getValue(BGIData data) {
        var optional = data.getBoolean(bgiField.name());
        return optional.orElse(null);
    }

    public Type<BgiBooleanSelectProperty<E>, Boolean> getType() {
        return ofType(bgiField);
    }

    public static <E extends Enum<BGIField>> Type<BgiBooleanSelectProperty<E>, Boolean> ofType(E bgiField) {
        return Type.create(MapCodec.unit(new BgiBooleanSelectProperty<>(bgiField)), Codec.BOOL);
    }

}
