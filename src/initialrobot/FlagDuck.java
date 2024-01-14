package initialrobot;

import battlecode.common.*;

import java.nio.file.Path;


public class FlagDuck {

    public static void init(RobotController rc) throws GameActionException {
        if (spawnFlagDuck(rc)) {
            FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam());
            holdFlag(rc, flags);
        }
    }

    public static void protectFlag(RobotController rc) throws GameActionException {


        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        if (enemies.length > 0) {
            if (rc.canWriteSharedArray(RobotPlayer.flagDuck, enemies.length)) {
                rc.writeSharedArray(RobotPlayer.flagDuck, enemies.length);
            }
            //do attack
            //do heal
            //do micro
        }
        else {
            if (rc.canWriteSharedArray(RobotPlayer.flagDuck, enemies.length)) {
                rc.writeSharedArray(RobotPlayer.flagDuck, enemies.length);
            }
            if (rc.getLocation().equals(RobotPlayer.exploreLocation)) {
                if (!Setup.checkDefenses(rc)) {
                    Setup.buildDefenses(rc);
                }
            }
            else {
                PathFind.moveTowards(rc, RobotPlayer.exploreLocation);
            }
        }
    }


    private static boolean spawnFlagDuck(RobotController rc) throws GameActionException {
        if (rc.readSharedArray(0) == 0) {
            if (rc.canSpawn(Map.flagSpawnLocations[0])) {
                rc.spawn(Map.flagSpawnLocations[0]);
                RobotPlayer.flagDuck = 1;
                RobotPlayer.exploreLocation = Map.flagLocations[0];
                rc.writeSharedArray(0, 1);
                return true;
            }
        }
        else if (rc.readSharedArray(0) == 1) {
            if (rc.canSpawn(Map.flagSpawnLocations[1])) {
                rc.spawn(Map.flagSpawnLocations[1]);
                RobotPlayer.flagDuck = 2;
                RobotPlayer.exploreLocation = Map.flagLocations[1];
                rc.writeSharedArray(0, 2);
                return true;
            }
        }
        else if (rc.readSharedArray(0) == 2) {
            if (rc.canSpawn(Map.flagSpawnLocations[2])) {
                rc.spawn(Map.flagSpawnLocations[2]);
                RobotPlayer.flagDuck = 3;
                RobotPlayer.exploreLocation = Map.flagLocations[2];
                rc.writeSharedArray(0, 3);
                return true;
            }
        }
        return false;
    }

    private static void holdFlag(RobotController rc, FlagInfo[] flags) throws GameActionException {
        for (FlagInfo flag : flags) {
            MapLocation flagLocation = flag.getLocation();
            if (rc.canPickupFlag(flagLocation)) {
                rc.pickupFlag(flagLocation);
                rc.setIndicatorString("Holding a flag! " + RobotPlayer.flagDuck);
                break;
            }
        }
    }




}