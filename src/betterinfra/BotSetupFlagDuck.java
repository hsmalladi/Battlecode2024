package betterinfra;

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
            }
        }
        if (!reachedTarget) {
            moveToLocation();
        } else {
            if (rc.hasFlag()) {
                dropFlag();
            }
            setUpStructure();
        }
    }

    public static boolean initFlagDuck() throws GameActionException {
        if (rc.canSpawn(Map.flagSpawnLocations[flagDuck-1])) {
            rc.spawn(Map.flagSpawnLocations[flagDuck-1]);
            exploreLocation = Map.flagLocations[flagDuck-1];
            return true;
        }
        return false;
    }

    private static void moveToLocation() throws GameActionException {
        if (rc.isMovementReady()) {
            isExploring = true;
            if (isExploring) {
                pf.moveTowards(exploreLocation);
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

    private static void setUpStructure() throws GameActionException {
        if (rc.hasFlag()) {
            dropFlag();
        }
        if (!rc.hasFlag()) {
            if (farmToLvl6Build()) {
                if (!checkDefenses() && !buildDefenses) {
                    buildDefenses();
                } else {
                    if (buildTrapsWithin3Tiles(flagDuck)) {
                        pf.moveTowards(Map.flagLocations[flagDuck - 1]);
                    }
                }
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

    public static boolean checkDefenses() throws GameActionException {
        MapLocation[] adj = Map.getAdjacentLocations(rc.getLocation());

        for (MapLocation loc : adj){
            if (rc.onTheMap(loc)) {
                MapInfo info = rc.senseMapInfo(loc);
                if (info.getTrapType() == TrapType.NONE) {
                    return false;
                }
            }
        }
        buildDefenses = true;
        return true;
    }

    public static void buildDefenses() throws GameActionException {
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

    public static boolean farmToLvl6Build() throws GameActionException {
        int currentLvl = rc.getLevel(SkillType.BUILD);
        MapLocation[] adj = sprintv2.Map.getAdjacentLocations(rc.getLocation());
        if (currentLvl < 6) {
            for (MapLocation location : adj) {
                if (rc.onTheMap(location)) {
                    if (rc.canDig(location)) {
                        rc.dig(location);
                    }
                    if (rc.canFill(location)) {
                        rc.fill(location);
                    }
                }
            }
        }
        else {
            return true;
        }
        return false;
    }

    public static boolean buildTrapsWithin3Tiles(int flagDuck) throws GameActionException {
        MapInfo[] trapsLocations = rc.senseNearbyMapInfos(Map.flagLocations[flagDuck-1], 6);
        if (trapsLocations.length > 0) {
            for (MapInfo trapLoc : trapsLocations) {
                if (trapLoc.getTrapType() == TrapType.NONE) {
                    if (rc.canBuild(TrapType.EXPLOSIVE, trapLoc.getMapLocation())) {
                        rc.build(TrapType.EXPLOSIVE, trapLoc.getMapLocation());
                        return false;
                    }
                }
            }

            for (MapInfo trapLoc : trapsLocations) {
                if (trapLoc.getTrapType() == TrapType.NONE && trapLoc.isPassable()) {
                    sprintv2.PathFind.moveTowards(rc, trapLoc.getMapLocation());
                    return false;
                }
            }
        }

        return true;
    }

}
