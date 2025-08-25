package me.isaiah.multiworld.command;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import me.isaiah.multiworld.I18n;
import me.isaiah.multiworld.InfoSuggest;
import me.isaiah.multiworld.config.FileConfiguration;
import me.isaiah.multiworld.perm.Perm;
import me.isaiah.multiworld.portal.Portal;
import me.isaiah.multiworld.portal.WandEventHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import static me.isaiah.multiworld.command.MultiworldCommand.message;

public class PortalCommand implements Command {

	public static HashMap<String, Portal> KNOWN_PORTALS = new HashMap<>();
	private static final String DEST_POS_REGEX = "^e:[a-z0-9_]+:[a-z0-9_]+:-?\\d+,-?\\d+,-?\\d+$";
	private static final String DEST_REGEX = "^e:[a-z0-9_]+:[a-z0-9_]+";
	
	public static void addKnownPortal(String key, Portal value) {
		String lower = key.toLowerCase(Locale.ROOT);
		KNOWN_PORTALS.put(lower, value);
	}
	
	public static Portal getKnownPortal(String key) {
		String lower = key.toLowerCase(Locale.ROOT);
		return KNOWN_PORTALS.get(lower);
	}
	
	// private static ItemStack wand = new ItemStack(Items.WOODEN_SHOVEL);
	
	/**
	 * Command Help Message
	 */
	public static String[] COMMAND_HELP = {
			"Multiworld Portals Command:",
			"&a/mw portal create <name> [destination]&r - Creates a new portal from the wand area",
			"&a/mw portal select <name>&r - TODO",
			"&a/mw portal wand&r - Gives a Portal Creation Wand",
			"&a/mw portal info <name>&r - Displays information about a portal.",
			"&a/mw portal remove <name>&r", // Remove the portal whose name is given.",
	};
	
	/**
	 * Valid Subcommands
	 */
	public static String[] SUBCOMMANDS = {
			"create", "wand", "info", "remove"
	};

	/**
	 * Execute the Command
	 */
	public static int run(MinecraftServer mc, ServerPlayer plr, String[] args) {

		// Portal Command Help
		if (args.length == 1) {
			for (String s : COMMAND_HELP) {
				message(plr, s);
            }
			return 0;
		}
		
		if (!Util.isForgeOrHasICommon()) {
			message(plr,  "&4WARN: iCommonLib is required for Portals to function properly");
			// return 0;
		}
		
		// Refresh
		if (args[1].equalsIgnoreCase("refresh")) {
			if (args.length < 3) {
				for (Portal p : KNOWN_PORTALS.values()) {
					p.refreshPortalArea();
				}
				return 1;
			}
			
			String name = args[2];
			Portal p = KNOWN_PORTALS.getOrDefault(name, getPortalIgnoreCase(name));
			if (null == p) {
				message(plr, "&4Portal with the name " + name + " not found!");
			}
			p.refreshPortalArea();
		}
		
		// Portal Info Command
		if (args[1].equalsIgnoreCase("info")) {
			if (args.length < 3) {
				message(plr, "&6Multiworld Portals (" + KNOWN_PORTALS.size() + "):");
				for (Portal p : KNOWN_PORTALS.values()) {
					message(plr, " Portal: \"" + p.getName() + "\": ");
					String from = p.getOriginWorldId() + " (" + p.getMinPos().toShortString();
					String to = p.getDestWorldName() + " (" + p.getDestLocation().toShortString();
					message(plr, " - " + from + ") -> " + to + ")");
				}
				return 1;
			}
			
			String name = args[2];
			Portal p = KNOWN_PORTALS.getOrDefault(name, getPortalIgnoreCase(name));
			if (null == p) {
				message(plr, "&4Portal with the name " + name + " not found!");
			}
			message(plr, "&6Multiworld Portals:");
			message(plr, " Portal: \"" + p.getName() + "\": ");
			message(plr, "  &6- From:&r " + p.getOriginWorldId() + " @ (" + p.getMinPos().toShortString() + ")");
			message(plr, "  &6- To:&r " + p.getDestWorldName() + " @ (" + p.getDestLocation().toShortString() + ")");
			message(plr, "  &6- Destination:&r " + p.getDestination());
			message(plr, "  &6- Portal Frame:&r " + p.getLocationConfigString());
		}
		
		// Portal Wand Command
		if (args[1].equalsIgnoreCase("wand")) {
			if (!Perm.has(plr, "multiworld.portal.wand")) {
				message(plr, "Invalid permission! Missing: multiworld.portal.wand");
				return 0;
			}
			
			message(plr, "&aGiving wand!");
			plr.addItem(WandEventHandler.getItemStack().copy());

			return 1;
		}
		
		// Portal Subcommand Help
		if (args.length == 2) {
			message(plr, I18n.CMD_PORTAL_USAGE);
			return 0;
		}
		
		// Portal Create Command
		if (args[1].equalsIgnoreCase("create")) {
			if (args.length < 4) {
				// arguments
				message(plr, I18n.CMD_PORTAL_USAGE_CREATE);
				return 0;
			}
			
			if (!Perm.has(plr, "multiworld.portal.create")) {
				message(plr, "Invalid permission! Missing: multiworld.portal.create");
				return 0;
			}

			return createPortal(plr, args);
		}

		if (args[1].equalsIgnoreCase("remove")) {
			return removePortal(plr, args);
		}
		
		return 1;
	}
	
	private static Portal getPortalIgnoreCase(String name) {
		for (String s : KNOWN_PORTALS.keySet()) {
			if (name.equalsIgnoreCase(s)) {
				return KNOWN_PORTALS.get(s);
			}
		}
		return null;
	}
	
	private static int createPortal(ServerPlayer plr, String[] args) {
		Object[] poss = WandEventHandler.getWandPositionsOrNull(plr.getUUID());
		
		if (null == poss) {
			message(plr, I18n.CMD_PORTAL_NO_SELECTION);
			return 0;
		}
		
		ServerLevel world = (ServerLevel) poss[0];
		BlockPos pos1 = (BlockPos) poss[1];
		BlockPos pos2 = (BlockPos) poss[2];
		
		if (null == world) {
			world = (ServerLevel) plr.level();
		}
		
		if (null == pos1 || null == pos2) {
			message(plr, "Missing one position (/mw portal wand)");
			return 0;
		}
		
		String name = args[2];
		String nameL = name.toLowerCase(Locale.ROOT);
		String dest = args[3];

        if (!isValidDestination(dest)) {
			message(plr, "&4Invalid destination format! Use: e:<world>:<x>,<y>,<z> or <world>:<x>,<y>,<z>");
            return 0;
        }

		if (KNOWN_PORTALS.containsKey(nameL)) {
			message(plr, "&4A Portal with the name \"" + name + "\" already exists!");
			return 0;
		}

		Portal p = new Portal(
				name,
				plr.getName().getString(),
				world.dimension().location(),
				dest,
				pos1,
				pos2 
				);

		if (args.length == 5) {
			String isTransport = args[4];
			p.buildPortalArea(pos1, pos2, world, Boolean.parseBoolean(isTransport));
		} else {
			p.buildPortalArea(pos1, pos2, world);
		}

		KNOWN_PORTALS.put(nameL, p);
		try {
			p.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		message(plr, "&aPortal Created.");
		return 1;
	}

	private static int removePortal(ServerPlayer plr, String[] args) {
		String arg = args[2];
		if (KNOWN_PORTALS.containsKey(arg)) {
			KNOWN_PORTALS.remove(arg);
			delete(arg);
			message(plr, "&aPortal \"" + arg + "\" deleted.");
		} else {
			message(plr, "&4Portal \"" + arg + "\" not found.");
			return 0;
		}

		return 1;
	}

	/**
	 */
	public static void make_config(Portal p) {
        try {
			p.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	 /**
     * Portal Command, "/mw portal"
     * 
     * "/mw portal <arg1> <arg2> <arg3> etc..."
     */
    public static void getSuggestions_PortalCommand(SuggestionsBuilder builder, String input, String[] cmds, CommandSourceStack plr, boolean ALL) {

    	// Argument 1:
    	if (cmds.length <= 2 || (cmds.length <= 3 && !input.endsWith(" "))) {
    		for (String s : PortalCommand.SUBCOMMANDS) {
                builder.suggest(s);
            }
    		return;
    	}
    	
    	String arg1 = cmds[2];
    	
    	// Argument 2:
    	if (cmds.length <= 3 || (cmds.length <= 4 && !input.endsWith(" ")) ) {
    		if (arg1.equalsIgnoreCase("create")) {
    			builder.suggest("myPortalName");
    			return;
    		}
    		
    		if (arg1.equalsIgnoreCase("info") || arg1.equalsIgnoreCase("select") || arg1.equalsIgnoreCase("remove") || arg1.equalsIgnoreCase("refresh")) {
    			for (Portal p : PortalCommand.KNOWN_PORTALS.values()) {
    				builder.suggest(p.getName());
    			}
    		}
    		return;
    	}
    	
    	// Argument 3:
    	if (cmds.length <= 4 || (cmds.length <= 5 && !input.endsWith(" ")) ) {
    		if (arg1.equalsIgnoreCase("create")) {

    			// Suggest Worlds
    			for (String s : InfoSuggest.getWorldNames()) {
    				builder.suggest(s);
    			}

    			// Suggest Portals
    			for (Portal p : PortalCommand.KNOWN_PORTALS.values()) {
    				builder.suggest("p:" + p.getName());
    			}
    			
    			// Suggest Exact Player Pos
    			Vec3 pos = plr.getPosition();
    			ServerLevel w = plr.getLevel();

    			ResourceLocation id = w.dimension().location();
    			String loc = round(pos.x()) + "," + round(pos.y()) + "," + round(pos.z());
    			builder.suggest("e:" + id + ":" + loc);
    			
    			return;
    		}
    		return;
    	}
    }
    
    private static double round(double d) {
    	return Math.round(d * 100.0) / 100.0;
    }

	public static void delete(String name){
		File configDir = new File("config");
		File cf = new File(configDir, "multiworld");
		File wc = new File(cf, "portals.yml");

		if (!wc.exists()) {
			// 文件不存在就不用删除
			return;
		}
		String prefix = "portals." + name;

		try {
			FileConfiguration config = new FileConfiguration(wc);

			config.remove(prefix + ".entryfee.amount");
			config.remove(prefix + ".entryfee");
			config.remove(prefix + ".safeteleport");
			config.remove(prefix + ".teleportnonplayers");
			config.remove(prefix + ".handlerscript");

			config.remove(prefix + ".owner"); // player
			config.remove(prefix + ".location"); // x1,y1,z1:x2,y2,z2
			config.remove(prefix + ".world");
			config.remove(prefix + ".destination");
			config.remove(prefix);

			config.save();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static boolean isValidDestination(String dest) {
        return dest.matches(DEST_POS_REGEX) || dest.matches(DEST_REGEX);
    }
}