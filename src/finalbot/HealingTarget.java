package finalbot;

import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class HealingTarget extends Globals {
    int health, attackLvl, healLvl, buildLvl, maxLvl, id;
    boolean flagHolder = false;
    MapLocation mloc;

    int numEnemies = 0;

    boolean isBetterThan(HealingTarget t) {
        if (t == null) return true;
        if (flagHolder && !t.flagHolder) return false;
        if (!flagHolder && t.flagHolder) return true;
        if (numEnemies > t.numEnemies) return true;
        if (health <= t.health) return true;
        if (maxLvl > t.maxLvl) return true;
        return id < t.id;
    }




    HealingTarget(RobotInfo r, RobotInfo[] enemies){
        health = r.getHealth();
        mloc = r.getLocation();
        flagHolder = r.hasFlag();
        attackLvl = r.getAttackLevel() + 1; // weigh attack ducks heavier than build/heal
        buildLvl = r.getBuildLevel();
        healLvl = r.getHealLevel() + 1;
        id = rc.getID();
        maxLvl = attackLvl + buildLvl + healLvl;
        numEnemies = getNumEnemies(enemies);
    }

    private int getNumEnemies(RobotInfo[] enemies) {
        int num = 0;
        for (RobotInfo e : enemies) {
            if (e.getLocation().isWithinDistanceSquared(mloc, 12)) {
                num++;
            }
        }
        return num;
    }
}
