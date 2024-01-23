package betterinfra;

import battlecode.common.*;

import java.util.ArrayList;


public class BotMainRoundAttackDuck extends BotMainRoundDuck {

    public static void play() throws GameActionException {
        retrieveCrumbs();
        if (!gettingCrumb) {
            goingToFlag = rc.readSharedArray(Comm.ENEMY_FLAG_HELD) != 1;
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
        MapLocation target = getTarget();
        pf.moveTowards(target);
    }

    private static MapLocation getTarget() throws GameActionException{
        MapLocation target = getBestTarget();
        if (target != null){
            rc.setIndicatorString("FOUND A GOOD TARGET IN VISION");
            return target;
        }
        //TODO: ADD ENEMY FLAG AND SPAWN TARGET
        target = closestFlag();
        if (target != null){
            rc.setIndicatorString("GOING TO BROADCAST");
            return target;
        }
        //TODO: MAKE SMARTER THAN JUST CHOOSING ONE SPOT
//        target = Map.enemySpawnLocations[0];
//        if (target != null) return target;
        rc.setIndicatorString("I'm DUMB. GOING TO RANDOM LOC");
        return Explore.getExploreTarget();
    }

    private static MapLocation getBestTarget() throws GameActionException{
        MoveTarget bestTarget = null;
        MapLocation closestEnemyFlag = getClosestVisionFlag();
        if (closestEnemyFlag != null){
            return closestEnemyFlag;
        }

        int dist = 10000;
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), GameConstants.VISION_RADIUS_SQUARED, rc.getTeam().opponent());
        for(RobotInfo enemy: enemies){
            MoveTarget mt = new MoveTarget(enemy);
            if (mt.isBetterThan(bestTarget)) bestTarget = mt;
        }
        if (bestTarget != null)
        {
            return bestTarget.mloc;
        }
        return null;
    }

    private static MapLocation closestFlag() throws GameActionException {
        ArrayList<MapLocation> flagLocs = new ArrayList<>();
        for (int i = Comm.ENEMY_FLAG_FIRST; i <= Comm.ENEMY_FLAG_LAST; i++) {
            MapLocation loc = Comm.getLocation(i);
            if (loc != null && !Comm.isCarried(i)) {
                flagLocs.add(loc);
            }
        }
        if (flagLocs.size() == 0) {
            MapLocation[] broadcastLocs = rc.senseBroadcastFlagLocations();
            for (MapLocation flagLoc : broadcastLocs) {
                flagLocs.add(flagLoc);
            }
        }

        MapLocation closestFlag = findClosest(rc.getLocation(), flagLocs);
        return closestFlag;
    }

    private static MapLocation getClosestVisionFlag() throws GameActionException {
        int dist = 10000;
        MapLocation closestFlag = null;
        FlagInfo[] flags = rc.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED, rc.getTeam().opponent());
        for (FlagInfo flag : flags) {
            if (!flag.isPickedUp() && rc.getLocation().distanceSquaredTo(flag.getLocation()) < dist)
            {
                closestFlag = flag.getLocation();
                dist = rc.getLocation().distanceSquaredTo(flag.getLocation());
            }
        }
        return closestFlag;
    }

    private static MapLocation getClosestBroadcastFlag() throws GameActionException {
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

        return null;
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
        if (builderDuck == 1) {
            RobotInfo[] oppRobotInfos = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            if (oppRobotInfos.length > 0) {
                MapLocation me = rc.getLocation();
                Direction dir = me.directionTo(closestEnemy(rc, oppRobotInfos));
                if (rc.canBuild(TrapType.EXPLOSIVE, me.add(dir))) {
                    rc.build(TrapType.EXPLOSIVE, me.add(dir));
                }
                if (rc.canBuild(TrapType.EXPLOSIVE, me.add(dir.rotateLeft()))) {
                    rc.build(TrapType.EXPLOSIVE, me.add(dir.rotateLeft()));
                }
                if (rc.canBuild(TrapType.EXPLOSIVE, me.add(dir.rotateRight()))) {
                    rc.build(TrapType.EXPLOSIVE, me.add(dir.rotateRight()));
                }
                if (rc.canBuild(TrapType.EXPLOSIVE, me)) {
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
