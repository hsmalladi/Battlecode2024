package smurf;

import battlecode.common.*;

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
        } else if (rc.hasFlag()) {
            myRole = FLAG_CARRIER;
        } else {
            myRole = ATTACK_DUCK;
        }
    }

    private static void tryFlagDropOff() throws GameActionException {
        try {
            if (!rc.isSpawned() && amHoldingFlag && !rc.hasFlag()) {
                amHoldingFlag = false;
                goingToFlag = true;
                roundDied = rc.getRoundNum();
                System.out.println("I DIED HOLDING FLAG");
            }
            else if (!rc.hasFlag() && amHoldingFlag) {
                amHoldingFlag = false;
                goingToFlag = true;
                Comm.updateFlagInfo(null, false, myFlagHolding);
                System.out.println("DROPPED OFF FLAG " + myFlagHolding);
            }
            if (rc.getRoundNum() == roundDied + 4) {
                Comm.updateFlagInfo(null, false, myFlagHolding);
                System.out.println("RESETTING FLAG LOCATION");
            }
        } catch (Exception e) {
            System.out.println("HELLO HELLO HELLO HELLO");
        }
    }



    private static void tryFlagPickUp() throws GameActionException {
        for (FlagInfo loc : rc.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED)) {
            if (rc.canPickupFlag(loc.getLocation())) {
                myFlagHolding = Comm.flagIDToIdx(loc.getID(), rc.getTeam().opponent());
                rc.pickupFlag(loc.getLocation());
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
