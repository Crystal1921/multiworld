package me.isaiah.multiworld.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import me.isaiah.multiworld.gui.screen.MapScreen;
import me.isaiah.multiworld.map.MapInstance;
import me.isaiah.multiworld.portal.Portal;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static me.isaiah.multiworld.gui.widget.WorldList.WorldEntry.setMapData;

public class PortalList extends ObjectSelectionList<PortalList.PortalEntry> {

    private final MapScreen mapScreen;

    public PortalList(MapScreen mapScreen, int listWidth, int top, int bottom) {
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
        mapScreen.buildPortalList(this::addEntry, item -> new PortalEntry(item, this.mapScreen));
    }

    public int getRowWidth() {
        return this.width;
    }

    public static class PortalEntry extends Entry<PortalEntry> {
        private final Portal portal;
        private final MapScreen mapScreen;

        public PortalEntry(Portal portal, MapScreen mapScreen) {
            this.portal = portal;
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
                ResourceLocation originWorldId = portal.getOriginWorldId();
                MapInstance.INSTANCE.mapConfigs.forEach(mapConfig -> {
                    if (mapConfig.worldID().equals(originWorldId.toString())) {
                        setMapData(originWorldId, mapConfig, mapWidget);

                        // Center the map on the portal position
                        BlockPos minPos = portal.getMinPos();
                        BlockPos maxPos = portal.getMaxPos();
                        // Calculate portal center position
                        double portalCenterX = (minPos.getX() + maxPos.getX()) / 2.0;
                        double portalCenterZ = (minPos.getZ() + maxPos.getZ()) / 2.0;
                        MapWidget.centerOnPosition(mapWidget.getMapConfig(),portalCenterX, portalCenterZ);
                    }
                });
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
            guiGraphics.drawString(mapScreen.getFontRenderer(), portal.getName(), left + 5, top + 5, 0xFFFFFF, false);
            guiGraphics.drawString(mapScreen.getFontRenderer(), portal.getDestWorldName(), left + 5, top + 15, 0xFFFFFF, false);

            pose.popPose();
        }
    }
}
