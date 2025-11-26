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
    @Getter
    private static double posX = 0;
    @Getter
    private static double posY = 0;
    @Getter
    private static double scale = 2;
    // Target center coordinates (in world space), null means use player position
    private static Double targetCenterX = null;
    private static Double targetCenterZ = null;
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
     * This offsets the map so that the target point appears at the center of the screen.
     *
     * @param worldX World X coordinate to center on
     * @param worldZ World Z coordinate to center on
     */
    public void centerOnPosition(double worldX, double worldZ) {
        if (mapConfig == null) {
            return;
        }

        // Store the target center coordinates
        targetCenterX = worldX;
        targetCenterZ = worldZ;

        // Reset position offset since we're now centering on a new target
        // User can still drag/zoom from this position
        posX = 0;
        posY = 0;
    }

    /**
     * Reset to center on player position.
     */
    public static void resetToPlayerCenter() {
        targetCenterX = null;
        targetCenterZ = null;
        posX = 0;
        posY = 0;
    }

    /**
     * Get the current target center X coordinate (world space), or null if centered on player.
     */
    public static Double getTargetCenterX() {
        return targetCenterX;
    }

    /**
     * Get the current target center Z coordinate (world space), or null if centered on player.
     */
    public static Double getTargetCenterZ() {
        return targetCenterZ;
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
                targetCenterX,  // use target position or null for player position
                targetCenterZ   // use target position or null for player position
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
