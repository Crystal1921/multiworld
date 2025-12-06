package me.isaiah.multiworld.map.waypoint;

/**
 * Represents a waypoint with coordinates, dimension, name, and color
 */
public record WayPoint(
        String name,
        double x,
        double y,
        double z,
        String dimensionId,
        int color
) {
    public WayPoint {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Waypoint name cannot be null or empty");
        }
        if (dimensionId == null || dimensionId.isEmpty()) {
            throw new IllegalArgumentException("Waypoint dimension ID cannot be null or empty");
        }
    }
}
