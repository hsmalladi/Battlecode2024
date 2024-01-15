package initialrobot;

import battlecode.common.*;
import battlecode.world.Flag;
import battlecode.world.control.TeamControlProvider;

public class MainRound {

    private static final int EXPLORE_ROUNDS = 150;
    private static MapLocation currentLocation = null;
    private static Micro m = null;
    private static boolean goingToFlag = true;
    private static boolean amHoldingFlag = false;

    public static void init(RobotController rc) throws GameActionException {
        m = new Micro(rc);
        goingToFlag = true;
    }

    public static void initTurn(RobotController rc) throws GameActionException {

    }

    public static void run(RobotController rc) throws GameActionException {
        tryFlagPickUp(rc);
        tryFlagDropOff(rc);
        if (rc.readSharedArray(10) == 1) {
            goingToFlag = false;
        } else {
           goingToFlag = true;
        }
        if (goingToFlag) {
            goToFlag(rc);
        } else {
            retreat(rc);
        }
    }

    private static void tryFlagPickUp(RobotController rc) throws GameActionException {
        for (FlagInfo loc : rc.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED)) {
            if (rc.canPickupFlag(loc.getLocation())) {
                rc.pickupFlag(loc.getLocation());
                goingToFlag = false;
                amHoldingFlag = true;
                System.out.println("PICKED UP FLAG");
                rc.writeSharedArray(10, 1);
                break;
            }
        }
    }

    private static void tryFlagDropOff(RobotController rc) throws GameActionException {
        if (!rc.hasFlag() && amHoldingFlag) {
            rc.writeSharedArray(10, 0);
            amHoldingFlag = false;
            goingToFlag = true;
        }
    }

    private static void goToFlag(RobotController rc) throws GameActionException {
        rc.setIndicatorString("ATTACK");
        tryAttack(rc);
        tryHeal(rc);
        tryMove(rc);
        tryAttack(rc);
        tryHeal(rc);
    }

    private static void retreat(RobotController rc) throws GameActionException {
        rc.setIndicatorString("RETREAT");
        if (!rc.hasFlag()) {
            tryAttack(rc);
            tryHeal(rc);
            tryMove(rc);
            tryAttack(rc);
            tryHeal(rc);
        } else {
            tryMoveBack(rc);
        }
    }

    private static void tryMoveBack(RobotController rc) throws GameActionException {
        if (!rc.hasFlag()) {
            if (m.doMicro()) return;
        }
        PathFind.moveTowards(rc, closestSpawn(rc));
    }

    private static MapLocation closestSpawn(RobotController rc) throws GameActionException {
//        MapLocation closest = null;
//        for (MapLocation loc : rc.getAllySpawnLocations()) {
//            if (closest == null) {
//                closest = loc;
//            }
//            else if (loc.distanceSquaredTo(rc.getLocation()) < closest.distanceSquaredTo(rc.getLocation())) {
//                closest = loc;
//            }
//        }
        return rc.getAllySpawnLocations()[0];
    }


    private static void tryAttack(RobotController rc) throws GameActionException {
        RobotInfo[] oppRobotInfos = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, rc.getTeam().opponent());
        if (oppRobotInfos.length > 0) {
            for (RobotInfo opps : oppRobotInfos) {
                if (rc.canAttack(opps.getLocation())) {
                    rc.attack(opps.getLocation());
                    break;
                }
            }
        }
    }

    private static void tryHeal(RobotController rc) throws GameActionException {
        RobotInfo[] allyRobots = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, rc.getTeam());
        for (RobotInfo r : allyRobots) {
            if (rc.canHeal(r.getLocation())) {
                rc.heal(r.getLocation());
                break;
            }
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
            if (rc.senseBroadcastFlagLocations().length > 0)
                return rc.senseBroadcastFlagLocations()[0];
            else
                return rc.getAllySpawnLocations()[0];
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