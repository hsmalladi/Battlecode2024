package betterinfra;

import battlecode.common.*;
public class Explore extends Globals{
    static MapLocation exploreLoc = null;

    //TODO: SMARTER EXPLORATION
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


}
