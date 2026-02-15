package me.isaiah.multiworld.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.command.commands.PortalCommand;
import me.isaiah.multiworld.gui.widget.*;
import me.isaiah.multiworld.map.MapInstance;
import me.isaiah.multiworld.map.waypoint.WayPoint;
import me.isaiah.multiworld.map.waypoint.WayPointManager;
import me.isaiah.multiworld.portal.Portal;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static me.isaiah.multiworld.gui.widget.MapWidget.centerOnPosition;
import static me.isaiah.multiworld.gui.widget.MapWidget.getWorldPosition;
import static me.isaiah.multiworld.gui.widget.WorldList.WorldEntry.setMapData;
import static org.apache.commons.lang3.math.NumberUtils.isParsable;

public class MapScreen extends Screen {
    public static final KeyMapping MAP_OPEN_KEY = new KeyMapping("key.multiworld.map_open.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "key.category.multiworld");
    public final static int BUTTON_PADDING = 30;
    public final static int BUTTON_WIDTH = 100;
    public final static int EDIT_BOX_WIDTH = 30;
    public final static int MAP_PADDING = 80;
    protected static final Button.CreateNarration DEFAULT_NARRATION = Supplier::get;
    private final static int WAYPOINT_BUTTON_WIDTH = 120;
    private final static int WAYPOINT_BUTTON_HEIGHT = 20;
    WorldList worldList;
    PortalList portalList;
    WayPointList wayPointList;
    @Getter
    MapWidget mapWidget;
    private CycleButton<MapMode> listSwitchButton;
    private Button createWaypointButton;
    private Button editWaypointButton;
    private DeleteCycleButton<Boolean> deleteWaypointButton;
    private EditBox xEditBox;
    private EditBox zEditBox;
    private boolean readyToTeleport = false;

    private double mouseClickX;
    private double mouseClickY;

    private WayPoint selectedWayPoint;

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
            MapInstance.INSTANCE.mapConfigs.stream()
                    .filter(Objects::nonNull)
                    .forEach(mapConfig -> {
                        if (mapConfig.worldID().equals(resourceLocation.toString())) {
                            config.set(mapConfig);
                        }
                    });
            WorldList.getPortalList(portals, resourceLocation);
        }

        if (config.get() == null) {
            if (MapInstance.INSTANCE.mapConfigs.isEmpty()) {
                config.set(MapInstance.DEFAULT);
            } else {
                config.set(MapInstance.INSTANCE.mapConfigs.getFirst());
            }
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
                            hideAllWaypointButton();
                        });

        mapWidget = new MapWidget(MAP_PADDING, 0, instance.getWindow().getGuiScaledWidth(), instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING, config.get(), portals, this);
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

        setListsVisibility(false, true, false);

        createWaypointButton = new WayPointButton(0, 0, WAYPOINT_BUTTON_WIDTH, WAYPOINT_BUTTON_HEIGHT, Component.translatable("multiworld.map.create_waypoint"), button -> {
            openWaypointScreen();
            hideAllWaypointButton();
        }, DEFAULT_NARRATION);

        createWaypointButton.visible = false;

        editWaypointButton = new WayPointButton(0, 0, WAYPOINT_BUTTON_WIDTH, WAYPOINT_BUTTON_HEIGHT, Component.translatable("multiworld.map.waypoint.label.edit"), button -> {
            if (selectedWayPoint != null) {
                openEditWaypointScreen(selectedWayPoint);
            }
            hideAllWaypointButton();
        }, DEFAULT_NARRATION);
        editWaypointButton.visible = false;

        deleteWaypointButton = DeleteCycleButton.Builder.booleanBuilder(Component.translatable("multiworld.map.waypoint.label.delete"), Component.translatable("multiworld.map.waypoint.label.confirm_delete"))
                .displayOnlyValue()
                .create(0, 0, WAYPOINT_BUTTON_WIDTH, WAYPOINT_BUTTON_HEIGHT, Component.translatable("multiworld.map.waypoint.label.delete"),
                        (cycleButton, value) -> {
                            if (value) {
                                if (selectedWayPoint != null) {
                                    WayPointManager.INSTANCE.removeWaypoint(selectedWayPoint);
                                    wayPointList.refreshList();
                                }
                                hideAllWaypointButton();
                            }
                        });
        deleteWaypointButton.visible = false;

        CycleButton<Boolean> teleportButton = CycleButton.<Boolean>builder((boolVal) -> Component.translatable(boolVal ? "multiworld.map.waypoint.label.teleport" : "multiworld.map.waypoint.label.teleport_on"))
                .withValues(true, false)
                .displayOnlyValue()
                .withInitialValue(false)
                .create(BUTTON_WIDTH * 2 + 10, instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING + 5, BUTTON_WIDTH / 2, BUTTON_PADDING - 10, Component.empty(),
                        (button, boolVal) -> {
                            if (boolVal) {
                                xEditBox.visible = true;
                                zEditBox.visible = true;
                                readyToTeleport = true;
                                xEditBox.setValue("");
                                zEditBox.setValue("");
                            } else {
                                if (isParsable(xEditBox.getValue()) && isParsable(zEditBox.getValue())) {
                                    int x = Integer.parseInt(xEditBox.getValue());
                                    int z = Integer.parseInt(zEditBox.getValue());
                                    centerOnPosition(mapWidget.getMapConfig(), x, z);
                                }
                                xEditBox.visible = false;
                                zEditBox.visible = false;
                                readyToTeleport = false;
                            }
                        });

        xEditBox = new EditBox(this.font, BUTTON_WIDTH * 2 + 70, instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING + 5, EDIT_BOX_WIDTH, BUTTON_PADDING - 10, Component.translatable("multiworld.map.coordinate.x"));
        xEditBox.visible = false;
        zEditBox = new EditBox(this.font, BUTTON_WIDTH * 2 + 110, instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING + 5, EDIT_BOX_WIDTH, BUTTON_PADDING - 10, Component.translatable("multiworld.map.coordinate.z"));
        zEditBox.visible = false;

        Button followButton = Button.builder(Component.translatable("multiworld.map.waypoint.label.follow"), button -> {
            if (player != null && mapWidget.getMapConfig().worldID().equals(player.level().dimension().location().toString())) {
                centerOnPosition(mapWidget.getMapConfig(), player.getX(), player.getY());
            }
        }).bounds(BUTTON_WIDTH * 2 + 150, instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING + 5, EDIT_BOX_WIDTH, BUTTON_PADDING - 10).build();

        this.addRenderableWidget(mapWidget);
        this.addRenderableWidget(listSwitchButton);
        this.addRenderableWidget(mapSettingsButton);
        this.addRenderableWidget(worldList);
        this.addRenderableWidget(portalList);
        this.addRenderableWidget(wayPointList);
        this.addRenderableWidget(createWaypointButton);
        this.addRenderableWidget(editWaypointButton);
        this.addRenderableWidget(deleteWaypointButton);
        this.addRenderableWidget(teleportButton);
        this.addRenderableWidget(xEditBox);
        this.addRenderableWidget(zEditBox);
        this.addRenderableWidget(followButton);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF000000);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderCoordinate(guiGraphics, mouseX, mouseY);
    }

    private void renderCoordinate(GuiGraphics guiGraphics, int x, int y) {
        Vec2 worldPosition = getWorldPosition(mapWidget.getMapConfig(), x, y);
        Minecraft instance = Minecraft.getInstance();
        if (worldPosition != null && !this.readyToTeleport) {
            guiGraphics.drawString(this.font, Component.literal("X: " + (int) worldPosition.x + "Z: " + (int) worldPosition.y), BUTTON_WIDTH * 2 + 70, instance.getWindow().getGuiScaledHeight() - BUTTON_PADDING + 5, 0xFFFFFF);
        }
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
        hideAllWaypointButton();
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
        WayPointManager.INSTANCE.getWaypoints().forEach(wayPoint -> {
            T entry = newEntry.apply(wayPoint);
            modListViewConsumer.accept(entry);
        });
    }

    private void setListsVisibility(boolean portalVisible, boolean worldVisible, boolean wayPointVisible) {
        portalList.visible = portalVisible;
        worldList.visible = worldVisible;
        wayPointList.visible = wayPointVisible;
    }

    /**
     * Show the waypoint creation button at the specified mouse position
     */
    public void showWaypointButton(double mouseX, double mouseY) {
        this.mouseClickX = mouseX;
        this.mouseClickY = mouseY;

        int buttonX = (int) Math.max(0, Math.min(mouseX, this.width - WAYPOINT_BUTTON_WIDTH));
        int buttonY = (int) Math.max(0, Math.min(mouseY, this.height - WAYPOINT_BUTTON_HEIGHT));

        Map<WayPoint, Vec2> screenWaypointForDimension = WayPointManager.INSTANCE.getScreenWaypointForDimension(mapWidget.getMapConfig(), (float) MapWidget.getScale(), Minecraft.getInstance().player);
        boolean hasWaypoint = screenWaypointForDimension.entrySet().stream().anyMatch(entry -> {
            Vec2 vec2 = entry.getValue();
            double dx = vec2.x - mouseX;
            double dy = vec2.y - mouseY;
            double distanceSquared = dx * dx + dy * dy;

            // 判断是否在点击范围内（半径10像素 -> 距离平方100）
            boolean matched = distanceSquared < 100;

            if (matched) {
                selectedWayPoint = entry.getKey();
            }
            return matched;
        });

        if (hasWaypoint) {
            editWaypointButton.setX(buttonX);
            editWaypointButton.setY(buttonY);
            editWaypointButton.visible = true;

            deleteWaypointButton.setX(buttonX);
            deleteWaypointButton.setY(buttonY + WAYPOINT_BUTTON_HEIGHT);
            deleteWaypointButton.visible = true;
            return;
        }

        createWaypointButton.setX(buttonX);
        createWaypointButton.setY(buttonY);
        createWaypointButton.visible = true;
    }

    /**
     * Hide the waypoint creation button
     */
    public void hideAllWaypointButton() {
        createWaypointButton.visible = false;
        editWaypointButton.visible = false;
        deleteWaypointButton.visible = false;
    }

    /**
     * Add a waypoint at the specified mouse position
     */
    private void openWaypointScreen() {
        Vec2 worldPosition = getWorldPosition(mapWidget.getMapConfig(), mouseClickX, mouseClickY);
        if (this.minecraft != null && worldPosition != null) {
            this.minecraft.setScreen(new CreateWayPointScreen((int) worldPosition.x, (int) worldPosition.y, this.mapWidget.getMapConfig(), this));
        }
    }

    private void openEditWaypointScreen(WayPoint wayPoint) {
        if (this.minecraft != null && wayPoint != null) {
            this.minecraft.setScreen(new EditWayPointScreen(wayPoint, this.mapWidget.getMapConfig(), this));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mapWidget.isMouseOver(mouseX, mouseY)) {
            if (button == 0) { // 左键
                boolean handled = pressButton(mouseX, mouseY);
                if (handled) return true;
                // 若未被 waypoint 按钮处理，继续让父类处理（例如其他控件或默认行为）
                return super.mouseClicked(mouseX, mouseY, button);
            } else if (button == 1) { // 右键
                hideAllWaypointButton();
                showWaypointButton(mouseX, mouseY);
                return true; // 已处理右键用于显示菜单
            }
        } else {
            hideAllWaypointButton();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean pressButton(double mouseX, double mouseY) {
        // 优先检查编辑与删除（当它们可见时），再检查创建
        if (editWaypointButton.isMouseOver(mouseX, mouseY)) {
            editWaypointButton.onPress();
            return true;
        }
        if (deleteWaypointButton.isMouseOver(mouseX, mouseY)) {
            deleteWaypointButton.onPress();
            return true;
        }
        if (createWaypointButton.isMouseOver(mouseX, mouseY)) {
            createWaypointButton.onPress();
            return true;
        }

        // 未点击到任何 waypoint 按钮，隐藏它们
        hideAllWaypointButton();
        return false;
    }

    public void onClose() {
        if (this.mapWidget.getGifPlayer() != null) {
            this.mapWidget.getGifPlayer().close();
        }
        super.onClose();
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
