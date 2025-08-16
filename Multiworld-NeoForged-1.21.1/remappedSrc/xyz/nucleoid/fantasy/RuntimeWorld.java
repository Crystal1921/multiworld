package xyz.nucleoid.fantasy;

import com.google.common.collect.ImmutableList;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;
import xyz.nucleoid.fantasy.util.VoidWorldProgressListener;

class RuntimeWorld extends ServerLevel {
    final Style style;
    private boolean flat;

    protected RuntimeWorld(MinecraftServer server, ResourceKey<Level> registryKey, RuntimeWorldConfig config, Style style) {
        super(
                server, Util.backgroundExecutor(), ((MinecraftServerAccess) server).getSession(),
                new RuntimeWorldProperties(server.getWorldData(), config),
                registryKey,
                config.createDimensionOptions(server),
                VoidWorldProgressListener.INSTANCE,
                false,
                BiomeManager.obfuscateSeed(config.getSeed()),
                ImmutableList.of(),
                config.shouldTickTime(),
                null
        );
        this.style = style;
        this.flat = false; //TODO // config.isFlat().orElse(super.isFlat());
    }

    @Override
    public long getSeed() {
        return ((RuntimeWorldProperties) this.levelData).config.getSeed();
    }

    @Override
    public void save(@Nullable ProgressListener progressListener, boolean flush, boolean enabled) {
        if (this.style == Style.PERSISTENT || !flush) {
            super.save(progressListener, flush, enabled);
        }
    }

    @Override
    public boolean isFlat() {
        return this.flat;
    }

    public enum Style {
        PERSISTENT,
        TEMPORARY
    }
}
