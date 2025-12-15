package xyz.nucleoid.fantasy.mixin;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Shadow
    public boolean tickTime;

    @Inject(method = "<init>", at = @org.spongepowered.asm.mixin.injection.At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        this.tickTime = true;
    }
}
