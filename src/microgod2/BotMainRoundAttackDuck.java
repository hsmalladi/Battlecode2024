package microgod2;

import battlecode.common.*;

import java.util.ArrayList;

import static microgod2.BotSetupExploreDuck.checkValidTrap;


public class BotMainRoundAttackDuck extends BotMainRoundDuck {

    static MapLocation[] prevStunTrap;
    static RobotInfo[] prevStunRobot;

    static RobotInfo[] enemies;
    static MapInfo[] mapInfos;
    static MapLocation[] crumbLocations;
    static MapLocation me;
    static boolean chickenBehavior = false;
    public static void play() throws GameActionException {
        checkChickenBehavior();
        updateVars();
        if (turnCount < 220 && enemies.length == 0) {
            if (crumbLocations.length != 0) {
                retrieveCrumbsMove();
                act(false);
            } else {
                tryMove();
                act(false);
            }

        }
        else {
            act(true);
            tryMove();
            updateVars();
            act(false);
            tryTrap(20);
        }
        updateStunTraps();
    }

    private static void updateVars() throws GameActionException {
        enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        mapInfos = rc.senseNearbyMapInfos(-1);
        crumbLocations = rc.senseNearbyCrumbs(-1);
        me = rc.getLocation();
    }

    private static void act(boolean before) throws GameActionException {
        if (rc.getLevel(SkillType.ATTACK)!=6 || rc.getActionCooldownTurns() > 0)
            tryTrap(10);
        tryAttack();
        tryAttack();
        tryHeal(before);
    }

    static void updateStunTraps() {

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

        if (crumbLocations.length > 0) {
            MapLocation closestCrumb = Map.getClosestLocation(me, crumbLocations);
            if (reachable(closestCrumb)) {
                rc.setIndicatorString("Getting Crumb");
                return closestCrumb;
            }
        }
        return null;
    }

    private static void retrieveCrumbsMove() throws GameActionException {
        //Retrieve all crumb locations within robot vision radius
        if (crumbLocations.length > 0) {
            MapLocation closestCrumb = Map.getClosestLocation(me, crumbLocations);
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

    private static void tryMove() throws GameActionException {
        if (!rc.isMovementReady()) return;


        if (micro.doMicro()) return;
        MapLocation target = getClosestDroppedAllyFlag();
        if (target != null) {
            rc.setIndicatorString("PROTECT DROPPED FlAG IN VISION");
            pf.moveTowards(target);
        }
        target = getClosestVisionFlag();
        if(target != null){
            rc.setIndicatorString("SEE FlAG IN VISION");
            pf.moveTowards(target);
        }

        target = Explore.protectFlagHolder();
        if (target != null) {
            rc.setIndicatorString("PROTECT ALLY FlAG IN VISION");
            pf.moveTowards(target);
        }
        target = getBestTarget();
        if (target != null){
            rc.setIndicatorString("FOUND A GOOD TARGET IN VISION");
            pf.moveTowards(target);
        }
        if (chickenBehavior && healMicro.doMicro()) return;
        target = getTarget();
        rc.setIndicatorLine(rc.getLocation(), target, 255,0,0);
        pf.moveTowards(target);
    }

    private static MapLocation getTarget() throws GameActionException{
        MapLocation target = Explore.getFlagTarget();
        if (target !=  null){
            if (Explore.randomBroadCast == null || (Explore.randomBroadCast != null &&
                    me.distanceSquaredTo(target)
                            < me.distanceSquaredTo(Explore.randomBroadCast)
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
            target = Map.getClosestLocation(me, Map.corners);
            if(target != null){
                if(me.distanceSquaredTo(target) <= 20){
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


    private static MapLocation getBestTarget() {
        MoveTarget bestTarget = null;

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
            if (!flag.isPickedUp() && me.distanceSquaredTo(flag.getLocation()) < dist)
            {
                closestFlag = flag.getLocation();
                dist = me.distanceSquaredTo(flag.getLocation());
            }
        }
        return closestFlag;
    }

    // Comm.AllyFlagLocations
    private static MapLocation getClosestDroppedAllyFlag()  throws GameActionException {
        int dist = 10000;
        MapLocation[] initialAllyFlagLocations = Comm.allyFlagLocs;
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam());

        MapLocation closestDroppedAllyFlag = null;
        for (FlagInfo flag: flags){
            boolean isInitialLoc = false;
            for(MapLocation initialFlagLoc: initialAllyFlagLocations){
                if(initialFlagLoc.equals(flag.getLocation())){
                    isInitialLoc = true;
                }
            }
            if(!isInitialLoc){
                if(me.distanceSquaredTo(flag.getLocation()) < dist){
                    closestDroppedAllyFlag = flag.getLocation();
                    dist = me.distanceSquaredTo(flag.getLocation());
                }
            }
        }
        return closestDroppedAllyFlag;
    }


    public static void tryTrap(int cd) throws GameActionException {

        if (!rc.isActionReady()) return;
        if (rc.getCrumbs() < 160) return;
        if (trapTooMuchCD(cd)) return;
        if (enemies.length == 0) return;
        Direction dir = me.directionTo(closestEnemy(enemies));
        if (rc.getLevel(SkillType.BUILD) > 3) {
            if (enemies.length >= 5) {
                rc.setIndicatorString("BUILDING TRAPS");
                buildExplosiveTrap(me, dir, dir, cd);
                buildExplosiveTrap(me, dir.rotateLeft(), dir, cd);
                buildExplosiveTrap(me, dir.rotateRight(), dir, cd);
            }
        }
        if (enemies.length >= 3) {
            buildStunTrap(me, dir, dir, cd);
            buildStunTrap(me, dir.rotateLeft(), dir, cd);
            buildStunTrap(me, dir.rotateRight(), dir, cd);
        }
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
        if (trapTooMuchCD(cd)) return;
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
        for (RobotInfo opps : enemies) {
            if (opps.hasFlag()) {
                Direction dir = me.directionTo(opps.getLocation());
                buildWaterTrap(me, dir, cd);
                buildWaterTrap(me, dir.rotateLeft(), cd);
                buildWaterTrap(me, dir.rotateRight(), cd);
                buildWaterTrap(me, Direction.CENTER, cd);
            }
        }
    }

    private static void buildWaterTrap(MapLocation me, Direction dir, int cd) throws GameActionException {
        if (trapTooMuchCD(cd)) return;
        if (rc.canBuild(TrapType.WATER, me.add(dir))) {
            boolean build = true;
            for (MapLocation adj : Map.getAdjacentLocations(me.add(dir))) {
                if (rc.canSenseLocation(adj)) {
                    if (rc.senseMapInfo(adj).getTrapType() == TrapType.WATER){
                        build = false;
                        break;
                    }
                }
            }
            if (build)
                rc.build(TrapType.WATER, me.add(dir));
        }
    }

    public static MapLocation closestEnemy(RobotInfo[] robotInfos) {
        MapLocation[] mapLocations = new MapLocation[robotInfos.length];
        for (int i = 0; i < robotInfos.length; i++) {
            mapLocations[i] = robotInfos[i].getLocation();
        }

        return Map.getClosestLocation(me, mapLocations);
    }

    public static void tryAttack() throws GameActionException {
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

    private static void tryHeal(boolean beforeTryMove) throws GameActionException {
        if(!rc.isActionReady()) return;

        RobotInfo[] healTargets = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, rc.getTeam());

        if (enemies.length > 0) {
            if (rc.getLevel(SkillType.ATTACK) > rc.getLevel(SkillType.HEAL)) {
                return;
            }
            if (rc.getLevel(SkillType.HEAL) < 4 || rc.getLevel(SkillType.BUILD) < 4) {
                if (rc.getHealth() >= 300) {
                    if (dontHeal(enemies, healTargets))
                        return;
                }
            }
        }


        HealingTarget bestTarget =  null;
        for (RobotInfo r : healTargets) {
            if (rc.canHeal(r.getLocation())) {
                HealingTarget hl = new HealingTarget(r, enemies);
                if (hl.isBetterThan(bestTarget)) bestTarget = hl;
            }
        }
        if(bestTarget != null && rc.canHeal(bestTarget.mloc)){
            rc.heal(bestTarget.mloc);
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
                if (rc.canSenseLocation(me.add(dir)) &&  rc.canSenseLocation(me.add(dir).add(dir))) {
                    if (rc.senseMapInfo(me.add(dir)).isWall() || rc.senseMapInfo(me.add(dir).add(dir)).isWall()) {
                        allyInFront = true;
                    }
                    else {
                        for (RobotInfo ally : allies) {
                            if (ally.getLocation().equals(me.add(dir))
                                    || ally.getLocation().equals(me.add(dir).add(dir))) {
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
    public static void checkChickenBehavior(){
        if (!chickenBehavior && rc.getHealth() <= 400) chickenBehavior = true;
        if (chickenBehavior && rc.getHealth() >= 800) chickenBehavior = false;
    }
}
