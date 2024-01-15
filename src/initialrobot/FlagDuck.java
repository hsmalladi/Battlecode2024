package initialrobot;

import battlecode.common.*;

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
                if (Setup.checkDefenses(rc)) {
                    Setup.buildDefenses(rc);
                }
            }
            else {
                PathFind.moveTowards(rc, RobotPlayer.exploreLocation);
            }
        }
    }


    private static boolean spawnFlagDuck(RobotController rc) throws GameActionException {
        int val = rc.readSharedArray(Communication.FLAG_COMM);
        if (val < 3) {
            if (rc.canSpawn(Map.flagSpawnLocations[val])) {
                rc.spawn(Map.flagSpawnLocations[val]);
                RobotPlayer.flagDuck = val + 1;
                RobotPlayer.exploreLocation = Map.flagLocations[val];
                rc.writeSharedArray(Communication.FLAG_COMM, RobotPlayer.flagDuck);
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