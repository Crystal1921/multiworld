package me.isaiah.multiworld.event;

import me.isaiah.multiworld.gui.MapScreen;
import me.isaiah.multiworld.map.MapInstance;
import me.isaiah.multiworld.waypoint.WayPointManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber
public class ClientEvent {
    @SubscribeEvent
    public static void onClientSetUp(FMLClientSetupEvent event) {
        MapInstance.initMapConfig();
        WayPointManager.initWayPointConfig();
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MapScreen.MAP_OPEN_KEY);
    }
}
