// (c) 2023 Isaiah
package xyz.nucleoid.fantasy.mixin;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.IMC;
import xyz.nucleoid.fantasy.util.SafeIterator;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer implements IMC {

	// The locals you have to manage for an inject are insane. And do it twice. A redirect is much cleaner.
		// Here is what it looks like with an inject: https://gist.github.com/i509VCB/f80077cc536eb4dba62b794eba5611c1
	@Redirect(method = "createLevels", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
	private <K, V> V onLoadWorld(Map<K, V> worlds, K registryKey, V serverWorld) {
        return worlds.put(registryKey, serverWorld);
	}

	@Redirect(method = "tickChildren", at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;", ordinal = 0), require = 0)
	private Iterator<ServerLevel> fantasy$copyBeforeTicking(Iterable<ServerLevel> instance) {
		return new SafeIterator<>((Collection<ServerLevel>) instance);
	}

	@Inject(at = @At("HEAD"), method = "tickServer")
	private void on_start_tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		Fantasy fantasy = Fantasy.get((MinecraftServer) (Object) this);
        fantasy.tick();
	}

	@Inject(at = @At("HEAD"), method = "stopServer")
	private void before_shutdown_server(CallbackInfo info) {
		Fantasy fantasy = Fantasy.get((MinecraftServer) (Object) this);
        fantasy.onServerStopping();
	}
	
	@Final
	@Shadow
    private Map<ResourceKey<Level>, ServerLevel> levels;

	@Override
	public void add_world(ResourceKey<Level> key, ServerLevel value) {
		// TODO Auto-generated method stub
		levels.put(key, value);
	}

}