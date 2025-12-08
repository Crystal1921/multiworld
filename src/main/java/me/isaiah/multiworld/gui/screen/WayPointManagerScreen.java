package me.isaiah.multiworld.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WayPointManagerScreen extends Screen {
    private final MapScreen parent;
    public WayPointManagerScreen(MapScreen parentScreen) {
        super(Component.empty());
        this.parent = parentScreen;
    }
}
