package me.isaiah.multiworld.map.waypoint;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.gui.widget.MapWidget;
import me.isaiah.multiworld.map.MapInstance;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages waypoints on the client side
 * Stores and retrieves waypoint data using YAML files
 */
public enum WayPointManager {
    INSTANCE;

    private static final String CONFIG_PATH = "config/multiworld/waypoints.yml";
    private final List<WayPoint> waypoints = new ArrayList<>();

    /**
     * Initialize waypoint configuration
     */
    public static void initWayPointConfig() {
        File configDir = new File("config/multiworld");
        configDir.mkdirs();

        File waypointFile = new File(configDir, "waypoints.yml");

        try {
            if (waypointFile.exists()) {
                INSTANCE.load();
            } else {
                INSTANCE.save();
            }
        } catch (Exception e) {
            MultiworldMod.LOGGER.warn("Failed to load waypoint config: {}", e.getMessage());
        }
    }

    /**
     * Add a new waypoint
     */
    public void addWaypoint(WayPoint waypoint) {
        waypoints.add(waypoint);
        try {
            save();
        } catch (Exception e) {
            MultiworldMod.LOGGER.error("Failed to save waypoint: {}", e.getMessage());
        }
    }

    /**
     * Remove a waypoint
     */
    public void removeWaypoint(WayPoint waypoint) {
        waypoints.remove(waypoint);
        try {
            save();
        } catch (Exception e) {
            MultiworldMod.LOGGER.error("Failed to save after removing waypoint: {}", e.getMessage());
        }
    }

    /**
     * Get all waypoints
     */
    public List<WayPoint> getWaypoints() {
        return new ArrayList<>(waypoints);
    }

    /**
     * Get waypoints for a specific dimension
     */
    public List<WayPoint> getWaypointsForDimension(MapInstance.MapConfig mapConfig) {
        String dimensionId = mapConfig.worldID();
        return waypoints.stream()
                .filter(wp -> wp.dimensionId().equals(dimensionId))
                .toList();
    }

    public List<Vec2> getScreenWaypointForDimension(MapInstance.MapConfig mapConfig, float scale, LocalPlayer player) {
        String dimensionId = mapConfig.worldID();
        return waypoints.stream()
                .filter(wp -> wp.dimensionId().equals(dimensionId))
                .map(wp -> MapWidget.getScreenPosition(mapConfig, wp.x(), wp.z(), scale, player))
                .toList();
    }

    /**
     * Create YAML configuration for consistent formatting
     */
    private Yaml createYaml() {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    }

    /**
     * Save waypoints to YAML file
     */
    private void save() throws Exception {
        List<Map<String, Object>> waypointList = getMaps();

        Map<String, Object> data = new HashMap<>();
        data.put("waypoints", waypointList);

        Yaml yaml = createYaml();

        try (var writer = java.nio.file.Files.newBufferedWriter(
                java.nio.file.Paths.get(CONFIG_PATH),
                java.nio.charset.StandardCharsets.UTF_8)) {
            yaml.dump(data, writer);
        }
    }

    private @NotNull List<Map<String, Object>> getMaps() {
        List<Map<String, Object>> waypointList = new ArrayList<>();

        for (WayPoint waypoint : waypoints) {
            Map<String, Object> waypointData = new HashMap<>();
            waypointData.put("name", waypoint.name());
            waypointData.put("x", waypoint.x());
            waypointData.put("y", waypoint.y());
            waypointData.put("z", waypoint.z());
            waypointData.put("dimensionId", waypoint.dimensionId());
            waypointData.put("color", waypoint.color());
            waypointList.add(waypointData);
        }
        return waypointList;
    }

    /**
     * Load waypoints from YAML file
     */
    private void load() throws Exception {
        waypoints.clear();

        Yaml yaml = createYaml();

        try (var reader = java.nio.file.Files.newBufferedReader(
                java.nio.file.Paths.get(CONFIG_PATH),
                java.nio.charset.StandardCharsets.UTF_8)) {
            Map<String, Object> data = yaml.load(reader);

            if (data == null || !data.containsKey("waypoints")) {
                return;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> waypointList = (List<Map<String, Object>>) data.get("waypoints");

            if (waypointList == null) {
                return;
            }

            for (Map<String, Object> waypointData : waypointList) {
                try {
                    String name = (String) waypointData.get("name");
                    double x = ((Number) waypointData.get("x")).doubleValue();
                    double y = ((Number) waypointData.get("y")).doubleValue();
                    double z = ((Number) waypointData.get("z")).doubleValue();
                    String dimensionId = (String) waypointData.get("dimensionId");
                    int color = ((Number) waypointData.get("color")).intValue();

                    if (name != null && dimensionId != null) {
                        waypoints.add(new WayPoint(name, x, y, z, dimensionId, color));
                    }
                } catch (NullPointerException | ClassCastException e) {
                    MultiworldMod.LOGGER.warn("Failed to load waypoint: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Clear all waypoints
     */
    public void clearWaypoints() {
        waypoints.clear();
        try {
            save();
        } catch (Exception e) {
            MultiworldMod.LOGGER.error("Failed to save after clearing waypoints: {}", e.getMessage());
        }
    }

    public boolean contains(String name) {
        return waypoints.stream().anyMatch(wp -> wp.name().equals(name));
    }
}
