package detectstuntraps;

import battlecode.common.*;

import java.util.ArrayList;

public class Explore extends Globals {
    static MapLocation exploreLoc = null;

    static MapLocation flagExploreLoc = null;
    static MapLocation randomBroadCast = null;

    public static boolean exploredBroadcast = false;

    public static boolean exploredCorner = false;

    //TODO: SMARTER EXPLORATION
    public static MapLocation getFlagTarget() throws GameActionException {
        ArrayList<MapLocation> flagLocs = new ArrayList<>();
        for (int i = Comm.ENEMY_FLAG_FIRST; i <= Comm.ENEMY_FLAG_LAST; i++) {
            MapLocation loc = Comm.getLocation(i);
            if (loc != null && !Comm.isCarried(i)) {
                flagLocs.add(loc);
            }
        }
        MapLocation closestFlag = findClosest(rc.getLocation(), flagLocs);
        return closestFlag;
    }

    public static MapLocation getBroadcastFlagTarget() throws GameActionException {
        MapLocation[] broadcastLocs = rc.senseBroadcastFlagLocations();
        ArrayList<MapLocation> flagLocs = new ArrayList<>();
        for (MapLocation flagLoc : broadcastLocs) {
            flagLocs.add(flagLoc);
        }
        MapLocation closestFlag = findClosest(rc.getLocation(), flagLocs);
        return closestFlag;
    }

    public static MapLocation randomBroadcast() throws GameActionException {
        broadcastLocs = rc.senseBroadcastFlagLocations();
        if (broadcastLocs.length > 0) {
            if (randomBroadCast == null) {
                randomBroadCast = broadcastLocs[rng.nextInt(broadcastLocs.length)];
            }
            return randomBroadCast;
        }
        return null;
    }

    public static MapLocation randomBroadcastBuilder(int builder) throws GameActionException {
        MapLocation[] broadcastLocs = rc.senseBroadcastFlagLocations();
        if (broadcastLocs.length > 0) {
            if (randomBroadCast == null) {
                randomBroadCast = broadcastLocs[builder % broadcastLocs.length];
            }
            return randomBroadCast;
        }
        return null;
    }

    public static MapLocation protectFlagHolder() throws GameActionException {
        RobotInfo[] robotInfos = rc.senseNearbyRobots(-1, rc.getTeam());
        for (RobotInfo r : robotInfos) {
            if (r.hasFlag()) {
                if(r.getLocation().distanceSquaredTo(rc.getLocation()) <= 4){
                    return null;
                }
                Direction d = r.getLocation().directionTo(Map.getClosestLocation(r.getLocation(), Map.allySpawnLocations));
                return r.getLocation().add(d.opposite());
            }
        }
        return null;
    }

    public static MapLocation attackFlagHolder() throws GameActionException {
        RobotInfo[] robotInfos = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (RobotInfo r : robotInfos) {
            if (r.hasFlag()) {
                return r.getLocation();
            }
        }
        return null;
    }

    public static MapLocation getDefenseTarget() throws GameActionException {
        for (int i = 51; i < 54; i++) {
            if (rc.readSharedArray(i) > 0) { //flag missing for 10 or less turns
                MapLocation defend = closestEnemySpawnZoneToOurFlag(i);
                int distance = Comm.allyFlagLocs[i -51].distanceSquaredTo(defend);
                if (rc.readSharedArray(i) < Math.sqrt(distance) * 2) {
                    return defend;
                }
            }
        }
        return null;
    }

    public static MapLocation closestEnemySpawnZoneToOurFlag(int flag) throws GameActionException{
        int index = flag - 51;

        MapLocation defendTarget = Map.getClosestLocation(Comm.allyFlagLocs[index], Map.enemySpawnLocations);
        return defendTarget;
    }
    public static MapLocation getExploreTarget(){
        if (exploreLoc != null && rc.getLocation().distanceSquaredTo(exploreLoc) <= GameConstants.VISION_RADIUS_SQUARED) exploreLoc = null;
        if(exploreLoc == null){
            getRandomTarget(15);
        }
        return exploreLoc;
    }

    public static void getRandomTarget(int tries) {
        MapLocation myLoc = rc.getLocation();
        int maxX = rc.getMapWidth();
        int maxY = rc.getMapHeight();
        while (tries-- > 0){
            if (exploreLoc != null) return;
            MapLocation newLoc = new MapLocation((int)(Math.random()*maxX), (int)(Math.random()*maxY));
            if (myLoc.distanceSquaredTo(newLoc) > GameConstants.VISION_RADIUS_SQUARED){
                exploreLoc = newLoc;
            }
        }
    }

    public static MapLocation getTargetAroundBroadcast(MapLocation broadcastLocation) {
        if (flagExploreLoc != null && rc.getLocation().distanceSquaredTo(flagExploreLoc) <= GameConstants.VISION_RADIUS_SQUARED) flagExploreLoc = null;
        if (flagExploreLoc == null){
            getRandomTargetNearFlag(15, broadcastLocation);
        }
        return flagExploreLoc;
    }

    public static void getRandomTargetNearFlag(int tries, MapLocation broadcastLocation) {
        int maxX = Math.min(rc.getMapWidth(), broadcastLocation.x + Constants.MAX_BROADCAST_DISTANCE + 1);
        int maxY = Math.min(rc.getMapHeight(), broadcastLocation.y + Constants.MAX_BROADCAST_DISTANCE + 1);
        int minX = Math.max(0, broadcastLocation.x - Constants.MAX_BROADCAST_DISTANCE);
        int minY = Math.max(0, broadcastLocation.y - Constants.MAX_BROADCAST_DISTANCE);
        while(tries-- > 0){
            if (flagExploreLoc != null) return;
            MapLocation newLoc = new MapLocation((int) (Math.random() * (maxX - minX)) + minX, (int) (Math.random() * (maxY - minY)) + minY);
            if(rc.getLocation().distanceSquaredTo(newLoc) > GameConstants.VISION_RADIUS_SQUARED){
                flagExploreLoc = newLoc;
            }
        }

    }


}
