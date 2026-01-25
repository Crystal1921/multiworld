package me.isaiah.multiworld.event;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.command.HomeCommand;
import me.isaiah.multiworld.command.SpawnCommand;
import me.isaiah.multiworld.command.WarpCommand;
import me.isaiah.multiworld.command.commands.BorderCommand;
import me.isaiah.multiworld.util.FantasyDayTime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.util.Map;

@EventBusSubscriber
public class ServerStartEvent {
    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        MultiworldMod.onServerStarted(event.getServer());
        MinecraftServer server = event.getServer();
        BorderCommand.initWorldBorder(server);
        WarpCommand.initWarp(server);
        SpawnCommand.initSpawn(server);
        HomeCommand.initHome(server);

        FantasyDayTime.initDayTimeConfig();
        Map<ResourceLocation, FantasyDayTime.TimeData> dataMap = FantasyDayTime.INSTANCE.getDataMap();
        server.getAllLevels().forEach(level -> {
            ResourceLocation location = level.dimension().location();
            FantasyDayTime.TimeData timeData = dataMap.get(location);
            if (timeData != null) {
                level.setDayTime(timeData.datTime());
                level.setDayTimeFraction(timeData.timeFraction());
                level.setDayTimePerTick(timeData.timePerTick());
            }
        });
    }
}
