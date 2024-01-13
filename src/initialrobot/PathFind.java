package initialrobot;

import battlecode.common.*;

import java.util.LinkedList;
import java.util.Queue;

public class PathFind {
    static Direction direction;

    public static void moveTowards(RobotController rc, MapLocation location) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(location);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
        else if (rc.canFill(rc.getLocation().add(dir))) {
            rc.fill(rc.getLocation().add(dir));
        }
        else {
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    break;
                }
                else {
                    dir = dir.rotateLeft();
                }
            }
        }

    }

    public static void random(RobotController rc) throws GameActionException {
        if (rc.isMovementReady()) {
            if (direction != null && rc.canMove(direction)) {
                rc.move(direction);
            }
            else {
                direction = Direction.allDirections()[RobotPlayer.rng.nextInt(8)];
            }
        }
    }
}