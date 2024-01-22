package betterinfra;

import battlecode.common.*;


public class BotMainRoundAttackDuck extends BotMainRoundDuck {

    public static void play() throws GameActionException {
        retrieveCrumbs();
        if (!gettingCrumb) {
            goingToFlag = rc.readSharedArray(Communication.ENEMY_FLAG_HELD) != 1;
            if (goingToFlag) {
                goToFlag();
            } else {
                retreat();
            }
        } else {
            tryAttack();
            tryHeal();
        }
    }

    private static void retrieveCrumbs() throws GameActionException {
        //Retrieve all crumb locations within robot vision radius
        MapLocation[] crumbLocations = rc.senseNearbyCrumbs(-1);
        if (crumbLocations.length > 0) {
            MapLocation closestCrumb = Map.getClosestLocation(rc.getLocation(), crumbLocations);
            rc.setIndicatorString("Getting Crumb");
            gettingCrumb = true;
            if (rc.isMovementReady()) {
                pf.moveTowards(closestCrumb);
            }
            isExploring = false;
        } else {
            gettingCrumb = false;
        }
    }

    private static void goToFlag() throws GameActionException {
        rc.setIndicatorString("ATTACK");
        tryAttack();
        tryHeal();
        tryMove();
        tryAttack();
        tryHeal();
        tryTrap();
    }

    private static void retreat() throws GameActionException {
        rc.setIndicatorString("RETREAT");

        tryAttack();
        tryHeal();
        tryMove();
        tryAttack();
        tryHeal();
        tryTrap();

    }

    private static void tryMove() throws GameActionException {
        if(!rc.isMovementReady()) return;
        if (micro.doMicro()) return;
        MapLocation closestEnemy = closestEnemy();
        if (closestEnemy != null) {
            pf.moveTowards(closestEnemy);
            return;
        }
        pf.moveTowards(closestFlag(rc));
    }

    private static MapLocation closestEnemy() throws GameActionException {
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
        }

        int mindist = 100000;
        MapLocation closestFlag = null;
        MapLocation[] flagLocations = rc.senseBroadcastFlagLocations();
        for (MapLocation flagLoc : flagLocations){
            int flagDist = rc.getLocation().distanceSquaredTo(flagLoc);
            if (flagDist < mindist){
                mindist = flagDist;
                closestFlag = flagLoc;
            }
        }
        if (closestFlag != null){
            return closestFlag;
        }

        return getRandomTarget(15);
    }

    private static MapLocation getRandomTarget(int tries) {
        MapLocation myLoc = rc.getLocation();
        int maxX = rc.getMapWidth();
        int maxY = rc.getMapHeight();
        MapLocation exploreLoc = null;
        while(tries-- > 0){
            MapLocation newLoc = new MapLocation((int)(Math.random()*maxX), (int)(Math.random()*maxY));
            if (myLoc.distanceSquaredTo(newLoc) > GameConstants.VISION_RADIUS_SQUARED){
                exploreLoc = newLoc;
            }
        }
        return exploreLoc;
    }

    public static void tryTrap() throws GameActionException {
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

    public static MapLocation closestEnemy(RobotController rc, RobotInfo[] robotInfos) {
        MapLocation[] mapLocations = new MapLocation[robotInfos.length];
        for (int i = 0; i < robotInfos.length; i++) {
            mapLocations[i] = robotInfos[i].getLocation();
        }

        return Map.getClosestLocation(rc.getLocation(), mapLocations);
    }

    private static void tryAttack() throws GameActionException {
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

    private static void tryHeal() throws GameActionException {
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
}
