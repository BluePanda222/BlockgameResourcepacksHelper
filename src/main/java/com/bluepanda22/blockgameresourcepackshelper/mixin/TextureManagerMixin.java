package com.bluepanda22.blockgameresourcepackshelper.mixin;

import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain;
import com.bluepanda22.blockgameresourcepackshelper.util.EmptySprite;
import com.bluepanda22.blockgameresourcepackshelper.util.TextureUtil;
import net.minecraft.client.texture.ReloadableTexture;
import net.minecraft.client.texture.TextureContents;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextureManager.class)
public class TextureManagerMixin {

//    @Inject(
//        method = "loadTexture(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/Identifier;Lnet/minecraft/client/texture/ReloadableTexture;)Lnet/minecraft/client/texture/TextureContents;",
//        at = @At(
//            value = "INVOKE",
//            target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
//            ordinal = 0
//        ),
//        cancellable = true
//    )
//    @SuppressWarnings("mapping")
//    private static void loadTexture(ResourceManager resourceManager, Identifier textureId, ReloadableTexture texture, CallbackInfoReturnable<TextureContents> cir) {
////        if (textureId.getNamespace().equals("blockgame")) {
////            if (textureId.getPath().startsWith("textures/entity/equipment/humanoid")) {
////                var equipmentAssetKey = TextureUtil.INSTANCE.getEquipmentAssetKeyFromTextureId(textureId);
////                var sanitisedTextureId = TextureUtil.INSTANCE.getSanitisedTextureIdFromTextureId(textureId);
////                if (equipmentAssetKey != null && sanitisedTextureId != null) {
////                    BlockgameResourcepacksHelperMain.MISSING_EQUIPMENT_ASSET_IDS.add(equipmentAssetKey);
////                    BlockgameResourcepacksHelperMain.MISSING_TEXTURE_IDS.add(sanitisedTextureId);
//////                    BlockgameResourcepacksHelperMain.LOGGER.warn("DID CATCH THE ERROR THINGY!!! :3");
//////                    BlockgameResourcepacksHelperMain.LOGGER.warn(equipmentAssetKey.toString());
//////                    BlockgameResourcepacksHelperMain.LOGGER.warn(sanitisedTextureId.toString());
////                }
////
////                cir.setReturnValue(new TextureContents(EmptySprite.createImage(), null));
////            }
////        }
//    }

}
