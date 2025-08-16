package xyz.nucleoid.fantasy;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class RuntimeWorldHandle {
    private final Fantasy fantasy;
    private final ServerLevel world;

    RuntimeWorldHandle(Fantasy fantasy, ServerLevel world) {
        this.fantasy = fantasy;
        this.world = world;
    }

    public void setTickWhenEmpty(boolean tickWhenEmpty) {
        ((FantasyWorldAccess) this.world).fantasy$setTickWhenEmpty(tickWhenEmpty);
    }

    public void delete() {
    	if (this.world instanceof RuntimeWorld runtimeWorld && runtimeWorld.style == RuntimeWorld.Style.TEMPORARY) {
            this.fantasy.enqueueWorldDeletion(this.world);
        } else {
            // TODO !
        	// this.fantasy.enqueueWorldUnloading(this.world);
        	this.fantasy.enqueueWorldDeletion(this.world);
        }
    }

    public ServerLevel asWorld() {
        return this.world;
    }

    public ResourceKey<Level> getRegistryKey() {
        return this.world.dimension();
    }
}
