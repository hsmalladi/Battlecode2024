package spacing;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;


public class BotMainRoundFlagDuck extends BotMainRoundDuck {



    public static void play() throws GameActionException {
        tryMoveBack();
    }

    private static void tryMoveBack() throws GameActionException {
        MapLocation closestSpawn = closestSpawn();
        if (escaping) {
            buildEscape(closestSpawn);
            return;
        }
        if(!rc.isMovementReady()) return;
        if(closestSpawn.distanceSquaredTo(rc.getLocation()) <= GameConstants.VISION_RADIUS_SQUARED){
            pf.moveTowards(closestSpawn());
            return;
        }
        //if (flagMicro.doMicro()) return;
        pf.moveTowards(closestSpawn());
    }

    private static void buildEscape(MapLocation location) throws GameActionException {
        if (!rc.isActionReady()) {
            Debug.log("I AM NOT READY TO DIG");
            return;
        }
        Direction dir = rc.getLocation().directionTo(location);
        boolean dirCanPass = pf.canPass(dir);
        boolean dirRightCanPass = pf.canPass(dir.rotateRight(), dir);
        boolean dirLeftCanPass = pf.canPass(dir.rotateLeft(), dir);
        if (dirCanPass || dirRightCanPass || dirLeftCanPass) {
            if (rc.canFill(rc.getLocation().add(dir.rotateLeft()))) {
                if (escaping) escaping = false;
                rc.fill(rc.getLocation().add(dir.rotateLeft()));
            } else if (rc.canFill(rc.getLocation().add(dir.rotateRight()))) {
                if (escaping) escaping = false;
                rc.fill(rc.getLocation().add(dir.rotateRight()));
            } else if (rc.canFill(rc.getLocation().add(dir))) {
                if (escaping) escaping = false;
                rc.fill(rc.getLocation().add(dir));
            }
            // Debug.log("I AM ESCAPING");

        }

    }

    public static MapLocation closestSpawn() {
        return Map.getClosestLocation(rc.getLocation(), Map.allySpawnLocations);
    }
}
