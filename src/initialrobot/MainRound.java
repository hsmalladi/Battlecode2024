package initialrobot;

import battlecode.common.*;

public class MainRound {
    private static final int EXPLORE_ROUNDS = 150;

    public static void init(RobotController rc) throws GameActionException {
        AttackDuck.init(rc);
    }

    public static void initTurn(RobotController rc) throws GameActionException {

    }
    public static void run(RobotController rc) throws GameActionException {

    }



    public static void exit(RobotController rc) throws GameActionException {

    }

    //potential improvement: communicate who is getting crumb, so not all ducks go to the same crumb
    private static void explore(RobotController rc) throws GameActionException {
        RobotPlayer.isExploring = true;

        if (RobotPlayer.isExploring) {
            if (rc.isMovementReady()) {
                PathFind.moveTowards(rc, RobotPlayer.exploreLocation);
            }
        }
    }


}
