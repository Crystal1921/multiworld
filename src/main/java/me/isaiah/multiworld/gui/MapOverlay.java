package me.isaiah.multiworld.gui;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.config.ClientConfig;
import me.isaiah.multiworld.map.MapInstance;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class MapOverlay implements LayeredDraw.Layer {
    public static MapInstance.MapConfig mapConfig = null;

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel world = minecraft.level;
        Options options = minecraft.options;
        Player player = minecraft.player;
        if (player == null || world == null || options.hideGui) {
            return;
        }
        if (player.isSpectator()) {
            return;
        }

        if (mapConfig == null) {
            return;
        }

        if (!ClientConfig.ENABLE_LITTLE_MAP.get()) {
            return;
        }

        ResourceLocation map = ResourceLocation.fromNamespaceAndPath(MultiworldMod.MOD_ID, "textures/map/" + mapConfig.mapName() + ".png");

        int guiWidth = guiGraphics.guiWidth();
        int mapDisplayWidth = MapInstance.INSTANCE.mapSize;
        int mapDisplayHeight = MapInstance.INSTANCE.mapSize;
        int mapScreenX = guiWidth - mapDisplayWidth - MapInstance.INSTANCE.mapPosX;
        int mapScreenY = MapInstance.INSTANCE.mapPosY;

        // Calculate scale based on world size (same as original)
        int minX = mapConfig.minX();
        int minZ = mapConfig.minZ();
        int maxX = mapConfig.maxX();
        int maxZ = mapConfig.maxZ();
        float worldWidth = Math.max(1, (float) (maxX - minX));
        float worldHeight = Math.max(1, (float) (maxZ - minZ));
        double mapScale = (double) Math.max(worldWidth, worldHeight) / 32;

        MapRenderer.drawMap(
                guiGraphics,
                player,
                map,
                mapConfig,
                Collections.emptyList(),  // No portals in overlay
                minecraft.font,
                mapScreenX,
                mapScreenY,
                mapDisplayWidth,
                mapDisplayHeight,
                mapScale,
                mapScreenX,
                mapScreenY,
                mapScreenX + mapDisplayWidth,
                mapScreenY + mapDisplayHeight,
                false,  // Don't show portals
                null,   // Use player position for center
                null    // Use player position for center
        );

        // Display player coordinates text (specific to MapOverlay)
        guiGraphics.drawString(
                minecraft.font,
                "X: " + (int) player.position().x + " Y: " + (int) player.position().y + " Z: " + (int) player.position().z,
                mapScreenX,
                mapScreenY + mapDisplayHeight + 5,
                0xFFFFFF,
                false
        );
    }
}
