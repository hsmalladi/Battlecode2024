package betterinfra;


import battlecode.common.FlagInfo;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;


public class BotSetupDuck extends BotDuck {

    final public static int
                    SETUP_EXPLORER_ROLE = 1,
                    SETUP_FLAG_ROLE = 2;

    private static int myRole = -1;

    public static void play() throws GameActionException {
        if (myRole == -1) {
            determineRole();
        }
        if (myRole == SETUP_EXPLORER_ROLE) {
            BotSetupExploreDuck.play();
        } else {
            BotSetupFlagDuck.play();
        }
    }

    public static void exit() throws GameActionException {
        if (myRole == SETUP_EXPLORER_ROLE){
            BotSetupExploreDuck.exit();
        }
    }




    private static void clearSharedArray() throws GameActionException {
        for (int i = 0; i < 4; i++) {
            if (rc.canWriteSharedArray(i, 0)) {
                rc.writeSharedArray(i, 0);
            }
        }
    }

    private static void determineRole() throws GameActionException {
        int type = rc.readSharedArray(Communication.FLAG_SETUP_COMM);
        if (type < 3) {
            myRole = SETUP_FLAG_ROLE;
            flagDuck = type + 1;
            rc.writeSharedArray(Communication.FLAG_SETUP_COMM, flagDuck);
        } else {
            myRole = SETUP_EXPLORER_ROLE;
        }
    }

}
