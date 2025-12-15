package me.isaiah.multiworld.event;

import me.isaiah.multiworld.util.ClientMethod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber
public class PlayerJoinEvent {
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity().level().isClientSide()) {
            ClientMethod.onEntityJoin(event);
        }
    }
}
