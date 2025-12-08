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

import static me.isaiah.multiworld.util.UtilsMethod.*;

public class EditWayPointScreen extends Screen {
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_HEIGHT = 20;
    private static final int SPACING = 5;

    private final MapScreen parent;
    private final MapInstance.MapConfig mapConfig;
    private final WayPoint originalWayPoint;

    private EditBox nameField;
    private EditBox xField;
    private EditBox yField;
    private EditBox zField;
    private EditBox dimensionField;
    private EditBox colorField;

    public EditWayPointScreen(WayPoint originalWayPoint, MapInstance.MapConfig mapConfig, MapScreen parent) {
        super(Component.translatable("multiworld.map.waypoint.label.edit"));
        this.parent = parent;
        this.mapConfig = mapConfig;
        this.originalWayPoint = originalWayPoint;
    }

    @Override
    protected void init() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        int centerX = this.width / 2;
        int startY = this.height / 2 - 80;

        // Name field
        this.nameField = new EditBox(this.font, centerX - FIELD_WIDTH / 2, startY, FIELD_WIDTH, FIELD_HEIGHT,
                Component.translatable("multiworld.map.waypoint.name"));
        this.nameField.setMaxLength(50);
        this.nameField.setValue(originalWayPoint.name());
        this.nameField.setResponder((string -> nameField.setHint(Component.empty())));
        this.addRenderableWidget(this.nameField);

        // X coordinate field
        this.xField = new EditBox(this.font, centerX - FIELD_WIDTH / 2, startY + FIELD_HEIGHT + SPACING,
                FIELD_WIDTH, FIELD_HEIGHT, Component.literal("X"));
        this.xField.setMaxLength(10);
        this.xField.setValue(String.valueOf(originalWayPoint.x()));
        this.xField.setHint(Component.literal("X"));
        this.xField.setResponder(text -> UtilsMethod.validateNumericField(this.xField, text));
        this.addRenderableWidget(this.xField);

        // Y coordinate field
        this.yField = new EditBox(this.font, centerX - FIELD_WIDTH / 2, startY + 2 * (FIELD_HEIGHT + SPACING),
                FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Y"));
        this.yField.setMaxLength(10);
        this.yField.setValue(String.valueOf(originalWayPoint.y()));
        this.yField.setHint(Component.literal("Y"));
        this.yField.setResponder(text -> UtilsMethod.validateNumericField(this.yField, text));
        this.addRenderableWidget(this.yField);

        // Z coordinate field
        this.zField = new EditBox(this.font, centerX - FIELD_WIDTH / 2, startY + 3 * (FIELD_HEIGHT + SPACING),
                FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Z"));
        this.zField.setMaxLength(10);
        this.zField.setValue(String.valueOf(originalWayPoint.z()));
        this.zField.setHint(Component.literal("Z"));
        this.zField.setResponder(text -> UtilsMethod.validateNumericField(this.zField, text));
        this.addRenderableWidget(this.zField);

        // Dimension field
        this.dimensionField = new EditBox(this.font, centerX - FIELD_WIDTH / 2, startY + 4 * (FIELD_HEIGHT + SPACING),
                FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Dimension"));
        this.dimensionField.setMaxLength(50);
        this.dimensionField.setValue(originalWayPoint.dimensionId());
        this.dimensionField.setResponder((string -> dimensionField.setHint(Component.empty())));
        this.addRenderableWidget(this.dimensionField);

        // Color field
        this.colorField = new EditBox(this.font, centerX - FIELD_WIDTH / 2, startY + 5 * (FIELD_HEIGHT + SPACING),
                FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Color"));
        this.colorField.setMaxLength(7);
        String hexColor = String.format("#%06X", (0xFFFFFF & originalWayPoint.color()));
        this.colorField.setValue(hexColor);
        this.colorField.setHint(Component.literal("#RRGGBB"));
        this.colorField.setResponder(text -> UtilsMethod.validateColorField(this.colorField, text));
        this.addRenderableWidget(this.colorField);

        // Save button
        int buttonY = startY + 6 * (FIELD_HEIGHT + SPACING) + 10;
        Button saveButton = Button.builder(Component.translatable("multiworld.map.waypoint.label.edit"),
                        button -> saveWaypoint())
                .bounds(centerX - BUTTON_WIDTH - SPACING, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(saveButton);

        // Cancel button
        Button cancelButton = Button.builder(Component.translatable("multiworld.map.waypoint.cancel"),
                        button -> onClose())
                .bounds(centerX + SPACING, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(cancelButton);

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

        if (UtilsMethod.validateColorField(this.colorField)) {
            // Draw color preview box
            guiGraphics.fill(centerX - FIELD_WIDTH / 2 - 50,
                    startY + 5 * (FIELD_HEIGHT + SPACING),
                    centerX - FIELD_WIDTH / 2 - 30,
                    startY + 5 * (FIELD_HEIGHT + SPACING) + FIELD_HEIGHT,
                    parseColorHexToArgb(this.colorField.getValue()));
        }
    }

    private void saveWaypoint() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        // Get name
        String name = this.nameField.getValue().trim();
        String dimensionId = this.dimensionField.getValue().trim();

        if (name.isEmpty()) {
            nameField.setValue("");
            nameField.setHint(Component.translatable("multiworld.map.warn.name_empty").withStyle(ChatFormatting.GRAY));
            return;
        }

        // Check for duplicate name ONLY if the name has changed
        if (!name.equals(originalWayPoint.name()) && WayPointManager.INSTANCE.contains(name)) {
            nameField.setValue("");
            nameField.setHint(Component.translatable("multiworld.map.warn.waypoint_exists").withStyle(ChatFormatting.GRAY));
            return;
        }

        if (!MapInstance.getWorldIdList().contains(dimensionId)) {
            dimensionField.setValue("");
            dimensionField.setHint(Component.translatable("multiworld.map.warn.dimension_not_exist").withStyle(ChatFormatting.GRAY));
            return;
        }

        // Parse coordinates
        try {
            double x = Double.parseDouble(this.xField.getValue());
            double y = Double.parseDouble(this.yField.getValue());
            double z = Double.parseDouble(this.zField.getValue());

            // Parse color hex input
            int color;
            try {
                String colorText = this.colorField.getValue().trim();
                Integer parsedColor = parseColorHexToArgb(colorText);
                if (parsedColor == null) {
                    throw new NumberFormatException("Invalid color");
                }
                color = parsedColor;
            } catch (NumberFormatException e) {
                this.colorField.setTextColor(0xFF5555);
                return;
            }

            // Create new waypoint
            WayPoint newWaypoint = new WayPoint(name, x, y, z, dimensionId, color);

            // Remove old waypoint and add new one
            WayPointManager.INSTANCE.removeWaypoint(originalWayPoint);
            WayPointManager.INSTANCE.addWaypoint(newWaypoint);

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