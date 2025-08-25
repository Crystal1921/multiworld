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

public class BrigadierListCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("list")
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
                .executes(ctx -> execute(ctx)));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        // Use existing logic from MultiworldMod.broadcast() - List Command section
        ServerPlayer player = ctx.getSource().getPlayer();
        
        if (player == null) {
            return 1; // Console not supported for this command
        }
        
        me.isaiah.multiworld.MultiworldMod.message(player, "&bAll Worlds:");

        net.minecraft.world.level.Level pworld = player.level();
        net.minecraft.resources.ResourceLocation pwid = pworld.dimension().location();

        ctx.getSource().getServer().getAllLevels().forEach(world -> {
            net.minecraft.resources.ResourceLocation id = world.dimension().location();
            String name = id.toString();
            if (name.startsWith("multiworld:")) name = name.replace("multiworld:", "");

            if (id.equals(pwid)) {
                me.isaiah.multiworld.MultiworldMod.message(player, "- " + name + " &a(Currently in)");
            } else {
                me.isaiah.multiworld.MultiworldMod.message(player, "- " + name);
            }
        });
        
        return 1;
    }
}