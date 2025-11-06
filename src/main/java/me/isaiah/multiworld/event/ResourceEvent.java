package me.isaiah.multiworld.event;

import me.isaiah.multiworld.dataGen.loader.MapConfigLoader;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber
public class ResourceEvent {
    @SubscribeEvent
    public static void onResourceReload(AddReloadListenerEvent event) {
        event.addListener(new MapConfigLoader());
    }
}
