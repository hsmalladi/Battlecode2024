package betterinfra;

import battlecode.common.*;


public class BotMainRoundSentryDuck extends BotSetupFlagDuck {

    public static void play() throws GameActionException {
        protectFlag();
    }

    public static void protectFlag() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        rc.setIndicatorString("ENEMIES: " + enemies.length);


        if (rc.canWriteSharedArray(flagDuck, enemies.length)) {
            rc.writeSharedArray(flagDuck, enemies.length);
        }
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam());
        tryAttack();
        tryHeal();
        if (micro.doMicro()) return;
        if (flags.length > 0) {
            Comm.allyFlagLocs[flagDuck-1] = flags[0].getLocation();
            if (!rc.getLocation().equals(Comm.allyFlagLocs[flagDuck-1])) {
                pf.moveTowards(Comm.allyFlagLocs[flagDuck - 1]);
            }
            else {
                tryTrap();
            }
        }
        else {
            pf.moveTowards(Map.flagSpawnLocations[flagDuck-1]);
        }
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

    private static void tryTrap() throws GameActionException {
        if(!rc.isActionReady()) return;

        if (rc.canBuild(TrapType.STUN, rc.getLocation())) {
            rc.build(TrapType.STUN, rc.getLocation());
        }
        MapLocation[] adj = Map.getAdjacentLocations(rc.getLocation());

        for (MapLocation ad : adj) {
            if (rc.canBuild(TrapType.EXPLOSIVE, ad)) {
                rc.build(TrapType.EXPLOSIVE, ad);
            }
        }

    }

}
