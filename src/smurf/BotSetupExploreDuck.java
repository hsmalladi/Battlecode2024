package smurf;

import battlecode.common.*;

import java.util.Arrays;

public class BotSetupExploreDuck extends BotSetupDuck {
    static MapLocation setupLocation;

    public static void play() throws GameActionException {
        updateGlobals();
        if (!rc.isSpawned()) {
            init();
        }
        if (rc.isSpawned()) {
            if (Comm.needSymmetryReport && rc.canWriteSharedArray(0, 0)) {
                Comm.reportSym();
                Comm.commit_write();
            }
            MapRecorder.recordSym(500);

            if (builderDuck != 0){
                digToLv6();
            }

            if (!reachedTarget && turnCount < Constants.EXPLORE_ROUNDS) {
                explore();
            } else {
                if (turnCount > 180) {
                    if (!isNextToDam()) {
                        pf.moveTowards(Map.center);
                    }
                }
                else {
                    lineUpAtDam();
                }
            }
        }
    }

    public static void exit() throws GameActionException {
        //System.out.println(Arrays.toString(Map.enemySpawnLocations));
        updateFlagLocations();
    }
    
    public static boolean init() throws GameActionException {
        int val = rc.readSharedArray(Comm.EXPLORER_COMM);
        setupLocation = new MapLocation(0, 0);
        rc.writeSharedArray(12, 4444);
        if (val < 4) {
            if (trySpawn()) {
                exploreLocation = Map.corners[val];
                rc.writeSharedArray(Comm.EXPLORER_COMM, val + 1);
                isExploring = true;
//                rc.setIndicatorString(String.valueOf(exploreLocation));
            }
        }

        else if (val == 4) { //center
            if (trySpawn()) {
                exploreLocation = Map.center;
                rc.writeSharedArray(Comm.EXPLORER_COMM, val + 1);
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

    private static void digToLv6() throws GameActionException {
        if (rc.getLevel(SkillType.BUILD) < 6) {
            for (MapLocation adj : Map.getAdjacentLocations(rc.getLocation())) {
                if (adj.x % 2 == 0 && adj.y % 2 == 0) {
                    if (rc.canDig(adj)) {
                        rc.dig(adj);
                    }
                }
            }
        }
    }


    private static void updateFlagLocations() throws GameActionException {
        Map.allyFlagLocations = Comm.allyFlagLocs;
    }

    private static void lineUpAtDam() throws GameActionException {
       if (isNextToDam()) {
           reachedTarget = true;
           comEmptySpotsNextToDam();
           return;
       }

       MapInfo[] mapInfos = rc.senseNearbyMapInfos(-1);

       for (MapInfo mapInfo : mapInfos) {
            if (mapInfo.isDam() && mapInfo.getTeamTerritory() == Team.NEUTRAL) {
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
            if (mapInfo.isDam() && mapInfo.getTeamTerritory() == Team.NEUTRAL) {
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
