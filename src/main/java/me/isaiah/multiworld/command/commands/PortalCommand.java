package me.isaiah.multiworld.command.commands;

import me.isaiah.multiworld.I18n;
import me.isaiah.multiworld.config.FileConfiguration;
import me.isaiah.multiworld.perm.Perm;
import me.isaiah.multiworld.portal.Portal;
import me.isaiah.multiworld.portal.WandEventHandler;
import me.isaiah.multiworld.registry.DataAttachmentsRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        // Show specific portal info
        Portal p = KNOWN_PORTALS.getOrDefault(portalName, getPortalIgnoreCase(portalName));
        if (null == p) {
            message(plr, "&4Portal with the name " + portalName + " not found!");
            return 0;
        }
        message(plr, "&6Multiworld Portals:");
        message(plr, " Portal: \"" + p.getName() + "\": ");
        BlockPos addPos = p.getMinPos().offset(p.getMaxPos());
        BlockPos avgPos = new BlockPos(addPos.getX() / 2, addPos.getY() / 2, addPos.getZ() / 2);
        MutableComponent from = Component.literal("  - From: ").withColor(Color.ORANGE.getRGB()).append(Component.literal( p.getOriginWorldId() + "@ (").withColor(Color.WHITE.getRGB()))
                .append(Component.literal(avgPos.toShortString()).withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mv tp " + p.getOriginWorldId() + " " + getString(avgPos)))))
                .append(Component.literal(")").withColor(Color.WHITE.getRGB()));
        plr.displayClientMessage(from, false);
        MutableComponent to = Component.literal("  - To: ").withColor(Color.ORANGE.getRGB()).append(Component.literal( p.getOriginWorldId() + "@ (").withColor(Color.WHITE.getRGB()))
                .append(Component.literal(avgPos.toShortString()).withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mv tp " + p.getOriginWorldId() + " " + getString(p.getDestLocation())))))
                .append(Component.literal(")").withColor(Color.WHITE.getRGB()));
        plr.displayClientMessage(to, false);
        message(plr, "  &6- Destination:&r " + p.getDestination());
        message(plr, "  &6- Portal Frame:&r " + p.getLocationConfigString());
        return 1;
    }

    private static @NotNull String getString(BlockPos avgPos) {
        return avgPos.getX() + " " + avgPos.getY() + " " + avgPos.getZ();
    }

    public static int runInfo(MinecraftServer mc, ServerPlayer plr, int page) {
        int pageSize = 5; // 每页显示数量
        int total = KNOWN_PORTALS.size();
        int totalPages = (int) Math.ceil(total / (double) pageSize);

        // 页码越界检查
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        message(plr, "&6Multiworld Portals (" + total + ") - Page " + page + "/" + totalPages + ":");

        // 把 map 转换为 list，方便分页
        List<Portal> portals = new ArrayList<>(KNOWN_PORTALS.values());

        // 计算 start 和 end
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        // 输出当前页的数据
        for (int i = start; i < end; i++) {
            Portal p = portals.get(i);
            BlockPos addPos = p.getMinPos().offset(p.getMaxPos());
            BlockPos avgPos = new BlockPos(addPos.getX() / 2, addPos.getY() / 2, addPos.getZ() / 2);
            plr.displayClientMessage(Component.literal(" Portal: \"" + p.getName() + "\": ").withStyle(ChatFormatting.AQUA), false);
            MutableComponent component = Component.literal("  - ")
                    .append(Component.literal(p.getOriginWorldId() + " (" + p.getMinPos().toShortString() + ")").withStyle(Style.EMPTY.withColor(Color.GREEN.getRGB()).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mv tp " + p.getOriginWorldId() + " " + getString(avgPos)))))
                    .append(Component.literal(" -> "))
                    .append(Component.literal(p.getDestWorldName() + " (" + p.getDestLocation().toShortString() + ")").withStyle(Style.EMPTY.withColor(Color.GREEN.getRGB()).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mv tp " + p.getDestWorld().dimension().location() + " " + getString(p.getDestLocation())))));
            plr.displayClientMessage(component, false);
        }

        return 1;
    }

    public static int runInfo(ServerPlayer plr) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Multiworld Portals:").append(KNOWN_PORTALS.size()).append("\n");
        for (String p : KNOWN_PORTALS.keySet()) {
            stringBuilder.append(p).append(",");
        }
        message(plr, stringBuilder.toString());
        return 1;
    }

    public static int runCreate(MinecraftServer mc, ServerPlayer plr, String portalName, String destination) {
        return runCreate(mc, plr, portalName, destination, false);
    }

    public static int runCreate(MinecraftServer mc, ServerPlayer plr, String portalName, String destination, boolean isTransparent) {
        return runCreate(mc, plr, portalName, destination, isTransparent, Direction.NORTH);
    }

    /**
     * Run portal create command
     *
     * @param mc          MinecraftServer instance
     * @param plr         ServerPlayer executing the command
     * @param portalName  Portal name
     * @param destination Portal destination (can be null for default)
     */
    public static int runCreate(MinecraftServer mc, ServerPlayer plr, String portalName, String destination, boolean isTransparent, Direction direction) {

        if (!Perm.has(plr, "multiworld.portal.create")) {
            message(plr, "Invalid permission! Missing: multiworld.portal.create");
            return 0;
        }

        if (destination == null || destination.isEmpty()) {
            message(plr, I18n.CMD_PORTAL_USAGE_CREATE);
            return 0;
        }

        return createPortal(plr, portalName, destination, isTransparent, direction);
    }

    /**
     * Run portal remove command
     *
     * @param mc         MinecraftServer instance
     * @param plr        ServerPlayer executing the command
     * @param portalName Portal name to remove
     */
    public static int runRemove(MinecraftServer mc, ServerPlayer plr, String portalName) {

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

    private static int createPortal(ServerPlayer plr, String name, String dest, boolean isTransparent, Direction direction) {
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
                pos2,
                direction
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

    public static int runDebug(MinecraftServer server, ServerPlayer player) {
        Boolean data = player.getData(DataAttachmentsRegistry.PORTAL_DEBUG.get());
        if (!data) {
            player.setData(DataAttachmentsRegistry.PORTAL_DEBUG.get(), true);
            message(player, "&aPortal debug enabled.");
        } else {
            player.setData(DataAttachmentsRegistry.PORTAL_DEBUG.get(), false);
            message(player, "&cPortal debug disabled.");
        }
        return 1;
    }
}