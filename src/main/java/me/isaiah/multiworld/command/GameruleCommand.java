package me.isaiah.multiworld.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.config.FileConfiguration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.IntegerValue;
import net.minecraft.world.level.GameRules.Value;
import net.minecraft.world.level.Level;

public class GameruleCommand implements Command {

	// TODO
	public static GameRules getGameRules(ServerLevel world) {
		return world.getGameRules();
	}

	@SuppressWarnings("rawtypes")
	public static HashMap<String, GameRules.Key> keys = new HashMap<>();
	
    /**
     * Run gamerule command with native command logic
     * @param mc MinecraftServer instance
     * @param plr ServerPlayer executing the command
     * @param rule Gamerule name
     * @param value Gamerule value (can be null to query current value)
     */
	public static int run(MinecraftServer mc, ServerPlayer plr, String rule, String value) {
        ServerLevel w = (ServerLevel) plr.level();

        return setGamerule(mc, plr, rule, value, w, true);
    }

    @SuppressWarnings("unchecked")
    public static int setGamerule(MinecraftServer mc, ServerPlayer plr, String rule, String value, ServerLevel w, boolean showMessage) {
        if (keys.isEmpty()) {
            setup(w);
        }

        // Query current value if no value provided
        if (value == null || value.isEmpty()) {
            Value<?> currentRule = getGameRules(w).getRule(keys.get(rule));
            MultiworldCommand.message(plr, "[&4Multiworld&r] Value of " + rule + " is: " + currentRule);
            return 1;
        }

        // Set new value
        boolean isBool = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");

        if (isBool) {
        	// Boolean Rule
        	BooleanValue boolRule = (BooleanValue) getGameRules(w).getRule(keys.get(rule));
        	boolRule.set(Boolean.valueOf(value), mc);
        } else {
        	// Int Rule
        	IntegerValue intRule = (IntegerValue) getGameRules(w).getRule(keys.get(rule));
        	intRule.set(Integer.valueOf(value), mc);
        }

        // Save to world config
        try {
            setRuleConfig(w, rule, value);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (showMessage) {
            MultiworldCommand.message(plr, "[&cMultiworld&r]: Gamerule " + rule + " is now set to: " + value);
        }

        return 1;
    }

    /**
     * Read the Gamerule names – fetches gamerules from server
     */
    public static void setupServer(MinecraftServer server) {
        keys.clear();
        // Create a temporary GameRules instance to access the accept method
        server.getGameRules().visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            @Override
            public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                String name = key.getId();
                keys.put(name, key);
            }
        });
    }
    
    /**
     * Read the Gamerule names – fetches gamerules from world
     */
    public static void setup(ServerLevel world) {
        keys.clear();
        // Create a temporary GameRules instance to access the accept method
        world.getGameRules().visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            @Override
            public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                String name = key.getId();
                keys.put(name, key);
            }
        });
    }
    
    /**
     * Save the Gamerule to our world config
     * 
     * @param w - The World to apply the Gamerule
     * @param a - The Gamerule name (ex: "doDaylightCycle")
     * @param b - The value for the Gamerule (ex: "true", or "100")
     */
    public static void setRuleConfig(Level w, String a, String b) throws IOException {
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

        if (!config.is_set("gamerules")) {
        	config.set("gamerules", new ArrayList<String>());
        }

        config.set("gamerule_" + a, b);

        config.save();
    }

    /**
     * Load gamerule from config entry
     * 
     * @param world - The ServerWorld to apply the Gamerule
     * @param key - Config key for Gamerule (ex: "gamerule_doDaylightCycle")
     * @param val - The value for the Gamerule (ex: "true", or "100")
     * @see {@link CreateCommand#reinit_world_from_config(MinecraftServer, String)}
     */
	@SuppressWarnings("unchecked")
	public static void setGameruleFromConfig(ServerLevel world, String key, String val) {
		if (keys.isEmpty()) {
			setup(world);
		}

        String name = key.replace("gamerule_", "").trim();
        String a1 = val.trim();
		
		boolean is_bol = false;
        
        if (a1.equalsIgnoreCase("true") || a1.equalsIgnoreCase("false")) {
        	is_bol = true;
        }
		
        if (is_bol) {
        	// Boolean Rule
        	BooleanValue rule = (BooleanValue) getGameRules(world).getRule(keys.get(name));
        	rule.set(Boolean.valueOf(a1), MultiworldMod.mc);
        } else {
        	// Int Rule
        	IntegerValue rule = (IntegerValue) getGameRules(world).getRule(keys.get(name));
        	rule.set(Integer.valueOf(a1), MultiworldMod.mc);
        }
		
	}


}
