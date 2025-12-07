package me.isaiah.multiworld.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import me.isaiah.multiworld.map.MapInstance;
import me.isaiah.multiworld.map.waypoint.WayPoint;
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
     * @param waypoints      List of waypoints to draw
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
     * @param showWaypoints  Whether to show waypoint markers
     * @param centerX        X coordinate to center on (in world space), or null to use player position
     * @param centerZ        Z coordinate to center on (in world space), or null to use player position
     */
    public static void drawMap(@NotNull GuiGraphics guiGraphics,
                               Player player,
                               ResourceLocation background,
                               MapInstance.MapConfig mapConfig,
                               List<Vec2> portals,
                               List<WayPoint> waypoints,
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
                               boolean showWaypoints,
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

        // Draw waypoint markers if enabled
        if (showWaypoints && waypoints != null) {
            final int waypointSize = 6;
            
            for (WayPoint waypoint : waypoints) {
                // Only draw waypoints for current dimension
                if (!waypoint.dimensionId().equals(mapConfig.worldID())) {
                    continue;
                }
                
                float worldPointX = (float) waypoint.x();
                float worldPointZ = (float) waypoint.z();

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

                // Draw a 45° rotated square (diamond shape)
                drawRotatedSquare(guiGraphics, pointScreenX, pointScreenY, waypointSize, waypoint.color());
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

    /**
     * Draw a 45° rotated square (diamond shape) at the specified position
     *
     * @param guiGraphics Graphics context
     * @param centerX     Center X coordinate
     * @param centerY     Center Y coordinate
     * @param size        Size of the square (distance from center to vertex)
     * @param color       Color in ARGB format
     */
    private static void drawRotatedSquare(GuiGraphics guiGraphics, int centerX, int centerY, int size, int color) {
        // Ensure alpha channel is set (if not provided, default to fully opaque)
        int colorWithAlpha = (color & 0xFF000000) != 0 ? color : (0xFF000000 | color);
        
        // Calculate the four vertices of a diamond (square rotated 45°)
        int halfSize = size / 2;
        
        // Top vertex
        int topX = centerX;
        int topY = centerY - halfSize;
        
        // Right vertex
        int rightX = centerX + halfSize;
        int rightY = centerY;
        
        // Bottom vertex
        int bottomX = centerX;
        int bottomY = centerY + halfSize;
        
        // Left vertex
        int leftX = centerX - halfSize;
        int leftY = centerY;
        
        // Draw filled diamond by drawing two triangles
        // Triangle 1: top-right-bottom
        fillTriangle(guiGraphics, topX, topY, rightX, rightY, bottomX, bottomY, colorWithAlpha);
        
        // Triangle 2: top-left-bottom
        fillTriangle(guiGraphics, topX, topY, leftX, leftY, bottomX, bottomY, colorWithAlpha);
    }

    /**
     * Fill a triangle with three vertices
     */
    private static void fillTriangle(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        // Sort vertices by Y coordinate
        if (y1 > y2) {
            int tx = x1, ty = y1;
            x1 = x2; y1 = y2;
            x2 = tx; y2 = ty;
        }
        if (y1 > y3) {
            int tx = x1, ty = y1;
            x1 = x3; y1 = y3;
            x3 = tx; y3 = ty;
        }
        if (y2 > y3) {
            int tx = x2, ty = y2;
            x2 = x3; y2 = y3;
            x3 = tx; y3 = ty;
        }
        
        // Draw horizontal lines to fill the triangle
        for (int y = y1; y <= y3; y++) {
            int xStart, xEnd;
            
            if (y <= y2) {
                // Upper part of triangle
                xStart = interpolate(y1, x1, y2, x2, y);
                xEnd = interpolate(y1, x1, y3, x3, y);
            } else {
                // Lower part of triangle
                xStart = interpolate(y2, x2, y3, x3, y);
                xEnd = interpolate(y1, x1, y3, x3, y);
            }
            
            if (xStart > xEnd) {
                int temp = xStart;
                xStart = xEnd;
                xEnd = temp;
            }
            
            guiGraphics.fill(xStart, y, xEnd + 1, y + 1, color);
        }
    }

    /**
     * Linear interpolation to find X coordinate at given Y
     */
    private static int interpolate(int y1, int x1, int y2, int x2, int y) {
        if (y1 == y2) return x1;
        return x1 + (x2 - x1) * (y - y1) / (y2 - y1);
    }
}
