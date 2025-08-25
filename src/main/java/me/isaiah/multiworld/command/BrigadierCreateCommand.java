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

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class BrigadierCreateCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("create")
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
                                .executes(ctx -> execute(ctx))
                                .then(Commands.argument("generator", StringArgumentType.greedyString())
                                        .suggests(new GeneratorSuggestionProvider())
                                        .executes(ctx -> executeWithGenerator(ctx))))));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "id");
        String environment = StringArgumentType.getString(ctx, "environment");
        ServerPlayer player = ctx.getSource().getPlayer();
        
        String[] args = {"create", id, environment};
        return CreateCommand.run(ctx.getSource().getServer(), player, args);
    }

    private static int executeWithGenerator(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "id");
        String environment = StringArgumentType.getString(ctx, "environment");
        String generator = StringArgumentType.getString(ctx, "generator");
        ServerPlayer player = ctx.getSource().getPlayer();
        
        // Parse generator and seed arguments
        String[] generatorArgs = generator.split(" ");
        String[] args = new String[3 + generatorArgs.length];
        args[0] = "create";
        args[1] = id;
        args[2] = environment;
        System.arraycopy(generatorArgs, 0, args, 3, generatorArgs.length);
        
        return CreateCommand.run(ctx.getSource().getServer(), player, args);
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
            
            // Add custom generators
            for (String key : CreateCommand.customs.keySet()) {
                builder.suggest("-g=" + key.toUpperCase(Locale.ROOT));
            }
            
            return builder.buildFuture();
        }
    }
}