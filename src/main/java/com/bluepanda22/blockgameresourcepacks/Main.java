package com.bluepanda22.blockgameresourcepacks;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.item.property.select.SelectProperties;
import net.minecraft.util.Identifier;

public class Main implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SelectProperties.ID_MAPPER.put(Identifier.ofVanilla("bgi_id"), BlockgameItemStringProperty.TYPE);
    }

}
