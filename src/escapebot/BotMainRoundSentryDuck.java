package escapebot;

import battlecode.common.*;


public class BotMainRoundSentryDuck extends BotSetupFlagDuck {

    public static void play() throws GameActionException {
        if (alertEnemyHasOurFlag()) {
            Debug.log("ENEMY HAS OUR FLAG" + rc.readSharedArray(flagDuck+50));
            //51, 52, 53
            if (rc.readSharedArray(flagDuck + 50) == 0) {
                rc.writeSharedArray(flagDuck + 50, 1);
            }
            else {
                rc.writeSharedArray(flagDuck + 50, rc.readSharedArray(flagDuck + 50) +  1);
            }

        }
        protectFlag();
    }

    public static void protectFlag() throws GameActionException {
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
                flagDuck = 0;
            }
        }
        else {
            pf.moveTowards(Comm.allyFlagLocs[flagDuck-1]);
            if (rc.readSharedArray(flagDuck + 50) >= 50) {
                flagDuck = 0;
            }
        }
    }

    private static boolean alertEnemyHasOurFlag() throws GameActionException {
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam());

        if (rc.canSenseLocation(Comm.allyFlagLocs[flagDuck-1])) {
            for (FlagInfo f : flags) {
                if (f.getLocation().equals(Comm.allyFlagLocs[flagDuck-1])) {
                    rc.writeSharedArray(flagDuck + 50, 0);
                    return false;
                }
            }
        }
        else {
            return false;
        }
        return true;
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
        Direction dir = rc.getLocation().directionTo(Map.center);

        if (rc.canBuild(TrapType.WATER, rc.getLocation().add(dir))) {
            rc.build(TrapType.WATER, rc.getLocation().add(dir));
        }


    }

}
