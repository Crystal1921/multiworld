package me.isaiah.multiworld;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Utility interface for cross-version development support
 * See implementation in "Multiworld-Fabric-1.XX.X/src/"
 */
public interface ICreator {

	/**
	 */
	public ServerLevel createWorld(String id, ResourceLocation dim, ChunkGenerator gen, Difficulty dif, long seed);

	/**
	 */
	public BlockPos getPos(double x, double y, double z);

	/**
	 */
	@Deprecated
	public default Component coloredLiteral(String txt, ChatFormatting color) {
		try {
			return Component.nullToEmpty(txt).copy().withStyle(color);
		} catch (Exception | IncompatibleClassChangeError e) {
			// MutableText interface was changed to a class in 1.19;
			// Incase for 1.18:
			return Component.nullToEmpty(txt);
		}
	}

	/**
	 */
	void teleport(ServerPlayer player, ServerLevel world, double x, double y, double z);

	/**
	 */
	void setDifficulty(String id, Difficulty dif);

    /**
     * Return a {@link ChunkGenerator} for the given vanilla environment,
     * or NULL if the passed argument is not NORMAL / NETHER / END.
     */
    default ChunkGenerator getChunkGen(MinecraftServer mc, String env) {
    	ChunkGenerator gen = null;
    	if (env.contains("NORMAL") || env.contains("DEFAULT")) {
			gen = mc.getLevel(Level.OVERWORLD).getChunkSource().getGenerator(); // .withSeed(seed);
		}

		if (env.contains("NETHER")) {
			gen = mc.getLevel(Level.NETHER).getChunkSource().getGenerator();
		}
		
		if (env.contains("END")) {
			gen = mc.getLevel(Level.END).getChunkSource().getGenerator(); // .withSeed(seed);
		}
		
		if (env.contains("FLAT")) {
			FlatLevelSource genn = (FlatLevelSource) this.getVoidGen(mc);

			FlatLevelGeneratorSettings flat = genn.settings();
			
			FlatLayerInfo[] layers = {
					new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
					new FlatLayerInfo(5, Blocks.DIRT),
					new FlatLayerInfo(2, Blocks.BEDROCK)
			};
	        
	        for (int i = layers.length - 1; i >= 0; --i) {
	            flat.getLayersInfo().add(layers[i]);
	        }

	        flat.updateLayers();

			return genn;
		}
		
		if (env.contains("VOID")) {
			return this.getVoidChunkGen(mc);
		}

		return gen;
    } 

	// TODO: move to icommonlib ?:
	public BlockPos getSpawn(ServerLevel world);
	public boolean isTheEnd(ServerLevel world);
	public ChunkGenerator getVoidGen(MinecraftServer mc);
	public ChunkGenerator getVoidChunkGen(MinecraftServer mc);

	void deleteWorld(String id);
	
}