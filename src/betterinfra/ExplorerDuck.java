package betterinfra;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Arrays;
import java.util.List;

public class ExplorerDuck {

    public static void init(RobotController rc) throws GameActionException {
        spawnExplorerDuck(rc);
    }

    private static boolean spawnExplorerDuck(RobotController rc) throws GameActionException {
        int val = rc.readSharedArray(Communication.EXPLORER_COMM);
        if (val < 4) { //top left corner
            if (trySpawn(rc)) {
                RobotPlayer.exploreLocation = Map.corners[val];
                rc.writeSharedArray(Communication.EXPLORER_COMM, val + 1);
                RobotPlayer.isExploring = true;
                rc.setIndicatorString(String.valueOf(RobotPlayer.exploreLocation));
                return true;
            }
        }

        else if (val == 4) { //center
            if (trySpawn(rc)) {
                RobotPlayer.exploreLocation = Map.center;
                rc.writeSharedArray(Communication.EXPLORER_COMM, val + 1);
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
