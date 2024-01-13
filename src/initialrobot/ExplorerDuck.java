package initialrobot;

import battlecode.common.*;

import java.util.Arrays;
import java.util.List;

public class ExplorerDuck {

    public static void init(RobotController rc) throws GameActionException {
        spawnExplorerDuck(rc);
    }

    private static boolean spawnExplorerDuck(RobotController rc) throws GameActionException {
        if (rc.readSharedArray(1) == 0) { //top left corner
            if (trySpawn(rc)) {
                RobotPlayer.exploreLocation = Map.corners[0];
                rc.writeSharedArray(1, 1);
                RobotPlayer.isExploring = true;
                rc.setIndicatorString("TopLeft " + RobotPlayer.exploreLocation);
                return true;
            }
        }
        else if (rc.readSharedArray(1) == 1) { // top right corner
            if (trySpawn(rc)) {
                RobotPlayer.exploreLocation = Map.corners[1];
                rc.writeSharedArray(1, 2);
                RobotPlayer.isExploring = true;
                rc.setIndicatorString("TopRight " + RobotPlayer.exploreLocation);
                return true;
            }
        }
        else if (rc.readSharedArray(1) == 2) { //bottom left corner
            if (trySpawn(rc)) {
                RobotPlayer.exploreLocation = Map.corners[2];
                rc.writeSharedArray(1, 3);
                RobotPlayer.isExploring = true;
                rc.setIndicatorString("BotLeft " + RobotPlayer.exploreLocation);
                return true;
            }
        }
        else if (rc.readSharedArray(1) == 3) { //bottom right corner
            if (trySpawn(rc)) {
                RobotPlayer.exploreLocation = Map.corners[3];
                rc.writeSharedArray(1, 4);
                RobotPlayer.isExploring = true;
                rc.setIndicatorString("BotRight " + RobotPlayer.exploreLocation);
                return true;
            }
        }
        else if (rc.readSharedArray(1) == 4) { //center
            if (trySpawn(rc)) {
                RobotPlayer.exploreLocation = Map.center;
                rc.writeSharedArray(1, 5);
                RobotPlayer.isExploring = true;
                rc.setIndicatorString("Center " + RobotPlayer.exploreLocation);
                return true;
            }
        }
        else { // random location
            MapLocation target = Map.getRandomLocation(RobotPlayer.rng);
            if (trySpawn(rc)) {
                RobotPlayer.exploreLocation = target;
                RobotPlayer.isExploring = true;
                rc.setIndicatorString("Explorer " + RobotPlayer.exploreLocation);
            }
        }
        return false;
    }

    public static boolean trySpawn(RobotController rc) throws GameActionException {
        for (MapLocation loc : Map.allySpawnLocations) {
            if (rc.canSpawn(loc)) {
                rc.spawn(loc);
                return true;
            }
        }
        return false;
    }
    private static boolean trySpawnSorted(RobotController rc, MapLocation target) throws GameActionException {
        List<MapLocation> sorted = Arrays.asList(Map.allySpawnLocations);
        Map.sortCoordinatesByDistance(sorted, target);
        for (MapLocation loc : sorted) {
            if (rc.canSpawn(loc)) {
                rc.spawn(loc);
                return true;
            }
        }
        return false;
    }

}
