package initialrobot;

import battlecode.common.*;

public class MainRound {
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
        GlobalUpgrades.useGlobalUpgrade(rc);

        tryFlagPickUp(rc);
        tryFlagDropOff(rc);
        Setup.retrieveCrumbs(rc);

        goingToFlag = rc.readSharedArray(10) != 1;
        if (goingToFlag) {
            goToFlag(rc);
        } else {
            retreat(rc);
        }


    }

    private static int protectFlag(RobotController rc) throws GameActionException {
        for (int i = 1; i < 4; i++) {
            int numEnemies = rc.readSharedArray(i);
            if (numEnemies >= 3) {
                System.out.println("FLAG IN DANGER");
                return i;
            }
        }
        return 0;
    }

    private static void retreatToFlag(RobotController rc, int flag) throws GameActionException {
        if (!rc.hasFlag()) {
            PathFind.moveTowards(rc, Map.flagLocations[flag]);
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
        tryTrap(rc);
    }

    private static void retreat(RobotController rc) throws GameActionException {
        rc.setIndicatorString("RETREAT");
        if (!rc.hasFlag()) {
            tryAttack(rc);
            tryHeal(rc);
            tryMove(rc);
            tryAttack(rc);
            tryHeal(rc);
            tryTrap(rc);
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

    private static MapLocation closestSpawn(RobotController rc) {
        return Map.getClosestLocation(rc.getLocation(), Map.allySpawnLocations);
    }


    public static void tryTrap(RobotController rc) throws GameActionException {
        if (rc.getCrumbs() > 500) {
            RobotInfo[] oppRobotInfos = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            if (oppRobotInfos.length > 0) {
                MapLocation me = rc.getLocation();
                Direction dir = me.directionTo(oppRobotInfos[0].getLocation());
                if (rc.canBuild(TrapType.STUN, me.add(dir))) {
                    rc.build(TrapType.STUN, me.add(dir));
                }
                else if (rc.canBuild(TrapType.STUN, me)) {
                    rc.build(TrapType.STUN, me);
                }

            }
        }
    }

    public static void tryAttack(RobotController rc) throws GameActionException {
        RobotInfo[] oppRobotInfos = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, rc.getTeam().opponent());
        focusFlagAttack(rc, oppRobotInfos);
        for (RobotInfo opps : oppRobotInfos) {
            if (rc.canAttack(opps.getLocation())) {
                rc.attack(opps.getLocation());
                break;
            }
        }
    }

    public static void focusFlagAttack(RobotController rc, RobotInfo[] robotInfos) throws GameActionException {
        for (RobotInfo robotInfo : robotInfos) {
            if (robotInfo.hasFlag()) {
                if (rc.canAttack(robotInfo.getLocation())) {
                    rc.attack(robotInfo.getLocation());
                    break;
                }
            }
        }
    }



    public static void tryHeal(RobotController rc) throws GameActionException {
        RobotInfo[] allyRobots = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, rc.getTeam());
        focusFlagHeal(rc, allyRobots);
        for (RobotInfo r : allyRobots) {
            if (rc.canHeal(r.getLocation())) {
                rc.heal(r.getLocation());
                break;
            }
        }
    }

    public static void focusFlagHeal(RobotController rc, RobotInfo[] robotInfos) {
        for (RobotInfo robotInfo : robotInfos) {
            if (robotInfo.hasFlag()) {
                if (rc.canHeal(robotInfo.getLocation())) {
                    rc.canHeal(robotInfo.getLocation());
                    break;
                }
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
