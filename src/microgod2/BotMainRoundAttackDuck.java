package microgod2;

import battlecode.common.*;

import java.util.ArrayList;

import static microgod2.BotSetupExploreDuck.checkValidTrap;


public class BotMainRoundAttackDuck extends BotMainRoundDuck {

    static MapLocation[] prevStunTrap;
    static RobotInfo[] prevStunRobot;
    public static void play() throws GameActionException {
        if (turnCount < 220 && rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length == 0) {
            retrieveCrumbsMove();
        }

        if (!gettingCrumb || turnCount >= 220) {
            act();
            if(!micro.doMicro()){
                macro();
            }
            act();
            tryTrap(20);
        }
        updateStunTraps();
    }


    private static void act() throws GameActionException {
        tryTrap(10);
        tryAttack();
        tryAttack();
        tryHeal();
    }


    static void updateStunTraps() throws GameActionException {
        MapInfo[] mapInfos = rc.senseNearbyMapInfos(-1);
        ArrayList<MapLocation> stunTraps = new ArrayList<>();
        for (MapInfo map : mapInfos) {
            if (map.getTrapType() == TrapType.STUN) {
                stunTraps.add(map.getMapLocation());
            }
        }
        prevStunTrap = stunTraps.toArray(new MapLocation[0]);
    }


    private static MapLocation retrieveCrumbs() throws GameActionException {
        //Retrieve all crumb locations within robot vision radius
        MapLocation[] crumbLocations = rc.senseNearbyCrumbs(-1);
        if (crumbLocations.length > 0) {
            MapLocation closestCrumb = Map.getClosestLocation(rc.getLocation(), crumbLocations);
            if (reachable(closestCrumb)) {
                rc.setIndicatorString("Getting Crumb");
                return closestCrumb;
            }
        }
        return null;
    }

    private static void retrieveCrumbsMove() throws GameActionException {
        //Retrieve all crumb locations within robot vision radius
        MapLocation[] crumbLocations = rc.senseNearbyCrumbs(-1);
        if (crumbLocations.length > 0) {
            MapLocation closestCrumb = Map.getClosestLocation(rc.getLocation(), crumbLocations);
            if (reachable(closestCrumb)) {
                rc.setIndicatorString("Getting Crumb");
                gettingCrumb = true;
                if (rc.isMovementReady()) {
                    pf.moveTowards(closestCrumb);
                }

            }
        }
        gettingCrumb = false;
    }

    private static boolean reachable(MapLocation location) throws GameActionException {
        MapLocation[] adj = Map.getAdjacentLocations(location);
        for (MapLocation ad : adj) {
            if (rc.canSenseLocation(ad)) {
                if (!rc.senseMapInfo(ad).isWall()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void macro() throws GameActionException {
        if (!rc.isMovementReady()) return;
        MapLocation target = getTarget();
        rc.setIndicatorLine(rc.getLocation(), target, 255,0,0);
        pf.moveTowards(target);
    }

    private static MapLocation getTarget() throws GameActionException{
        MapLocation target = getClosestVisionFlag();
        if (target != null) {
            return target;
        }
        target = getClosestDroppedAllyFlag();
        if(target != null){
            return target;
        }

        target = Explore.protectFlagHolder();
        if (target != null){
            rc.setIndicatorString("PROTECT FlAG IN VISION");
            return target;
        }

        target = getBestTarget();
        if (target != null){
            rc.setIndicatorString("FOUND A GOOD TARGET IN VISION");
            return target;
        }

        target = Explore.getFlagTarget();
        if (target !=  null){
            if (Explore.randomBroadCast == null || (Explore.randomBroadCast != null &&
                    rc.getLocation().distanceSquaredTo(target)
                            < rc.getLocation().distanceSquaredTo(Explore.randomBroadCast)
                            + GameConstants.FLAG_BROADCAST_NOISE_RADIUS + 1)) {
                Explore.exploredBroadcast = false;
                Explore.exploredCorner = false;
                Explore.randomBroadCast = null;
                rc.setIndicatorString("GOING TO COMMED FLAG");
                return target;
            }
        }

        broadcastLocs = rc.senseBroadcastFlagLocations();
        if (broadcastLocs.length == 0) {
            return Explore.getExploreTarget();
        }

        if(!Explore.exploredBroadcast){
            if (builderDuck !=0) {
                target = Explore.randomBroadcastBuilder(builderDuck);
            }
            else {
                target = Explore.randomBroadcast();
            }
            if(target != null){
                if(rc.getLocation().distanceSquaredTo(target) <= 5){
                    Explore.exploredBroadcast = true;
                }
                rc.setIndicatorString("GOING TO BROADCAST FLAG");
                return target;
            }
        }

        if(!Explore.exploredCorner){
            target = Map.getClosestLocation(rc.getLocation(), Map.corners);
            if(target != null){
                if(rc.getLocation().distanceSquaredTo(target) <= 5){
                    Explore.exploredCorner = true;
                }
                rc.setIndicatorString("GOING TO CORNER");
                return target;
            }
        }

        target = retrieveCrumbs();

        if (target !=  null){
            rc.setIndicatorString("GOING TO CRUMB");
            return target;
        }

        rc.setIndicatorString("I'm DUMB. GOING TO RANDOM LOC");
        return Explore.getExploreTarget();
    }


    private static MapLocation getBestTarget() throws GameActionException{
        MoveTarget bestTarget = null;

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
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

    private static MapLocation getClosestVisionFlag() throws GameActionException {
        int dist = 10000;
        MapLocation closestFlag = null;
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo flag : flags) {
            if (!flag.isPickedUp() && rc.getLocation().distanceSquaredTo(flag.getLocation()) < dist)
            {
                closestFlag = flag.getLocation();
                dist = rc.getLocation().distanceSquaredTo(flag.getLocation());
            }
        }
        return closestFlag;
    }

    // Comm.AllyFlagLocations
    private static MapLocation getClosestDroppedAllyFlag()  throws GameActionException {
        int dist = 10000;
        MapLocation[] initialAllyFlagLocations = Comm.allyFlagLocs;
        FlagInfo[] flags = rc.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED, rc.getTeam());

        MapLocation closestDroppedAllyFlag = null;
        for (FlagInfo flag: flags){
            boolean isInitialLoc = false;
            for(MapLocation initialFlagLoc: initialAllyFlagLocations){
                if(initialFlagLoc.equals(flag.getLocation())){
                    isInitialLoc = true;
                }
            }
            if(!isInitialLoc && !flag.isPickedUp()){
                if(rc.getLocation().distanceSquaredTo(flag.getLocation()) < dist){
                    closestDroppedAllyFlag = flag.getLocation();
                    dist = rc.getLocation().distanceSquaredTo(flag.getLocation());
                }
            }
        }
        return closestDroppedAllyFlag;
    }


    public static void tryTrap(int cd) throws GameActionException {

        if (!rc.isActionReady()) return;
        if (trapTooMuchCD(cd)) return;
        RobotInfo[] oppRobotInfos = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (oppRobotInfos.length == 0) return;
        MapLocation me = rc.getLocation();
        Direction dir = me.directionTo(closestEnemy(rc, oppRobotInfos));
        if (rc.getLevel(SkillType.BUILD) > 3) {
            rc.setIndicatorString("BUILDING TRAPS");
            buildStunTrap(me, dir, dir, cd);
            buildExplosiveTrap(me, dir, dir, cd);
            buildStunTrap(me, dir.rotateLeft(), dir, cd);
            buildStunTrap(me, dir.rotateRight(), dir, cd);
            buildExplosiveTrap(me, dir.rotateLeft(), dir, cd);
            buildExplosiveTrap(me, dir.rotateRight(), dir, cd);
        }
        buildStunTrap(me, dir, dir, cd);
        buildStunTrap(me, dir.rotateLeft(), dir, cd);
        buildStunTrap(me, dir.rotateRight(), dir, cd);
        tryWaterTrap(cd);
    }

    private static boolean trapTooMuchCD(int cd) {
        return rc.getActionCooldownTurns() + Math.ceil(Constants.BUILD_COOLDOWN_COST[rc.getLevel(SkillType.BUILD)]) >= cd;
    }

    private static void buildExplosiveTrap(MapLocation me, Direction dir, Direction enemy, int cd) throws GameActionException {
        if (trapTooMuchCD(cd)) return;
        if (rc.canBuild(TrapType.EXPLOSIVE, me.add(dir)) && checkValidTrap(me.add(dir), enemy)) {
            if (rc.canSenseLocation(me.add(dir))) {
                if (!rc.senseMapInfo(me.add(dir)).isWater()) {
                    rc.build(TrapType.EXPLOSIVE, me.add(dir));
                }
            }
        }
    }

    private static void buildStunTrap(MapLocation me, Direction dir, Direction enemy, int cd) throws GameActionException {
        if (trapTooMuchCD(10)) return;
        if (rc.canBuild(TrapType.STUN, me.add(dir)) && checkValidTrap(me.add(dir), enemy)) {
            boolean build = true;
            for (MapLocation adj : Map.getAdjacentLocations(me.add(dir))) {
                if (rc.canSenseLocation(adj)) {
                    if (rc.senseMapInfo(adj).getTrapType() == TrapType.STUN){
                        build = false;
                        break;
                    }
                }
            }
            if (build)
                rc.build(TrapType.STUN, me.add(dir));
        }
    }

    public static void tryWaterTrap(int cd) throws GameActionException {
        if (trapTooMuchCD(cd)) return;
        RobotInfo[] oppRobotInfos = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (RobotInfo opps : oppRobotInfos) {
            if (opps.hasFlag()) {
                MapLocation me = rc.getLocation();
                Direction dir = me.directionTo(opps.getLocation());
                if (rc.canBuild(TrapType.WATER, me.add(dir))) {
                    rc.build(TrapType.WATER, me.add(dir));
                }
                else if (rc.canBuild(TrapType.WATER, me.add(dir.rotateLeft()))) {
                    rc.build(TrapType.WATER, me.add(dir.rotateLeft()));
                }
                else if (rc.canBuild(TrapType.WATER, me.add(dir.rotateRight()))) {
                    rc.build(TrapType.WATER, me.add(dir.rotateRight()));
                }
                else if (rc.canBuild(TrapType.WATER, me)) {
                    rc.build(TrapType.WATER, me);
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
        RobotInfo[] allies = rc.senseNearbyRobots(-1, rc.getTeam());
        AttackTarget bestTarget = null;
        for (RobotInfo enemy : enemies) {
            if (rc.canAttack(enemy.location)){
                AttackTarget at = new AttackTarget(enemy, allies);
                if (at.isBetterThan(bestTarget)) bestTarget = at;
            }
        }
        if (bestTarget != null && rc.canAttack(bestTarget.mloc)) {
            rc.attack(bestTarget.mloc);
        }
    }

    private static void tryHeal() throws GameActionException {
        if(!rc.isActionReady()) return;
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        RobotInfo[] allies = rc.senseNearbyRobots(-1, rc.getTeam());
        RobotInfo[] healTargets = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, rc.getTeam());
        if(rc.isMovementReady()) {
            HealingTarget bestTarget =  null;
            for (RobotInfo r : healTargets) {
                if (rc.canHeal(r.getLocation())) {
                    HealingTarget hl = new HealingTarget(r, enemyRobots);
                    if (hl.isBetterThan(bestTarget)) bestTarget = hl;
                }
            }

            if(bestTarget != null && rc.canHeal(bestTarget.mloc)){
                rc.heal(bestTarget.mloc);
            }
        }
        else {
            if (dontHeal(enemyRobots, allies)) return;
            HealingTarget bestTarget =  null;
            for (RobotInfo r : healTargets) {
                if (rc.canHeal(r.getLocation())) {
                    HealingTarget hl = new HealingTarget(r, enemyRobots);
                    if (hl.isBetterThan(bestTarget)) bestTarget = hl;
                }
            }
            if(bestTarget != null && rc.canHeal(bestTarget.mloc)){
                rc.heal(bestTarget.mloc);
            }
        }
    }


    private static boolean dontHeal(RobotInfo[] enemies, RobotInfo[] allies) throws GameActionException {
        MapLocation me = rc.getLocation();
        for (RobotInfo enemy : enemies) {
            if (enemy.getLocation().isWithinDistanceSquared(me, GameConstants.ATTACK_RADIUS_SQUARED)) {
                return true;
            }
            else {
                boolean allyInFront = false;
                Direction dir = me.directionTo(enemy.getLocation());
                if (rc.canSenseLocation(me.add(dir))) {
                    if (rc.senseMapInfo(me.add(dir)).isWall()) {
                        allyInFront = true;
                    }
                    else {
                        for (RobotInfo ally : allies) {
                            if (ally.getLocation().equals(me.add(dir)) || ally.getLocation().equals(me.add(dir).add(dir))){
                                allyInFront = true;
                                break;
                            }

                        }
                    }
                }
                if (!allyInFront) {
                    return true;
                }
            }

        }
        return false;
    }
}
