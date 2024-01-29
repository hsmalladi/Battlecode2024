package detectstuntraps;


import battlecode.common.GameActionException;


public class BotSetupDuck extends BotDuck {

    final public static int
                    SETUP_EXPLORER_ROLE = 1,
                    SETUP_FLAG_ROLE = 2,
                    SETUP_BUILDER_ROLE = 3;

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

    private static void determineRole() throws GameActionException {
        int type = rc.readSharedArray(Comm.FLAG_SETUP_COMM);
        if (type < 3) {
            myRole = SETUP_FLAG_ROLE;
            flagDuck = type + 1;
            rc.writeSharedArray(Comm.FLAG_SETUP_COMM, flagDuck);
        } else if (type < 5) {
            myRole = SETUP_EXPLORER_ROLE;
            builderDuck = type;
            rc.writeSharedArray(Comm.FLAG_SETUP_COMM, type + 1);
        } else {
            myRole = SETUP_EXPLORER_ROLE;
        }
    }

}
