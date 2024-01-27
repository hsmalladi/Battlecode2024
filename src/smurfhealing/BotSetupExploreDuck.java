package smurfhealing;

import battlecode.common.*;

import static builder.BotMainRoundAttackDuck.closestEnemy;

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
                if (turnCount == 2 || reachedTarget){
                    exploreLocation = Map.getRandomLocation(rng);
                    reachedTarget = false;
                }
                if (rc.getCrumbs() > 2500) {
                    digToLv(6);
                }
                else {
                    digToLv(rc.getCrumbs() / 500);
                }

            }

            if (!reachedTarget && turnCount < Constants.EXPLORE_ROUNDS) {
                explore();
            }
            else {
                if (builderDuck != 0) {
                    if (rc.getCrumbs() > 680) {
                        buildTrapsAtDam();
                    }

                }

                if (turnCount > 185) {
                    if (!isNextToDam()) {
                        pf.moveTowards(Map.center);
                    }
                }
                else {
                    lineUpAtDam();
                    if (isNextToDam() && rc.getCrumbs() > 680) {
                        buildTrapsAtDam();
                    }
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

    private static void digToLv(int level) throws GameActionException {
        if (rc.getLevel(SkillType.BUILD) < level) {
            for (MapLocation adj : Map.getAdjacentLocations(rc.getLocation())) {
                if ((adj.x % 2 + adj.y) % 2 == 0) {
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
            if (mapInfo.isDam() && mapInfo.getTeamTerritory() == Team.NEUTRAL && !mapInfo.isWall()) {
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
            if (mapInfo.isDam() && mapInfo.getTeamTerritory() == Team.NEUTRAL && !mapInfo.isWall()) {
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
                MapInfo ad = rc.senseMapInfo(adj);
                if (ad.isDam() && !ad.isWall()) {
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

    private static void builderExplore() throws GameActionException {
        isExploring = true;
        if (flagDuck == 0)
            retrieveCrumbs();
        if (isExploring) {
            if (rc.isMovementReady()) {
                pf.follow(exploreLocation);
            }
        }
    }

    private static void buildTrapsAtDam() throws GameActionException {
        if (builderDuck !=0 ){
            RobotInfo[] oppRobotInfos = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            if (oppRobotInfos.length > 0) {
                MapLocation me = rc.getLocation();
                Direction dir = me.directionTo(closestEnemy(rc, oppRobotInfos));
                if (rc.canBuild(TrapType.EXPLOSIVE, me.add(dir))) {
                    rc.build(TrapType.EXPLOSIVE, me.add(dir));
                }
                if (rc.canBuild(TrapType.EXPLOSIVE, me.add(dir.rotateLeft()))) {
                    rc.build(TrapType.EXPLOSIVE, me.add(dir.rotateLeft()));
                }
                if (rc.canBuild(TrapType.EXPLOSIVE, me.add(dir.rotateRight()))) {
                    rc.build(TrapType.EXPLOSIVE, me.add(dir.rotateRight()));
                }
                else if (rc.canBuild(TrapType.EXPLOSIVE, me)) {
                    rc.build(TrapType.EXPLOSIVE, me);
                }
            }
        }
        else {
            RobotInfo[] oppRobotInfos = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            if (oppRobotInfos.length > 0) {
                boolean build = true;
                for (MapLocation adj : Map.getAdjacentLocationsNoCorners(rc.getLocation())) {
                    if (rc.canSenseLocation(adj)) {

                        if (rc.senseMapInfo(adj).getTrapType() != TrapType.NONE){
                            build = false;
                        }
                    }
                }
                if (build) {
                    if (rc.canBuild(TrapType.STUN, rc.getLocation())) {
                        rc.build(TrapType.STUN, rc.getLocation());
                    }
                }
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
