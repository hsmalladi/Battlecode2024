package microimprovements;

import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class AttackTarget extends Globals {
    int health;
    boolean flagHolder = false;
    MapLocation mloc;

    boolean isBetterThan(AttackTarget t){
        if (t == null) return true;
        if (flagHolder && !t.flagHolder) return true;
        if (!flagHolder && t.flagHolder) return false;
        return health <= t.health;
    }

    AttackTarget(RobotInfo r){
        health = r.getHealth();
        mloc = r.getLocation();
        flagHolder = r.hasFlag();
    }

}
