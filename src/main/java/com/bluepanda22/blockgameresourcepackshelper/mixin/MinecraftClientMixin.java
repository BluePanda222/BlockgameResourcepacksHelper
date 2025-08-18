package com.bluepanda22.blockgameresourcepackshelper.mixin;

import com.bluepanda22.blockgameresourcepackshelper.BlockgameResourcepacksHelperMain;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(
        method = "<init>",
        at = @At(
            value = "CTOR_HEAD"
        )
    )
    private void init(RunArgs args, CallbackInfo ci) {
        BlockgameResourcepacksHelperMain.packsDir = args.directories.resourcePackDir.toPath();
    }

    @Inject(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/resource/ResourcePackManager;createResourcePacks()Ljava/util/List;"
        )
    )
    private void init2(RunArgs args, CallbackInfo ci) {
        var resourceManager = (ReloadableResourceManagerImpl) MinecraftClient.getInstance().getResourceManager();
        resourceManager.registerReloader(BlockgameResourcepacksHelperMain.INSTANCE);
    }

}
