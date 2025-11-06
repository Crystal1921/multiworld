package me.isaiah.multiworld.event;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.gui.MapOverlay;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import static net.neoforged.neoforge.client.gui.VanillaGuiLayers.CROSSHAIR;

@EventBusSubscriber(value = Dist.CLIENT, modid = MultiworldMod.MOD_ID)
public class GuiOverlayEvent {
    @SubscribeEvent
    public static void RegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(CROSSHAIR, ResourceLocation.fromNamespaceAndPath(MultiworldMod.MOD_ID, "map_overlay"), new MapOverlay());
    }
}
