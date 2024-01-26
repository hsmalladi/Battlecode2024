package oldescapebot;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class MicroFlag extends Globals {

    PathFind path;
    static final int FLEE_STRIDE = 50;

    MicroFlag(){
        path = new PathFind();
    }


    MapLocation normalize (int x, int y){
        double dx = x, dy = y;
        double norm = Math.sqrt((x*x) + (y*y));
        dx/=norm;
        dy/=norm;
        dx *= FLEE_STRIDE;
        dy *= FLEE_STRIDE;
        int xi = rc.getLocation().x + (int) dx, yi = rc.getLocation().y + (int) dy;
        if (xi < 0) xi = 0;
        if (yi < 0) yi = 0;
        if (xi >= rc.getMapWidth()) xi = rc.getMapWidth() - 1;
        if (yi >= rc.getMapHeight()) yi = rc.getMapHeight() - 1;
        return new MapLocation (xi, yi);
    }

    boolean doMicro(){
        try{
            RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), -1, rc.getTeam().opponent());
            MapLocation myLoc = rc.getLocation();
            int x = 0;
            int y = 0;
            boolean found = false;
            for (RobotInfo r : enemies){
                MapLocation loc = r.getLocation();
                int dx = myLoc.x - loc.x, dy = myLoc.y - loc.y;
                if (dx < 0) dx = (-5) - dx;
                else if (dx > 0) dx = 5 - dx;
                if (dy < 0) dy = (-5) - dy;
                if (dy > 0) dy = 5 - dy;
                x += dx;
                y += dy;
                found = true;
            }
            if (!found) return false;
            if (x == 0 && y == 0) return true;
            MapLocation target = normalize(x,y);
            path.moveTowards(target);
            return true;
        } catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static Direction midDirection(Direction dir1, Direction dir2) {
        int d = (dir1.ordinal() + dir2.ordinal()) / 2;

        return Globals.directions[d];
    }


}
