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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.concurrent.CompletableFuture;

public class BrigadierTpCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tp")
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
                        .executes(ctx -> execute(ctx))
                        .then(Commands.argument("player", StringArgumentType.string())
                                .suggests(new PlayerSuggestionProvider())
                                .executes(ctx -> executeWithPlayer(ctx)))));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String worldName = StringArgumentType.getString(ctx, "world");
        ServerPlayer player = ctx.getSource().getPlayer();
        
        String[] args = {"tp", worldName};
        return TpCommand.run(ctx.getSource().getServer(), player, args);
    }

    private static int executeWithPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String worldName = StringArgumentType.getString(ctx, "world");
        String playerName = StringArgumentType.getString(ctx, "player");
        
        String[] args = {"tp", worldName, playerName};
        return TpCommand.run(ctx.getSource().getServer(), null, args);
    }

    public static class WorldSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            // Get world names from the server
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
            // Get player names from the server
            context.getSource().getServer().getPlayerNames().forEach(builder::suggest);
            return builder.buildFuture();
        }
    }
}