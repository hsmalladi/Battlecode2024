package betterinfra;

import battlecode.common.*;


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
        if (flagMicro.doMicro()) return;
        pf.moveTowards(closestSpawn());
    }

    private static MapLocation closestSpawn() {
        return Map.getClosestLocation(rc.getLocation(), Map.allySpawnLocations);
    }
}
