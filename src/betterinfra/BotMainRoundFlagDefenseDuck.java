package betterinfra;

import battlecode.common.*;

public class BotMainRoundFlagDefenseDuck extends BotSetupFlagDuck {

    public static void play() throws GameActionException {
        protectFlag();
    }

    public static void protectFlag() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        rc.setIndicatorString("ENEMIES: " + enemies.length);


        if (rc.canWriteSharedArray(flagDuck, enemies.length)) {
            rc.writeSharedArray(flagDuck, enemies.length);
        }


        if (rc.getLocation().equals(exploreLocation)) {
            buildDefenses();
        }
        else {
            pf.moveTowards(exploreLocation);
        }
        tryAttack();
        tryHeal();
        if (micro.doMicro()) return;

        pf.moveTowards(Map.allyFlagLocations[flagDuck-1]);
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
