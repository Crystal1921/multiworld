package me.isaiah.multiworld.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import me.isaiah.multiworld.gui.MapScreen;
import me.isaiah.multiworld.map.MapInstance;
import me.isaiah.multiworld.waypoint.WayPoint;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static me.isaiah.multiworld.gui.widget.WorldList.WorldEntry.setMapData;

public class WayPointList extends ObjectSelectionList<WayPointList.WayPointEntry> {

    private final MapScreen mapScreen;

    public WayPointList(MapScreen mapScreen, int listWidth, int top, int bottom) {
        super(mapScreen.getMinecraftInstance(), listWidth, bottom, top, mapScreen.getFontRenderer().lineHeight * 2 + 8);
        this.mapScreen = mapScreen;
        this.refreshList();
    }

    public void refreshList() {
        this.clearEntries();
        mapScreen.buildWayPointList(this::addEntry, item -> new WayPointEntry(item, this.mapScreen));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible) {
            return super.mouseClicked(mouseX, mouseY, button);
        } else {
            return false;
        }
    }

    public int getRowWidth() {
        return this.width;
    }

    public static class WayPointEntry extends Entry<WayPointEntry> {
        private final WayPoint waypoint;
        private final MapScreen mapScreen;

        public WayPointEntry(WayPoint waypoint, MapScreen mapScreen) {
            this.waypoint = waypoint;
            this.mapScreen = mapScreen;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(waypoint.name());
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            MapWidget mapWidget = mapScreen.getMapWidget();
            if (mapWidget != null) {
                try {
                    ResourceLocation dimensionId = ResourceLocation.parse(waypoint.dimensionId());
                    
                    // Find the matching map config for this dimension
                    MapInstance.INSTANCE.mapConfigs.forEach(mapConfig -> {
                        if (mapConfig.worldID().equals(dimensionId.toString())) {
                            setMapData(dimensionId, mapConfig, mapWidget);
                            
                            // Center the map on the waypoint position
                            mapWidget.centerOnPosition(waypoint.x(), waypoint.z());
                        }
                    });
                } catch (IllegalArgumentException e) {
                    // Handle invalid dimension ID format
                }
            }
            return true;
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(left + 5, top + 5, 0.0D);
            pose.scale(0.7F, 0.7F, 0.7F);
            pose.translate(-(left + 5), -(top + 5), 0.0D);
            
            // Draw waypoint name with color
            guiGraphics.drawString(
                    mapScreen.getFontRenderer(),
                    waypoint.name(),
                    left + 5,
                    top + 5,
                    waypoint.color(),
                    false
            );

            // Draw coordinates
            String coords = String.format("%.1f, %.1f, %.1f", waypoint.x(), waypoint.y(), waypoint.z());
            guiGraphics.drawString(
                    mapScreen.getFontRenderer(),
                    coords,
                    left + 5,
                    top + 15,
                    0xAAAAAA,
                    false
            );

            pose.popPose();
        }
    }
}
