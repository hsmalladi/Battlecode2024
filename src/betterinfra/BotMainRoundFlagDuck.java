package betterinfra;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;


public class BotMainRoundFlagDuck extends BotMainRoundDuck {

    private static MapLocation target;
    public static void play() throws GameActionException {
        tryMoveBack();
    }

    private static void tryMoveBack() throws GameActionException {
        if(!rc.isMovementReady()) return;
        if (flagMicro.doMicro()) return;
        if (target == null) {
            target = closestSpawn();
        }
        pf.moveTowards(target);
    }

    private static MapLocation closestSpawn() {
        System.out.println(Map.getClosestLocation(rc.getLocation(), Map.allySpawnLocations));
        return Map.getClosestLocation(rc.getLocation(), Map.allySpawnLocations);
    }
}
