package me.isaiah.multiworld.waypoint;

import me.isaiah.multiworld.MultiworldMod;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

    private final List<WayPoint> waypoints = new ArrayList<>();
    private static final String CONFIG_PATH = "config/multiworld/waypoints.yml";

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
            MultiworldMod.LOGGER.warn("Failed to load waypoint config: " + e.getMessage());
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
            MultiworldMod.LOGGER.error("Failed to save waypoint: " + e.getMessage());
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
            MultiworldMod.LOGGER.error("Failed to save after removing waypoint: " + e.getMessage());
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
    public List<WayPoint> getWaypointsForDimension(String dimensionId) {
        return waypoints.stream()
                .filter(wp -> wp.dimensionId().equals(dimensionId))
                .toList();
    }

    /**
     * Save waypoints to YAML file
     */
    private void save() throws Exception {
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

        Map<String, Object> data = new HashMap<>();
        data.put("waypoints", waypointList);

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(CONFIG_PATH)) {
            yaml.dump(data, writer);
        }
    }

    /**
     * Load waypoints from YAML file
     */
    private void load() throws Exception {
        waypoints.clear();

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);

        try (FileReader reader = new FileReader(CONFIG_PATH)) {
            Map<String, Object> data = yaml.load(reader);
            
            if (data == null || !data.containsKey("waypoints")) {
                return;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> waypointList = (List<Map<String, Object>>) data.get("waypoints");
            
            for (Map<String, Object> waypointData : waypointList) {
                String name = (String) waypointData.get("name");
                double x = ((Number) waypointData.get("x")).doubleValue();
                double y = ((Number) waypointData.get("y")).doubleValue();
                double z = ((Number) waypointData.get("z")).doubleValue();
                String dimensionId = (String) waypointData.get("dimensionId");
                int color = ((Number) waypointData.get("color")).intValue();
                
                waypoints.add(new WayPoint(name, x, y, z, dimensionId, color));
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
            MultiworldMod.LOGGER.error("Failed to save after clearing waypoints: " + e.getMessage());
        }
    }
}
