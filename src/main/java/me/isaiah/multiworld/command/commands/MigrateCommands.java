package me.isaiah.multiworld.command.commands;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.command.MultiworldCommand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

import java.io.IOException;
import java.util.HashMap;

import static me.isaiah.multiworld.command.commands.GameruleCommand.setRuleConfig;
import static net.minecraft.world.level.GameRules.visitGameRuleTypes;

public class MigrateCommands {
    public static int runMigrate(MinecraftServer server, ServerPlayer player, ResourceLocation fromWorld, ResourceLocation toWorld) {
        HashMap<ResourceLocation, ServerLevel> worlds = new HashMap<>();
        server.levelKeys().forEach(r -> {
            ServerLevel world = server.getLevel(r);
            worlds.put(r.location(), world);
        });

        ServerLevel fromLevel = worlds.get(fromWorld);
        ServerLevel targetLevel = worlds.get(toWorld);

        if (fromLevel == null || targetLevel == null) {
            MultiworldCommand.message(player, "[&4Multiworld&r] One of the specified worlds does not exist.");
            return 0;
        }

        GameRules gameRules = fromLevel.getGameRules();
        setGamerules(targetLevel, gameRules);

        return 1;
    }

    public static void setGamerules(ServerLevel targetLevel, GameRules gameRules) {
        GameRules finalGameRules = gameRules.copy();
        visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            public void visitBoolean(GameRules.Key<GameRules.BooleanValue> key, GameRules.Type<GameRules.BooleanValue> type) {
                try {
                    setRuleConfig(targetLevel, key.getId(), String.valueOf(finalGameRules.getRule(key).get()));
                } catch (IOException e) {
                    MultiworldMod.LOGGER.error(e.getMessage());
                }
            }

            public void visitInteger(GameRules.Key<GameRules.IntegerValue> key, GameRules.Type<GameRules.IntegerValue> type) {
                try {
                    setRuleConfig(targetLevel, key.getId(), String.valueOf(finalGameRules.getRule(key).get()));
                } catch (IOException e) {
                    MultiworldMod.LOGGER.error(e.getMessage());
                }
            }
        });
    }
}
