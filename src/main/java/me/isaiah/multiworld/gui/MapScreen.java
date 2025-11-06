package me.isaiah.multiworld.gui;

import com.mojang.blaze3d.platform.InputConstants;
import me.isaiah.multiworld.command.commands.PortalCommand;
import me.isaiah.multiworld.map.MapInstance;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MapScreen extends Screen {
    public static final KeyMapping MAP_OPEN_KEY = new KeyMapping("key.multiworld.map_open.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "key.category.multiworld");
    public static int PADDING = 30;
    MapWidget mapWidget;

    public MapScreen() {
        super(Component.literal("Map"));
    }

    @Override
    protected void init() {
        Minecraft instance = Minecraft.getInstance();
        LocalPlayer player = instance.player;
        AtomicReference<MapInstance.MapConfig> config = new AtomicReference<>();
        List<Vec2> portals = new ArrayList<>();
        if (player != null) {
            ResourceLocation resourceLocation = player.level().dimension().location();
            MapInstance.INSTANCE.mapConfigs.forEach(mapConfig -> {
                if (mapConfig.worldID().equals(resourceLocation.toString())) {
                    config.set(mapConfig);
                }
            });
            PortalCommand.KNOWN_PORTALS.forEach((s, portal) -> {
                if (portal.getOriginWorld().dimension().location().equals(resourceLocation)) {
                    BlockPos minPos = portal.getMinPos();
                    BlockPos maxPos = portal.getMaxPos();
                    portals.add(new Vec2((minPos.getX() + maxPos.getX()) / 2f, (minPos.getZ() + maxPos.getZ()) / 2f));
                }
            });
        }
        mapWidget = new MapWidget(0, 0, instance.getWindow().getGuiScaledWidth(), instance.getWindow().getGuiScaledHeight() - PADDING, config.get(), portals);

        this.addRenderableWidget(mapWidget);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF000000);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
