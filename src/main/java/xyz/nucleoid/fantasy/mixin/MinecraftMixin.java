package xyz.nucleoid.fantasy.mixin;

import me.isaiah.multiworld.gui.gif.GifManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "resizeDisplay", at = @At(value = "TAIL"))
    private void resizeDisplay(CallbackInfo ci) {
        GifManager manager = GifManager.getInstance();
        manager.recreateAllTextures();
    }
}
