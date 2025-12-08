package me.isaiah.multiworld.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class WayPointButton extends Button {
    public WayPointButton(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, onPress, createNarration);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int color = this.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF000000;
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x80000000);
        guiGraphics.renderOutline(this.getX(), this.getY(), this.width, this.height, color);

        // 绘制文字
        int textColor = this.active ? 16777215 : 10526880;
        guiGraphics.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font,
                this.getMessage(),
                this.getX() + this.width / 2,
                this.getY() + (this.height - 8) / 2,
                textColor);
    }
}
