package betterinfra;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

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

    static int turnCount = 0;

    static Random rng;

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
    }

    public static void updateGlobals() {
        if (!reachedTarget && rc.isSpawned() && rc.getLocation().equals(exploreLocation)) {
            reachedTarget = true;
        }
    }


}