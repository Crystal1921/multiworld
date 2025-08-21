package me.isaiah.multiworld.command;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import me.isaiah.multiworld.ConsoleCommand;
import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

public class DeleteCommand implements Command {

	public static Logger LOGGER = ConsoleCommand.LOGGER;

	private static HashMap<String, Long> map = new HashMap<>();
	
	/**
	 * Run Command
	 */
    public static int run(MinecraftServer mc, CommandSourceStack source, String[] args) {
        if (args.length == 1) {
        	LOGGER.error("Usage: /mw delete <id>");
            return 0;
        }

        String id = args[1];
        
        if (!map.containsKey(id)) {
        	map.put(id, System.currentTimeMillis() );
            source.sendSuccess(() -> Component.literal("Delete request for world \"" + id + "\" received. Type the command again to confirm."), false);
        	return 1;
        }
        
        long start = map.get(id);
        long now = System.currentTimeMillis();
        long TIMEOUT = 20_000;
        
        if (now - start > TIMEOUT) {
            source.sendSuccess(() -> Component.literal("Delete request timed-out (>20s). Please try again."), false);
        	map.remove(id);
        	return 0;
        }

        source.sendSuccess(() -> Component.literal("Deleting multiworld config for \"" + id + "\"..."), false);
        try {
			File config = Util.get_config_file(MultiworldMod.new_id(id));
			config.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
        source.sendSuccess(() -> Component.literal("Deleting world folder \"" + id + "\"..."), false);
        MultiworldMod.get_world_creator().deleteWorld(id);

        return 1;
    }

}