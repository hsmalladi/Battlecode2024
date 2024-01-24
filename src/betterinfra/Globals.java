package betterinfra;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;

import java.util.ArrayList;
import java.util.Random;

public class Globals {

    static int id;
     //0 for not a flag duck, 1-3 to represent the flags.
    static MapLocation exploreLocation;
    static boolean isExploring = false;
    static boolean gettingCrumb = false;
    static boolean reachedTarget = false;
    static boolean buildDefenses = false;
    public static RobotController rc;

    public static Micro micro;
    public static MicroFlag flagMicro;

    static int turnCount = 0;

    static Random rng;
    static String indicator;

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public static void init(RobotController r) {
        rc = r;
        Map.init(rc);
        rng = new Random(rc.getID());
        id = rc.getID();
        exploreLocation = new MapLocation(0, 0);
        micro = new Micro();
        flagMicro = new MicroFlag();
        indicator = "";
    }

    public static void updateGlobals() {
        if (!reachedTarget && rc.isSpawned() && rc.getLocation().equals(exploreLocation)) {
            reachedTarget = true;
        }
    }

    public static MapLocation findClosest(MapLocation ref, ArrayList<MapLocation> locations) {
        int maxDistance = 100000;
        MapLocation best = null;
        for (MapLocation loc : locations) {
            int distTo = ref.distanceSquaredTo(loc);
            if (distTo < maxDistance) {
                maxDistance = distTo;
                best = loc;
            }
        }
        return best;
    }



}
