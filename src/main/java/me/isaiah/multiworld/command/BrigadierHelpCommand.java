package me.isaiah.multiworld.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.perm.Perm;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class BrigadierHelpCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("help")
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
        ServerPlayer player = ctx.getSource().getPlayer();
        
        if (player == null) {
            return 1; // Console not supported for this command
        }
        
        for (String s : MultiworldMod.COMMAND_HELP) {
            MultiworldMod.message(player, s);
        }
        
        return 1;
    }
}