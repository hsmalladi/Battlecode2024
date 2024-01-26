package mergedbuilderescape;

import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class HealingTarget extends Globals {
    int health, attackLvl, healLvl, buildLvl, maxLvl, score;
    boolean flagHolder = false;
    MapLocation mloc;

    int maxLevel(int attack, int heal, int build) {
        return Math.max(attack, Math.max(heal, build));
    }

    int getScore(int maxLvl, int hp) { //higher scoring ducks are healed
        return maxLvl * 100 + (800 - hp);
    }

    boolean isBetterThan(HealingTarget t) {
        if (t == null) return true;
        if (flagHolder && !t.flagHolder) return true;
        if (!flagHolder && t.flagHolder) return false;
        if (health <= t.health) return true;
        if (maxLvl > t.maxLvl) return true;
//        if (score > t.score) return true;
        return false;
    }



    HealingTarget(RobotInfo r){
        health = r.getHealth();
        mloc = r.getLocation();
        flagHolder = r.hasFlag();
        attackLvl = r.getAttackLevel() + 2; // weigh attack ducks heavier than build/heal
        buildLvl = r.getBuildLevel();
        healLvl = r.getHealLevel() + 1;
        maxLvl = maxLevel(attackLvl, healLvl, buildLvl);
        score = getScore(maxLvl, health);
    }
}
