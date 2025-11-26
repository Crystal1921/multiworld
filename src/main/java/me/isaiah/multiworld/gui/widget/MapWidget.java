package me.isaiah.multiworld.gui.widget;

import lombok.Getter;
import lombok.Setter;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.gui.MapRenderer;
import me.isaiah.multiworld.map.MapInstance;
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

import static me.isaiah.multiworld.gui.MapScreen.MAP_PADDING;

public class MapWidget extends AbstractWidget {
    private static double posX = 0;
    private static double posY = 0;
    private static double scale = 2;
    public boolean showPortalList = true;
    @Setter
    @Getter
    private MapInstance.MapConfig mapConfig;
    @Setter
    private List<Vec2> portals;

    public MapWidget(int x, int y, int width, int height, MapInstance.MapConfig mapConfig, List<Vec2> portals) {
        super(x, y, width, height, Component.literal("map_open"));
        this.mapConfig = mapConfig;
        this.portals = portals;
    }

    /**
     * Set the map position and scale to center on a specific world coordinate.
     *
     * @param worldX World X coordinate to center on
     * @param worldZ World Z coordinate to center on
     */
    public void centerOnPosition(double worldX, double worldZ) {
        if (mapConfig == null) {
            return;
        }

        Minecraft instance = Minecraft.getInstance();
        if (instance.player == null) {
            return;
        }

        // Get player position
        double playerX = instance.player.position().x;
        double playerZ = instance.player.position().z;

        // Map world boundaries
        final int minX = mapConfig.minX();
        final int minZ = mapConfig.minZ();
        final int maxX = mapConfig.maxX();
        final int maxZ = mapConfig.maxZ();

        // Protection against division by 0
        final float worldWidth = Math.max(1.0f, (float) (maxX - minX));
        final float worldHeight = Math.max(1.0f, (float) (maxZ - minZ));

        // Map display size
        int mapDisplayWidth = Math.max(1, MapInstance.INSTANCE.mapSize);
        int mapDisplayHeight = Math.max(1, MapInstance.INSTANCE.mapSize);

        // Texture to world units ratio
        final float texPerWorldX = (float) mapDisplayWidth / worldWidth;
        final float texPerWorldY = (float) mapDisplayHeight / worldHeight;

        // Calculate pixel offset between player and target position
        // The map is centered on player, so we need to offset by the difference
        double deltaWorldX = worldX - playerX;
        double deltaWorldZ = worldZ - playerZ;

        // Convert world offset to texture/pixel offset
        double deltaPixelX = deltaWorldX * texPerWorldX;
        double deltaPixelY = deltaWorldZ * texPerWorldY;

        // Apply scale to get screen offset
        // posX and posY offset the map in screen space
        // Positive posX moves the map left, positive posY moves the map up
        posX = -deltaPixelX * scale;
        posY = deltaPixelY * scale;
    }

    /**
     * Set the map scale.
     *
     * @param newScale The new scale value
     */
    public static void setScale(double newScale) {
        scale = newScale;
    }

    /**
     * Get the current map scale.
     *
     * @return The current scale value
     */
    public static double getScale() {
        return scale;
    }

    /**
     * Set the map position offset.
     *
     * @param x X offset
     * @param y Y offset
     */
    public static void setPosition(double x, double y) {
        posX = x;
        posY = y;
    }

    /**
     * Get the current X position offset.
     *
     * @return The current X offset
     */
    public static double getPosX() {
        return posX;
    }

    /**
     * Get the current Y position offset.
     *
     * @return The current Y offset
     */
    public static double getPosY() {
        return posY;
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
                showPortalList,
                null,  // use player position for center
                null   // use player position for center
        );
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

        return true;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        posX -= dragX;
        posY += dragY;
    }

    @Override
    public void playDownSound(@NotNull SoundManager handler) {

    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
