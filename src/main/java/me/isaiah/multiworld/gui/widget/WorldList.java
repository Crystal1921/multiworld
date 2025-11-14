package me.isaiah.multiworld.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import me.isaiah.multiworld.command.commands.PortalCommand;
import me.isaiah.multiworld.gui.MapScreen;
import me.isaiah.multiworld.map.MapInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WorldList extends ObjectSelectionList<WorldList.WorldEntry> {

    private final MapScreen mapScreen;

    public WorldList(MapScreen mapScreen, int listWidth, int top, int bottom) {
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

    public static void getPortalList(List<Vec2> portals, ResourceLocation resourceLocation) {
        PortalCommand.KNOWN_PORTALS.forEach((s, portal) -> {
            if (portal.getOriginWorld().dimension().location().equals(resourceLocation)) {
                BlockPos minPos = portal.getMinPos();
                BlockPos maxPos = portal.getMaxPos();
                portals.add(new Vec2((minPos.getX() + maxPos.getX()) / 2f, (minPos.getZ() + maxPos.getZ()) / 2f));
            }
        });
    }

    private void refreshList() {
        this.clearEntries();
        mapScreen.buildWorldList(this::addEntry, item -> new WorldEntry(item, this.mapScreen));
    }

    public int getRowWidth() {
        return this.width;
    }

    public static class WorldEntry extends ObjectSelectionList.Entry<WorldEntry> {
        private final MapInstance.MapConfig mapConfig;
        private final MapScreen mapScreen;

        public WorldEntry(MapInstance.MapConfig mapConfig, MapScreen mapScreen) {
            this.mapConfig = mapConfig;
            this.mapScreen = mapScreen;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.empty();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            MapWidget mapWidget = mapScreen.getMapWidget();
            Minecraft instance = Minecraft.getInstance();
            LocalPlayer player = instance.player;
            List<Vec2> portals = new ArrayList<>();
            if (player != null) {
                ResourceLocation resourceLocation = ResourceLocation.parse(mapConfig.worldID());
                getPortalList(portals, resourceLocation);
            }

            mapWidget.setMapConfig(this.mapConfig);
            mapWidget.setPortals(portals);

            return true;
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(left + 5, top + 5, 0.0D);
            pose.scale(0.7F, 0.7F, 0.7F);
            pose.translate(-(left + 5), -(top + 5), 0.0D);
            guiGraphics.drawString(
                    mapScreen.getFontRenderer(),
                    mapConfig.mapName(),
                    left + 5,
                    top + 5,
                    0xFFFFFF,
                    false
            );

            pose.popPose();
        }
    }
}
