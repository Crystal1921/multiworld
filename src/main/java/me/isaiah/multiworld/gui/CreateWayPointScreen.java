package me.isaiah.multiworld.gui;

import me.isaiah.multiworld.map.waypoint.WayPoint;
import me.isaiah.multiworld.map.waypoint.WayPointManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class CreateWayPointScreen extends Screen {
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_HEIGHT = 20;
    private static final int SPACING = 5;
    
    private final int posX;
    private final int posZ;
    private final MapScreen parent;
    private EditBox nameField;
    private EditBox xField;
    private EditBox yField;
    private EditBox zField;
    private Button createButton;
    private Button cancelButton;
    
    public CreateWayPointScreen(int posX, int posZ, MapScreen parent) {
        super(Component.translatable("multiworld.map.create_waypoint"));
        this.parent = parent;
        this.posX = posX;
        this.posZ = posZ;
    }

    @Override
    protected void init() {
        super.init();
        
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        
        // Get player's Y coordinate
        int playerY = (int) this.minecraft.player.getY();
        
        // Calculate center positions
        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;
        
        // Name field
        this.nameField = new EditBox(this.font, centerX - FIELD_WIDTH / 2, startY, FIELD_WIDTH, FIELD_HEIGHT, 
                Component.translatable("multiworld.map.waypoint.name"));
        this.nameField.setMaxLength(50);
        this.nameField.setHint(Component.translatable("multiworld.map.waypoint.name"));
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
        
        // Create button
        int buttonY = startY + 4 * (FIELD_HEIGHT + SPACING) + 10;
        this.createButton = Button.builder(Component.translatable("multiworld.map.waypoint.create"), 
                button -> createWaypoint())
                .bounds(centerX - BUTTON_WIDTH - SPACING, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(this.createButton);
        
        // Cancel button
        this.cancelButton = Button.builder(Component.translatable("multiworld.map.waypoint.cancel"), 
                button -> onClose())
                .bounds(centerX + SPACING, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(this.cancelButton);
        
        // Set initial focus to name field
        this.setInitialFocus(this.nameField);
    }
    
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render background
        guiGraphics.fill(0, 0, this.width, this.height, 0xC0101010);
        
        // Render title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        
        // Render labels for fields
        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;
        
        guiGraphics.drawString(this.font, Component.translatable("multiworld.map.waypoint.name"), 
                centerX - FIELD_WIDTH / 2, startY - 12, 0xFFFFFF);
        guiGraphics.drawString(this.font, Component.literal("X:"), 
                centerX - FIELD_WIDTH / 2, startY + FIELD_HEIGHT + SPACING - 12, 0xFFFFFF);
        guiGraphics.drawString(this.font, Component.literal("Y:"), 
                centerX - FIELD_WIDTH / 2, startY + 2 * (FIELD_HEIGHT + SPACING) - 12, 0xFFFFFF);
        guiGraphics.drawString(this.font, Component.literal("Z:"), 
                centerX - FIELD_WIDTH / 2, startY + 3 * (FIELD_HEIGHT + SPACING) - 12, 0xFFFFFF);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    private void validateNumericField(EditBox field, String text) {
        if (text.isEmpty()) {
            field.setTextColor(0xE0E0E0);
            return;
        }
        try {
            Double.parseDouble(text);
            field.setTextColor(0xE0E0E0);
        } catch (NumberFormatException e) {
            field.setTextColor(0xFF5555);
        }
    }
    
    private void createWaypoint() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        
        // Get name
        String name = this.nameField.getValue().trim();
        if (name.isEmpty()) {
            // Use default name
            name = Component.translatable("multiworld.map.waypoint.default_name").getString();
        }
        
        // Parse coordinates
        try {
            double x = Double.parseDouble(this.xField.getValue());
            double y = Double.parseDouble(this.yField.getValue());
            double z = Double.parseDouble(this.zField.getValue());
            
            // Get dimension ID
            String dimensionId = this.minecraft.player.level().dimension().location().toString();
            
            // Generate random color (RGB format as integer)
            Random random = new Random();
            int color = random.nextInt(0xFFFFFF) | 0xFF000000; // Ensure alpha is 255
            
            // Create waypoint
            WayPoint waypoint = new WayPoint(name, x, y, z, dimensionId, color);
            
            // Add to manager
            WayPointManager.INSTANCE.addWaypoint(waypoint);
            
            // Close screen
            onClose();
        } catch (NumberFormatException e) {
            // Mark invalid fields with red text color
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
