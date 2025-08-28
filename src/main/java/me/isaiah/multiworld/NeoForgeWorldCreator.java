package me.isaiah.multiworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPresets;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.HashMap;

public class NeoForgeWorldCreator implements ICreator {

    public HashMap<String, RuntimeWorldConfig> worldConfigs;

    public NeoForgeWorldCreator() {
        this.worldConfigs = new HashMap<>();
    }

    public static void init() {
        MultiworldMod.setICreator(new NeoForgeWorldCreator());
    }

    private static ResourceKey<DimensionType> dim_of(ResourceLocation id) {
        return ResourceKey.create(Registries.DIMENSION_TYPE, id);
    }

    public static ChunkGenerator getStaticVoidGen(MinecraftServer mc) {
        Registry<FlatLevelGeneratorPreset> flatLevelGeneratorPresets = mc.registryAccess().registryOrThrow(Registries.FLAT_LEVEL_GENERATOR_PRESET);
        FlatLevelGeneratorPreset preset = flatLevelGeneratorPresets.getOrThrow(FlatLevelGeneratorPresets.THE_VOID);
        return new CustomFlatChunkGenerator(preset.settings());
    }

    public ServerLevel createWorld(String id, ResourceLocation dim, ChunkGenerator gen, Difficulty dif, long seed) {
        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(dim_of(dim))
                .setGenerator(gen)
                .setDifficulty(dif)
                .setSeed(seed)
                .setShouldTickTime(true);

        Fantasy fantasy = Fantasy.get(MultiworldMod.mc);
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(ResourceLocation.parse(id), config);
        this.worldConfigs.put(id, config);
        ServerLevel world = worldHandle.asWorld();
        NeoForge.EVENT_BUS.post(new LevelEvent.Load(world));
        return world;
    }

    @Override
    public void setDifficulty(String id, Difficulty dif) {
        this.worldConfigs.get(id).setDifficulty(dif);
    }

    public void deleteWorld(String id) {
        Fantasy fantasy = Fantasy.get(MultiworldMod.mc);
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(ResourceLocation.parse(id), null);
        worldHandle.delete();
    }

    @Override
    public boolean isTheEnd(ServerLevel world) {
        return world.dimensionTypeRegistration() == BuiltinDimensionTypes.END;
    }

    @Override
    public BlockPos getPos(double x, double y, double z) {
        return BlockPos.containing(x, y, z);
    }

    @Override
    public BlockPos getSpawn(ServerLevel world) {
        return world.getLevelData().getSpawnPos();
    }

    @Override
    public void teleport(ServerPlayer player, ServerLevel world, double x, double y, double z) {
        DimensionTransition target = new DimensionTransition(world, new Vec3(x, y, z), new Vec3(0, 0, 0), 0f, 0f, DimensionTransition.DO_NOTHING);
        player.changeDimension(target);
    }

    @Override
    public ChunkGenerator getVoidGen(MinecraftServer mc) {
        return getStaticVoidGen(mc);
    }

    @Override
    public ChunkGenerator getVoidChunkGen(MinecraftServer mc) {
        return this.getVoidGen(mc);
    }

    // Custom Flat Gen
    static class CustomFlatChunkGenerator extends FlatLevelSource {
        public CustomFlatChunkGenerator(FlatLevelGeneratorSettings config) {
            super(config);
        }

        @Override
        public int getMinY() {
            return 0;
        }

        @Override
        public int getSeaLevel() {
            return 0;
        }
    }

}