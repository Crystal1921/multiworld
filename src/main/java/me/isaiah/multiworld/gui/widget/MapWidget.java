package me.isaiah.multiworld.gui.widget;

import lombok.Getter;
import lombok.Setter;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.gui.MapRenderer;
import me.isaiah.multiworld.gui.MapScreen;
import me.isaiah.multiworld.map.MapInstance;
import me.isaiah.multiworld.map.waypoint.WayPointManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.isaiah.multiworld.config.ClientConfig.ENABLE_PORTALS;
import static me.isaiah.multiworld.config.ClientConfig.ENABLE_WAYPOINTS;
import static me.isaiah.multiworld.gui.MapScreen.MAP_PADDING;

public class MapWidget extends AbstractWidget {
    @Getter
    private static double posX = 0;
    @Getter
    private static double posY = 0;
    @Getter
    private static double scale = 3;
    private final MapScreen mapScreen;
    @Setter
    @Getter
    private MapInstance.MapConfig mapConfig;
    @Setter
    private List<Vec2> portals;

    public MapWidget(int x, int y, int width, int height, MapInstance.MapConfig mapConfig, List<Vec2> portals, MapScreen mapScreen) {
        super(x, y, width, height, Component.literal("map_open"));
        this.mapConfig = mapConfig;
        this.portals = portals;
        this.mapScreen = mapScreen;

        int posX = (mapConfig.maxX() + mapConfig.minX()) / 2;
        int posZ = (mapConfig.maxZ() + mapConfig.minZ()) / 2;
        centerOnPosition(this.mapConfig,posX, posZ);
    }

    /**
     * Set the map position and scale to center on a specific world coordinate.
     *
     * @param worldX World X coordinate to center on
     * @param worldZ World Z coordinate to center on
     */
    public static void centerOnPosition(MapInstance.MapConfig mapConfig, double worldX, double worldZ) {
        if (mapConfig == null) {
            return;
        }

        Minecraft instance = Minecraft.getInstance();
        if (instance.player == null) {
            return;
        }
        var player = instance.player;

        // --- 1. 获取屏幕和地图尺寸 ---
        final int guiWidth = instance.getWindow().getGuiScaledWidth();
        final int guiHeight = instance.getWindow().getGuiScaledHeight();
        final int mapDisplayWidth = Math.max(1, MapInstance.INSTANCE.mapSize);
        final int mapDisplayHeight = Math.max(1, MapInstance.INSTANCE.mapSize);

        // --- 2. 计算世界坐标到屏幕像素的转换 ---
        // 世界坐标差值 (目标点 - 玩家位置)
        final double worldDeltaX = worldX - player.position().x;
        final double worldDeltaZ = worldZ - player.position().z;

        // 世界尺寸
        final float worldWidth = Math.max(1.0f, (float) (mapConfig.maxX() - mapConfig.minX()));
        final float worldHeight = Math.max(1.0f, (float) (mapConfig.maxZ() - mapConfig.minZ()));

        // 世界单位 -> 纹理像素单位 的比率
        final float texPerWorldX = (float) mapDisplayWidth / worldWidth;
        final float texPerWorldY = (float) mapDisplayHeight / worldHeight;

        // 将世界坐标差值转换为应用了缩放的屏幕像素差值
        final double screenDeltaX = worldDeltaX * texPerWorldX * scale;
        final double screenDeltaY = worldDeltaZ * texPerWorldY * scale;

        // --- 3. 计算 posX 和 posY ---
        // 目标：将目标点移动到屏幕中心 (guiWidth / 2, guiHeight / 2)
        posX = (guiWidth / 2.0) - (mapDisplayWidth / 2.0) - screenDeltaX;
        posY = (guiHeight / 2.0) - (mapDisplayHeight / 2.0) - screenDeltaY;
    }

    /**
     * Get world coordinates from screen coordinates.
     * <br>
     * Inverse of logic used in renderWidget.
     *
     * @param screenX Screen X coordinate (e.g. mouse X)
     * @param screenY Screen Y coordinate (e.g. mouse Y)
     * @return World position as Vec2, or null if map is not valid
     */
    public static Vec2 getWorldPosition(MapInstance.MapConfig mapConfig, double screenX, double screenY) {
        if (mapConfig == null) {
            return null;
        }

        Minecraft instance = Minecraft.getInstance();
        if (instance.player == null) {
            return null;
        }
        var player = instance.player;

        // 1. 获取屏幕和地图尺寸 (与 renderWidget 中一致)
        final int guiWidth = instance.getWindow().getGuiScaledWidth();
        final int mapDisplayWidth = Math.max(1, MapInstance.INSTANCE.mapSize);
        final int mapDisplayHeight = Math.max(1, MapInstance.INSTANCE.mapSize);

        // 2. 计算地图在屏幕上的位置 (参考 renderWidget 中的 mapScreenX/Y 计算)
        // 注意：这里使用 double 以保持精度
        double mapScreenX = (guiWidth - mapDisplayWidth - posX);
        double mapScreenY = posY;

        // 3. 计算地图在屏幕上的中心坐标
        double mapCenterX = mapScreenX + mapDisplayWidth / 2.0;
        double mapCenterY = mapScreenY + mapDisplayHeight / 2.0;

        // 4. 计算鼠标/屏幕点距离地图中心的像素偏移
        double dx = screenX - mapCenterX;
        double dy = screenY - mapCenterY;

        // 5. 计算世界坐标到像素的转换比率 (与 centerOnPosition 中一致)
        final float worldWidth = Math.max(1.0f, (float) (mapConfig.maxX() - mapConfig.minX()));
        final float worldHeight = Math.max(1.0f, (float) (mapConfig.maxZ() - mapConfig.minZ()));

        final float texPerWorldX = (float) mapDisplayWidth / worldWidth;
        final float texPerWorldY = (float) mapDisplayHeight / worldHeight;

        // 6. 反向计算世界坐标偏移
        // 正向公式: screenDelta = worldDelta * texPerWorld * scale
        // 逆向公式: worldDelta = screenDelta / (texPerWorld * scale)
        double worldDeltaX = dx / (texPerWorldX * scale);
        double worldDeltaZ = dy / (texPerWorldY * scale);

        // 7. 加上参考点坐标 (地图渲染中心默认是玩家位置)
        return new Vec2(
                (float) (player.position().x + worldDeltaX),
                (float) (player.position().z + worldDeltaZ)
        );
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v) {
        Minecraft instance = Minecraft.getInstance();
        if (instance.player == null) {
            return;
        }
        var player = instance.player;

        if (mapConfig == null) {
            return;
        }

        ResourceLocation background = ResourceLocation.fromNamespaceAndPath(MultiworldMod.MOD_ID, "textures/map/" + mapConfig.mapName() + ".png");

        int guiWidth = guiGraphics.guiWidth();
        int mapDisplayWidth = Math.max(1, MapInstance.INSTANCE.mapSize);
        int mapDisplayHeight = Math.max(1, MapInstance.INSTANCE.mapSize);
        int mapScreenX = (int) (guiWidth - mapDisplayWidth - posX);
        int mapScreenY = (int) posY;

        MapRenderer.drawMap(
                guiGraphics,
                player,
                background,
                mapConfig,
                portals,
                WayPointManager.INSTANCE.getWaypoints(),
                instance.font,
                mapScreenX,
                mapScreenY,
                mapDisplayWidth,
                mapDisplayHeight,
                scale,
                MAP_PADDING,
                0,
                getWidth(),
                getHeight(),
                ENABLE_PORTALS.get(),
                ENABLE_WAYPOINTS.get(),
                null,  // use player position for center
                null   // use player position for center
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (button == 1) {
            // Right-click: notify parent MapScreen to show waypoint creation button
            mapScreen.showWaypointButton(mouseX, mouseY);
            return true;
        }
        if (button == 0) {
            // Left-click: notify parent MapScreen to handle button press
            mapScreen.pressButton(mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * 处理鼠标滚轮：以鼠标为中心缩放地图。
     * <br>
     * 注意：
     * - mouseX, mouseY 是鼠标在屏幕/GUI 坐标系的坐标（通常由事件提供）。
     * - guiWidth 获取方式需要根据你所在的 GUI/Screen 环境调整，这里假设传入当前 GUI 宽度（或通过 Minecraft 获取）。
     * <br>
     * 调用后会更新 this.scale, this.posX, this.posY。
     *
     * @param mouseX 鼠标 X（屏幕坐标）
     * @param mouseY 鼠标 Y（屏幕坐标）
     * @return 如果处理了事件返回 true
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0) return false;

        double deltaScale = scrollY > 0 ? 1.1 : 0.9;

        double oldScale = scale;
        double newScale = oldScale * deltaScale;

        // 地图在屏幕上的左上角（与 drawMap 中一致的计算）
        int mapDisplayWidth = MapInstance.INSTANCE.mapSize;
        int mapDisplayHeight = MapInstance.INSTANCE.mapSize;
        double mapScreenX = (double) (getWidth() - mapDisplayWidth) - posX;
        double mapScreenY = posY;

        // 地图中心屏幕坐标
        double centerX = mapScreenX + mapDisplayWidth / 2.0;
        double centerY = mapScreenY + mapDisplayHeight / 2.0;

        // 鼠标相对于地图中心的偏移（在屏幕坐标系）
        double dx = mouseX - centerX;
        double dy = centerY - mouseY;

        if (Math.abs(oldScale) < 1e-9) {
            // 防止除零：如果 oldScale 极小，直接更新 scale（不会平滑地保持中心）
            scale = newScale;
        } else {
            double ratio = (newScale - oldScale) / oldScale;
            posX += ratio * dx;
            posY += ratio * dy;
            scale = newScale;
        }

        // Hide waypoint button when scrolling
        mapScreen.hideWaypointButton();

        return true;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        posX -= dragX;
        posY += dragY;
        // Hide waypoint button when dragging
        mapScreen.hideWaypointButton();
    }

    @Override
    public void playDownSound(@NotNull SoundManager handler) {

    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
