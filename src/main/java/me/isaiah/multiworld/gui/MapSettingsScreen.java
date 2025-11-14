package me.isaiah.multiworld.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MapSettingsScreen extends Screen {
    protected MapSettingsScreen() {
        super(Component.translatable("multiworld.map.settings.title"));
    }
}
