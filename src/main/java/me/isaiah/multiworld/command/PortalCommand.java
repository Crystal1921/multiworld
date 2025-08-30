package me.isaiah.multiworld.command;

import me.isaiah.multiworld.I18n;
import me.isaiah.multiworld.config.FileConfiguration;
import me.isaiah.multiworld.perm.Perm;
import me.isaiah.multiworld.portal.Portal;
import me.isaiah.multiworld.portal.WandEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import static me.isaiah.multiworld.command.MultiworldCommand.message;

public class PortalCommand implements Command {

    private static final String DEST_POS_REGEX = "^e:[a-z0-9_]+:[a-z0-9_]+:-?\\d+,-?\\d+,-?\\d+$";
    private static final String DEST_REGEX = "^e:[a-z0-9_]+:[a-z0-9_]+";
    public static HashMap<String, Portal> KNOWN_PORTALS = new HashMap<>();
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

    // private static ItemStack wand = new ItemStack(Items.WOODEN_SHOVEL);

    public static void addKnownPortal(String key, Portal value) {
        String lower = key.toLowerCase(Locale.ROOT);
        KNOWN_PORTALS.put(lower, value);
    }

    public static Portal getKnownPortal(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        return KNOWN_PORTALS.get(lower);
    }

    /**
     * Run portal help command
     *
     * @param mc  MinecraftServer instance
     * @param plr ServerPlayer executing the command
     */
    public static int runHelp(MinecraftServer mc, ServerPlayer plr) {
        for (String s : COMMAND_HELP) {
            message(plr, s);
        }
        return 1;
    }

    /**
     * Run portal wand command
     *
     * @param mc  MinecraftServer instance
     * @param plr ServerPlayer executing the command
     */
    public static int runWand(MinecraftServer mc, ServerPlayer plr) {

        if (!true) {
            message(plr, "&4WARN: iCommonLib is required for Portals to function properly");
        }

        if (!Perm.has(plr, "multiworld.portal.wand")) {
            message(plr, "Invalid permission! Missing: multiworld.portal.wand");
            return 0;
        }

        message(plr, "&aGiving wand!");
        plr.addItem(WandEventHandler.getItemStack().copy());
        return 1;
    }

    /**
     * Run portal info command
     *
     * @param mc         MinecraftServer instance
     * @param plr        ServerPlayer executing the command
     * @param portalName Portal name (can be null to list all portals)
     */
    public static int runInfo(MinecraftServer mc, ServerPlayer plr, String portalName) {

        if (!true) {
            message(plr, "&4WARN: iCommonLib is required for Portals to function properly");
        }

        if (portalName == null || portalName.isEmpty()) {
            // List all portals
            message(plr, "&6Multiworld Portals (" + KNOWN_PORTALS.size() + "):");
            for (Portal p : KNOWN_PORTALS.values()) {
                message(plr, " Portal: \"" + p.getName() + "\": ");
                String from = p.getOriginWorldId() + " (" + p.getMinPos().toShortString();
                String to = p.getDestWorldName() + " (" + p.getDestLocation().toShortString();
                message(plr, " - " + from + ") -> " + to + ")");
            }
            return 1;
        }

        // Show specific portal info
        Portal p = KNOWN_PORTALS.getOrDefault(portalName, getPortalIgnoreCase(portalName));
        if (null == p) {
            message(plr, "&4Portal with the name " + portalName + " not found!");
            return 0;
        }
        message(plr, "&6Multiworld Portals:");
        message(plr, " Portal: \"" + p.getName() + "\": ");
        message(plr, "  &6- From:&r " + p.getOriginWorldId() + " @ (" + p.getMinPos().toShortString() + ")");
        message(plr, "  &6- To:&r " + p.getDestWorldName() + " @ (" + p.getDestLocation().toShortString() + ")");
        message(plr, "  &6- Destination:&r " + p.getDestination());
        message(plr, "  &6- Portal Frame:&r " + p.getLocationConfigString());
        return 1;
    }

    /**
     * Run portal create command
     *
     * @param mc          MinecraftServer instance
     * @param plr         ServerPlayer executing the command
     * @param portalName  Portal name
     * @param destination Portal destination (can be null for default)
     */
    public static int runCreate(MinecraftServer mc, ServerPlayer plr, String portalName, String destination, boolean isTransparent) {

        if (!true) {
            message(plr, "&4WARN: iCommonLib is required for Portals to function properly");
        }

        if (!Perm.has(plr, "multiworld.portal.create")) {
            message(plr, "Invalid permission! Missing: multiworld.portal.create");
            return 0;
        }

        if (destination == null || destination.isEmpty()) {
            message(plr, I18n.CMD_PORTAL_USAGE_CREATE);
            return 0;
        }

        return createPortal(plr, portalName, destination, isTransparent);
    }

    /**
     * Run portal remove command
     *
     * @param mc         MinecraftServer instance
     * @param plr        ServerPlayer executing the command
     * @param portalName Portal name to remove
     */
    public static int runRemove(MinecraftServer mc, ServerPlayer plr, String portalName) {

        if (!true) {
            message(plr, "&4WARN: iCommonLib is required for Portals to function properly");
        }

        // Create args array for the existing removePortal method
        String[] args = {"portal", "remove", portalName};
        return removePortal(plr, args);
    }

    private static Portal getPortalIgnoreCase(String name) {
        for (String s : KNOWN_PORTALS.keySet()) {
            if (name.equalsIgnoreCase(s)) {
                return KNOWN_PORTALS.get(s);
            }
        }
        return null;
    }

    private static int createPortal(ServerPlayer plr, String name, String dest, boolean isTransparent) {
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

        String nameL = name.toLowerCase(Locale.ROOT);

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

        p.buildPortalArea(pos1, pos2, world, isTransparent);

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

    public static void delete(String name) {
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