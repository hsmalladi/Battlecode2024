package stableversion;

import battlecode.common.*;

public class BotSetupFlagDuck extends BotSetupDuck {

    public static final int
                MOVE_FLAG = 10,
                DEFEND_FLAG = 11;

    private static FlagInfo[] flags = null;

    public static void play() throws GameActionException {
        updateGlobals();
        if (!rc.isSpawned()) {
            if (initFlagDuck()) {
                flags = rc.senseNearbyFlags(-1, rc.getTeam());
                holdFlag();

                Comm.commFlagLocation(rc.getLocation(), flagDuck);

            }
        }
        else {
            Comm.readFlagLocation();
            calculateOptimalFlagLocation();
            moveToLocation();
            Comm.commFlagLocationDropped(flagDuck);
            if (rc.hasFlag() && rc.getLocation().equals(exploreLocation)) {
                dropFlag();
                buildDefenses();
            }
        }
    }

    public static boolean initFlagDuck() throws GameActionException {
        if (rc.canSpawn(Map.flagSpawnLocations[flagDuck-1])) {
            rc.spawn(Map.flagSpawnLocations[flagDuck-1]);
            exploreLocation = Map.allyFlagLocations[flagDuck-1];
//            rc.setIndicatorString(String.valueOf(Map.flagSpawnLocations[flagDuck-1]));
            return true;
        }
        return false;
    }




    public static void calculateOptimalFlagLocation() throws GameActionException {
        MapInfo[] mapInfos = rc.senseNearbyMapInfos(-1);

        MapLocation best = mapInfos[0].getMapLocation();
        int maxScore = -99999999;
        for (MapInfo mapInfo : mapInfos) {
            if (!mapInfo.isWall() && !mapInfo.isDam()) {
                int score = getScore(mapInfo, Comm.allyFlagLocs);
                if (maxScore < score) {
                    best = mapInfo.getMapLocation();
                    maxScore = score;
                }
            }
        }
        exploreLocation = best;
    }

    private static int getScore(MapInfo mapInfo, MapLocation[] flags) {
        int score = 0;
        for (MapLocation flag : flags) {
            if (!flag.equals(rc.getLocation())) {
                if (flag.distanceSquaredTo(mapInfo.getMapLocation()) < 63) {
                    score -= 10000 * flag.distanceSquaredTo(mapInfo.getMapLocation());
                }
                else {
                    //score += (int) Math.sqrt(flag.distanceSquaredTo(mapInfo.getMapLocation()));
                }
            }
        }
        for (MapLocation enemySpawn : Map.enemyFlagSpawnLocations) {
            score += (int) Math.sqrt(enemySpawn.distanceSquaredTo(mapInfo.getMapLocation()));
        }
        return score;
    }

    private static int getWallScore(MapInfo mapInfo) {
        return 0;
    }

    private static void moveToLocation() throws GameActionException {
        if (rc.isMovementReady()) {
            isExploring = true;
            if (isExploring) {
                if (!rc.getLocation().equals(exploreLocation)) {
                    pf.moveTowards(exploreLocation);
                }
            }
        }
    }

    private static void holdFlag() throws GameActionException {
        for (FlagInfo flag : flags) {
            MapLocation flagLocation = flag.getLocation();
            if (rc.canPickupFlag(flagLocation)) {
                rc.pickupFlag(flagLocation);
                rc.setIndicatorString("Holding a flag! " + flagDuck);
                break;
            }
        }
    }

    private static void dropFlag() throws GameActionException {
        if(rc.senseLegalStartingFlagPlacement(rc.getLocation())) {
            if(rc.canDropFlag(rc.getLocation())) {
                rc.dropFlag(rc.getLocation());
            }
        }
    }


    public static void buildDefenses() throws GameActionException {
        MapLocation[] adj = Map.getAdjacentLocations(rc.getLocation());
        if(rc.canBuild(TrapType.STUN, rc.getLocation())) {
            rc.build(TrapType.STUN, rc.getLocation());
        }
        for (MapLocation loc : adj){
            if(rc.canBuild(TrapType.WATER, loc)) {
                rc.build(TrapType.WATER, loc);
                break;
            }
        }
    }


}
