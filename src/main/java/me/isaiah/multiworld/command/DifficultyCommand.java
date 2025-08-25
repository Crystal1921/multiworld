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

    public static int run(MinecraftServer mc, ServerPlayer plr, String[] args) {
        ServerLevel w = (ServerLevel) plr.level();

		if (args.length < 2) {
			MultiworldCommand.message(plr, "[&4Multiworld&r] Usage: /mw difficulty <value> [world id]");
			return 1;
		}
		
        String a1 = args[1];
        // String a2 = args[2];
        
        if (args.length >= 3) {
        	String a2 = args[2];
        	
        	HashMap<String,ServerLevel> worlds = new HashMap<>();
            mc.levelKeys().forEach(r -> {
                ServerLevel world = mc.getLevel(r);
                worlds.put(r.location().toString(), world);
            });

            if (a2.indexOf(':') == -1) a2 = "multiworld:" + a2;

            if (worlds.containsKey(a2)) {
                w = worlds.get(a2);
            }
        }

		Difficulty d;

		// String to Difficulty
		if (a1.equalsIgnoreCase("EASY"))         { d = Difficulty.EASY; }
		else if (a1.equalsIgnoreCase("HARD"))    { d = Difficulty.HARD; }
		else if (a1.equalsIgnoreCase("NORMAL"))  { d = Difficulty.NORMAL; }
		else if (a1.equalsIgnoreCase("PEACEFUL")){ d = Difficulty.PEACEFUL; }
		else {
			MultiworldCommand.message(plr, "Invalid difficulty: " + a1);
			return 1;
		}

        MultiworldMod.get_world_creator().setDifficulty(w.dimension().location().toString(), d);

        try {
			FileConfiguration config = Util.get_config(w);
			config.set("difficulty", a1);
			config.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        MultiworldCommand.message(plr, "[&cMultiworld&r]: Difficulty of world '" + w.dimension().location().toString() + "' is now set to: " + a1);
        return 1;
    }

}