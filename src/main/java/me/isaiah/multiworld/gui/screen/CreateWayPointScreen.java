package me.isaiah.multiworld.gui.screen;

import me.isaiah.multiworld.map.MapInstance;
import me.isaiah.multiworld.map.waypoint.WayPoint;
import me.isaiah.multiworld.map.waypoint.WayPointManager;
import me.isaiah.multiworld.util.UtilsMethod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static me.isaiah.multiworld.util.UtilsMethod.*;

public class CreateWayPointScreen extends Screen {
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_HEIGHT = 20;
    private static final int SPACING = 5;

    private final int posX;
    private final int posZ;
    private final MapScreen parent;
    private final MapInstance.MapConfig mapConfig;
    private EditBox nameField;
    private EditBox xField;
    private EditBox yField;
    private EditBox zField;
    private EditBox dimensionField;
    private EditBox colorField;

    public CreateWayPointScreen(int posX, int posZ, MapInstance.MapConfig mapConfig, MapScreen parent) {
        super(Component.translatable("multiworld.map.create_waypoint"));
        this.parent = parent;
        this.mapConfig = mapConfig;
        this.posX = posX;
        this.posZ = posZ;
    }

    @Override
    protected void init() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        // Get player's Y coordinate and default dimension
        int playerY = (int) this.minecraft.player.getY();

        // Calculate center positions
        int centerX = this.width / 2;
        int startY = this.height / 2 - 80;

        // Name field
        this.nameField = new EditBox(this.font, centerX - FIELD_WIDTH / 2, startY, FIELD_WIDTH, FIELD_HEIGHT,
                Component.translatable("multiworld.map.waypoint.name"));
        this.nameField.setMaxLength(50);
        this.nameField.setResponder((string -> nameField.setHint(Component.empty())));
        this.addRenderableWidget(this.nameField);

        // X coordinate field
        this.xField = new EditBox(this.font, centerX - FIELD_WIDTH / 2, startY + FIELD_HEIGHT + SPACING,
                FIELD_WIDTH, FIELD_HEIGHT, Component.literal("X"));
        this.xField.setMaxLength(10);
        this.xField.setValue(String.valueOf(this.posX));
        this.xField.setHint(Component.literal("X"));
        this.xField.setResponder(text -> validateNumericField(this.xField, text));
        this.addRenderableWidget(this.xField);

        // Y coordinate field
        this.yField = new EditBox(this.font, centerX - FIELD_WIDTH / 2, startY + 2 * (FIELD_HEIGHT + SPACING),
                FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Y"));
        this.yField.setMaxLength(10);
        this.yField.setValue(String.valueOf(playerY));
        this.yField.setHint(Component.literal("Y"));
        this.yField.setResponder(text -> validateNumericField(this.yField, text));
        this.addRenderableWidget(this.yField);

        // Z coordinate field
        this.zField = new EditBox(this.font, centerX - FIELD_WIDTH / 2, startY + 3 * (FIELD_HEIGHT + SPACING),
                FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Z"));
        this.zField.setMaxLength(10);
        this.zField.setValue(String.valueOf(this.posZ));
        this.zField.setHint(Component.literal("Z"));
        this.zField.setResponder(text -> validateNumericField(this.zField, text));
        this.addRenderableWidget(this.zField);

        // Dimension ID field
        this.dimensionField = new EditBox(this.font, centerX - FIELD_WIDTH / 2, startY + 4 * (FIELD_HEIGHT + SPACING),
                FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Dimension"));
        this.dimensionField.setMaxLength(200);
        this.dimensionField.setValue(mapConfig.worldID());
        this.dimensionField.setHint(Component.literal("Dimension"));
        this.dimensionField.setResponder(text -> {
            if (MapInstance.getWorldIdList().contains(text)) {
                dimensionField.setTextColor(0xE0E0E0);
            } else {
                dimensionField.setTextColor(0xFF5555);
            }
            dimensionField.setHint(Component.empty());
        });
        this.addRenderableWidget(this.dimensionField);

        // Color field (hex)
        Random rand = new Random();
        String defaultColorHex = String.format("#%06X", rand.nextInt(0xFFFFFF + 1));
        this.colorField = new EditBox(this.font, centerX - FIELD_WIDTH / 2, startY + 5 * (FIELD_HEIGHT + SPACING),
                FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Color"));
        this.colorField.setMaxLength(7); // allow leading '#'
        this.colorField.setValue(defaultColorHex);
        this.colorField.setHint(Component.literal("#RRGGBB"));
        this.colorField.setResponder(text -> validateColorField(this.colorField, text));
        this.addRenderableWidget(this.colorField);

        // Create button (moved down to account for extra fields)
        int buttonY = startY + 6 * (FIELD_HEIGHT + SPACING) + 10;
        Button createButton = Button.builder(Component.translatable("multiworld.map.waypoint.create"),
                        button -> createWaypoint())
                .bounds(centerX - BUTTON_WIDTH - SPACING, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(createButton);

        // Cancel button
        Button cancelButton = Button.builder(Component.translatable("multiworld.map.waypoint.cancel"),
                        button -> onClose())
                .bounds(centerX + SPACING, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(cancelButton);

        // Set initial focus to name field
        this.setInitialFocus(this.nameField);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        // Render title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        int centerX = this.width / 2;
        int startY = this.height / 2 - 80;

        // Prepare translatable labels
        Component nameComp = Component.translatable("multiworld.map.waypoint.name");
        Component xComp = Component.translatable("multiworld.map.waypoint.label.x");
        Component yComp = Component.translatable("multiworld.map.waypoint.label.y");
        Component zComp = Component.translatable("multiworld.map.waypoint.label.z");
        Component dimComp = Component.translatable("multiworld.map.waypoint.label.dimension");
        Component colorComp = Component.translatable("multiworld.map.waypoint.label.color");

        // Use the translated string widths to compute right-aligned baseline (use longest label as baseline)
        int dimWidth = this.font.width(dimComp.getString());
        int labelRight = centerX - FIELD_WIDTH / 2 - 26 + dimWidth;

        int yName = startY + 6;
        int yX = startY + FIELD_HEIGHT + SPACING + 6;
        int yY = startY + 2 * (FIELD_HEIGHT + SPACING) + 6;
        int yZ = startY + 3 * (FIELD_HEIGHT + SPACING) + 6;
        int yDim = startY + 4 * (FIELD_HEIGHT + SPACING) + 6;
        int yColor = startY + 5 * (FIELD_HEIGHT + SPACING) + 6;

        // Draw each label right-aligned to the computed baseline
        guiGraphics.drawString(this.font, nameComp, labelRight - this.font.width(nameComp.getString()), yName, 0xFFFFFF);
        guiGraphics.drawString(this.font, xComp, labelRight - this.font.width(xComp.getString()), yX, 0xFFFFFF);
        guiGraphics.drawString(this.font, yComp, labelRight - this.font.width(yComp.getString()), yY, 0xFFFFFF);
        guiGraphics.drawString(this.font, zComp, labelRight - this.font.width(zComp.getString()), yZ, 0xFFFFFF);
        guiGraphics.drawString(this.font, dimComp, labelRight - this.font.width(dimComp.getString()), yDim, 0xFFFFFF);
        guiGraphics.drawString(this.font, colorComp, labelRight - this.font.width(colorComp.getString()), yColor, 0xFFFFFF);

        if (validateColorField(this.colorField)) {
            guiGraphics.fill(centerX - FIELD_WIDTH / 2 - 50,
                    startY + 5 * (FIELD_HEIGHT + SPACING),
                    centerX - FIELD_WIDTH / 2 - 30,
                    startY + 5 * (FIELD_HEIGHT + SPACING) + FIELD_HEIGHT,
                    UtilsMethod.parseColorHexToArgb(this.colorField.getValue()));
        }
    }

    private void createWaypoint() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        // Get name
        String name = this.nameField.getValue().trim();
        String dimension = this.dimensionField.getValue().trim();
        if (name.isEmpty()) {
            nameField.setValue("");
            nameField.setHint(Component.translatable("multiworld.map.warn.name_empty").withStyle(ChatFormatting.GRAY));
            return;
        }

        if (WayPointManager.INSTANCE.contains(name)) {
            nameField.setValue("");
            nameField.setHint(Component.translatable("multiworld.map.warn.waypoint_exists").withStyle(ChatFormatting.GRAY));
            return;
        }

        if (!MapInstance.getWorldIdList().contains(dimension)) {
            dimensionField.setValue("");
            dimensionField.setHint(Component.translatable("multiworld.map.warn.dimension_not_exist").withStyle(ChatFormatting.GRAY));
            return;
        }

        // Parse coordinates
        try {
            double x = Double.parseDouble(this.xField.getValue());
            double y = Double.parseDouble(this.yField.getValue());
            double z = Double.parseDouble(this.zField.getValue());

            // Get dimension ID (use input if provided)
            String dimensionId = this.dimensionField.getValue().trim();

            // Parse color hex input
            int color;
            try {
                String colorText = this.colorField.getValue().trim();
                if (colorText.startsWith("#")) {
                    colorText = colorText.substring(1);
                }
                if (colorText.length() != 6) {
                    throw new NumberFormatException("Color must be 6 hex digits");
                }
                int rgb = Integer.parseInt(colorText, 16) & 0xFFFFFF;
                color = rgb | 0xFF000000; // Ensure alpha = 255
            } catch (NumberFormatException e) {
                this.colorField.setTextColor(0xFF5555);
                return;
            }

            // Create waypoint
            WayPoint waypoint = new WayPoint(name, x, y, z, dimensionId, color);

            // Add to manager
            WayPointManager.INSTANCE.addWaypoint(waypoint);

            // Close screen
            onClose();
        } catch (NumberFormatException e) {
            // Mark invalid coordinate fields with red text color
            try {
                Double.parseDouble(this.xField.getValue());
            } catch (NumberFormatException ex) {
                this.xField.setTextColor(0xFF5555);
            }
            try {
                Double.parseDouble(this.yField.getValue());
            } catch (NumberFormatException ex) {
                this.yField.setTextColor(0xFF5555);
            }
            try {
                Double.parseDouble(this.zField.getValue());
            } catch (NumberFormatException ex) {
                this.zField.setTextColor(0xFF5555);
            }
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        } else {
            super.onClose();
        }
    }
}
