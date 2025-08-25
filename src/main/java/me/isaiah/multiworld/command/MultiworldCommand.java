package me.isaiah.multiworld.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.isaiah.multiworld.ConsoleCommand;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.perm.Perm;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.commands.Commands.literal;

public class MultiworldCommand {
    private static final char COLOR_CHAR = '§';
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
        dispatcher.register(literal(MultiworldMod.CMD)
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
                        .then(Commands.argument("world", ResourceLocationArgument.id())
                                .suggests(new WorldSuggestionProvider())
                                .executes(ctx -> {
                                    String worldName = ResourceLocationArgument.getId(ctx, "world").toString();
                                    ServerPlayer player = ctx.getSource().getPlayer();
                                    return TpCommand.run(ctx.getSource().getServer(), player, worldName, null);
                                })
                                .then(Commands.argument("player", StringArgumentType.string())
                                        .suggests(new PlayerSuggestionProvider())
                                        .executes(ctx -> {
                                            String worldName = ResourceLocationArgument.getId(ctx, "world").toString();
                                            String playerName = StringArgumentType.getString(ctx, "player");
                                            return TpCommand.run(ctx.getSource().getServer(), null, worldName, playerName);
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
                            message(player, "Multiworld Mod version " + MultiworldMod.VERSION);
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
                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                .suggests(new IdSuggestionProvider())
                                .then(Commands.argument("environment", StringArgumentType.string())
                                        .suggests(new EnvironmentSuggestionProvider())
                                        .executes(ctx -> {
                                            String id = ResourceLocationArgument.getId(ctx, "id").toString();
                                            String environment = StringArgumentType.getString(ctx, "environment");
                                            ServerPlayer player = ctx.getSource().getPlayer();
                                            return CreateCommand.run(ctx.getSource().getServer(), player, id, environment, null);
                                        })
                                        .then(Commands.argument("options", greedyString())
                                                .suggests(new GeneratorSuggestionProvider())
                                                .executes(ctx -> {
                                                    String id = ResourceLocationArgument.getId(ctx, "id").toString();
                                                    String environment = StringArgumentType.getString(ctx, "environment");
                                                    String options = StringArgumentType.getString(ctx, "options");
                                                    ServerPlayer player = ctx.getSource().getPlayer();

                                                    return CreateCommand.run(ctx.getSource().getServer(), player, id, environment, options);
                                                })))))

                // Help Command
                .then(Commands.literal("help")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayer();
                            if (player == null) return 1;
                            for (String s : MultiworldMod.COMMAND_HELP) {
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
                            return SpawnCommand.run(ctx.getSource().getServer(), player);
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
                            return SetspawnCommand.run(ctx.getSource().getServer(), player);
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
                                    return GameruleCommand.run(ctx.getSource().getServer(), player, rule, null);
                                })
                                .then(Commands.argument("value", StringArgumentType.string())
                                        .suggests(new GameruleValueSuggestionProvider())
                                        .executes(ctx -> {
                                            String rule = StringArgumentType.getString(ctx, "rule");
                                            String value = StringArgumentType.getString(ctx, "value");
                                            ServerPlayer player = ctx.getSource().getPlayer();
                                            return GameruleCommand.run(ctx.getSource().getServer(), player, rule, value);
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
                                    return DifficultyCommand.run(ctx.getSource().getServer(), player, difficulty, null);
                                })
                                .then(Commands.argument("world", StringArgumentType.string())
                                        .suggests(new WorldSuggestionProvider())
                                        .executes(ctx -> {
                                            String difficulty = StringArgumentType.getString(ctx, "difficulty");
                                            String world = StringArgumentType.getString(ctx, "world");
                                            ServerPlayer player = ctx.getSource().getPlayer();
                                            return DifficultyCommand.run(ctx.getSource().getServer(), player, difficulty, world);
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
                            return PortalCommand.runHelp(ctx.getSource().getServer(), player);
                        })
                        .then(Commands.literal("create")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            ServerPlayer player = ctx.getSource().getPlayer();
                                            return PortalCommand.runCreate(ctx.getSource().getServer(), player, name, null);
                                        })
                                        .then(Commands.argument("destination", greedyString())
                                                .suggests(new DestinationSuggestionProvider())
                                                .executes(ctx -> {
                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    String destination = StringArgumentType.getString(ctx, "destination");
                                                    ServerPlayer player = ctx.getSource().getPlayer();
                                                    return PortalCommand.runCreate(ctx.getSource().getServer(), player, name, destination);
                                                }))))
                        .then(Commands.literal("wand")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayer();
                                    return PortalCommand.runWand(ctx.getSource().getServer(), player);
                                }))
                        .then(Commands.literal("info")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayer();
                                    return PortalCommand.runInfo(ctx.getSource().getServer(), player, null);
                                })
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests(new PortalNameSuggestionProvider())
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            ServerPlayer player = ctx.getSource().getPlayer();
                                            return PortalCommand.runInfo(ctx.getSource().getServer(), player, name);
                                        })))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests(new PortalNameSuggestionProvider())
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            ServerPlayer player = ctx.getSource().getPlayer();
                                            return PortalCommand.runRemove(ctx.getSource().getServer(), player, name);
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
                        .then(Commands.argument("world", ResourceLocationArgument.id())
                                .suggests(new DeleteWorldSuggestionProvider())
                                .executes(ctx -> {
                                    String world = ResourceLocationArgument.getId(ctx, "world").toString();
                                    return DeleteCommand.run(ctx.getSource().getServer(), ctx.getSource(), world);
                                })))
        );
    }

    private static int showMainHelp(CommandSourceStack source) throws CommandSyntaxException {
        if (!isPlayer(source)) {
            ConsoleCommand.broadcast_console(MultiworldMod.mc, source, null);
            return 1;
        }

        final ServerPlayer plr = get_player(source);

        message(plr, "&bMultiworld Mod for Minecraft " + MultiworldMod.mc.getServerVersion());

        Level world = plr.level();
        ResourceLocation id = world.dimension().location();

        message(plr, "Currently in: " + id);

        return 1;
    }

    public static void message(Player player, String message) {
        try {
            player.displayClientMessage(Component.nullToEmpty(translate_alternate_color_codes('&', message)), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String translate_alternate_color_codes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
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
            Arrays.stream(context.getSource().getServer().getPlayerNames()).forEach(builder::suggest);
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
