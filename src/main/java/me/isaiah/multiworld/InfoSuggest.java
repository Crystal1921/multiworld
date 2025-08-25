package me.isaiah.multiworld;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.isaiah.multiworld.command.CreateCommand;
import me.isaiah.multiworld.command.GameruleCommand;
import me.isaiah.multiworld.command.PortalCommand;
import me.isaiah.multiworld.perm.Perm;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * @deprecated This class is deprecated. Individual commands now use their own brigadier-based suggestion providers.
 * This class is kept for backward compatibility but should not be used in new code.
 * 
 * Our Implementation of a command SuggestionProvider.
 */
@Deprecated
public class InfoSuggest implements SuggestionProvider<CommandSourceStack> {

    /**
     * Valid Difficulty Arguments
     */
    public static String[] diff_names = {
            "PEACEFUL", "EASY", "NORMAL", "HARD"
    };

    /**
     * Valid Subcommands
     */
    private static String[] subcommands = {
            "tp", "list", "version", "create", "spawn", "setspawn", "gamerule", "help", "difficulty", "portal", "delete"
            // TODO: Add: delete, load, unload, info, clone, who, import
    };

    public static List<String> getWorldNames() {
        MinecraftServer mc = MultiworldMod.mc;
        List<String> names = new ArrayList<>();
        mc.getAllLevels().forEach(world -> {
            String name = ((ServerLevelData) world.getLevelData()).getLevelName();
            if (names.contains(name)) {
                if (world.dimension() == Level.NETHER) name = name + "_nether";
                if (world.dimension() == Level.END) name = name + "_the_end";
            }
        });
        mc.levelKeys().forEach(r -> {
            String val = r.location().toString();
            if (val.startsWith("multiworld:"))
                val = val.replace("multiworld:", "");
            names.add(val);
        });
        return names;
    }

    /**
     * @deprecated Use individual brigadier command suggestion providers instead
     */
    @Override
    @Deprecated
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        // This method is deprecated and should not be used with the new brigadier command system
        return builder.buildFuture();
    }

    /**
     * @deprecated Use BrigadierCreateCommand.GeneratorSuggestionProvider instead
     */
    @Deprecated
    public void getSuggestions_CreateCommand(SuggestionsBuilder builder, String input, String[] cmds, CommandSourceStack plr, boolean ALL) {
        // This method is deprecated
    }

}