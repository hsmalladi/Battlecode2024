package betterinfra;

import battlecode.common.*;

public class BotSetupExploreDuck extends BotSetupDuck {
    static MapLocation setupLocation;

    public static void play() throws GameActionException {
        updateGlobals();
        if (!rc.isSpawned()) {
            init();
        }
        if (rc.isSpawned()) {
            recordQuadrants();
            if (!reachedTarget && turnCount < Constants.EXPLORE_ROUNDS) {
                explore();
            } else {
                if (exploreLocation.equals(Map.center)) {
                    determineSymmetry();
                }
                lineUpAtDam();
            }

        }
    }

    public static void exit() throws GameActionException {
        updateFlagLocations();
    }
    
    public static boolean init() throws GameActionException {
        int val = rc.readSharedArray(Communication.EXPLORER_COMM);
        setupLocation = new MapLocation(0, 0);
        rc.writeSharedArray(12, 4444);
        if (val < 4) {
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

    private static void updateFlagLocations() throws GameActionException {
        for (int i = 1; i < 4; i++) {
            MapLocation location = Map.intToLocation(rc.readSharedArray(i));
            Map.allyFlagLocations[i-1] = location;
        }
    }

    public static void recordQuadrants() throws GameActionException {
        int quad = Map.getQuadrant(rc.getLocation());
        rc.writeSharedArray(quad + 19, 1);
    }

    public static void determineSymmetry() throws GameActionException {
        int q1 = 0, q2 = 0, q3 = 0, q4 = 0;

        q1 = rc.readSharedArray(20);
        q2 = rc.readSharedArray(21);
        q3 = rc.readSharedArray(22);
        q4 = rc.readSharedArray(23);

        if (q1 + q2 + q3 + q4 >=3) {
            rc.writeSharedArray(0, 3); //3 is diagonal
        }
        else if (q1 + q2 == 2 || q4+q3 == 2) {
            rc.writeSharedArray(0, 2); // 2 is vertical
        }
        else if (q2+q3 == 2 || q1+q4 == 2) {
            rc.writeSharedArray(0, 1); // 1 is hori
        }
        else {
            rc.writeSharedArray(0, 3);
        }
    }



    private static void lineUpAtDam() throws GameActionException {
       if (isNextToDam()) {
           reachedTarget = true;
           comEmptySpotsNextToDam();
           return;
       }

       MapInfo[] mapInfos = rc.senseNearbyMapInfos(-1);

       for (MapInfo mapInfo : mapInfos) {
            if (mapInfo.isDam()) {
                MapLocation[] adjacent = Map.getAdjacentLocationsNoCorners(mapInfo.getMapLocation());
                for (MapLocation location : adjacent) {
                    if (rc.canSenseLocation(location)) {
                        MapInfo adjInfo = rc.senseMapInfo(location);
                        if (adjInfo.getTeamTerritory() == rc.getTeam() && !rc.canSenseRobotAtLocation(location) && adjInfo.isPassable()) {
                            pf.moveTowards(location);
                            return;
                        }
                    }
                }
            }
       }
       if (rc.readSharedArray(12) != 4444){
           setupLocation = Map.intToLocation(rc.readSharedArray(12));
       }
       else {
           setupLocation = Map.center;
       }
       pf.moveTowards(setupLocation);
    }

    private static void comEmptySpotsNextToDam() throws GameActionException {
        MapInfo[] mapInfos = rc.senseNearbyMapInfos(-1);
        for (MapInfo mapInfo : mapInfos) {
            if (mapInfo.isDam()) {
                MapLocation[] adjacent = Map.getAdjacentLocationsNoCorners(mapInfo.getMapLocation());
                for (MapLocation location : adjacent) {
                    if (rc.canSenseLocation(location)) {
                        MapInfo adjInfo = rc.senseMapInfo(location);
                        if (adjInfo.getTeamTerritory() == rc.getTeam() && !rc.canSenseRobotAtLocation(location)) {
                            rc.writeSharedArray(12, Map.locationToInt(location));
                            break;
                        }
                    }
                }
            }
        }
    }

    private static boolean isNextToDam() throws GameActionException {
        MapLocation[] adjacent = Map.getAdjacentLocationsNoCorners(rc.getLocation());
        for (MapLocation adj : adjacent) {
            if (rc.canSenseLocation(adj)) {
                if (rc.senseMapInfo(adj).isDam()) {
                    return true;
                }
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
            if (rc.senseMapInfo(closestCrumb).getTeamTerritory() == rc.getTeam()) {
                rc.setIndicatorString("Getting Crumb");
                gettingCrumb = true;
                if (rc.isMovementReady()) {
                    pf.moveTowards(closestCrumb);
                }
                isExploring = false;
            }
            else {
                gettingCrumb = false;
            }
        } else {
            gettingCrumb = false;
        }
    }

}
