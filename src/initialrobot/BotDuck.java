package initialrobot;

import battlecode.common.*;

public class BotDuck {


    private final RobotController rc;


    public BotDuck(RobotController rc) throws GameActionException {
        this.rc = rc;
        Setup.init(rc);
    }

    void initTurn() throws GameActionException {
        if (!rc.isSpawned()){
             PathFind.resetDuck();
             trySpawn(rc);
        }

        if (rc.isSpawned()) {
            Setup.initTurn(rc);
        }
    }

    void play() throws GameActionException {
        if (rc.isSpawned()) {
            if (RobotPlayer.turnCount <= GameConstants.SETUP_ROUNDS) {
                Setup.run(rc);
            }
            else if (RobotPlayer.turnCount == GameConstants.SETUP_ROUNDS + 1) {
                Setup.exit(rc);
            } else if (RobotPlayer.turnCount > GameConstants.SETUP_ROUNDS && RobotPlayer.flagDuck == 0 ) {
                MainRound.run(rc);
            }
        }
    }

    void endTurn(){

    }

    void trySpawn(RobotController rc) throws GameActionException {
        for (MapLocation loc : Map.allySpawnLocations) {
            if (rc.canSpawn(loc)) {
                rc.spawn(loc);
                break;
            }
        }
    }
}
