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

public class BrigadierDeleteCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("delete")
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
                        .suggests(new WorldSuggestionProvider())
                        .executes(ctx -> execute(ctx))));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String world = StringArgumentType.getString(ctx, "world");
        
        String[] args = {"delete", world};
        return DeleteCommand.run(ctx.getSource().getServer(), ctx.getSource(), args);
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