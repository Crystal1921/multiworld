/**
 * Multiworld Mod
 * Copyright (c) 2021-2024 by Isaiah.
 */
package me.isaiah.multiworld;

import lombok.Getter;
import me.isaiah.multiworld.command.*;
import me.isaiah.multiworld.command.commands.CreateCommand;
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
    /**
     * -- GETTER --
     *  Gets the Multiversion ICreator instance
     */
    @Getter
    public static ICreator worldCreator;

    public static void setICreator(ICreator ic) {
        worldCreator = ic;
    }

    public static ServerLevel createWorld(String id, ResourceLocation dim, ChunkGenerator gen, Difficulty dif, long seed) {
        return worldCreator.createWorld(id, dim, gen, dif, seed);
    }

    /**
     * ModInitializer onInitialize
     */
    public static void init() {
        System.out.println("Multiworld init");
    }

    public static ResourceLocation newId(String id) {
        // tryParse works from 1.18 to 1.21+
        return ResourceLocation.tryParse(id);
    }

    // On server start
    public static void onServerStarted(MinecraftServer mc) {
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
                        CreateCommand.reinitWorldFromConfig(mc, id);
                    }
                }
            }

            int loaded = Portal.reinitPortalsFromConfig(mc);
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
            return textPlain(message);
        }
    }

    public static Component textPlain(String txt) {
        return Component.nullToEmpty(txt);
    }

}