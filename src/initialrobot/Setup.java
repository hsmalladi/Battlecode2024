package initialrobot;

import battlecode.common.*;

public class Setup {
    private static final int EXPLORE_ROUNDS = 150;

    public static void init(RobotController rc) throws GameActionException {
        FlagDuck.init(rc);
        ExplorerDuck.init(rc);
    }

    public static void initTurn(RobotController rc) throws GameActionException {
        if (!RobotPlayer.reachedTarget && rc.getLocation().equals(RobotPlayer.exploreLocation)) {
            RobotPlayer.reachedTarget = true;
        }
    }
    public static void run(RobotController rc) throws GameActionException {
        if (!RobotPlayer.reachedTarget) {
            explore(rc);
        }
        else {
            if (rc.hasFlag()) {
                dropFlag(rc);
            }

            if (RobotPlayer.flagDuck != 0) {
                if (!checkDefenses(rc)) {
                    buildDefenses(rc);
                }
                else {
                }

            }
            else {
                // PathFind.random(rc);
                PathFind.moveTowards(rc, Map.center);
                if (RobotPlayer.turnCount >= EXPLORE_ROUNDS) {
                    buildStunTrapsAtDam(rc);
                }
            }
        }
    }



    public static void exit(RobotController rc) throws GameActionException {
        rc.writeSharedArray(0, 0);
        rc.writeSharedArray(1, 0);
    }

    public static void buildStunTrapsAtDam(RobotController rc) throws GameActionException {
        MapLocation[] adj = Map.getAdjacentLocations(rc.getLocation());
        int adjDam = 0;
        for (MapLocation loc : adj){
            if (rc.onTheMap(loc)) {
                MapInfo info = rc.senseMapInfo(loc);
                if (info.getTrapType() == TrapType.STUN) {
                    adjDam = 0;
                    break;
                }
                if (info.isDam()) {
                    adjDam += 1;
                }
            }
        }
        if (adjDam >= 2) {
            if(rc.canBuild(TrapType.STUN, rc.getLocation())) {
                rc.build(TrapType.STUN, rc.getLocation());
            }
        }
    }

    private static void dropFlag(RobotController rc) throws GameActionException {
        if(rc.senseLegalStartingFlagPlacement(rc.getLocation())) {
            if(rc.canDropFlag(rc.getLocation())) rc.dropFlag(rc.getLocation());
        }
    }

    public static boolean checkDefenses(RobotController rc) throws GameActionException {
        MapLocation[] adj = Map.getAdjacentLocations(rc.getLocation());

        for (MapLocation loc : adj){
            if (rc.onTheMap(loc)) {
                MapInfo info = rc.senseMapInfo(loc);
                if (info.getTrapType() != TrapType.EXPLOSIVE) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void buildDefenses(RobotController rc) throws GameActionException {
        MapLocation[] adj = Map.getAdjacentLocations(rc.getLocation());
        if(rc.canBuild(TrapType.STUN, rc.getLocation())) {
            rc.build(TrapType.STUN, rc.getLocation());
        }
        for (MapLocation loc : adj){
            if(rc.canBuild(TrapType.EXPLOSIVE, loc)) {
                rc.build(TrapType.EXPLOSIVE, loc);
            }
        }
    }

    //potential improvement: communicate who is getting crumb, so not all ducks go to the same crumb
    private static void explore(RobotController rc) throws GameActionException {
        RobotPlayer.isExploring = true;
        if (RobotPlayer.flagDuck == 0)
            retrieveCrumbs(rc);
        if (RobotPlayer.isExploring) {
            if (rc.isMovementReady()) {
                PathFind.moveTowards(rc, RobotPlayer.exploreLocation);
            }
        }
    }

    public static void retrieveCrumbs(RobotController rc) throws GameActionException {
        //Retrieve all crumb locations within robot vision radius
        MapLocation[] crumbLocations = rc.senseNearbyCrumbs(-1);
        if (crumbLocations.length > 0) {
            MapLocation closestCrumb = Map.getClosestLocation(rc.getLocation(), crumbLocations);
            rc.setIndicatorString("Getting Crumb");
            if (rc.isMovementReady()) {
                PathFind.moveTowards(rc, closestCrumb);
            }
            RobotPlayer.isExploring = false;
        }
    }

}
