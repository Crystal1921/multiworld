package xyz.nucleoid.fantasy.util;

import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public final class VoidWorldProgressListener implements ChunkProgressListener {
    public static final VoidWorldProgressListener INSTANCE = new VoidWorldProgressListener();

    private VoidWorldProgressListener() {
    }

    @Override
    public void updateSpawnPos(ChunkPos spawnPos) {
    }

    @Override
    public void onStatusChange(ChunkPos pos, @Nullable ChunkStatus status) {
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
    }
}
