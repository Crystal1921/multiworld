package me.isaiah.multiworld.command.commands;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import net.minecraft.network.chat.Component;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

public class DeleteCommand implements Command {

	private static HashMap<String, Long> map = new HashMap<>();
	
	/**
	 * Run delete command with native command logic
	 * @param mc MinecraftServer instance
	 * @param source CommandSourceStack executing the command
	 * @param worldId World identifier to delete
	 */
    public static int run(MinecraftServer mc, CommandSourceStack source, String worldId) {
        var player = source.getPlayer();
        if (!map.containsKey(worldId)) {
        	map.put(worldId, System.currentTimeMillis() );
            if (player != null) {
                player.sendSystemMessage(Component.literal("Delete request for world \"" + worldId + "\" received. Type the command again to confirm."));
            } else {
                source.sendSuccess(() -> Component.literal("Delete request for world \"" + worldId + "\" received. Type the command again to confirm."), false);
            }
        	return 1;
        }
        
        long start = map.get(worldId);
        long now = System.currentTimeMillis();
        long TIMEOUT = 20_000;
        
        if (now - start > TIMEOUT) {
            if (player != null) {
                player.sendSystemMessage(Component.literal("Delete request timed-out (>20s). Please try again."));
            } else {
                source.sendSuccess(() -> Component.literal("Delete request timed-out (>20s). Please try again."), false);
            }
        	map.remove(worldId);
        	return 0;
        }

        if (player != null) {
            player.sendSystemMessage(Component.literal("Deleting multiworld config for \"" + worldId + "\"..."));
        } else {
            source.sendSuccess(() -> Component.literal("Deleting multiworld config for \"" + worldId + "\"..."), false);
        }
        try {
			File config = Util.get_config_file(MultiworldMod.new_id(worldId));
			config.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
        if (player != null) {
            player.sendSystemMessage(Component.literal("Deleting world folder \"" + worldId + "\"..."));
        } else {
            source.sendSuccess(() -> Component.literal("Deleting world folder \"" + worldId + "\"..."), false);
        }
        MultiworldMod.getWorldCreator().deleteWorld(worldId);

        return 1;
    }

}