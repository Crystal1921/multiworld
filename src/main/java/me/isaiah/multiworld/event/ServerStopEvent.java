package me.isaiah.multiworld.event;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.util.FantasyDayTime;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber
public class ServerStopEvent {
    @SubscribeEvent
    public static void onServerStop(ServerStoppingEvent event) {
        MinecraftServer server = event.getServer();
        server.getAllLevels().forEach(level -> {
            if (level.getLevelData() instanceof ServerLevelData serverLevelData) {
                FantasyDayTime.TimeData timeData = new FantasyDayTime.TimeData(serverLevelData.getDayTime(), serverLevelData.getDayTimeFraction(), serverLevelData.getDayTimePerTick());
                FantasyDayTime.INSTANCE.getDataMap().put(level.dimension().location(), timeData);
            }
        });
        try {
            FantasyDayTime.save("config/multiworld/daytime.yml");
        } catch (Exception e) {
            MultiworldMod.LOGGER.warn("FantasyDayTime config file does not exist!");
        }
    }
}
