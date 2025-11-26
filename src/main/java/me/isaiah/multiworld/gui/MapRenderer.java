package me.isaiah.multiworld.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import me.isaiah.multiworld.map.MapInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Unified map rendering utility class.
 * Abstracts the common drawing logic from MapWidget and MapOverlay.
 */
public class MapRenderer {
    // Style constants
    public static final int MARKER_COLOR = 0xFFFF0000;
    public static final String PORTAL_MARKER = "❌";

    /**
     * Draw the map with all configurable parameters.
     *
     * @param guiGraphics    Graphics context
     * @param player         The player (for centering)
     * @param background     Map texture resource
     * @param mapConfig      Map configuration
     * @param portals        List of portal positions
     * @param font           Font for drawing markers
     * @param mapScreenX     X position of map on screen
     * @param mapScreenY     Y position of map on screen
     * @param mapDisplayWidth  Map display width in pixels
     * @param mapDisplayHeight Map display height in pixels
     * @param mapScale       Scale factor for the map
     * @param scissorMinX    Scissor region min X
     * @param scissorMinY    Scissor region min Y
     * @param scissorMaxX    Scissor region max X
     * @param scissorMaxY    Scissor region max Y
     * @param showPortals    Whether to show portal markers
     * @param centerX        X coordinate to center on (in world space), or null to use player position
     * @param centerZ        Z coordinate to center on (in world space), or null to use player position
     */
    public static void drawMap(@NotNull GuiGraphics guiGraphics,
                               Player player,
                               ResourceLocation background,
                               MapInstance.MapConfig mapConfig,
                               List<Vec2> portals,
                               Font font,
                               int mapScreenX,
                               int mapScreenY,
                               int mapDisplayWidth,
                               int mapDisplayHeight,
                               double mapScale,
                               int scissorMinX,
                               int scissorMinY,
                               int scissorMaxX,
                               int scissorMaxY,
                               boolean showPortals,
                               Double centerX,
                               Double centerZ) {

        if (mapConfig == null || background == null || player == null) {
            return;
        }

        guiGraphics.enableScissor(scissorMinX, scissorMinY, scissorMaxX, scissorMaxY);

        // Map world boundaries
        final int minX = mapConfig.minX();
        final int minZ = mapConfig.minZ();
        final int maxX = mapConfig.maxX();
        final int maxZ = mapConfig.maxZ();

        // Protection against division by 0 or negative values
        final float worldWidth = Math.max(1.0f, (float) (maxX - minX));
        final float worldHeight = Math.max(1.0f, (float) (maxZ - minZ));

        // Texture to world units ratio
        final float texPerWorldX = (float) mapDisplayWidth / worldWidth;
        final float texPerWorldY = (float) mapDisplayHeight / worldHeight;

        // Determine center position (use provided coordinates or player position)
        Vec3 position = player.position();
        double worldCenterX = (centerX != null) ? centerX : position.x;
        double worldCenterZ = (centerZ != null) ? centerZ : position.z;

        // Center position in texture (pixel) space
        final float textureCenterX = (float) (worldCenterX - minX) * texPerWorldX;
        final float textureCenterY = (float) (worldCenterZ - minZ) * texPerWorldY;

        // Offset relative to texture top-left corner (for centering)
        final float centerOffsetX = textureCenterX - mapDisplayWidth / 2.0f;
        final float centerOffsetY = textureCenterY - mapDisplayHeight / 2.0f;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        // Translate to map center (screen coordinates)
        poseStack.translate(
                mapScreenX + mapDisplayWidth / 2.0,
                mapScreenY + mapDisplayHeight / 2.0,
                0
        );

        // Apply scale
        poseStack.scale((float) mapScale, (float) mapScale, 1.0f);

        // Translate to center the target position
        poseStack.translate(-centerOffsetX, -centerOffsetY, 0);

        // Draw the full map texture
        guiGraphics.blit(
                background,
                -mapDisplayWidth / 2,
                -mapDisplayHeight / 2,
                0,
                0,
                mapDisplayWidth,
                mapDisplayHeight,
                mapDisplayWidth,
                mapDisplayHeight
        );

        // Restore to screen coordinates
        poseStack.popPose();

        // Pre-calculate screen center
        final float mapCenterScreenX = mapScreenX + mapDisplayWidth / 2.0f;
        final float mapCenterScreenY = mapScreenY + mapDisplayHeight / 2.0f;
        final float fMapScale = (float) mapScale;

        // Calculate visible radius for culling
        final float visibleRadiusX = (mapDisplayWidth / 2.0f) / fMapScale + 8;
        final float visibleRadiusY = (mapDisplayHeight / 2.0f) / fMapScale + 8;

        // Draw portal markers if enabled
        if (showPortals && portals != null && font != null) {
            final int markerSize = 4;
            final int half = markerSize / 2;

            for (Vec2 worldPoint : portals) {
                float worldPointX = worldPoint.x;
                float worldPointZ = worldPoint.y;

                // World -> texture (pixel) space
                float texturePointX = (worldPointX - minX) * texPerWorldX;
                float texturePointY = (worldPointZ - minZ) * texPerWorldY;

                // Offset relative to center in texture
                float dxTexture = texturePointX - textureCenterX;
                float dyTexture = texturePointY - textureCenterY;

                // Skip if outside visible range
                if (Math.abs(dxTexture) > visibleRadiusX || Math.abs(dyTexture) > visibleRadiusY) {
                    continue;
                }

                // Texture pixel offset -> screen pixel offset (with scale)
                float dxScreen = dxTexture * fMapScale;
                float dyScreen = dyTexture * fMapScale;

                // Final screen coordinates
                int pointScreenX = Math.round(mapCenterScreenX + dxScreen);
                int pointScreenY = Math.round(mapCenterScreenY + dyScreen);

                guiGraphics.drawString(font, PORTAL_MARKER,
                        pointScreenX - half,
                        pointScreenY - half,
                        MARKER_COLOR
                );
            }
        }

        // Draw center marker (red dot at center)
        final int playerMarkerSize = 3;
        final int halfP = playerMarkerSize / 2;
        final int screenCenterX = Math.round(mapCenterScreenX);
        final int screenCenterY = Math.round(mapCenterScreenY);
        guiGraphics.fill(
                screenCenterX - halfP,
                screenCenterY - halfP,
                screenCenterX + halfP,
                screenCenterY + halfP,
                MARKER_COLOR
        );

        guiGraphics.disableScissor();
    }
}
