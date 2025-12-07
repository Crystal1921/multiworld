package me.isaiah.multiworld.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import me.isaiah.multiworld.gui.MapScreen;
import me.isaiah.multiworld.map.MapInstance;
import me.isaiah.multiworld.map.waypoint.WayPoint;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static me.isaiah.multiworld.gui.widget.WorldList.WorldEntry.setMapData;

public class WayPointList extends ObjectSelectionList<WayPointList.WayPointEntry> {

    private final MapScreen mapScreen;

    public WayPointList(MapScreen mapScreen, int listWidth, int top, int bottom) {
        super(mapScreen.getMinecraftInstance(), listWidth, bottom, top, mapScreen.getFontRenderer().lineHeight * 2 + 8);
        this.mapScreen = mapScreen;
        this.refreshList();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible) {
            return super.mouseClicked(mouseX, mouseY, button);
        } else {
            return false;
        }
    }

    private void refreshList() {
        this.clearEntries();
        mapScreen.buildWayPointList(this::addEntry, item -> new WayPointEntry(item, this.mapScreen));
    }

    public int getRowWidth() {
        return this.width;
    }

    public static class WayPointEntry extends Entry<WayPointEntry> {
        private final WayPoint wayPoint;
        private final MapScreen mapScreen;

        public WayPointEntry(WayPoint wayPoint, MapScreen mapScreen) {
            this.wayPoint = wayPoint;
            this.mapScreen = mapScreen;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.empty();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            MapWidget mapWidget = mapScreen.getMapWidget();
            if (mapWidget != null) {
                try {
                    ResourceLocation wayPointWorldId = ResourceLocation.parse(wayPoint.dimensionId());
                    MapInstance.INSTANCE.mapConfigs.stream()
                            .filter(mapConfig -> mapConfig.worldID().equals(wayPointWorldId.toString()))
                            .findFirst()
                            .ifPresent(mapConfig -> {
                                setMapData(wayPointWorldId, mapConfig, mapWidget);

                                // Center the map on the waypoint position
                                MapWidget.centerOnPosition(mapWidget.getMapConfig(), wayPoint.x(), wayPoint.z());
                            });
                } catch (Exception e) {
                    // Silently ignore invalid dimension IDs
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
            guiGraphics.drawString(mapScreen.getFontRenderer(), wayPoint.name(), left + 5, top + 5, 0xFFFFFF, false);
            guiGraphics.drawString(mapScreen.getFontRenderer(), wayPoint.dimensionId(), left + 5, top + 15, 0xFFFFFF, false);

            pose.popPose();
        }
    }
}
