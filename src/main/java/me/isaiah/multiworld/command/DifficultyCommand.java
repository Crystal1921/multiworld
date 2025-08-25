package me.isaiah.multiworld.command;

import java.io.IOException;
import java.util.HashMap;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.config.FileConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;

public class DifficultyCommand implements Command {

    /**
     * Run difficulty command with native command logic
     * @param mc MinecraftServer instance
     * @param plr ServerPlayer executing the command
     * @param difficulty Difficulty level
     * @param worldName Target world name (can be null for current world)
     */
    public static int run(MinecraftServer mc, ServerPlayer plr, String difficulty, String worldName) {
        ServerLevel w = (ServerLevel) plr.level();

        // Handle world selection
        if (worldName != null && !worldName.isEmpty()) {
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

            if (worlds.containsKey(processedWorldName)) {
                w = worlds.get(processedWorldName);
            }
        }

		Difficulty d;

		// String to Difficulty with default fallback
		if (difficulty.equalsIgnoreCase("EASY"))         { d = Difficulty.EASY; }
		else if (difficulty.equalsIgnoreCase("HARD"))    { d = Difficulty.HARD; }
		else if (difficulty.equalsIgnoreCase("NORMAL"))  { d = Difficulty.NORMAL; }
		else if (difficulty.equalsIgnoreCase("PEACEFUL")){ d = Difficulty.PEACEFUL; }
		else {
			MultiworldCommand.message(plr, "Invalid difficulty: " + difficulty + ". Valid values: PEACEFUL, EASY, NORMAL, HARD");
			return 0;
		}

        MultiworldMod.get_world_creator().setDifficulty(w.dimension().location().toString(), d);

        try {
			FileConfiguration config = Util.get_config(w);
			config.set("difficulty", difficulty);
			config.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
        MultiworldCommand.message(plr, "[&cMultiworld&r]: Difficulty of world '" + w.dimension().location().toString() + "' is now set to: " + difficulty);
        return 1;
    }

    /**
     * Legacy Run Command - kept for backwards compatibility
     * @deprecated Use run(MinecraftServer, ServerPlayer, String, String) instead
     */
    @Deprecated
    public static int run(MinecraftServer mc, ServerPlayer plr, String[] args) {
        if (args.length < 2) {
			MultiworldCommand.message(plr, "[&4Multiworld&r] Usage: /mw difficulty <value> [world id]");
			return 0;
		}
        
        String difficulty = args[1];
        String worldName = (args.length >= 3) ? args[2] : null;
        
        return run(mc, plr, difficulty, worldName);
    }

}