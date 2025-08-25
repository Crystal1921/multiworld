package me.isaiah.multiworld.command;

import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import java.io.File;

import me.isaiah.multiworld.I18n;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.config.*;

public class TpCommand implements Command {

    /**
     * Run teleport command with native command logic
     * @param mc MinecraftServer instance
     * @param plr ServerPlayer to teleport (can be null for console commands)
     * @param worldName Target world name (will be prefixed with "multiworld:" if no namespace)
     * @param targetPlayerName Target player name (for console teleporting another player, can be null)
     */
    public static int run(MinecraftServer mc, ServerPlayer plr, String worldName, String targetPlayerName) {
        HashMap<String,ServerLevel> worlds = new HashMap<>();
        mc.levelKeys().forEach(r -> {
            ServerLevel world = mc.getLevel(r);
            worlds.put(r.location().toString(), world);
        });
        
        // Default namespace handling
        String processedWorldName = worldName;
        if (worldName.indexOf(':') == -1) {
            processedWorldName = "multiworld:" + worldName;
        }
        
        // Handle console commands
        if (null == plr) {
        	if (targetPlayerName == null || targetPlayerName.isEmpty()) {
        		return 0;
        	}
        	plr = mc.getPlayerList().getPlayerByName(targetPlayerName);
        	if (plr == null) {
        		return 0;
        	}
        }

        if (worlds.containsKey(processedWorldName)) {
            ServerLevel w = worlds.get(processedWorldName);
            // BlockPos sp = multiworld_method_43126(w);
            BlockPos sp = SpawnCommand.getSpawn(w);
			
			boolean isEnd = false;
			
			try {
				boolean is_the_end = MultiworldMod.get_world_creator().isTheEnd(w);
				if (is_the_end) {
					isEnd = true;
				}
			} catch (NoSuchMethodError | Exception e) {
			}
			
			String env = read_env_from_config(processedWorldName);
			if (null != env) {
				if (env.equalsIgnoreCase("END")) {
					isEnd = true;
				}
			}

			if (isEnd) {
				//ServerWorld.createEndSpawnPlatform(w);
				method_29200_createEndSpawnPlatform(w);
				sp = ServerLevel.END_SPAWN_POINT;
			}
			
            if (null == sp) {
                // Send Null Spawn Message
                I18n.message(plr, I18n.NULL_SPAWN);
                sp = new BlockPos(1, 40, 1);
            }
            // plr.sendMessage(text("Teleporting...", Formatting.GOLD), false);
            
            // Send Teleporting Message
            I18n.message(plr, I18n.TELEPORTING);

            sp = findSafePos(w, sp);

            // TeleportTarget target = new TeleportTarget(new Vec3d(sp.getX(), sp.getY(), sp.getZ()), new Vec3d(0, 0, 0), 0f, 0f);
            // FabricDimensionInternals.changeDimension(plr, w, target);

            MultiworldMod.get_world_creator().teleport(plr, w, sp.getX(), sp.getY(), sp.getZ());
            
            return 1;
        }
        return 1;
    }

    /**
     * Legacy Run Command - kept for backwards compatibility
     * @deprecated Use run(MinecraftServer, ServerPlayer, String, String) instead
     */
    @Deprecated
    public static int run(MinecraftServer mc, ServerPlayer plr, String[] args) {
        if (args.length < 2) {
            return 0;
        }
        
        String worldName = args[1];
        String targetPlayerName = (args.length > 2) ? args[2] : null;
        
        return run(mc, plr, worldName, targetPlayerName);
    }
    
    /**
     * net.minecraft.class_3218.method_29200
     * 
     * TODO: check why method_29200 removed in 1.20.1
     */
    public static void method_29200_createEndSpawnPlatform(ServerLevel world) {
        BlockPos lv = ServerLevel.END_SPAWN_POINT;
        int i = lv.getX();
        int j = lv.getY() - 2;
        int k = lv.getZ();
        BlockPos.betweenClosed(i - 2, j + 1, k - 2, i + 2, j + 3, k + 2).forEach(pos -> world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState()));
        BlockPos.betweenClosed(i - 2, j, k - 2, i + 2, j, k + 2).forEach(pos -> world.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState()));
    }

    private static BlockPos findSafePos(ServerLevel w, BlockPos sp) {
        BlockPos pos = sp;
        while (w.getBlockState(pos) != Blocks.AIR.defaultBlockState()) {
            pos = pos.offset(0, 1, 0);
        }
        return pos;
    }
	
	// getSpawnPos
	public static BlockPos multiworld_method_43126(ServerLevel world) {
        return SpawnCommand.multiworld_method_43126(world);
    }
	
	public static String read_env_from_config(String id) {
        File config_dir = new File("config");
        config_dir.mkdirs();
		
		String[] spl = id.split(":");
        
        File cf = new File(config_dir, "multiworld"); 
        cf.mkdirs();

        File worlds = new File(cf, "worlds");
        worlds.mkdirs();

        File namespace = new File(worlds, spl[0]);
        namespace.mkdirs();

        File wc = new File(namespace, spl[1] + ".yml");
        FileConfiguration config;
        try {
			if (!wc.exists()) {
				wc.createNewFile();
			}
            config = new FileConfiguration(wc);
			String env = config.getString("environment");
			return env;
        } catch (Exception e) {
            e.printStackTrace();
        }
		return "NORMAL";
    }

}
