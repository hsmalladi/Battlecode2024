package initialrobot;

import battlecode.common.*;


public class FlagDuck {

    public static void init(RobotController rc) throws GameActionException {
        if (spawnFlagDuck(rc)) {
            FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam());
            holdFlag(rc, flags);
        }
    }

    private static boolean spawnFlagDuck(RobotController rc) throws GameActionException {
        if (rc.readSharedArray(0) == 0) {
            if (rc.canSpawn(Map.flagSpawnLocations[0])) {
                rc.spawn(Map.flagSpawnLocations[0]);
                RobotPlayer.flagDuck = 1;
                rc.writeSharedArray(0, 1);
                return true;
            }
        }
        else if (rc.readSharedArray(0) == 1) {
            if (rc.canSpawn(Map.flagSpawnLocations[1])) {
                rc.spawn(Map.flagSpawnLocations[1]);
                RobotPlayer.flagDuck = 2;
                rc.writeSharedArray(0, 2);
                return true;
            }
        }
        else if (rc.readSharedArray(0) == 2) {
            if (rc.canSpawn(Map.flagSpawnLocations[2])) {
                rc.spawn(Map.flagSpawnLocations[2]);
                RobotPlayer.flagDuck = 3;
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