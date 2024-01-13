package initialrobot;

import battlecode.common.*;

public class Setup {
    private static final int EXPLORE_ROUNDS = 150;


    public static void init(RobotController rc) throws GameActionException {
        FlagDuck.init(rc);
        ExplorerDuck.init(rc);
    }

    public static void run(RobotController rc) throws GameActionException {
        if (RobotPlayer.turnCount < EXPLORE_ROUNDS) {
            if (!rc.hasFlag()) { //not a flag holding duck so exploring
                if (!RobotPlayer.reachedTarget) {
                    explore(rc);
                }
                else {
                    PathFind.random(rc);
                }
            }
            else {
                if (RobotPlayer.flagDuck == 1) {
                    PathFind.moveTowards(rc, Map.flagLocations[0]);
                }
                else if (RobotPlayer.flagDuck == 2) {
                    PathFind.moveTowards(rc, Map.flagLocations[1]);
                }
                else if (RobotPlayer.flagDuck == 3) {
                    PathFind.moveTowards(rc, Map.flagLocations[2]);
                }
            }
        }
        else {
            if (RobotPlayer.flagDuck != 0 ) {
                dropFlag(rc);
                buildDefenses(rc);
                if (RobotPlayer.flagDuck == 2 || RobotPlayer.flagDuck == 3) {
                    buildDefenses(rc);
                    if (RobotPlayer.turnCount % 10 == 0) {
                        PathFind.moveTowards(rc, Map.center);
                    }
                }
            }
            else {
                PathFind.moveTowards(rc, Map.center);
            }

        }

    }



    public static void exit(RobotController rc) throws GameActionException {
        rc.writeSharedArray(0, 0);
        rc.writeSharedArray(1, 0);
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
            PathFind.random(rc);
        }

    }

    private static void buildDefenses(RobotController rc) throws GameActionException {
        MapLocation[] adj = Map.getAdjacentLocations(rc.getLocation());
        if(rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation())) {
            rc.build(TrapType.EXPLOSIVE, rc.getLocation());
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
        retrieveCrumbs(rc, rc.senseNearbyCrumbs(-1));
        MapLocation me = rc.getLocation();
        if (me.equals(RobotPlayer.exploreLocation)) {
            RobotPlayer.reachedTarget = true;
            RobotPlayer.isExploring = false;
        }

        if (RobotPlayer.isExploring) {
            PathFind.moveTowards(rc, RobotPlayer.exploreLocation);
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
            PathFind.moveTowards(rc, closestCrumb);
            RobotPlayer.isExploring = false;
        }
    }

}
