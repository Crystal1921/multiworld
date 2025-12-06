package me.isaiah.multiworld.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CreateWayPointScreen extends Screen {
    private int posX;
    private int posY;
    private int posZ;
    private final MapScreen parent;
    public CreateWayPointScreen(int posX, int posY, MapScreen parent) {
        super(Component.translatable("multiworld.map.create_waypoint"));
        this.parent = parent;
        this.posX = posX;
        this.posY = posY;
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
