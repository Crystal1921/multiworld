package me.isaiah.multiworld.command;

import static me.isaiah.multiworld.MultiworldMod.message;
import static me.isaiah.multiworld.MultiworldMod.text_plain;

import java.io.File;
import java.io.IOException;

import me.isaiah.multiworld.config.FileConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class SetspawnCommand implements Command {

    public static int run(MinecraftServer mc, ServerPlayer plr, String[] args) {
        Level w = plr.level();
        BlockPos pos = plr.blockPosition();
        try {
            setSpawn(w, pos);
			
			String txt = "Spawn for world \"" + w.dimension().location() + "\" changed to " + pos.toShortString();
            message(plr, "&6" + txt);
        } catch (IOException e) {
            plr.displayClientMessage(text_plain("Error: " + e.getMessage()), false);
            e.printStackTrace();
        }
        return 1;
    }

    public static void setSpawn(Level w, BlockPos spawn) throws IOException {
        File cf = new File(Util.get_platform_config_dir(), "multiworld"); 
        cf.mkdirs();

        File worlds = new File(cf, "worlds");
        worlds.mkdirs();

        ResourceLocation id = w.dimension().location();
        File namespace = new File(worlds, id.getNamespace());
        namespace.mkdirs();

        File wc = new File(namespace, id.getPath() + ".yml");
        wc.createNewFile();
        FileConfiguration config = new FileConfiguration(wc);

        config.set("spawnpos", spawn.asLong());
        config.save();
    }


}