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

    public static void moveAndDig(RobotController rc) throws GameActionException {
        MapLocation water = rc.getLocation().add(Direction.allDirections()[RobotPlayer.rng.nextInt(8)]);
        if (rc.canDig(water)) {
            rc.dig(water);
        }
        else {
           // PathFind.random(rc);
        }

    }

    private static boolean checkDefenses(RobotController rc) throws GameActionException {
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

    private static void buildDefenses(RobotController rc) throws GameActionException {
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

    private static void digMoat(RobotController rc) throws GameActionException {
        MapLocation[] adj = Map.getAdjacentLocations(rc.getLocation());
        for (MapLocation loc : adj){
            if(rc.canDig(loc)) {
                rc.dig(loc);
            }
        }
    }

    //potential improvement: communicate who is getting crumb, so not all ducks go to the same crumb
    private static void explore(RobotController rc) throws GameActionException {
        RobotPlayer.isExploring = true;
        if (RobotPlayer.flagDuck == 0)
            retrieveCrumbs(rc, rc.senseNearbyCrumbs(-1));
        if (RobotPlayer.isExploring) {
            if (rc.isMovementReady()) {
                PathFind.moveTowards(rc, RobotPlayer.exploreLocation);
            }
        }
    }

    private static void calculateSymmetry(RobotController rc) throws GameActionException {
    }

    private static void communicateMap(RobotController rc) throws GameActionException {

        MapInfo[] mapInfos = rc.senseNearbyMapInfos();
        for (int i = 0; i < mapInfos.length; i++) {
            if (rc.canWriteSharedArray(i, covertMapLocationToInt(mapInfos[i]))) {
                rc.writeSharedArray(i, covertMapLocationToInt(mapInfos[i]));
            }
        }
    }

    private static void updateMap(RobotController rc) throws GameActionException {
        for (int i = 0; i < 64; i++) {
            int info = rc.readSharedArray(i);
            if (info > 0) {
                int[] array = covertIntToMapInfo(info);

                Map.map[array[0]][array[1]] = array[2];
                rc.writeSharedArray(i, 0);
            }
        }
    }

    private static int covertMapLocationToInt(MapInfo info) {
        MapLocation location = info.getMapLocation();

        int ret = 0;
        ret += location.x * Map.mapWidth + location.y;
        ret += 10000 * convertTileTypeToInt(info);

        return ret;
    }

    private static int[] covertIntToMapInfo(int num) {
        int firstDigit = Integer.parseInt(Integer.toString(num).substring(0, 1));
        num -= firstDigit * 10000;
        int x = num / Map.mapWidth;
        int y = num % Map.mapWidth;
        return new int[]{x,y,firstDigit};
    }

    private static int convertTileTypeToInt(MapInfo info) {
        if (info.isPassable()) {
            return 1;
        }
        if (info.isWall()) {
            return 2;
        }
        return 1;
    }

    private static void retrieveCrumbs(RobotController rc, MapLocation[] crumbLocations) throws GameActionException {
        //Retrieve all crumb locations within robot vision radius
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
