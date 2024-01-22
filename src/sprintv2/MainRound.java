package sprintv2;

import battlecode.common.*;

public class MainRound {
    private static Micro m = null;
    private static MicroFlag flagM = null;
    private static boolean goingToFlag = true;
    private static boolean amHoldingFlag = false;

    public static void init(RobotController rc) throws GameActionException {
        m = new Micro(rc);
        flagM = new MicroFlag(rc);
        goingToFlag = true;
    }

    public static void initTurn(RobotController rc) throws GameActionException {

    }

    public static void run(RobotController rc) throws GameActionException {
        GlobalUpgrades.useGlobalUpgrade(rc);

        tryFlagPickUp(rc);
        tryFlagDropOff(rc);
        if (!rc.hasFlag()) {
            //dieAtAllCostsForFlag(rc);
            Setup.retrieveCrumbs(rc);
        }

        if (!RobotPlayer.gettingCrumb) {
            goingToFlag = rc.readSharedArray(10) != 1;
            if (goingToFlag) {
                goToFlag(rc);
            } else {
                retreat(rc);
            }
        }
        else {
            tryAttack(rc);
            tryHeal(rc);
        }

    }

    private static int protectFlag(RobotController rc) throws GameActionException {
        for (int i = 1; i < 4; i++) {
            int numEnemies = rc.readSharedArray(i);
            if (numEnemies >= 3) {
                return i;
            }
        }
        return 0;
    }

    private static void retreatToFlag(RobotController rc, int flag) throws GameActionException {
        if (!rc.hasFlag()) {
            tryAttack(rc);
            tryHeal(rc);
            PathFind.moveTowards(rc, Map.flagLocations[flag]);
            tryAttack(rc);
            tryHeal(rc);
            tryTrap(rc);
        }
    }


    private static void dieAtAllCostsForFlag(RobotController rc) throws GameActionException {
        RobotInfo[] oppRobotInfos = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (RobotInfo robotInfo : oppRobotInfos) {
            if (robotInfo.hasFlag()) {
                PathFind.moveTowards(rc, robotInfo.location);
                break;
            }
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
        if(!rc.isMovementReady()) return;
        if (flagM.doMicro()) return;
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
                Direction dir = me.directionTo(closestEnemy(rc, oppRobotInfos));
                if (rc.canBuild(TrapType.EXPLOSIVE, me.add(dir))) {
                    rc.build(TrapType.EXPLOSIVE, me.add(dir));
                }
                else if (rc.canBuild(TrapType.EXPLOSIVE, me)) {
                    rc.build(TrapType.EXPLOSIVE, me);
                }
            }
        }
    }

    public static void tryAttack(RobotController rc) throws GameActionException {
        if(!rc.isActionReady()) return;
        RobotInfo[] enemies = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, rc.getTeam().opponent());
        AttackTarget bestTarget = null;

        for (RobotInfo enemy : enemies) {
            if (rc.canAttack(enemy.location)){
                AttackTarget at = new AttackTarget(enemy);
                if (at.isBetterThan(bestTarget)) bestTarget = at;
            }
        }
        if (bestTarget != null && rc.canAttack(bestTarget.mloc)) {
            rc.attack(bestTarget.mloc);
        }
    }



    public static void tryHeal(RobotController rc) throws GameActionException {
        if(!rc.isActionReady()) return;
        RobotInfo[] allyRobots = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, rc.getTeam());
        HealingTarget bestTarget =  null;
        for (RobotInfo r : allyRobots) {
            if (rc.canHeal(r.getLocation())) {
                HealingTarget hl = new HealingTarget(r);
                if (hl.isBetterThan(bestTarget)) bestTarget = hl;
            }
        }
        if(bestTarget != null && rc.canHeal(bestTarget.mloc)){
            rc.heal(bestTarget.mloc);
        }
    }


    private static void tryMove(RobotController rc) throws GameActionException {
        if(!rc.isMovementReady()) return;
        if (m.doMicro()) return;
        MapLocation closestEnemy = closestEnemy(rc);
        if (closestEnemy != null) {
            PathFind.moveTowards(rc, closestEnemy);
        }
        PathFind.moveTowards(rc, closestFlag(rc));
    }

    private static MapLocation closestEnemy(RobotController rc) throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), GameConstants.VISION_RADIUS_SQUARED, rc.getTeam().opponent());
        MapLocation bestTarget = null;
        int best_dist = 100000;
        for (RobotInfo enemy: enemies){
            int enemy_dist = rc.getLocation().distanceSquaredTo(enemy.getLocation());
            if (enemy_dist < best_dist) {
                best_dist = enemy_dist;
                bestTarget = enemy.getLocation();
            }
        }

        return bestTarget;
    }

    private static MapLocation closestFlag(RobotController rc) throws GameActionException {
         FlagInfo[] flags = rc.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED, rc.getTeam().opponent());
         if (flags.length > 0) {
             for (FlagInfo flag : flags) {
                 if (!flag.isPickedUp())
                    return flag.getLocation();
             }
         } else {
            if (rc.senseBroadcastFlagLocations().length > 0) {
                if (RobotPlayer.turnCount < 1500 || rc.senseBroadcastFlagLocations().length == 1)
                    return rc.senseBroadcastFlagLocations()[0];
                else
                    return rc.senseBroadcastFlagLocations()[1];
            }
            else
                return rc.getAllySpawnLocations()[0];
         }
         return rc.getAllySpawnLocations()[0];
    }

    public static MapLocation closestEnemy(RobotController rc, RobotInfo[] robotInfos) {
        MapLocation[] mapLocations = new MapLocation[robotInfos.length];
        for (int i = 0; i < robotInfos.length; i++) {
            mapLocations[i] = robotInfos[i].getLocation();
        }

        return Map.getClosestLocation(rc.getLocation(), mapLocations);
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


    public static class AttackTarget{
        int health;
        boolean flagHolder = false;
        MapLocation mloc;

        boolean isBetterThan(AttackTarget t){
            if (t == null) return true;
            if (flagHolder && !t.flagHolder) return true;
            if (!flagHolder && t.flagHolder) return false;
            return health <= t.health;
        }

        AttackTarget(RobotInfo r){
            health = r.getHealth();
            mloc = r.getLocation();
            flagHolder = r.hasFlag();
        }
    }

    public static class HealingTarget{
        int health;
        boolean flagHolder = false;
        MapLocation mloc;

        boolean isBetterThan(HealingTarget t){
            if (t == null) return true;
            if (flagHolder && !t.flagHolder) return true;
            if (!flagHolder && t.flagHolder) return false;
            return health <= t.health;
        }

        HealingTarget(RobotInfo r){
            health = r.getHealth();
            mloc = r.getLocation();
            flagHolder = r.hasFlag();
        }
    }


}
