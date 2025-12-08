/**
 * Multiworld Mod
 */
package me.isaiah.multiworld;

import me.isaiah.multiworld.command.HomeCommand;
import me.isaiah.multiworld.command.MultiworldCommand;
import me.isaiah.multiworld.command.SpawnCommand;
import me.isaiah.multiworld.command.WarpCommand;
import me.isaiah.multiworld.config.ClientConfig;
import me.isaiah.multiworld.gui.screen.MapConfigScreen;
import me.isaiah.multiworld.portal.WandEventHandler;
import me.isaiah.multiworld.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;


@Mod(MultiworldMod.MOD_ID)
public class MultiworldModNeoForge {

    public MultiworldModNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.init());
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) ->
                new ConfigurationScreen(modContainer, parent, MapConfigScreen::create));

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        new xyz.nucleoid.fantasy.FantasyInitializer(modEventBus);
        NeoForgeWorldCreator.init();
        PermForge.init();
        MultiworldMod.init();

        BlockRegistry.BLOCKS.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        GroupRegistry.TABS.register(modEventBus);
        ModCommandArgumentRegistry.COMMAND_ARG.register(modEventBus);
        DataAttachmentsRegistry.ATTACHMENT_TYPES.register(modEventBus);
    }

    public void commonSetup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        MultiworldMod.onServerStarted(event.getServer());
    }

    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) {
        MultiworldCommand.register_commands(event.getDispatcher());
        WarpCommand.register_commands(event.getDispatcher());
        SpawnCommand.register_commands(event.getDispatcher());
        HomeCommand.register_commands(event.getDispatcher());
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        WandEventHandler.leftClickBlock(event.getEntity(), event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        WandEventHandler.rightClickBlock(event.getEntity(), event.getLevel(), event.getHitVec());
    }

}
