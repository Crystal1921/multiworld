package me.isaiah.multiworld.command.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class TimeCommand {
    public static int setTime(CommandSourceStack source, int time) {
        ServerPlayer player = source.getPlayer();
        if (player != null) {
            source.getServer().getAllLevels().forEach(level -> {
                if (level.dimension().location().equals(player.level().dimension().location())) {
                    level.setDayTime(time);
                    source.sendSuccess(() -> Component.translatable("commands.time.set", time), true);
                    player.server.forceTimeSynchronization();
                }
            });
        }

        return getDayTime(source.getLevel());
    }

    public static int addTime(CommandSourceStack source, int amount) {
        ServerLevel serverLevel = source.getLevel();
        serverLevel.setDayTime(serverLevel.getDayTime() + (long) amount);

        int i = getDayTime(source.getLevel());
        source.sendSuccess(() -> Component.translatable("commands.time.set", i), true);
        return i;
    }

    /**
     * Returns the day time (time wrapped within a day)
     */
    private static int getDayTime(ServerLevel level) {
        return (int) (level.getDayTime() % 24000L);
    }
}
