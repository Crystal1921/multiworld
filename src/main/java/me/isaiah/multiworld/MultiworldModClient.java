package me.isaiah.multiworld;

import me.isaiah.multiworld.gui.screen.MapConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = MultiworldMod.MOD_ID, dist = Dist.CLIENT)
public class MultiworldModClient {
    public MultiworldModClient(IEventBus modEventBus, ModContainer modContainer) {
        this.registerConfigMenu(modContainer);
    }

    private void registerConfigMenu(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) ->
                new ConfigurationScreen(modContainer, parent, MapConfigScreen::create));
    }
}
