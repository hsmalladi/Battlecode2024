package stableversion2;

import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class AttackTarget extends Globals {
    int health, attackLvl, healLvl, buildLvl, maxLvl;
    boolean flagHolder = false;
    MapLocation mloc;

    int maxLevel(int attack, int heal, int build) {
        return Math.max(attack, Math.max(heal, build));
    }

    boolean isBetterThan(AttackTarget t){
        if (t == null) return true;
        if (flagHolder && !t.flagHolder) return true;
        if (!flagHolder && t.flagHolder) return false;
        if (health <= t.health) return true;
        if (maxLvl > t.maxLvl) return true;
        return false;
    }

    AttackTarget(RobotInfo r){
        health = r.getHealth();
        mloc = r.getLocation();
        flagHolder = r.hasFlag();
        attackLvl = r.getAttackLevel() + 2; // weigh attack ducks heavier than build/heal
        buildLvl = r.getBuildLevel();
        healLvl = r.getHealLevel() + 1;
        maxLvl = maxLevel(attackLvl, healLvl, buildLvl);
    }

}
