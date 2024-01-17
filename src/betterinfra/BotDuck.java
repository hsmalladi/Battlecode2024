package betterinfra;

import battlecode.common.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class BotDuck extends Globals {


    public static int flagDuck = 0;
    public static PathFind pf = null;


    public static void initDuck() throws GameActionException {
        if (pf == null) {
            pf = new PathFind();
        } else {
            pf.resetDuck();
        }
        if (turnCount < GameConstants.SETUP_ROUNDS) {
            trySpawn();
        }
        else {
            smartSpawn();
        }
    }

    public static void loop() throws GameActionException {
        while (true) {
            try {
                turnCount += 1;
//                if (!rc.isSpawned()) {
//                    initDuck();
//                }
                play();
            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                Clock.yield();
            }
        }
    }

    public static void play() throws GameActionException {
        if (turnCount <= GameConstants.SETUP_ROUNDS) {
            BotSetupDuck.play();
        }
        else {
            BotMainRoundDuck.play();
        }
    }

//    public static void endTurn() throws GameActionException {
//        if (turnCount == GameConstants.SETUP_ROUNDS) {
//            Setup.exit(rc);
//        }
//    }

    public static boolean trySpawn() throws GameActionException {
        for (MapLocation loc : Map.allySpawnLocations) {
            if (rc.canSpawn(loc)) {
                rc.spawn(loc);
                return true;
            }
        }
        return false;
    }

    public static void smartSpawn() throws GameActionException {
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
                trySpawn();
            }
        }
    }
}
