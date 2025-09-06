package me.isaiah.multiworld.event;

import me.isaiah.multiworld.command.HomeCommand;
import me.isaiah.multiworld.command.SpawnCommand;
import me.isaiah.multiworld.command.WarpCommand;
import me.isaiah.multiworld.command.commands.BorderCommand;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@EventBusSubscriber
public class ServerStartEvent {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerStart(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        BorderCommand.initWorldBorder(server);
        WarpCommand.initWarp(server);
        SpawnCommand.initSpawn(server);
        HomeCommand.initHome(server);
    }
}
