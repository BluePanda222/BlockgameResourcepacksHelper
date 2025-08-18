package com.bluepanda22.blockgameresourcepackshelper.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

@Environment(EnvType.CLIENT)
public final class EmptySprite {
    private static final int WIDTH = 16;
    private static final int HEIGHT = 16;
    private static final String EMPTY_SPRITE_ID = "empty_sprite";
    private static final Identifier EMPTY_SPRITE = Identifier.ofVanilla(EMPTY_SPRITE_ID);

    public static NativeImage createImage() {
        return createImage(16, 16);
    }

    public static NativeImage createImage(int width, int height) {
        NativeImage nativeImage = new NativeImage(width, height, false);
        int i = -524040;

        for (int j = 0; j < height; j++) {
            for (int k = 0; k < width; k++) {
                nativeImage.setColorArgb(k, j, ColorHelper.getArgb(0, 0, 0 ,0));
            }
        }

        return nativeImage;
    }

    public static SpriteContents createSpriteContents() {
        NativeImage nativeImage = createImage(16, 16);
        return new SpriteContents(EMPTY_SPRITE, new SpriteDimensions(16, 16), nativeImage, ResourceMetadata.NONE);
    }

    public static Identifier getMissingSpriteId() {
        return EMPTY_SPRITE;
    }
}
