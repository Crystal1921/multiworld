package me.isaiah.multiworld.gui;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import me.isaiah.multiworld.command.commands.PortalCommand;
import me.isaiah.multiworld.gui.widget.PortalList;
import me.isaiah.multiworld.gui.widget.WorldList;
import me.isaiah.multiworld.gui.widget.MapWidget;
import me.isaiah.multiworld.map.MapInstance;
import me.isaiah.multiworld.portal.Portal;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class MapScreen extends Screen {
    public static final KeyMapping MAP_OPEN_KEY = new KeyMapping("key.multiworld.map_open.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "key.category.multiworld");
    public final static int BUTTON_PADDING = 30;
    public final static int BUTTON_WIDTH = 100;
    public final static int MAP_PADDING = 80;
    WorldList worldList;
    PortalList portalList;
    @Getter
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
            WorldList.getPortalList(portals, resourceLocation);
        }

        CycleButton<MapMode> listSwitchButton = CycleButton.<MapMode>builder((mapMode) -> Component.translatable(mapMode.getSerializedName()))
                .withValues(MapMode.values())
                .displayOnlyValue()
                .withInitialValue(MapMode.WORLD_LIST)
                .create(0, instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING + 5, BUTTON_WIDTH, BUTTON_PADDING - 10, Component.empty(),
                (button, mapMode) -> {
                    switch (mapMode) {
                        case PORTAL_LIST -> {
                            setListsVisibility(true,false);
                        }
                        case WORLD_LIST -> {
                            setListsVisibility(false,true);
                        }
                    }
                });

        mapWidget = new MapWidget(MAP_PADDING, 0, instance.getWindow().getGuiScaledWidth(), instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING, config.get(), portals);
        worldList = new WorldList(this, MAP_PADDING, 0, instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING);
        portalList = new PortalList(this, MAP_PADDING, 0, instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING);

        this.addRenderableWidget(mapWidget);
        this.addRenderableWidget(listSwitchButton);
        this.addRenderableWidget(worldList);
        this.addRenderableWidget(portalList);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF000000);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public Font getFontRenderer() {
        return this.font;
    }

    public Minecraft getMinecraftInstance() {
        return Minecraft.getInstance();
    }

    public <T extends ObjectSelectionList.Entry<T>> void buildWorldList(Consumer<T> modListViewConsumer, Function<MapInstance.MapConfig, T> newEntry) {
        MapInstance.INSTANCE.mapConfigs.forEach(mapConfig -> {
            T entry = newEntry.apply(mapConfig);
            modListViewConsumer.accept(entry);
        });
    }

    public <T extends ObjectSelectionList.Entry<T>> void buildPortalList(Consumer<T> modListViewConsumer, Function<Portal, T> newEntry) {
        PortalCommand.KNOWN_PORTALS.forEach((s, portal) -> {
            T entry = newEntry.apply(portal);
            modListViewConsumer.accept(entry);
        });
    }

    private void setListsVisibility(boolean portalVisible, boolean worldVisible) {
        portalList.visible = portalVisible;
        worldList.visible = worldVisible;
    }

    public enum MapMode implements StringRepresentable {
        WORLD_LIST,
        PORTAL_LIST;

        @Override
        public @NotNull String getSerializedName() {
            return "gui.multiworld.map." + this.name().toLowerCase();
        }
    }
}
