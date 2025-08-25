/**
 * Multiworld Mod
 * Copyright (c) 2021-2024 by Isaiah.
 */
package me.isaiah.multiworld;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
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
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
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
                                .suggests(new WorldSuggestionProvider())
                                .executes(ctx -> {
                                    String worldName = StringArgumentType.getString(ctx, "world");
                                    ServerPlayer player = ctx.getSource().getPlayer();
                                    String[] args = {"tp", worldName};
                                    return TpCommand.run(ctx.getSource().getServer(), player, args);
                                })
                                .then(Commands.argument("player", StringArgumentType.string())
                                        .suggests(new PlayerSuggestionProvider())
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
                                .suggests(new IdSuggestionProvider())
                                .then(Commands.argument("environment", StringArgumentType.string())
                                        .suggests(new EnvironmentSuggestionProvider())
                                        .executes(ctx -> {
                                            String id = StringArgumentType.getString(ctx, "id");
                                            String environment = StringArgumentType.getString(ctx, "environment");
                                            ServerPlayer player = ctx.getSource().getPlayer();
                                            String[] args = {"create", id, environment};
                                            return CreateCommand.run(ctx.getSource().getServer(), player, args);
                                        })
                                        .then(Commands.argument("options", greedyString())
                                                .suggests(new GeneratorSuggestionProvider())
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
                
                // Spawn Command
                .then(Commands.literal("spawn")
                        .requires(source -> {
                            try {
                                ServerPlayer player = source.getPlayer();
                                return source.hasPermission(1) || 
                                       Perm.has(player, "multiworld.spawn") || 
                                       Perm.has(player, "multiworld.admin");
                            } catch (Exception e) {
                                return source.hasPermission(1);
                            }
                        })
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayer();
                            if (player == null) return 1;
                            String[] args = {"spawn"};
                            return SpawnCommand.run(ctx.getSource().getServer(), player, args);
                        }))
                
                // SetSpawn Command
                .then(Commands.literal("setspawn")
                        .requires(source -> {
                            try {
                                ServerPlayer player = source.getPlayer();
                                return source.hasPermission(1) || 
                                       Perm.has(player, "multiworld.setspawn") || 
                                       Perm.has(player, "multiworld.admin");
                            } catch (Exception e) {
                                return source.hasPermission(1);
                            }
                        })
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayer();
                            if (player == null) return 1;
                            String[] args = {"setspawn"};
                            return SetspawnCommand.run(ctx.getSource().getServer(), player, args);
                        }))
                
                // Gamerule Command
                .then(Commands.literal("gamerule")
                        .requires(source -> {
                            try {
                                ServerPlayer player = source.getPlayer();
                                return source.hasPermission(1) || 
                                       Perm.has(player, "multiworld.gamerule") || 
                                       Perm.has(player, "multiworld.admin");
                            } catch (Exception e) {
                                return source.hasPermission(1);
                            }
                        })
                        .then(Commands.argument("rule", StringArgumentType.string())
                                .suggests(new GameruleSuggestionProvider())
                                .executes(ctx -> {
                                    String rule = StringArgumentType.getString(ctx, "rule");
                                    ServerPlayer player = ctx.getSource().getPlayer();
                                    String[] args = {"gamerule", rule};
                                    return GameruleCommand.run(ctx.getSource().getServer(), player, args);
                                })
                                .then(Commands.argument("value", StringArgumentType.string())
                                        .suggests(new GameruleValueSuggestionProvider())
                                        .executes(ctx -> {
                                            String rule = StringArgumentType.getString(ctx, "rule");
                                            String value = StringArgumentType.getString(ctx, "value");
                                            ServerPlayer player = ctx.getSource().getPlayer();
                                            String[] args = {"gamerule", rule, value};
                                            return GameruleCommand.run(ctx.getSource().getServer(), player, args);
                                        }))))
                
                // Difficulty Command
                .then(Commands.literal("difficulty")
                        .requires(source -> {
                            try {
                                ServerPlayer player = source.getPlayer();
                                return source.hasPermission(1) || 
                                       Perm.has(player, "multiworld.difficulty") || 
                                       Perm.has(player, "multiworld.admin");
                            } catch (Exception e) {
                                return source.hasPermission(1);
                            }
                        })
                        .then(Commands.argument("difficulty", StringArgumentType.string())
                                .suggests(new DifficultySuggestionProvider())
                                .executes(ctx -> {
                                    String difficulty = StringArgumentType.getString(ctx, "difficulty");
                                    ServerPlayer player = ctx.getSource().getPlayer();
                                    String[] args = {"difficulty", difficulty};
                                    return DifficultyCommand.run(ctx.getSource().getServer(), player, args);
                                })
                                .then(Commands.argument("world", StringArgumentType.string())
                                        .suggests(new WorldSuggestionProvider())
                                        .executes(ctx -> {
                                            String difficulty = StringArgumentType.getString(ctx, "difficulty");
                                            String world = StringArgumentType.getString(ctx, "world");
                                            ServerPlayer player = ctx.getSource().getPlayer();
                                            String[] args = {"difficulty", difficulty, world};
                                            return DifficultyCommand.run(ctx.getSource().getServer(), player, args);
                                        }))))
                
                // Portal Command
                .then(Commands.literal("portal")
                        .requires(source -> {
                            try {
                                ServerPlayer player = source.getPlayer();
                                return source.hasPermission(1) || 
                                       Perm.has(player, "multiworld.portal") || 
                                       Perm.has(player, "multiworld.admin");
                            } catch (Exception e) {
                                return source.hasPermission(1);
                            }
                        })
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayer();
                            if (player == null) return 1;
                            String[] args = {"portal"};
                            return PortalCommand.run(ctx.getSource().getServer(), player, args);
                        })
                        .then(Commands.literal("create")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            ServerPlayer player = ctx.getSource().getPlayer();
                                            String[] args = {"portal", "create", name};
                                            return PortalCommand.run(ctx.getSource().getServer(), player, args);
                                        })
                                        .then(Commands.argument("destination", greedyString())
                                                .suggests(new DestinationSuggestionProvider())
                                                .executes(ctx -> {
                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    String destination = StringArgumentType.getString(ctx, "destination");
                                                    ServerPlayer player = ctx.getSource().getPlayer();
                                                    String[] args = {"portal", "create", name, destination};
                                                    return PortalCommand.run(ctx.getSource().getServer(), player, args);
                                                }))))
                        .then(Commands.literal("wand")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayer();
                                    String[] args = {"portal", "wand"};
                                    return PortalCommand.run(ctx.getSource().getServer(), player, args);
                                }))
                        .then(Commands.literal("info")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests(new PortalNameSuggestionProvider())
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            ServerPlayer player = ctx.getSource().getPlayer();
                                            String[] args = {"portal", "info", name};
                                            return PortalCommand.run(ctx.getSource().getServer(), player, args);
                                        })))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests(new PortalNameSuggestionProvider())
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            ServerPlayer player = ctx.getSource().getPlayer();
                                            String[] args = {"portal", "remove", name};
                                            return PortalCommand.run(ctx.getSource().getServer(), player, args);
                                        }))))
                
                // Delete Command
                .then(Commands.literal("delete")
                        .requires(source -> {
                            try {
                                ServerPlayer player = source.getPlayer();
                                return source.hasPermission(1) || 
                                       Perm.has(player, "multiworld.admin");
                            } catch (Exception e) {
                                return source.hasPermission(1);
                            }
                        })
                        .then(Commands.argument("world", StringArgumentType.string())
                                .suggests(new DeleteWorldSuggestionProvider())
                                .executes(ctx -> {
                                    String world = StringArgumentType.getString(ctx, "world");
                                    String[] args = {"delete", world};
                                    return DeleteCommand.run(ctx.getSource().getServer(), ctx.getSource(), args);
                                })))
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

    // Suggestion Providers
    public static class WorldSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            context.getSource().getServer().levelKeys().forEach(resourceKey -> {
                ResourceLocation location = resourceKey.location();
                String worldName = location.toString();
                if (worldName.startsWith("multiworld:")) {
                    worldName = worldName.replace("multiworld:", "");
                }
                builder.suggest(worldName);
            });
            return builder.buildFuture();
        }
    }

    public static class PlayerSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            context.getSource().getServer().getPlayerNames().forEach(builder::suggest);
            return builder.buildFuture();
        }
    }

    public static class IdSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            builder.suggest("myid:myvalue");
            return builder.buildFuture();
        }
    }

    public static class EnvironmentSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            builder.suggest("NORMAL");
            builder.suggest("NETHER");
            builder.suggest("END");
            return builder.buildFuture();
        }
    }

    public static class GeneratorSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            builder.suggest("-g=NORMAL");
            builder.suggest("-g=FLAT");
            builder.suggest("-g=VOID");
            builder.suggest("-s=1234");
            builder.suggest("-s=RANDOM");
            
            for (String key : CreateCommand.customs.keySet()) {
                builder.suggest("-g=" + key.toUpperCase(Locale.ROOT));
            }
            
            return builder.buildFuture();
        }
    }

    public static class GameruleSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            if (GameruleCommand.keys.isEmpty()) {
                GameruleCommand.setupServer(context.getSource().getServer());
            }

            for (String name : GameruleCommand.keys.keySet()) {
                builder.suggest(name);
            }
            
            return builder.buildFuture();
        }
    }

    public static class GameruleValueSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            builder.suggest("true");
            builder.suggest("false");
            builder.suggest("0");
            builder.suggest("1");
            builder.suggest("10");
            return builder.buildFuture();
        }
    }

    public static class DifficultySuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            String[] difficulties = {"PEACEFUL", "EASY", "NORMAL", "HARD"};
            for (String difficulty : difficulties) {
                builder.suggest(difficulty);
            }
            return builder.buildFuture();
        }
    }

    public static class PortalNameSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            for (String portalName : PortalCommand.KNOWN_PORTALS.keySet()) {
                builder.suggest(portalName);
            }
            return builder.buildFuture();
        }
    }

    public static class DestinationSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            context.getSource().getServer().levelKeys().forEach(resourceKey -> {
                String namespace = resourceKey.location().getNamespace();
                String path = resourceKey.location().getPath();
                builder.suggest("e:" + namespace + ":" + path);
            });
            
            return builder.buildFuture();
        }
    }

    public static class DeleteWorldSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            context.getSource().getServer().levelKeys().forEach(resourceKey -> {
                ResourceLocation location = resourceKey.location();
                String worldName = location.toString();
                if (worldName.startsWith("multiworld:")) {
                    worldName = worldName.replace("multiworld:", "");
                }
                // Don't suggest built-in worlds for deletion
                if (!worldName.equals("minecraft:overworld") && 
                    !worldName.equals("minecraft:the_nether") && 
                    !worldName.equals("minecraft:the_end")) {
                    builder.suggest(worldName);
                }
            });
            
            return builder.buildFuture();
        }
    }

}