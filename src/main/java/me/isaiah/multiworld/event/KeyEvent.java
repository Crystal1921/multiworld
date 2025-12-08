package me.isaiah.multiworld.event;

import me.isaiah.multiworld.gui.screen.MapScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber
public class KeyEvent {
    @SubscribeEvent
    public static void onPressKey(InputEvent.Key event) {
        int key = event.getKey();
        int scanCode = event.getScanCode();
        if (MapScreen.MAP_OPEN_KEY.matches(key, scanCode)) {
            Minecraft instance = Minecraft.getInstance();
            if (instance.screen == null) {
                instance.setScreen(new MapScreen());
            }
        }
    }
}
