/**
 * Multiworld Mod
 * Copyright (c) 2021-2024 by Isaiah.
 */
package me.isaiah.multiworld;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.isaiah.multiworld.command.*;
import me.isaiah.multiworld.perm.Perm;
import me.isaiah.multiworld.portal.Portal;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
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
import static com.mojang.brigadier.arguments.StringArgumentType.string;
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
        // Main command with all subcommands
        dispatcher.register(literal(CMD)
                .requires(source -> {
                    try {
                        return source.hasPermission(1) || Perm.has(get_player(source), "multiworld.cmd") ||
                                Perm.has(get_player(source), "multiworld.admin");
                    } catch (Exception e) {
                        return source.hasPermission(1);
                    }
                })
                .executes(ctx -> showMainHelp(ctx.getSource()))
                
                // TP Command
                .then(Commands.literal("tp")
                        .requires(source -> {
                            try {
                                ServerPlayer player = source.getPlayer();
                                return source.hasPermission(1) || 
                                       Perm.has(player, "multiworld.tp") || 
                                       Perm.has(player, "multiworld.admin");
                            } catch (Exception e) {
                                return source.hasPermission(1);
                            }
                        })
                        .then(Commands.argument("world", StringArgumentType.string())
                                .suggests(new BrigadierTpCommand.WorldSuggestionProvider())
                                .executes(ctx -> {
                                    String worldName = StringArgumentType.getString(ctx, "world");
                                    ServerPlayer player = ctx.getSource().getPlayer();
                                    String[] args = {"tp", worldName};
                                    return TpCommand.run(ctx.getSource().getServer(), player, args);
                                })
                                .then(Commands.argument("player", StringArgumentType.string())
                                        .suggests(new BrigadierTpCommand.PlayerSuggestionProvider())
                                        .executes(ctx -> {
                                            String worldName = StringArgumentType.getString(ctx, "world");
                                            String playerName = StringArgumentType.getString(ctx, "player");
                                            String[] args = {"tp", worldName, playerName};
                                            return TpCommand.run(ctx.getSource().getServer(), null, args);
                                        }))))
                
                // List Command
                .then(Commands.literal("list")
                        .requires(source -> {
                            try {
                                ServerPlayer player = source.getPlayer();
                                return source.hasPermission(1) || 
                                       Perm.has(player, "multiworld.cmd") || 
                                       Perm.has(player, "multiworld.admin");
                            } catch (Exception e) {
                                return source.hasPermission(1);
                            }
                        })
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayer();
                            if (player == null) return 1;
                            
                            message(player, "&bAll Worlds:");
                            Level pworld = player.level();
                            ResourceLocation pwid = pworld.dimension().location();

                            ctx.getSource().getServer().getAllLevels().forEach(world -> {
                                ResourceLocation id = world.dimension().location();
                                String name = id.toString();
                                if (name.startsWith("multiworld:")) name = name.replace("multiworld:", "");

                                if (id.equals(pwid)) {
                                    message(player, "- " + name + " &a(Currently in)");
                                } else {
                                    message(player, "- " + name);
                                }
                            });
                            return 1;
                        }))
                
                // Version Command
                .then(Commands.literal("version")
                        .requires(source -> {
                            try {
                                ServerPlayer player = source.getPlayer();
                                return source.hasPermission(1) || 
                                       Perm.has(player, "multiworld.cmd") || 
                                       Perm.has(player, "multiworld.admin");
                            } catch (Exception e) {
                                return source.hasPermission(1);
                            }
                        })
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayer();
                            if (player == null) return 1;
                            message(player, "Multiworld Mod version " + VERSION);
                            return 1;
                        }))
                
                // Create Command
                .then(Commands.literal("create")
                        .requires(source -> {
                            try {
                                ServerPlayer player = source.getPlayer();
                                return source.hasPermission(1) || 
                                       Perm.has(player, "multiworld.create") || 
                                       Perm.has(player, "multiworld.admin");
                            } catch (Exception e) {
                                return source.hasPermission(1);
                            }
                        })
                        .then(Commands.argument("id", StringArgumentType.string())
                                .suggests(new BrigadierCreateCommand.IdSuggestionProvider())
                                .then(Commands.argument("environment", StringArgumentType.string())
                                        .suggests(new BrigadierCreateCommand.EnvironmentSuggestionProvider())
                                        .executes(ctx -> {
                                            String id = StringArgumentType.getString(ctx, "id");
                                            String environment = StringArgumentType.getString(ctx, "environment");
                                            ServerPlayer player = ctx.getSource().getPlayer();
                                            String[] args = {"create", id, environment};
                                            return CreateCommand.run(ctx.getSource().getServer(), player, args);
                                        })
                                        .then(Commands.argument("options", StringArgumentType.greedyString())
                                                .suggests(new BrigadierCreateCommand.GeneratorSuggestionProvider())
                                                .executes(ctx -> {
                                                    String id = StringArgumentType.getString(ctx, "id");
                                                    String environment = StringArgumentType.getString(ctx, "environment");
                                                    String options = StringArgumentType.getString(ctx, "options");
                                                    ServerPlayer player = ctx.getSource().getPlayer();
                                                    
                                                    String[] optionArgs = options.split(" ");
                                                    String[] args = new String[3 + optionArgs.length];
                                                    args[0] = "create";
                                                    args[1] = id;
                                                    args[2] = environment;
                                                    System.arraycopy(optionArgs, 0, args, 3, optionArgs.length);
                                                    
                                                    return CreateCommand.run(ctx.getSource().getServer(), player, args);
                                                })))))
                
                // Help Command
                .then(Commands.literal("help")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayer();
                            if (player == null) return 1;
                            for (String s : COMMAND_HELP) {
                                message(player, s);
                            }
                            return 1;
                        }))
        );
    }

    private static int showMainHelp(CommandSourceStack source) throws CommandSyntaxException {
        if (!isPlayer(source)) {
            ConsoleCommand.broadcast_console(mc, source, null);
            return 1;
        }

        final ServerPlayer plr = get_player(source);
        
        message(plr, "&bMultiworld Mod for Minecraft " + mc.getServerVersion());

        Level world = plr.level();
        ResourceLocation id = world.dimension().location();

        message(plr, "Currently in: " + id.toString());

        return 1;
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