package builder;

import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class MoveTarget extends Globals {
    int health;
    int priority;
    MapLocation mloc;

    boolean isBetterThan(MoveTarget t){
        if(priority <= 1) return false;
        if (t == null) return true;
        if (t.priority <= 1) return true;
        if(priority > t.priority) return true;
        if(priority < t.priority) return true;
        return health <= t.health;
    }

    MoveTarget(RobotInfo r){
        this.health = r.getHealth();
        this.mloc = r.getLocation();
        if(r.hasFlag()) {
            priority = 2;
        }
        else {
            priority = 1;
        }
    }
}


