package escapebot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;

public class BotDuck extends Globals {


    public static int flagDuck = 0;

    public static int builderDuck = 0;
    public static PathFind pf = null;


    public static void loop() throws GameActionException {
        while (true) {
            try {
                if (pf == null)
                    pf = new PathFind();
                if (rc.getRoundNum() % 100 == 0){
                    Explore.exploredBroadcast = false;
                }
                Comm.turn_starts();
                turnCount += 1;
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
        if (turnCount < GameConstants.SETUP_ROUNDS) {
            BotSetupDuck.play();
        } else if (turnCount == GameConstants.SETUP_ROUNDS) {
            Comm.readFlagLocation();
            BotSetupDuck.exit();
        } else {
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
}
