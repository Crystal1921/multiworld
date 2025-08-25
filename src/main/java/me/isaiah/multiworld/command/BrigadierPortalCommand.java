package me.isaiah.multiworld.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.isaiah.multiworld.perm.Perm;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

public class BrigadierPortalCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("portal")
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
                .executes(ctx -> executeHelp(ctx))
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ctx -> executeCreate(ctx))
                                .then(Commands.argument("destination", StringArgumentType.greedyString())
                                        .suggests(new DestinationSuggestionProvider())
                                        .executes(ctx -> executeCreateWithDestination(ctx)))))
                .then(Commands.literal("wand")
                        .executes(ctx -> executeWand(ctx)))
                .then(Commands.literal("info")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests(new PortalNameSuggestionProvider())
                                .executes(ctx -> executeInfo(ctx))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests(new PortalNameSuggestionProvider())
                                .executes(ctx -> executeRemove(ctx)))));
    }

    private static int executeHelp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayer();
        
        if (player == null) {
            return 1; // Console not supported for this command
        }
        
        String[] args = {"portal"};
        return PortalCommand.run(ctx.getSource().getServer(), player, args);
    }

    private static int executeCreate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        ServerPlayer player = ctx.getSource().getPlayer();
        
        String[] args = {"portal", "create", name};
        return PortalCommand.run(ctx.getSource().getServer(), player, args);
    }

    private static int executeCreateWithDestination(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        String destination = StringArgumentType.getString(ctx, "destination");
        ServerPlayer player = ctx.getSource().getPlayer();
        
        String[] args = {"portal", "create", name, destination};
        return PortalCommand.run(ctx.getSource().getServer(), player, args);
    }

    private static int executeWand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayer();
        
        String[] args = {"portal", "wand"};
        return PortalCommand.run(ctx.getSource().getServer(), player, args);
    }

    private static int executeInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        ServerPlayer player = ctx.getSource().getPlayer();
        
        String[] args = {"portal", "info", name};
        return PortalCommand.run(ctx.getSource().getServer(), player, args);
    }

    private static int executeRemove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        ServerPlayer player = ctx.getSource().getPlayer();
        
        String[] args = {"portal", "remove", name};
        return PortalCommand.run(ctx.getSource().getServer(), player, args);
    }

    public static class PortalNameSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            // Suggest existing portal names
            for (String portalName : PortalCommand.KNOWN_PORTALS.keySet()) {
                builder.suggest(portalName);
            }
            return builder.buildFuture();
        }
    }

    public static class DestinationSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            // Suggest world destinations in format "e:namespace:path"
            context.getSource().getServer().levelKeys().forEach(resourceKey -> {
                String namespace = resourceKey.location().getNamespace();
                String path = resourceKey.location().getPath();
                builder.suggest("e:" + namespace + ":" + path);
            });
            
            return builder.buildFuture();
        }
    }
}