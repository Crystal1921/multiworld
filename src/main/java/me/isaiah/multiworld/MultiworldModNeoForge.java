/**
 * Multiworld Mod
 */
package me.isaiah.multiworld;

import me.isaiah.multiworld.command.MultiworldCommand;
import me.isaiah.multiworld.portal.WandEventHandler;
import me.isaiah.multiworld.registry.BlockRegistry;
import me.isaiah.multiworld.registry.GroupRegistry;
import me.isaiah.multiworld.registry.ItemRegistry;
import me.isaiah.multiworld.registry.ModCommandArgumentRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;


@Mod(MultiworldMod.MOD_ID)
public class MultiworldModNeoForge {

	public MultiworldModNeoForge(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(this::commonSetup);

		NeoForge.EVENT_BUS.register(this);

		new xyz.nucleoid.fantasy.FantasyInitializer(modEventBus);
		NeoForgeWorldCreator.init();
		PermForge.init();
		MultiworldMod.init();

		BlockRegistry.BLOCKS.register(modEventBus);
		ItemRegistry.ITEMS.register(modEventBus);
		GroupRegistry.TABS.register(modEventBus);
		ModCommandArgumentRegistry.REGISTRY.register(modEventBus);
	}

	public void commonSetup(final FMLCommonSetupEvent event) {
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		MultiworldMod.on_server_started(event.getServer());
	}

	@SubscribeEvent
	public void onCommandsRegister(RegisterCommandsEvent event) {
		MultiworldCommand.register_commands(event.getDispatcher());
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
