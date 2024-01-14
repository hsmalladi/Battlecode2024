package initialrobot;

import battlecode.common.FlagInfo;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class AttackDuck {

    public static void init(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) {
            trySpawn(rc);
        } else {

        }
    }

    public static boolean trySpawn(RobotController rc) throws GameActionException {

    }
}
