package me.isaiah.multiworld.gui;

import me.isaiah.multiworld.waypoint.WayPoint;
import me.isaiah.multiworld.waypoint.WayPointManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WayPointManagerScreen extends Screen {
    private final MapScreen parent;
    private WayPointSelectionList wayPointList;
    private Button addButton;
    private Button deleteButton;
    private Button backButton;
    private EditBox nameField;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    // Using Random for non-security-sensitive color generation (SecureRandom would be unnecessarily slow)
    private static final Random RANDOM = new Random();

    public WayPointManagerScreen(MapScreen parentScreen) {
        super(Component.translatable("multiworld.map.waypoint.manager"));
        this.parent = parentScreen;
    }

    @Override
    protected void init() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        // Create waypoint list
        wayPointList = new WayPointSelectionList(
                this.minecraft,
                this.width - 40,
                this.height - 80,
                40,
                25
        );
        this.addWidget(wayPointList);

        // Name input field
        nameField = new EditBox(this.font, this.width / 2 - 100, 10, 200, 20, Component.literal("Name"));
        nameField.setHint(Component.translatable("multiworld.map.waypoint.name.hint"));
        nameField.setMaxLength(32);
        this.addRenderableWidget(nameField);

        // Add button
        addButton = Button.builder(Component.translatable("multiworld.map.waypoint.add"), button -> addWaypoint())
                .bounds(this.width / 2 - 155, this.height - 30, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(addButton);

        // Delete button
        deleteButton = Button.builder(Component.translatable("multiworld.map.waypoint.delete"), button -> deleteSelectedWaypoint())
                .bounds(this.width / 2 - 50, this.height - 30, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(deleteButton);

        // Back button
        backButton = Button.builder(Component.translatable("gui.back"), button -> this.minecraft.setScreen(parent))
                .bounds(this.width / 2 + 55, this.height - 30, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(backButton);

        refreshWayPointList();
    }

    private void addWaypoint() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        String name = nameField.getValue().trim();
        if (name.isEmpty()) {
            name = "Waypoint " + (WayPointManager.INSTANCE.getWaypoints().size() + 1);
        }

        // Get current player position
        var player = this.minecraft.player;
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        ResourceLocation dimensionId = player.level().dimension().location();

        // Generate random color
        int color = 0xFF000000 | RANDOM.nextInt(0xFFFFFF);

        // Create and add waypoint
        WayPoint waypoint = new WayPoint(name, x, y, z, dimensionId.toString(), color);
        WayPointManager.INSTANCE.addWaypoint(waypoint);

        // Clear name field and refresh list
        nameField.setValue("");
        refreshWayPointList();
    }

    private void deleteSelectedWaypoint() {
        WayPointSelectionList.WayPointEntry selected = wayPointList.getSelected();
        if (selected != null) {
            WayPointManager.INSTANCE.removeWaypoint(selected.getWaypoint());
            refreshWayPointList();
        }
    }

    private void refreshWayPointList() {
        wayPointList.clearEntries();
        for (WayPoint waypoint : WayPointManager.INSTANCE.getWaypoints()) {
            wayPointList.addEntry(wayPointList.new WayPointEntry(waypoint));
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        wayPointList.render(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Draw title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 5, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    public class WayPointSelectionList extends ObjectSelectionList<WayPointSelectionList.WayPointEntry> {

        public WayPointSelectionList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        public class WayPointEntry extends Entry<WayPointEntry> {
            private final WayPoint waypoint;

            public WayPointEntry(WayPoint waypoint) {
                this.waypoint = waypoint;
            }

            public WayPoint getWaypoint() {
                return waypoint;
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.literal(waypoint.name());
            }

            @Override
            public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
                // Draw waypoint name with color
                guiGraphics.drawString(
                        WayPointManagerScreen.this.font,
                        waypoint.name(),
                        left + 5,
                        top + 2,
                        waypoint.color(),
                        false
                );

                // Draw dimension and coordinates
                String dimensionName;
                try {
                    dimensionName = ResourceLocation.parse(waypoint.dimensionId()).getPath();
                } catch (IllegalArgumentException e) {
                    dimensionName = waypoint.dimensionId();
                }
                String info = String.format("%s: %.1f, %.1f, %.1f", dimensionName, waypoint.x(), waypoint.y(), waypoint.z());
                guiGraphics.drawString(
                        WayPointManagerScreen.this.font,
                        info,
                        left + 5,
                        top + 12,
                        0xAAAAAA,
                        false
                );
            }
        }
    }
}
