package flagplacement;

import battlecode.common.*;
import com.sun.org.apache.xpath.internal.operations.Bool;

public class BotSetupFlagDuck extends BotSetupDuck {

    public static final int
                MOVE_FLAG = 10,
                DEFEND_FLAG = 11;

    private static FlagInfo[] flags = null;

    private static Direction[] directions = new Direction[]{Direction.EAST, Direction.NORTHEAST, Direction.NORTH,
                                                            Direction.NORTHWEST, Direction.WEST, Direction.SOUTHWEST,
                                                            Direction.SOUTH, Direction.SOUTHEAST};

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

    private static int getScore(MapInfo mapInfo, MapLocation[] flags) throws GameActionException {
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
            int distFromEnemySpawn = (int) Math.sqrt(enemySpawn.distanceSquaredTo(mapInfo.getMapLocation()));
            score += distFromEnemySpawn;
        }
        if(numWalls(mapInfo) == 7) {
            score *= 2;
        }
        return score;
    }

    private static int getWallScore(MapInfo mapInfo) {
        return 0;
    }

    private static void moveToLocation() throws GameActionException {
        if (rc.isMovementReady()) {
            if (!rc.getLocation().equals(exploreLocation)) {
                OldPathFind.moveTowards(exploreLocation);
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

    private static int numWalls(MapInfo info) throws GameActionException {
        int total = 0;
        MapLocation targetSquare = info.getMapLocation();
        for (Direction dir : directions) {
            MapLocation adjacentSquare = targetSquare.add(dir);
            if(rc.canSenseLocation(adjacentSquare)) {
                MapInfo adjSquare = rc.senseMapInfo(adjacentSquare);
                if(adjSquare.isWall()) {
                    total++;
                }
            }
        }
        if(targetSquare.x == 0 && targetSquare.y == 0) {
            total += 5;
        } else if(targetSquare.x == 0 && targetSquare.y == Map.mapHeight - 1) {
            total += 5;
        } else if(targetSquare.x == Map.mapWidth - 1 && targetSquare.y == 0) {
            total += 5;
        } else if(targetSquare.x == Map.mapWidth - 1 && targetSquare.y == Map.mapHeight - 1) {
            total += 5;
        } else if(targetSquare.x == Map.mapWidth-1 || targetSquare.x == 0 || targetSquare.y == Map.mapHeight-1 || targetSquare.y == 0) {
            total += 3;
        }
        if(total == 8) {
            total = 0;
        }
        return total;
    }
}
