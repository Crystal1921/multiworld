/**
 * Multiworld Mod
 * Copyright (c) 2021-2024 by Isaiah.
 */
package me.isaiah.multiworld;

import me.isaiah.multiworld.command.*;
import me.isaiah.multiworld.portal.Portal;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Multiworld Mod
 */
public class MultiworldMod {

    public static final Logger LOGGER = LoggerFactory.getLogger("multiworld");

    public static final String MOD_ID = "multiworld";
    // Mod Version
    public static final String VERSION = "1.10";
    public static MinecraftServer mc;
    public static String CMD = "mv";
    public static ICreator world_creator;
    public static String[] COMMAND_HELP = {
            "&4Multiworld Mod Commands:&r",
            "&a/mw spawn&r - Teleport to current world spawn",
            "&a/mw setspawn&r - Sets the current world spawn",
            "&a/mw tp <id>&r - Teleport to a world",
            "&a/mw list&r - List all worlds",
            "&a/mw gamerule <rule> <value>&r - Change a worlds Gamerules",
            "&a/mw create <id> <env> [-g=<generator> -s=<seed>]&r - create a new world",
            "&a/mw difficulty <value> [world id] - Sets the difficulty of a world"
    };

    public static void setICreator(ICreator ic) {
        world_creator = ic;
    }

    /**
     * Gets the Multiversion ICreator instance
     */
    public static ICreator get_world_creator() {
        return world_creator;
    }

    public static ServerLevel create_world(String id, ResourceLocation dim, ChunkGenerator gen, Difficulty dif, long seed) {
        return world_creator.createWorld(id, dim, gen, dif, seed);
    }

    /**
     * ModInitializer onInitialize
     */
    public static void init() {
        System.out.println("Multiworld init");
    }

    public static ResourceLocation new_id(String id) {
        // tryParse works from 1.18 to 1.21+
        return ResourceLocation.tryParse(id);
    }

    // On server start
    public static void on_server_started(MinecraftServer mc) {
        MultiworldMod.mc = mc;

        // LOGGER.info("Registering events...");
        // WandEventHandler.register();

        File cfg_folder = new File("config");
        if (cfg_folder.exists()) {
            File folder = new File(cfg_folder, "multiworld");
            File worlds = new File(folder, "worlds");
            if (worlds.exists()) {
                for (File f : worlds.listFiles()) {
                    if (f.getName().equals("minecraft")) {
                        continue;
                    }
                    for (File fi : f.listFiles()) {
                        String id = f.getName() + ":" + fi.getName().replace(".yml", "");
                        LOGGER.info("Found saved world " + id);
                        CreateCommand.reinit_world_from_config(mc, id);
                    }
                }
            }

            int loaded = Portal.reinit_portals_from_config(mc);
            if (loaded > 0) {
                LOGGER.info("Found " + loaded + " saved world portals.");
            }
        }
    }

    public static Component text(String message) {
        try {
            return Component.nullToEmpty(MultiworldCommand.translate_alternate_color_codes('&', message));
        } catch (Exception e) {
            e.printStackTrace();
            return text_plain(message);
        }
    }

    public static Component text_plain(String txt) {
        return Component.nullToEmpty(txt);
    }

}