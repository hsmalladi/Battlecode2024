package betterinfra;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class BotSetupExploreDuck extends BotSetupDuck {

    public static void play() throws GameActionException {
        updateGlobals();
        if (!rc.isSpawned()) {
            init();
        }
        if (rc.isSpawned()) {
            if (!reachedTarget && turnCount < Constants.EXPLORE_ROUNDS) {
                explore();
            } else {
                retrieveCrumbs();
                if (!gettingCrumb)
                    pf.moveTowards(Map.center);
            }
        }
    }
    
    public static boolean init() throws GameActionException {
        int val = rc.readSharedArray(Communication.EXPLORER_COMM);
        if (val < 4) { //top left corner
            if (trySpawn()) {
                exploreLocation = Map.corners[val];
                rc.writeSharedArray(Communication.EXPLORER_COMM, val + 1);
                isExploring = true;
                rc.setIndicatorString(String.valueOf(exploreLocation));
            }
        }

        else if (val == 4) { //center
            if (trySpawn()) {
                exploreLocation = Map.center;
                rc.writeSharedArray(Communication.EXPLORER_COMM, val + 1);
                isExploring = true;
                rc.setIndicatorString("Center " + exploreLocation);
                return true;
            }
        }
        else { // random location
            MapLocation target = Map.getRandomLocation(rng);
            if (trySpawn()) {
                exploreLocation = target;
                isExploring = true;
                rc.setIndicatorString("Explorer " + exploreLocation);
            }
        }
        return false;
    }

    private static void explore() throws GameActionException {
        isExploring = true;
        if (flagDuck == 0)
            retrieveCrumbs();
        if (isExploring) {
            if (rc.isMovementReady()) {
                pf.moveTowards(exploreLocation);
            }
        }
    }

    private static void retrieveCrumbs() throws GameActionException {
        //Retrieve all crumb locations within robot vision radius
        MapLocation[] crumbLocations = rc.senseNearbyCrumbs(-1);
        if (crumbLocations.length > 0) {
            MapLocation closestCrumb = Map.getClosestLocation(rc.getLocation(), crumbLocations);
            rc.setIndicatorString("Getting Crumb");
            gettingCrumb = true;
            if (rc.isMovementReady()) {
                pf.moveTowards(closestCrumb);
            }
            isExploring = false;
        } else {
            gettingCrumb = false;
        }
    }

}
