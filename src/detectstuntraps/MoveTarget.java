package detectstuntraps;

import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class MoveTarget extends Globals {
    int health;
    int priority;
    MapLocation mloc;

    boolean isBetterThan(MoveTarget t){
        if (t == null) return true;
        if(priority > t.priority) return true;
        if(priority < t.priority) return false;
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


