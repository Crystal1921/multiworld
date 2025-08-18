/**
 * Multiworld Mod
 * Copyright (c) 2021-2024 by Isaiah.
 */
package me.isaiah.multiworld;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.isaiah.multiworld.command.*;
import me.isaiah.multiworld.perm.Perm;
import me.isaiah.multiworld.portal.Portal;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Multiworld Mod
 */
public class MultiworldMod {

    public static final Logger LOGGER = LoggerFactory.getLogger("multiworld");

    public static final String MOD_ID = "multiworld";
    // Mod Version
    public static final String VERSION = "1.10";
    private static final char COLOR_CHAR = '\u00A7';
    public static MinecraftServer mc;
    public static String CMD = "mw";
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
     *
     * @see {@link me.isaiah.multiworld.fabric.MultiworldModFabric}
     */
    public static void init() {
        System.out.println("Multiworld init");

        // TODO: Testing
        // PortalCommand.test();

        //WandEventHandler.register();
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

    public static ServerPlayer get_player(CommandSourceStack s) throws CommandSyntaxException {
        ServerPlayer plr = s.getPlayer();
        if (null == plr) {
            // s.sendMessage(text_plain("Multiworld Mod for Minecraft " + mc.getVersion()));
            // s.sendMessage(text_plain("These commands currently require a Player."));

            throw CommandSourceStack.ERROR_NOT_PLAYER.create();
        }
        return plr;
    }

    public static boolean isPlayer(CommandSourceStack s) {
        try {
            ServerPlayer plr = s.getPlayer();
            if (null == plr) {
                return false;
            }
        } catch (Exception ex) {
            if (ex instanceof CommandSyntaxException) {
                if (s.getTextName().equalsIgnoreCase("Server")) return false;
            }
        }
        return true;
    }

    // On command register
    public static void register_commands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal(CMD)
                .requires(source -> {
                    try {
                        return source.hasPermission(1) || Perm.has(get_player(source), "multiworld.cmd") ||
                                Perm.has(get_player(source), "multiworld.admin");
                    } catch (Exception e) {
                        return source.hasPermission(1);
                    }
                })
                .executes(ctx -> broadcast(ctx.getSource(), ChatFormatting.AQUA, null))
                .then(argument("message", greedyString()).suggests(new InfoSuggest())
                        .executes(ctx -> {
                            try {
                                return broadcast(ctx.getSource(), ChatFormatting.AQUA, getString(ctx, "message"));
                            } catch (Exception e) {
                                e.printStackTrace();
                                return 1;
                            }
                        })));
    }

    public static int broadcast(CommandSourceStack source, ChatFormatting formatting, String message) throws CommandSyntaxException {
    	/*
    	if (!source.isExecutedByPlayer()) {
    		if (!source.getName().equalsIgnoreCase("Server")) return 1;
    		return broadcast_console(source, message);
    	}
    	*/

        if (!isPlayer(source)) {
            ConsoleCommand.broadcast_console(mc, source, message);
            return 1;
        }

        final ServerPlayer plr = get_player(source); // source.getPlayerOrThrow();

        if (null == message) {
            message(plr, "&bMultiworld Mod for Minecraft " + mc.getServerVersion());

            Level world = plr.level();
            ResourceLocation id = world.dimension().location();

            message(plr, "Currently in: " + id.toString());

            return 1;
        }

        boolean ALL = Perm.has(plr, "multiworld.admin");
        String[] args = message.split(" ");

        /*if (args[0].equalsIgnoreCase("portaltest")) {
            BlockPos pos = plr.getBlockPos();
            pos = pos.add(2, 0, 2);
            ServerWorld w = plr.getWorld();

            Portal p = new Portal();
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos pos2 = pos.add(x, y, 0);
                    if ((x > 0 && x < 3) && (y > 0 && y < 4)) {
                        p.blocks.add(pos2);
                        w.setBlockState(pos2, Blocks.NETHER_PORTAL.getDefaultState());
                    } else
                    w.setBlockState(pos2, Blocks.STONE.getDefaultState());
                }
            }
            p.addToMap();
            try {
                p.save();
            } catch (IOException e) {
                plr.sendMessage(text("Failed saving portal data. Check console for details.", Formatting.RED), false);
                e.printStackTrace();
            }
        }*/

        // Help Command
        if (args[0].equalsIgnoreCase("help")) {
            for (String s : COMMAND_HELP) {
                message(plr, s);
            }
        }

        // Debug
        if (args[0].equalsIgnoreCase("debugtick")) {
            ServerLevel w = (ServerLevel) plr.level();
            ResourceLocation id = w.dimension().location();
            message(plr, "World ID: " + id.toString());
            message(plr, "Players : " + w.players().size());
            w.tick(() -> true);
        }

        // SetSpawn Command
        if (args[0].equalsIgnoreCase("setspawn") && (ALL || Perm.has(plr, "multiworld.setspawn"))) {
            return SetspawnCommand.run(mc, plr, args);
        }

        // Spawn Command
        if (args[0].equalsIgnoreCase("spawn") && (ALL || Perm.has(plr, "multiworld.spawn"))) {
            return SpawnCommand.run(mc, plr, args);
        }

        // Gamerule Command
        if (args[0].equalsIgnoreCase("gamerule") && (ALL || Perm.has(plr, "multiworld.gamerule"))) {
            return GameruleCommand.run(mc, plr, args);
        }

        // Difficulty Command
        if (args[0].equalsIgnoreCase("difficulty") && (ALL || Perm.has(plr, "multiworld.difficulty"))) {
            return DifficultyCommand.run(mc, plr, args);
        }

        // TP Command
        if (args[0].equalsIgnoreCase("tp")) {
            if (!(ALL || Perm.has(plr, "multiworld.tp"))) {
                plr.displayClientMessage(Component.nullToEmpty("No permission! Missing permission: multiworld.tp"), false);
                return 1;
            }
            if (args.length == 1) {
                plr.displayClientMessage(text_plain("Usage: /" + CMD + " tp <world>"), false);
                return 0;
            }
            return TpCommand.run(mc, plr, args);
        }

        // List Command
        if (args[0].equalsIgnoreCase("list")) {
            if (!(ALL || Perm.has(plr, "multiworld.cmd"))) {
                plr.displayClientMessage(Component.nullToEmpty("No permission! Missing permission: multiworld.cmd"), false);
                return 1;
            }

            message(plr, "&bAll Worlds:");

            Level pworld = plr.level();
            ResourceLocation pwid = pworld.dimension().location();

            mc.getAllLevels().forEach(world -> {
                ResourceLocation id = world.dimension().location();
                String name = id.toString();
                if (name.startsWith("multiworld:")) name = name.replace("multiworld:", "");

                if (id.equals(pwid)) {
                    message(plr, "- " + name + " &a(Currently in)");
                } else {
                    message(plr, "- " + name);
                }
            });
        }

        // Version Command
        if (args[0].equalsIgnoreCase("version") && (ALL || Perm.has(plr, "multiworld.cmd"))) {
            message(plr, "Multiworld Mod version " + VERSION);
            return 1;
        }

        // Create Command
        if (args[0].equalsIgnoreCase("create")) {
            if (!(ALL || Perm.has(plr, "multiworld.create"))) {
                message(plr, "No permission! Missing permission: multiworld.create");
                return 1;
            }
            return CreateCommand.run(mc, plr, args);
        }

        // Delete Command
        if (args[0].equalsIgnoreCase("delete")) {
            if (!ALL) {
                message(plr, "No permission! Missing permission: multiworld.admin");
                return 1;
            }
            message(plr, "Delete Command is Console-only for security.");
        }

        // Help Command
        if (args[0].equalsIgnoreCase("portal")) {
            if (!(ALL || Perm.has(plr, "multiworld.portal"))) {
                message(plr, "No permission! Missing permission: multiworld.portal");
                return 1;
            }

            PortalCommand.run(mc, plr, args);
        }

        return Command.SINGLE_SUCCESS; // Success
    }

    public static Component text(String message) {
        try {
            return Component.nullToEmpty(translate_alternate_color_codes('&', message));
        } catch (Exception e) {
            e.printStackTrace();
            return text_plain(message);
        }
    }

    public static void message(Player player, String message) {
        try {
            player.displayClientMessage(Component.nullToEmpty(translate_alternate_color_codes('&', message)), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String translate_alternate_color_codes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    public static Component text_plain(String txt) {
        return Component.nullToEmpty(txt);
    }

}