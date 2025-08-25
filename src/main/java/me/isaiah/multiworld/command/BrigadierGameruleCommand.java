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

public class BrigadierGameruleCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gamerule")
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
                        .executes(ctx -> executeGetRule(ctx))
                        .then(Commands.argument("value", StringArgumentType.string())
                                .suggests(new GameruleValueSuggestionProvider())
                                .executes(ctx -> executeSetRule(ctx)))));
    }

    private static int executeGetRule(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String rule = StringArgumentType.getString(ctx, "rule");
        ServerPlayer player = ctx.getSource().getPlayer();
        
        String[] args = {"gamerule", rule};
        return GameruleCommand.run(ctx.getSource().getServer(), player, args);
    }

    private static int executeSetRule(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String rule = StringArgumentType.getString(ctx, "rule");
        String value = StringArgumentType.getString(ctx, "value");
        ServerPlayer player = ctx.getSource().getPlayer();
        
        String[] args = {"gamerule", rule, value};
        return GameruleCommand.run(ctx.getSource().getServer(), player, args);
    }

    public static class GameruleSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            // Initialize gamerule keys if needed
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
            // Most gamerules are boolean, so suggest true/false
            builder.suggest("true");
            builder.suggest("false");
            
            // Could also suggest numeric values for integer rules, but that's less common
            builder.suggest("0");
            builder.suggest("1");
            builder.suggest("10");
            
            return builder.buildFuture();
        }
    }
}