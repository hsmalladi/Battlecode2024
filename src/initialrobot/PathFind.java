package initialrobot;

import battlecode.common.*;

import java.util.HashSet;


public class PathFind {

    static Direction direction;

    static MapLocation prevLocation = null;

    private static int duckState = 0; // 0 head to target, 1 circle obstacle

    private static Direction duckDir = null;

    private static MapLocation prevDest = null;
    private static HashSet<MapLocation> line = null;
    private static int obstacleStartDist = 10000;


    public static void resetDuck() {
        duckState = 0; // 0 head to target, 1 circle obstacle
        obstacleStartDist = 10000;
        prevDest = null;
        line = null;
        duckDir = null;
        prevLocation = null;
    }

    public static void duckNav0(RobotController rc, MapLocation location) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(location);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
        else if (rc.canFill(rc.getLocation().add(dir))) {
            rc.fill(rc.getLocation().add(dir));
        }
        else {
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    break;
                }
                else {
                    dir = dir.rotateLeft();
                }
            }
        }

    }

//    public static void duckNav1(RobotController rc, MapLocation destination) throws GameActionException{
//        if (duckState == 0) {
//            duckDir = rc.getLocation().directionTo(destination);
//            if (rc.canMove(duckDir)) {
//                rc.move(duckDir);
//            } else {
//                duckState = 1;
//                closestObstacle = null;
//                closestObstacleDist = 10000;
//            }
//        } else {
//            if (rc.getLocation().equals(closestObstacle)) {
//                duckState = 0;
//            }
//
//            if (rc.getLocation().distanceSquaredTo(destination) < closestObstacleDist) {
//                closestObstacleDist = rc.getLocation().distanceSquaredTo(destination);
//                closestObstacle = rc.getLocation();
//            }
//
//            for (int i = 0; i < 8; i++) {
//                if (rc.canMove(duckDir)) {
//                    rc.move(duckDir);
//                    duckDir = duckDir.rotateRight();
//                    break;
//                } else {
//                    duckDir = duckDir.rotateLeft();
//                }
//            }
//        }
//    }

    public static void moveTowards(RobotController rc, MapLocation destination) throws GameActionException {
        prevDest = destination;
        line = createLine(rc.getLocation(), destination);



        for(MapLocation loc : line) {
            rc.setIndicatorDot(loc, 255, 0, 0);
        }

        if (duckState == 0) {
            duckDir = rc.getLocation().directionTo(destination);
            if (rc.canMove(duckDir) && !rc.getLocation().add(duckDir).equals(prevLocation)) {
                prevLocation = rc.getLocation();
                rc.move(duckDir);
            }
            else if (rc.canFill(rc.getLocation().add(duckDir))) {
                rc.fill(rc.getLocation().add(duckDir));
            } else {
                duckState = 1;
                obstacleStartDist = rc.getLocation().distanceSquaredTo(destination);
                duckDir = rc.getLocation().directionTo(destination);
            }
        } else {
            if (line.contains(rc.getLocation()) && rc.getLocation().distanceSquaredTo(destination) <= obstacleStartDist) {
                duckState = 0;
            }

            for (int i = 0; i < 8; i++) {
                if (rc.canMove(duckDir) && !rc.getLocation().add(duckDir).equals(prevLocation)) {
                    prevLocation = rc.getLocation();
                    rc.move(duckDir);
                    duckDir = duckDir.rotateRight();
                    break;
                } else if (rc.canFill(rc.getLocation().add(duckDir))) {
                    rc.setIndicatorString("FILLING");
                    rc.fill(rc.getLocation().add(duckDir));
                } else {
                    duckDir = duckDir.rotateLeft();
                }
            }
        }
    }

    private static HashSet<MapLocation> createLine(MapLocation a, MapLocation b) {
        HashSet<MapLocation> locs = new HashSet<>();
        int x = a.x, y = a.y;
        int dx = b.x - a.x;
        int dy = b.y - a.y;
        int sx = (int) Math.signum(dx);
        int sy = (int) Math.signum(dy);
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        int d = Math.max(dx, dy);
        int r = d/2;
        if (dx > dy) {
            for (int i = 0; i < d; i++) {
                locs.add(new MapLocation(x, y));
                x += sx;
                r += dy;
                if (r >= dx) {
                    locs.add(new MapLocation(x, y));
                    y += sy;
                    r -= dx;
                }
            }
        } else {
            for (int i = 0; i < d; i++) {
                locs.add(new MapLocation(x, y));
                y += sy;
                r += dx;
                if (r >= dy) {
                    locs.add(new MapLocation(x, y));
                    x += sx;
                    r -= dy;
                }
            }
        }
        locs.add(new MapLocation(x, y));
        return locs;
    }
}