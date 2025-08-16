package me.isaiah.multiworld.neoforge;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import me.isaiah.multiworld.ICreator;
import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

public class NeoForgeWorldCreator implements ICreator {
    
	public HashMap<String, RuntimeWorldConfig> worldConfigs;
	
	public NeoForgeWorldCreator() {
		this.worldConfigs = new HashMap<>();
	}
	
    public static void init() {
        MultiworldMod.setICreator(new NeoForgeWorldCreator());
    }

    public ServerLevel create_world(String id, ResourceLocation dim, ChunkGenerator gen, Difficulty dif, long seed) {
        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(dim_of(dim))
                .setGenerator(gen)
                .setDifficulty(dif)
				.setSeed(seed)
				.setShouldTickTime(true)
                ;

        Fantasy fantasy = Fantasy.get(MultiworldMod.mc);
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(ResourceLocation.parse(id), config);
        this.worldConfigs.put(id, config);
        return worldHandle.asWorld();
    }
    
    @Override
    public void set_difficulty(String id, Difficulty dif) {
    	this.worldConfigs.get(id).setDifficulty(dif);
    }
    
    private static ResourceKey<DimensionType> dim_of(ResourceLocation id) {
        return ResourceKey.create(Registries.DIMENSION_TYPE, id);
    }
    
    public void delete_world(String id) {
        Fantasy fantasy = Fantasy.get(MultiworldMod.mc);
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(ResourceLocation.parse(id), null);
        worldHandle.delete();
    }

	@Override
	public boolean is_the_end(ServerLevel world) {
		return world.dimensionTypeRegistration() == BuiltinDimensionTypes.END;
	}

	@Override
	public BlockPos get_pos(double x, double y, double z) {
		return BlockPos.containing(x, y, z);
	}
	
	@Override
	public BlockPos get_spawn(ServerLevel world) {
		return world.getLevelData().getSpawnPos();
	}

	@Override
	public void teleleport(ServerPlayer player, ServerLevel world, double x, double y, double z) {
		DimensionTransition target = new DimensionTransition(world, new Vec3(x, y, z), new Vec3(0, 0, 0), 0f, 0f, DimensionTransition.DO_NOTHING);
		// FabricDimensionInternals.changeDimension(player, world, target);

		// Per https://fabricmc.net/2024/05/31/121.html
		// for 1.21, FabricDimension API is replaced by teleportTo
		player.changeDimension(target);}
	
	@Override
	public ChunkGenerator get_flat_chunk_gen(MinecraftServer mc) {
		var biome = mc.registryAccess().registryOrThrow(Registries.BIOME).wrapAsHolder(mc.registryAccess().registryOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS));
        FlatLevelGeneratorSettings flat = new FlatLevelGeneratorSettings(Optional.empty(), biome, Collections.emptyList());
        FlatLevelSource generator = new CustomFlatChunkGenerator(flat);
        return generator;
	}
	
	// Custom Flat Gen
	class CustomFlatChunkGenerator extends FlatLevelSource {
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

	@Override
	public ChunkGenerator get_void_chunk_gen(MinecraftServer mc) {
		return this.get_flat_chunk_gen(mc);
	}

}