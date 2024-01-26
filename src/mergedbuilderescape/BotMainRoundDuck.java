package mergedbuilderescape;

import battlecode.common.FlagInfo;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;

import java.util.Arrays;
import java.util.List;

public class BotMainRoundDuck extends BotDuck {

    private static final int
            ATTACK_DUCK = 1,
            FLAG_CARRIER = 2,
            FLAG_DEFENSE_DUCK = 3;

    private static int myRole = -1;

    public static int myFlagHolding = -1;
    static boolean amHoldingFlag = false;
    private static int roundDied = -1;


    public static boolean goingToFlag = true;

    public static void play() throws GameActionException {
        GlobalUpgrades.useGlobalUpgrade();
        tryFlagDropOff();
        if (!rc.isSpawned()) {
            if (flagDuck != 0) {
                //System.out.println("DIED ENEMY PROLLY HAS OUR FLAG" + rc.readSharedArray(flagDuck+50));
                rc.writeSharedArray(flagDuck + 50, rc.readSharedArray(flagDuck + 50) +  1);
            }
            smartSpawn();
        }
        if (rc.isSpawned()) {
            if (Comm.needSymmetryReport && rc.canWriteSharedArray(0, 0)) {
                Comm.reportSym();
                Comm.commit_write();
            }
            MapRecorder.recordSym(500);
            tryFlagPickUp();
            determineRole();
            if (myRole == ATTACK_DUCK) {
                BotMainRoundAttackDuck.play();
            } else if (myRole == FLAG_CARRIER){
                BotMainRoundFlagDuck.play();
            } else {
                BotMainRoundSentryDuck.play();
            }
        }
    }

    private static void determineRole() {
        if (flagDuck != 0) {
            myRole = FLAG_DEFENSE_DUCK;
        } else if (rc.hasFlag() || escaping) {
            myRole = FLAG_CARRIER;
        } else {
            myRole = ATTACK_DUCK;
        }
    }

    private static void tryFlagDropOff() throws GameActionException {
        try {
            if (!rc.isSpawned() && escaping) {
                escaping = false;

            }
            else if (!rc.isSpawned() && amHoldingFlag && !rc.hasFlag()) {
                amHoldingFlag = false;
                goingToFlag = true;
                roundDied = rc.getRoundNum();
                Debug.log("I DIED HOLDING FLAG");
            }
            else if (!rc.hasFlag() && amHoldingFlag) {
                amHoldingFlag = false;
                goingToFlag = true;
                Comm.updateFlagInfo(null, false, myFlagHolding);
                Debug.log("DROPPED OFF FLAG " + myFlagHolding);
            }
            if (rc.getRoundNum() == roundDied + 5) {
                Comm.updateFlagInfo(Comm.enemyFlagsInitial[myFlagHolding-Comm.ENEMY_FLAG_FIRST], false, myFlagHolding);
                Debug.log("RESETTING FLAG LOCATION");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Debug.log("HELLO HELLO HELLO HELLO");
        }
    }



    private static void tryFlagPickUp() throws GameActionException {
        if (escaping)
            return;
        for (FlagInfo loc : rc.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED)) {
            if (rc.canPickupFlag(loc.getLocation())) {
                rc.pickupFlag(loc.getLocation());
                myFlagHolding = Comm.flagIDToIdx(loc.getID(), rc.getTeam().opponent());
                Comm.updateFlagInfo(rc.getLocation(), true, myFlagHolding);
                Debug.log("PICKED UP FLAG " + myFlagHolding);
                goingToFlag = false;
                amHoldingFlag = true;
                rc.writeSharedArray(Comm.ENEMY_FLAG_HELD, 1);
                break;
            }
        }
    }

    public static void smartSpawn() throws GameActionException {
        for (int i = 1; i < 4; i++) {
            int numEnemies = rc.readSharedArray(i);
            if (numEnemies >= 1) {
                List<MapLocation> sorted = Arrays.asList(Map.allySpawnLocations);
                Map.sortCoordinatesByDistance(sorted, Map.allyFlagLocations[i-1]);
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
