package xyz.nucleoid.fantasy;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FileUtils;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

final class RuntimeWorldManager {
    private final MinecraftServer server;
    private final MinecraftServerAccess serverAccess;

    public Map<ResourceKey<Level>, ServerLevel> worldss;
    
    RuntimeWorldManager(MinecraftServer server) {
        this.server = server;
        this.worldss = new HashMap<>();
        this.serverAccess = (MinecraftServerAccess) server;
    }

    RuntimeWorld add(ResourceKey<Level> worldKey, RuntimeWorldConfig config, RuntimeWorld.Style style) {
        LevelStem options = config.createDimensionOptions(this.server);

        if (style == RuntimeWorld.Style.TEMPORARY) {
            ((FantasyDimensionOptions) (Object) options).fantasy$setSave(false);
        }

        MappedRegistry<LevelStem> dimensionsRegistry = getDimensionsRegistry(this.server);
        boolean isFrozen = ((RemoveFromRegistry<?>) dimensionsRegistry).fantasy$isFrozen();
        ((RemoveFromRegistry<?>) dimensionsRegistry).fantasy$setFrozen(false);
        dimensionsRegistry.register(ResourceKey.create(Registries.LEVEL_STEM, worldKey.location()), options, RegistrationInfo.BUILT_IN);
        ((RemoveFromRegistry<?>) dimensionsRegistry).fantasy$setFrozen(isFrozen);

        RuntimeWorld world = new RuntimeWorld(this.server, worldKey, config, style);

        IMC imc = (IMC) this.server;
        imc.add_world(world.dimension(), world);

        if (FantasyInitializer.after_tick_start) {
        	worldss.put(world.dimension(), world);
        }

        // this.serverAccess.getWorlds().put(world.getRegistryKey(), world);
        // ServerWorldEvents.LOAD.invoker().onWorldLoad(this.server, world);
        
        // tick the world to ensure it is ready for use right away
        world.tick(() -> true);

        return world;
    }

    void delete(ServerLevel world) {
        ResourceKey<Level> dimensionKey = world.dimension();

        if (this.serverAccess.getWorlds().remove(dimensionKey, world)) {
            // ServerWorldEvents.UNLOAD.invoker().onWorldUnload(this.server, world);

            MappedRegistry<LevelStem> dimensionsRegistry = getDimensionsRegistry(this.server);
            RemoveFromRegistry.remove(dimensionsRegistry, dimensionKey.location());

            LevelStorageSource.LevelStorageAccess session = this.serverAccess.getSession();
            File worldDirectory = session.getDimensionPath(dimensionKey).toFile();
            if (worldDirectory.exists()) {
                try {
                    FileUtils.deleteDirectory(worldDirectory);
                } catch (IOException e) {
                    Fantasy.LOGGER.warn("Failed to delete world directory", e);
                    try {
                        FileUtils.forceDeleteOnExit(worldDirectory);
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    private static MappedRegistry<LevelStem> getDimensionsRegistry(MinecraftServer server) {
        RegistryAccess registryManager = server.registries().compositeAccess();
        return (MappedRegistry<LevelStem>) registryManager.registryOrThrow(Registries.LEVEL_STEM);
    }
}
