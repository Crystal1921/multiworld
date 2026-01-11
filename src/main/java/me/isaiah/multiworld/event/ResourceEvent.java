package me.isaiah.multiworld.event;

import me.isaiah.multiworld.resource.GifResource;
import me.isaiah.multiworld.resource.MapConfigResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ResourceEvent {
    @SubscribeEvent
    public static void onResourceEvent(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(MapConfigResource.INSTANCE);
        event.registerReloadListener(GifResource.INSTANCE);
    }
}
