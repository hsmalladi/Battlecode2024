package betterinfra;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;


public class BotMainRoundFlagDuck extends BotMainRoundDuck {

    public static void play() throws GameActionException {
        tryMoveBack();
    }

    private static void tryMoveBack() throws GameActionException {
        if(!rc.isMovementReady()) return;
        if (flagMicro.doMicro()) return;
        pf.moveTowards(closestSpawn(rc));
    }

    private static MapLocation closestSpawn(RobotController rc) {
        return Map.getClosestLocation(rc.getLocation(), Map.allySpawnLocations);
    }
}
