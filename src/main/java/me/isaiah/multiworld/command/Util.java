package me.isaiah.multiworld.command;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.config.FileConfiguration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class Util {

	public static Locale AMERICAN_STANDARD = Locale.ROOT; 
	
	// Dimension Ids
    public static final ResourceLocation OVERWORLD_ID = id("overworld");
    public static final ResourceLocation THE_NETHER_ID = id("the_nether");
    public static final ResourceLocation THE_END_ID = id("the_end");
    
    // todo
    public static final ResourceLocation VOID_ID = id("the_void");
    
   

    public static ResourceLocation id(String id) {
    	return MultiworldMod.new_id(id);
    }
    
    public static File get_platform_config_dir() {
    	return new File("config");
    }
    
    /**
     * 
     */
    public static FileConfiguration get_config(Level w) throws IOException {
        File cf = new File(get_platform_config_dir(), "multiworld"); 
        cf.mkdirs();

        File worlds = new File(cf, "worlds");
        worlds.mkdirs();

        ResourceLocation id = w.dimension().location();
        File namespace = new File(worlds, id.getNamespace());
        namespace.mkdirs();

        File wc = new File(namespace, id.getPath() + ".yml");
        wc.createNewFile();
        FileConfiguration config = new FileConfiguration(wc);
        return config;
    }
    
    
    /**
     * 
     */
    public static FileConfiguration get_config(ResourceLocation id) throws IOException {
        File cf = new File(get_platform_config_dir(), "multiworld"); 
        cf.mkdirs();

        File worlds = new File(cf, "worlds");
        worlds.mkdirs();

        File namespace = new File(worlds, id.getNamespace());
        namespace.mkdirs();

        File wc = new File(namespace, id.getPath() + ".yml");
        wc.createNewFile();
        FileConfiguration config = new FileConfiguration(wc);
        return config;
    }
    
    /**
     * 
     */
    public static File get_config_file(ResourceLocation id) throws IOException {
        File cf = new File(get_platform_config_dir(), "multiworld"); 
        cf.mkdirs();

        File worlds = new File(cf, "worlds");
        worlds.mkdirs();

        File namespace = new File(worlds, id.getNamespace());
        namespace.mkdirs();

        File wc = new File(namespace, id.getPath() + ".yml");
        return wc;
    }

}