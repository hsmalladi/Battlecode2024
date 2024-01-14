package initialrobot;

import battlecode.common.*;
import battlecode.world.Flag;
import battlecode.world.control.TeamControlProvider;

public class MainRound {

    private static final int EXPLORE_ROUNDS = 150;
    private static MapLocation currentLocation = null;
    private static Micro m = null;

    public static void init(RobotController rc) throws GameActionException {
        m = new Micro(rc);
    }

    public static void initTurn(RobotController rc) throws GameActionException {

    }

    public static void run(RobotController rc) throws GameActionException {
        tryAttack(rc);
        tryMove(rc);
        tryAttack(rc);
    }

    private static void tryAttack(RobotController rc) throws GameActionException {
        RobotInfo[] oppRobotInfos = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, rc.getTeam().opponent());
        if (oppRobotInfos.length > 0 && rc.canAttack(oppRobotInfos[0].getLocation())) {
            rc.attack(oppRobotInfos[0].getLocation());
        }
    }

    private static void tryMove(RobotController rc) throws GameActionException {
        if (m.doMicro()) return;
        PathFind.moveTowards(rc, closestFlag(rc));
    }

    private static MapLocation closestFlag(RobotController rc) throws GameActionException {
         FlagInfo[] flags = rc.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED, rc.getTeam().opponent());
         if (flags.length > 0) {
             return flags[0].getLocation();
         } else {
             return rc.senseBroadcastFlagLocations()[0];
         }
    }



    public static void exit(RobotController rc) throws GameActionException {

    }

    //potential improvement: communicate who is getting crumb, so not all ducks go to the same crumb
    private static void explore(RobotController rc) throws GameActionException {
        RobotPlayer.isExploring = true;

        if (RobotPlayer.isExploring) {
            if (rc.isMovementReady()) {
                PathFind.moveTowards(rc, RobotPlayer.exploreLocation);
            }
        }
    }


}
