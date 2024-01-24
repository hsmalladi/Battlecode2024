package smurf;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;


public class BotMainRoundFlagDuck extends BotMainRoundDuck {



    public static void play() throws GameActionException {
        tryMoveBack();
    }

    private static void tryMoveBack() throws GameActionException {
        if(!rc.isMovementReady()) return;
        MapLocation closestSpawn = closestSpawn();
        if(closestSpawn.distanceSquaredTo(rc.getLocation()) <= GameConstants.VISION_RADIUS_SQUARED){
            pf.moveTowards(closestSpawn());
            return;
        }
        if (micro.doMicro()) return;
        pf.moveTowards(closestSpawn());
    }

    public static MapLocation closestSpawn() {
        return Map.getClosestLocation(rc.getLocation(), Map.allySpawnLocations);
    }
}
