package xyz.nucleoid.fantasy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorageSource;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;

/**
 * Fantasy is a library that allows for dimensions to be created and destroyed at runtime on the server.
 * It supports both temporary dimensions which do not get saved, as well as persistent dimensions which can be safely
 * used across server restarts.
 *
 * @see Fantasy#get(MinecraftServer)
 * @see Fantasy#openTemporaryWorld(RuntimeWorldConfig)
 * @see Fantasy#getOrOpenPersistentWorld(ResourceLocation, RuntimeWorldConfig)
 */
public final class Fantasy {
    public static final Logger LOGGER = LogManager.getLogger(Fantasy.class);
    public static final String ID = "fantasy";
    public static final ResourceKey<DimensionType> DEFAULT_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(Fantasy.ID, "default"));

    private static Fantasy instance;

    private final MinecraftServer server;
    private final MinecraftServerAccess serverAccess;

    public final RuntimeWorldManager worldManager;

    private final Set<ServerLevel> deletionQueue = new ReferenceOpenHashSet<>();

  /*  static {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            Fantasy fantasy = get(server);
            fantasy.tick();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            Fantasy fantasy = get(server);
            fantasy.onServerStopping();
        });
    }*/

    private Fantasy(MinecraftServer server) {
        this.server = server;
        this.serverAccess = (MinecraftServerAccess) server;

        this.worldManager = new RuntimeWorldManager(server);
    }

    /**
     * Gets the {@link Fantasy} instance for the given server instance.
     *
     * @param server the server to work with
     * @return the {@link Fantasy} instance to work with runtime dimensions
     */
    public static Fantasy get(MinecraftServer server) {
        Preconditions.checkState(server.isSameThread(), "cannot create worlds from off-thread!");

        if (instance == null || instance.server != server) {
            instance = new Fantasy(server);
        }

        return instance;
    }

    public void tick() {
        Set<ServerLevel> deletionQueue = this.deletionQueue;
        if (!deletionQueue.isEmpty()) {
            deletionQueue.removeIf(this::tickDeleteWorld);
        }
    }

    /**
     * Creates a new temporary world with the given {@link RuntimeWorldConfig} that will not be saved and will be
     * deleted when the server exits.
     * <p>
     * The created world is returned asynchronously through a {@link RuntimeWorldHandle}.
     * This handle can be used to acquire the {@link ServerLevel} object through {@link RuntimeWorldHandle#asWorld()},
     * as well as to delete the world through {@link RuntimeWorldHandle#delete()}.
     *
     * @param config the config with which to construct this temporary world
     * @return a future providing the created world
     */
    public RuntimeWorldHandle openTemporaryWorld(RuntimeWorldConfig config) {
        RuntimeWorld world = this.addTemporaryWorld(config);
        return new RuntimeWorldHandle(this, world);
    }

    /**
     * Gets or creates a new persistent world with the given identifier and {@link RuntimeWorldConfig}. These worlds
     * will be saved to disk and can be restored after a server restart.
     * <p>
     * If a world with this identifier exists already, it will be returned and no new world will be constructed.
     * <p>
     * <b>Note!</b> These persistent worlds will not be automatically restored! This function
     * must be called after a server restart with the relevant identifier and configuration such that it can be loaded.
     * <p>
     * The created world is returned asynchronously through a {@link RuntimeWorldHandle}.
     * This handle can be used to acquire the {@link ServerLevel} object through {@link RuntimeWorldHandle#asWorld()},
     * as well as to delete the world through {@link RuntimeWorldHandle#delete()}.
     *
     * @param key the unique identifier for this dimension
     * @param config the config with which to construct this persistent world
     * @return a future providing the created world
     */
    public RuntimeWorldHandle getOrOpenPersistentWorld(ResourceLocation key, RuntimeWorldConfig config) {
		ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, key);

        ServerLevel world = this.server.getLevel(worldKey);
        if (world == null) {
            world = this.addPersistentWorld(key, config);
        } else {
            this.deletionQueue.remove(world);
        }

        return new RuntimeWorldHandle(this, world);
    }

    private RuntimeWorld addPersistentWorld(ResourceLocation key, RuntimeWorldConfig config) {
		ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, key);
        return this.worldManager.add(worldKey, config, RuntimeWorld.Style.PERSISTENT);
    }

    private RuntimeWorld addTemporaryWorld(RuntimeWorldConfig config) {
        ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, generateTemporaryWorldKey());

        try {
            LevelStorageSource.LevelStorageAccess session = this.serverAccess.getSession();
            FileUtils.forceDeleteOnExit(session.getDimensionPath(worldKey).toFile());
        } catch (IOException ignored) {
        }

        return this.worldManager.add(worldKey, config, RuntimeWorld.Style.TEMPORARY);
    }

    void enqueueWorldDeletion(ServerLevel world) {
        this.server.submit(() -> {
            this.deletionQueue.add(world);
        });
    }

    private boolean tickDeleteWorld(ServerLevel world) {
        if (this.isWorldUnloaded(world)) {
            this.worldManager.delete(world);
            return true;
        } else {
            this.kickPlayers(world);
            return false;
        }
    }

    private void kickPlayers(ServerLevel world) {
        if (world.players().isEmpty()) {
            return;
        }

        ServerLevel overworld = this.server.overworld();
        BlockPos spawnPos = overworld.getSharedSpawnPos();
        float spawnAngle = overworld.getSharedSpawnAngle();

        List<ServerPlayer> players = new ArrayList<>(world.players());
        for (ServerPlayer player : players) {
            player.teleportTo(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, spawnAngle, 0.0F);
        }
    }

    private boolean isWorldUnloaded(ServerLevel world) {
        return world.players().isEmpty() && world.getChunkSource().getLoadedChunksCount() <= 0;
    }

    public void onServerStopping() {
        List<RuntimeWorld> temporaryWorlds = this.collectTemporaryWorlds();
        for (RuntimeWorld temporary : temporaryWorlds) {
            this.kickPlayers(temporary);
            this.worldManager.delete(temporary);
        }
    }

    private List<RuntimeWorld> collectTemporaryWorlds() {
        List<RuntimeWorld> temporaryWorlds = new ArrayList<>();
        for (ServerLevel world : this.server.getAllLevels()) {
            if (world instanceof RuntimeWorld) {
                RuntimeWorld runtimeWorld = (RuntimeWorld) world;
                if (runtimeWorld.style == RuntimeWorld.Style.TEMPORARY) {
                    temporaryWorlds.add(runtimeWorld);
                }
            }
        }
        return temporaryWorlds;
    }

    private static ResourceLocation generateTemporaryWorldKey() {
        String key = RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789");
        return ResourceLocation.fromNamespaceAndPath(Fantasy.ID, key);
    }
}
