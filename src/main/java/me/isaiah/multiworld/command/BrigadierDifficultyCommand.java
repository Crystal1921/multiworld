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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

public class BrigadierDifficultyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("difficulty")
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
                        .executes(ctx -> execute(ctx))
                        .then(Commands.argument("world", StringArgumentType.string())
                                .suggests(new WorldSuggestionProvider())
                                .executes(ctx -> executeWithWorld(ctx)))));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String difficulty = StringArgumentType.getString(ctx, "difficulty");
        ServerPlayer player = ctx.getSource().getPlayer();
        
        String[] args = {"difficulty", difficulty};
        return DifficultyCommand.run(ctx.getSource().getServer(), player, args);
    }

    private static int executeWithWorld(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String difficulty = StringArgumentType.getString(ctx, "difficulty");
        String world = StringArgumentType.getString(ctx, "world");
        ServerPlayer player = ctx.getSource().getPlayer();
        
        String[] args = {"difficulty", difficulty, world};
        return DifficultyCommand.run(ctx.getSource().getServer(), player, args);
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
}