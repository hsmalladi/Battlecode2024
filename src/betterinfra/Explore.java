package betterinfra;

import battlecode.common.*;

import java.util.ArrayList;

public class Explore extends Globals{
    static MapLocation exploreLoc = null;

    static MapLocation flagExploreLoc = null;

    public static boolean exploredBroadcast = false;

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
