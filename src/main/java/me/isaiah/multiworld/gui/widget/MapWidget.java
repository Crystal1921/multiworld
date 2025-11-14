package me.isaiah.multiworld.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Setter;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.map.MapInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.isaiah.multiworld.gui.MapScreen.*;

public class MapWidget extends AbstractWidget {
    // 常量/风格设置
    static final int MARKER_COLOR = 0xFFFF0000;
    static final String PORTAL_MARKER = "❌";
    private static double posX = 0;
    private static double posY = 0;
    private static double scale = 2;
    public boolean showPortalList = true;
    @Setter
    private MapInstance.MapConfig mapConfig;
    @Setter
    private List<Vec2> portals;

    public MapWidget(int x, int y, int width, int height, MapInstance.MapConfig mapConfig, List<Vec2> portals) {
        super(x, y, width, height, Component.literal("map_open"));
        this.mapConfig = mapConfig;
        this.portals = portals;
    }

    private static void drawMap(@NotNull GuiGraphics guiGraphics,
                                Player player,
                                ResourceLocation BACKGROUND,
                                MapInstance.MapConfig mapConfig,
                                List<Vec2> portals, MapWidget mapWidget, Font font) {
        if (mapConfig == null || mapWidget == null || BACKGROUND == null || player == null) {
            return;
        }

        guiGraphics.enableScissor(MAP_PADDING, 0, mapWidget.getWidth(), mapWidget.getHeight());

        // 玩家世界坐标
        Vec3 position = player.position();

        // 地图与世界边界
        int guiWidth = guiGraphics.guiWidth();
        final int minX = mapConfig.minX();
        final int minZ = mapConfig.minZ();
        final int maxX = mapConfig.maxX();
        final int maxZ = mapConfig.maxZ();

        // 保护性处理，避免除以 0 或负值
        final float worldWidth = Math.max(1.0f, (float) (maxX - minX));
        final float worldHeight = Math.max(1.0f, (float) (maxZ - minZ));

        // 地图在屏幕上的显示尺寸（像素）
        final int mapDisplayWidth = Math.max(1, MapInstance.INSTANCE.mapSize);
        final int mapDisplayHeight = Math.max(1, MapInstance.INSTANCE.mapSize);

        // 地图在屏幕上的左上角位置（使用外部 posX/posY/scale 变量）
        final int mapScreenX = (int) (guiWidth - mapDisplayWidth - posX);
        final int mapScreenY = (int) posY;
        final double mapScale = scale;

        // 纹理每个世界单位对应的像素（纹理/像素空间）
        final float texPerWorldX = (float) mapDisplayWidth / worldWidth;
        final float texPerWorldY = (float) mapDisplayHeight / worldHeight;

        // 玩家在纹理（像素）上的位置
        final float texturePlayerX = (float) (position.x - minX) * texPerWorldX;
        final float texturePlayerY = (float) (position.z - minZ) * texPerWorldY;

        // 玩家相对纹理左上角的偏移（用于把玩家放到中心）
        final float playerOffsetX = texturePlayerX - mapDisplayWidth / 2.0f;
        final float playerOffsetY = texturePlayerY - mapDisplayHeight / 2.0f;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        // 平移到小地图中心（屏幕坐标）
        poseStack.translate(
                mapScreenX + mapDisplayWidth / 2.0,
                mapScreenY + mapDisplayHeight / 2.0,
                0
        );

        // 应用缩放（放大或缩小地图）
        poseStack.scale((float) mapScale, (float) mapScale, 1.0f);

        // 平移使玩家位置居中（把纹理上的玩家像素坐标移动到中心）
        poseStack.translate(-playerOffsetX, -playerOffsetY, 0);

        // 绘制整张地图纹理（纹理左上角在 -mapDisplayWidth/2, -mapDisplayHeight/2）
        // 保证整型坐标，避免精度误差
        guiGraphics.blit(
                BACKGROUND,
                -mapDisplayWidth / 2,
                -mapDisplayHeight / 2,
                0,
                0,
                mapDisplayWidth,
                mapDisplayHeight,
                mapDisplayWidth,
                mapDisplayHeight
        );

        // 恢复到屏幕坐标（后面我们用屏幕坐标绘制标记）
        poseStack.popPose();

        // 预计算常用的屏幕中心（减少循环内计算）
        final float mapCenterScreenX = mapScreenX + mapDisplayWidth / 2.0f;
        final float mapCenterScreenY = mapScreenY + mapDisplayHeight / 2.0f;
        // 已是每世界->像素比例
        final float fMapScale = (float) mapScale;

        // 只绘制可见范围内的标记（节省性能）
        final float visibleRadiusX = (mapDisplayWidth / 2.0f) / fMapScale + 8; // 8px margin
        final float visibleRadiusY = (mapDisplayHeight / 2.0f) / fMapScale + 8;

        // 缓存 marker 大小
        final int markerSize = 4;
        final int half = markerSize / 2;

        if (mapWidget.showPortalList) {
            // 绘制所有传送点（将世界坐标 -> 纹理像素 -> 屏幕像素）
            for (Vec2 worldPoint : portals) {
                float worldPointX = worldPoint.x;
                float worldPointZ = worldPoint.y;

                // 世界 -> 纹理（像素）空间
                float texturePointX = (worldPointX - minX) * texPerWorldX;
                float texturePointY = (worldPointZ - minZ) * texPerWorldY;

                // 相对于玩家在纹理上的偏移（像素）
                float dxTexture = texturePointX - texturePlayerX;
                float dyTexture = texturePointY - texturePlayerY;

                // 如果该点在地图可视范围之外就跳过（快速剔除）
                if (Math.abs(dxTexture) > visibleRadiusX || Math.abs(dyTexture) > visibleRadiusY) {
                    continue;
                }

                // 纹理像素偏移 -> 屏幕像素偏移（考虑缩放）
                float dxScreen = dxTexture * fMapScale;
                float dyScreen = dyTexture * fMapScale;

                // 最终屏幕坐标
                int pointScreenX = Math.round(mapCenterScreenX + dxScreen);
                int pointScreenY = Math.round(mapCenterScreenY + dyScreen);

                // 以中心对齐绘制标志（避免频繁 new 对象）
                guiGraphics.drawString(font, PORTAL_MARKER,
                        pointScreenX - half,
                        pointScreenY - half,
                        MARKER_COLOR
                );
            }
        }

        // 绘制玩家中心点（保留原来的红点）
        final int playerMarkerSize = 3;
        final int halfP = playerMarkerSize / 2;
        // 把中心点四舍五入为屏幕像素
        final int centerX = Math.round(mapCenterScreenX);
        final int centerY = Math.round(mapCenterScreenY);
        guiGraphics.fill(
                centerX - halfP,
                centerY - halfP,
                centerX + halfP,
                centerY + halfP,
                MARKER_COLOR
        );

        // 关闭剪裁
        guiGraphics.disableScissor();
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

        ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MultiworldMod.MOD_ID, "textures/map/" + mapConfig.mapName() + ".png");

        drawMap(guiGraphics, player, BACKGROUND, mapConfig, portals, this, instance.font);
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
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
