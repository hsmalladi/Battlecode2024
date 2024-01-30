package microgod;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class AttackTarget extends Globals {
    int health, attackLvl, healLvl, buildLvl, maxLvl, id;
    boolean flagHolder = false;
    MapLocation mloc;
    int numAllies;
    int maxLevel(int attack, int heal, int build) {
        return Math.max(attack, Math.max(heal, build));
    }

    boolean isBetterThan(AttackTarget t){
        if (t == null) return true;
        if (flagHolder && !t.flagHolder) return true;
        if (!flagHolder && t.flagHolder) return false;
        if (health <= t.health) return true;
        if (numAllies > t.numAllies) return true;
        if (maxLvl > t.maxLvl) return true;
        return id > t.id;
    }



    AttackTarget(RobotInfo r, RobotInfo[] allies){
        health = r.getHealth();
        mloc = r.getLocation();
        flagHolder = r.hasFlag();
        attackLvl = r.getAttackLevel() + 1; // weigh attack ducks heavier than build/heal
        buildLvl = r.getBuildLevel();
        healLvl = r.getHealLevel() + 1;
        id = rc.getID();
        maxLvl = attackLvl + buildLvl + healLvl;
        numAllies = getNumAllies(allies);
    }

    private int getNumAllies(RobotInfo[] allies) {
        int num = 0;
        for (RobotInfo a : allies) {
            if (a.getLocation().isWithinDistanceSquared(mloc, GameConstants.ATTACK_RADIUS_SQUARED)) {
                num++;
            }
        }
        return num;
    }

}
