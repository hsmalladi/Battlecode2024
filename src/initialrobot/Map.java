package initialrobot;

import battlecode.common.*;

import java.util.*;

public class Map {
    static int mapWidth;
    static int mapHeight;
    static MapLocation[] allySpawnLocations;
    static MapLocation[] flagSpawnLocations;
    static int[][] map;
    static MapLocation center;
    static MapLocation[] corners;
    static MapLocation[] flagLocations;
    static ArrayList<MapLocation> neutralCrumbs;


    public static void init(RobotController rc) {
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        allySpawnLocations = rc.getAllySpawnLocations();
        flagSpawnLocations = getCenters(allySpawnLocations);
        center = getCenter();
        corners = getFourCorners();
        flagLocations = calculateFlagLocations();
        neutralCrumbs = new ArrayList<>();
    }
    private static MapLocation[] calculateFlagLocations() {
        MapLocation bestCorner = calculateBestCorner();
        MapLocation two = null;
        MapLocation three = null;
        if (bestCorner == Map.corners[0]) {
            two = bestCorner.translate(0, -7);
            three = bestCorner.translate(7, 0);
        }
        else if (bestCorner == Map.corners[1]) {
            two = bestCorner.translate(0, -7);
            three = bestCorner.translate(-7, 0);
        }
        else if (bestCorner == Map.corners[2]) {
            two = bestCorner.translate(0, 7);
            three = bestCorner.translate(7, 0);
        }
        else if (bestCorner == Map.corners[3]) {
            two = bestCorner.translate(0, 7);
            three = bestCorner.translate(-7, 0);
        }
         return new MapLocation[]{bestCorner, two, three};
    }
    private static MapLocation calculateBestCorner() {
        int max = 999999999;
        int best = -1;
        for (int i = 0; i < Map.corners.length; i++) {
            int distance = 0;
            for (MapLocation flag : Map.flagSpawnLocations) {
                distance += Map.corners[i].distanceSquaredTo(flag);
            }
            if (distance < max) {
                max = distance;
                best = i;
            }
        }
        return Map.corners[best];
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

    private static MapLocation[] getCenters(MapLocation[] spawnZones) {
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

    public static MapLocation[] getAdjacentLocations(MapLocation location) {
        Direction[] directions = Direction.allDirections();
        MapLocation[] adjacentLocations = new MapLocation[8];
        for (int i = 0; i < 8; i++) {
            adjacentLocations[i] = location.add(directions[i]);
        }
        return adjacentLocations;
    }
}
