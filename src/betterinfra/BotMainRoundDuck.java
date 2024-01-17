package betterinfra;

import battlecode.common.*;

import java.util.Arrays;
import java.util.List;

public class BotMainRoundDuck extends BotDuck {

    private static final int
                    ATTACK_DUCK = 1,
                    FLAG_CARRIER = 2;

    private static int myRole = -1;

    public static boolean goingToFlag = true;

    public static void play() throws GameActionException {
        GlobalUpgrades.useGlobalUpgrade();
        if (!rc.isSpawned()) {
            smartSpawn();
        }
        if (rc.isSpawned()) {
            tryFlagPickUp();
            determineRole();
            if (myRole == ATTACK_DUCK) {
                BotMainRoundAttackDuck.play();
            } else {
                BotMainRoundFlagDuck.play();
            }
        }
    }

    private static void determineRole() {
        if (rc.hasFlag()) {
            myRole = FLAG_CARRIER;
        } else {
            myRole = ATTACK_DUCK;
        }
    }

    private static void tryFlagPickUp() throws GameActionException {
        for (FlagInfo loc : rc.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED)) {
            if (rc.canPickupFlag(loc.getLocation())) {
                rc.pickupFlag(loc.getLocation());
                goingToFlag = false;
                rc.writeSharedArray(Communication.ENEMY_FLAG_HELD, 1);
                break;
            }
        }
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
