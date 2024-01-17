package betterinfra;


import battlecode.common.FlagInfo;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import sprintv2.PathFind;
import sprintv2.RobotPlayer;

public class BotSetupFlagDuck extends BotSetupDuck {

    public static final int
                MOVE_FLAG = 10,
                DEFEND_FLAG = 11;

    private static FlagInfo[] flags = null;

    public static void play() throws GameActionException {
        if (!rc.isSpawned()) {
            if (initFlagDuck()) {
                flags = rc.senseNearbyFlags(-1, rc.getTeam());
                holdFlag();
            }
        }
        if (rc.isMovementReady()) {
            isExploring = true;
            if (isExploring) {
                PathFind.moveTowards(rc, exploreLocation);
            }
        }
    }

    public static boolean initFlagDuck() throws GameActionException {
        if (rc.canSpawn(Map.flagSpawnLocations[flagDuck-1])) {
            rc.spawn(Map.flagSpawnLocations[flagDuck-1]);
            exploreLocation = Map.flagLocations[flagDuck-1];
            return true;
        }
        return false;
    }

    private static void holdFlag() throws GameActionException {
        for (FlagInfo flag : flags) {
            MapLocation flagLocation = flag.getLocation();
            if (rc.canPickupFlag(flagLocation)) {
                rc.pickupFlag(flagLocation);
                rc.setIndicatorString("Holding a flag! " + flagDuck);
                break;
            }
        }
    }


}
