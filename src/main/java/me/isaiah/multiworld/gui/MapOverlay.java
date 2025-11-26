package me.isaiah.multiworld.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.config.ClientConfig;
import me.isaiah.multiworld.map.MapInstance;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class MapOverlay implements LayeredDraw.Layer {
    public static MapInstance.MapConfig mapConfig = null;

    private static void drawMap(@NotNull GuiGraphics guiGraphics, Player player, ResourceLocation BACKGROUND, MapInstance.MapConfig mapConfig) {
        Vec3 position = player.position();

        int guiWidth = guiGraphics.guiWidth();
        int minX = mapConfig.minX();
        int minZ = mapConfig.minZ();
        int maxX = mapConfig.maxX();
        int maxZ = mapConfig.maxZ();

        // 保护性处理，避免除以 0
        float worldWidth = Math.max(1, (float) (maxX - minX));
        float worldHeight = Math.max(1, (float) (maxZ - minZ));

        int mapDisplayWidth = MapInstance.INSTANCE.mapSize;
        int mapDisplayHeight = MapInstance.INSTANCE.mapSize;
        int mapScreenX = guiWidth - mapDisplayWidth - MapInstance.INSTANCE.mapPosX;
        int mapScreenY = MapInstance.INSTANCE.mapPosY;
        double mapScale = (double) Math.max(worldWidth, worldHeight) / 32;

        // 世界坐标 -> 纹理像素的比例（每个世界单位对应多少纹理像素）
        float texPerWorldX = (float) mapDisplayWidth / worldWidth;
        float texPerWorldY = (float) mapDisplayHeight / worldHeight;

        // 玩家在纹理像素空间中的位置（以纹理左上角为原点）
        float texturePlayerX = (float) (position.x - minX) * texPerWorldX;
        float texturePlayerY = (float) (position.z - minZ) * texPerWorldY;

        // 如果你的纹理绘制是以纹理中心为原点（如下面的做法），需要计算玩家相对纹理中心的偏移
        float playerOffsetX = texturePlayerX - mapDisplayWidth / 2.0f;
        float playerOffsetY = texturePlayerY - mapDisplayHeight / 2.0f;

        // 启用裁剪测试（在屏幕上只显示小地图区域）
        guiGraphics.enableScissor(mapScreenX, mapScreenY, mapScreenX + mapDisplayWidth, mapScreenY + mapDisplayHeight);

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

        // 绘制完整地图纹理（纹理的左上角在 (-mapDisplayWidth/2, -mapDisplayHeight/2)）
        // 注意：最后两个参数是纹理的实际尺寸（用于正确映射纹理坐标）
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

        poseStack.popPose();

        // 在屏幕中心绘制玩家标记（玩家被放在小地图中心）
        int playerMarkerSize = 3;
        guiGraphics.fill(
                mapScreenX + mapDisplayWidth / 2 - playerMarkerSize / 2,
                mapScreenY + mapDisplayHeight / 2 - playerMarkerSize / 2,
                mapScreenX + mapDisplayWidth / 2 + playerMarkerSize / 2,
                mapScreenY + mapDisplayHeight / 2 + playerMarkerSize / 2,
                0xFFFF0000  // 红色标记
        );

        guiGraphics.disableScissor();

        // 显示玩家坐标文本
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                "X: " + (int) position.x + " Y: " + (int) position.y + " Z: " + (int) position.z,
                mapScreenX,
                mapScreenY + mapDisplayHeight + 5,
                0xFFFFFF,
                false
        );
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel world = minecraft.level;
        Options options = minecraft.options;
        Player player = minecraft.player;
        if (player == null || world == null || options.hideGui) {
            return;
        }
        if (player.isSpectator()) {
            return;
        }

        if (mapConfig == null) {
            return;
        }

        if (!ClientConfig.ENABLE_LITTLE_MAP.get()) {
            return;
        }

        ResourceLocation map = ResourceLocation.fromNamespaceAndPath(MultiworldMod.MOD_ID, "textures/map/" + mapConfig.mapName() + ".png");
        drawMap(guiGraphics, player, map, mapConfig);
    }
}
