package betterinfra;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;


public class BotMainRoundFlagDuck extends BotMainRoundDuck {


    public static void play() throws GameActionException {
        tryMoveBack();
    }

    private static void tryMoveBack() throws GameActionException {
        if(!rc.isMovementReady()) return;
        captureFlag();
        if (flagMicro.doMicro()) return;
        pf.moveTowards(closestSpawn());
    }

    private static void captureFlag() throws GameActionException {
        MapLocation tar = closestSpawn();
        if (tar.distanceSquaredTo(rc.getLocation()) < 2) {
            Direction dir = rc.getLocation().directionTo(tar);
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        }
    }

    private static MapLocation closestSpawn() {
        return Map.getClosestLocation(rc.getLocation(), Map.allySpawnLocations);
    }
}
