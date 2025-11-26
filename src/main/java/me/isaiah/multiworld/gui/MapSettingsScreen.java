package me.isaiah.multiworld.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class MapSettingsScreen extends Screen {
    private final MapScreen parent;
    public final static int BUTTON_WIDTH = 100;
    protected MapSettingsScreen(MapScreen mapScreen) {
        super(Component.translatable("multiworld.map.settings.title"));
        this.parent = mapScreen;
    }

    @Override
    protected void init() {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int widthCenter = this.width / 2;
        CycleButton.booleanBuilder(Component.translatable("multiworld.map.settings.little_map_on"), Component.translatable("multiworld.map.settings.little_map_off"))
                .create(widthCenter - BUTTON_WIDTH / 2, this.height / 2 - 10, BUTTON_WIDTH, 20,
                        Component.translatable("multiworld.map.settings.little_map"),
                        (button, value) -> {
                            if (minecraft.player != null) {

                            }
                        });
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF000000);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        } else {
            super.onClose();
        }
    }
}
