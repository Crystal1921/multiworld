package me.isaiah.multiworld.gui;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.command.commands.PortalCommand;
import me.isaiah.multiworld.gui.widget.PortalList;
import me.isaiah.multiworld.gui.widget.WorldList;
import me.isaiah.multiworld.gui.widget.WayPointList;
import me.isaiah.multiworld.gui.widget.MapWidget;
import me.isaiah.multiworld.map.MapInstance;
import me.isaiah.multiworld.portal.Portal;
import me.isaiah.multiworld.waypoint.WayPoint;
import me.isaiah.multiworld.waypoint.WayPointManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec2;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static me.isaiah.multiworld.gui.widget.WorldList.WorldEntry.setMapData;

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
    WayPointList wayPointList;
    @Getter
    MapWidget mapWidget;
    private CycleButton<MapMode> listSwitchButton;

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

        listSwitchButton = CycleButton.<MapMode>builder((mapMode) -> Component.translatable(mapMode.getSerializedName()))
                .withValues(MapMode.values())
                .displayOnlyValue()
                .withInitialValue(MapMode.WORLD_LIST)
                .create(0, instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING + 5, BUTTON_WIDTH, BUTTON_PADDING - 10, Component.empty(),
                (button, mapMode) -> {
                    switch (mapMode) {
                        case PORTAL_LIST -> setListsVisibility(true, false, false);
                        case WORLD_LIST -> setListsVisibility(false, true, false);
                        case WAYPOINT_LIST -> setListsVisibility(false, false, true);
                    }
                });

        mapWidget = new MapWidget(MAP_PADDING, 0, instance.getWindow().getGuiScaledWidth(), instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING, config.get(), portals);
        worldList = new WorldList(this, MAP_PADDING, 0, instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING);
        portalList = new PortalList(this, MAP_PADDING, 0, instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING);
        wayPointList = new WayPointList(this, MAP_PADDING, 0, instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING);

        Button mapSettingsButton = Button
                .builder(Component.translatable("multiworld.map.settings.title"), button -> {
                    if (this.minecraft == null) {
                        return;
                    }
                    ModList.get().getModContainerById(MultiworldMod.MOD_ID)
                            .flatMap(modContainer -> modContainer.getCustomExtension(IConfigScreenFactory.class))
                            .map(factory -> factory.createScreen(
                                    ModList.get().getModContainerById(MultiworldMod.MOD_ID).get(),
                                    this
                            ))
                            .ifPresent(newScreen -> this.minecraft.setScreen(newScreen));
                })
                .bounds(BUTTON_WIDTH, instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING + 5, BUTTON_WIDTH, BUTTON_PADDING - 10).build();

        Button wayPointButton = Button
                .builder(Component.translatable("multiworld.map.waypoint"), button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new WayPointManagerScreen(this));
                    }
                })
                .bounds(2 * BUTTON_WIDTH + 10, instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING + 5, BUTTON_WIDTH, BUTTON_PADDING - 10).build();

        setListsVisibility(false, true, false);
        
        this.addRenderableWidget(mapWidget);
        this.addRenderableWidget(listSwitchButton);
        this.addRenderableWidget(mapSettingsButton);
        this.addRenderableWidget(wayPointButton);
        this.addRenderableWidget(worldList);
        this.addRenderableWidget(portalList);
        this.addRenderableWidget(wayPointList);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF000000);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void resize(@NotNull Minecraft minecraft, int width, int height) {
        boolean portalVisible = portalList.visible;
        boolean worldVisible = worldList.visible;
        boolean wayPointVisible = wayPointList.visible;
        MapMode mapMode = listSwitchButton.getValue();
        MapInstance.MapConfig mapConfig = mapWidget.getMapConfig();
        super.resize(minecraft, width, height);
        setListsVisibility(portalVisible, worldVisible, wayPointVisible);
        listSwitchButton.setValue(mapMode);
        setMapData(ResourceLocation.parse(mapConfig.worldID()), mapConfig, mapWidget);
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

    public <T extends ObjectSelectionList.Entry<T>> void buildWayPointList(Consumer<T> modListViewConsumer, Function<WayPoint, T> newEntry) {
        WayPointManager.INSTANCE.getWaypoints().forEach(waypoint -> {
            T entry = newEntry.apply(waypoint);
            modListViewConsumer.accept(entry);
        });
    }

    private void setListsVisibility(boolean portalVisible, boolean worldVisible, boolean wayPointVisible) {
        portalList.visible = portalVisible;
        worldList.visible = worldVisible;
        wayPointList.visible = wayPointVisible;
    }

    public enum MapMode implements StringRepresentable {
        WORLD_LIST,
        PORTAL_LIST,
        WAYPOINT_LIST;

        @Override
        public @NotNull String getSerializedName() {
            return "gui.multiworld.map." + this.name().toLowerCase();
        }
    }
}
