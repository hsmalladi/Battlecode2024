package sprintv1;

import battlecode.common.*;

import java.util.Arrays;
import java.util.List;

public class BotDuck {


    private final RobotController rc;


    public BotDuck(RobotController rc) throws GameActionException {
        this.rc = rc;
        Setup.init(rc);
        MainRound.init(rc);
    }

    void initTurn() throws GameActionException {
        if (!rc.isSpawned()){
             PathFind.resetDuck();
             if (RobotPlayer.turnCount < GameConstants.SETUP_ROUNDS) {
                 trySpawn(rc);
             }
             else {
                 smartSpawn(rc);
             }

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
            else if (RobotPlayer.flagDuck == 0) {
                MainRound.run(rc);
            }
            else {
                FlagDuck.protectFlag(rc);
            }
        }
    }

    void endTurn() throws GameActionException {
        if (RobotPlayer.turnCount == GameConstants.SETUP_ROUNDS) {
            Setup.exit(rc);
        }
    }

    void trySpawn(RobotController rc) throws GameActionException {
        for (MapLocation loc : Map.allySpawnLocations) {
            if (rc.canSpawn(loc)) {
                rc.spawn(loc);
                break;
            }
        }
    }

    void smartSpawn(RobotController rc) throws GameActionException {
        for (int i = 1; i < 4; i++) {
            int numEnemies = rc.readSharedArray(i);
            if (numEnemies >= 1) {
                List<MapLocation> sorted = Arrays.asList(Map.allySpawnLocations);
                Map.sortCoordinatesByDistance(sorted, Map.flagLocations[i-1]);
                for (MapLocation loc : sorted) {
                    if (rc.canSpawn(loc)) {
                        rc.spawn(loc);
                        break;
                    }
                }
            }
            else {
                trySpawn(rc);
            }
        }
    }
}
