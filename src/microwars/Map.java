package microwars;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Map {
    static int mapWidth;
    static int mapHeight;
    static MapLocation[] allySpawnLocations;
    static MapLocation[] flagSpawnLocations;
    static MapLocation[] enemyFlagSpawnLocations;
    static int[][] map;
    static MapLocation center;
    static MapLocation[] corners;
    static MapLocation[] allyFlagLocations;
    static ArrayList<MapLocation> neutralCrumbs;
    static MapLocation[] enemySpawnLocations;
    static MapLocation[] enemyFlagLocations;

    public static void init(RobotController rc) {
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        allySpawnLocations = rc.getAllySpawnLocations();
        enemySpawnLocations = new MapLocation[allySpawnLocations.length];
        flagSpawnLocations = getCenters(allySpawnLocations);
        enemyFlagSpawnLocations = new MapLocation[flagSpawnLocations.length];
        enemyFlagLocations = new MapLocation[flagSpawnLocations.length];
        center = getCenter();
        corners = getFourCorners();
        allyFlagLocations = getFlagSpawnLocations();
        neutralCrumbs = new ArrayList<>();
    }

    public static MapLocation[] getFourCorners() {
        MapLocation bottomLeft = new MapLocation(0, 0);
        MapLocation bottomRight = new MapLocation(mapWidth - 1, 0);
        MapLocation topLeft = new MapLocation(0, mapHeight - 1);
        MapLocation topRight = new MapLocation(mapWidth - 1, mapHeight - 1);

        return new MapLocation[]{topLeft, topRight, bottomLeft, bottomRight};
    }

    public static MapLocation getCenter() {
        return new MapLocation(mapWidth/2 , mapHeight/2);
    }

    public static MapLocation getClosestLocation(MapLocation me, MapLocation[] locs) {
        int minDistance = Integer.MAX_VALUE;
        MapLocation closestLocation = null;

        for (MapLocation loc : locs) {
            int distance = me.distanceSquaredTo(loc);
            if (distance < minDistance) {
                minDistance = distance;
                closestLocation = loc;
            }
        }

        return closestLocation;
    }

    public static MapLocation getRandomLocation(Random rng) {
        int randomX = rng.nextInt(mapWidth);
        int randomY = rng.nextInt(mapHeight);

        return new MapLocation(randomX, randomY);
    }

    public static void sortCoordinatesByDistance(List<MapLocation> coordinates, MapLocation target) {
        coordinates.sort((c1, c2) -> {
            double distance1 = c1.distanceSquaredTo(target);
            double distance2 = c2.distanceSquaredTo(target);

            return Double.compare(distance1, distance2);
        });
    }

    public static int getQuadrant(MapLocation location) {
        int x = location.x;
        int y = location.y;
        if (x < mapWidth / 2) {
            if (y > mapHeight / 2) {
                return 4;
            }
            else {
                return 3;
            }
        }
        else {
            if (y > mapHeight / 2) {
                return 1;
            }
            else {
                return 2;
            }
        }
    }
    public static MapLocation[] getCenters(MapLocation[] spawnZones) {
        ArrayList<MapLocation> group1 = new ArrayList<>();
        ArrayList<MapLocation> group2 = new ArrayList<>();
        ArrayList<MapLocation> group3 = new ArrayList<>();

        for (MapLocation location : spawnZones) {
            if (group1.isEmpty() || isInGroup(location, group1)) {
                group1.add(location);
            }
            else if (group2.isEmpty() || isInGroup(location, group2)) {
                group2.add(location);
            }
            else {
                group3.add(location);
            }
        }

        return new MapLocation[]{calculateCenters(group1), calculateCenters(group2), calculateCenters(group3)};
    }

    private static MapLocation calculateCenters(ArrayList<MapLocation> group) {
        int x = 0;
        int y = 0;
        for (MapLocation mapLocation : group) {
            x += mapLocation.x;
            y += mapLocation.y;
        }

        return new MapLocation(x / group.size(), y / group.size());
    }

    private static boolean isInGroup(MapLocation location, ArrayList<MapLocation> group) {
        for (MapLocation mapLocation : group) {
            if (location.distanceSquaredTo(mapLocation) <= 9) {
                return true;
            }
        }
        return false;
    }

    public static MapLocation[] getFlagSpawnLocations() {
        return new MapLocation[]{allySpawnLocations[5], allySpawnLocations[14], allySpawnLocations[23]};
    }

    public static MapLocation[] getAdjacentLocations(MapLocation location) {
        Direction[] directions = Direction.allDirections();
        MapLocation[] adjacentLocations = new MapLocation[directions.length];
        for (int i = 0; i < directions.length; i++) {
            adjacentLocations[i] = location.add(directions[i]);
        }
        return adjacentLocations;
    }

    public static MapLocation[] getAdjacentLocationsNoCorners(MapLocation location) {
        Direction[] directions = Direction.cardinalDirections();
        MapLocation[] adjacentLocations = new MapLocation[directions.length];
        for (int i = 0; i < directions.length; i++) {
            adjacentLocations[i] = location.add(directions[i]);
        }
        return adjacentLocations;
    }

    public static int locationToInt(MapLocation location) {
        return location.x * Map.mapWidth + location.y;
    }

    public static MapLocation intToLocation(int num) {
        int x = num / Map.mapWidth;
        int y = num % Map.mapWidth;
        return new MapLocation(x, y);
    }
}
