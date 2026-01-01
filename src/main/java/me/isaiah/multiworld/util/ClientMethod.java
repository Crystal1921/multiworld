package me.isaiah.multiworld.util;

import me.isaiah.multiworld.gui.MapOverlay;
import me.isaiah.multiworld.map.MapInstance;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.Objects;

public class ClientMethod {
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LocalPlayer player) {
            ResourceLocation location = player.level().dimension().location();
            setMapConfig(location);
        }
    }

    private static void setMapConfig(ResourceLocation location) {
        boolean anyMatch = MapInstance.INSTANCE.mapConfigs.stream()
                .filter(Objects::nonNull)
                .anyMatch(mapConfig -> {
                    if (mapConfig.worldID().equals(location.toString())) {
                        MapOverlay.mapConfig = mapConfig;
                        return true;
                    }
                    return false;
                });
        if (!anyMatch) {
            MapOverlay.mapConfig = null;
        }
    }
}
