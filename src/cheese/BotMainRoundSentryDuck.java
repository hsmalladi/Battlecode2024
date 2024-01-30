package cheese;

import battlecode.common.Direction;
import battlecode.common.FlagInfo;
import battlecode.common.GameActionException;
import battlecode.common.TrapType;


public class BotMainRoundSentryDuck extends BotSetupFlagDuck {

    public static void play() throws GameActionException {
        buildDefenses();
    }

    public static void buildDefenses() throws GameActionException {
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam());
        if (flags.length > 0) {
            Comm.allyFlagLocs[flagDuck-1] = flags[0].getLocation();
            if (rc.getLocation().equals(Comm.allyFlagLocs[flagDuck-1])) {
                tryTrap();
            }
        }
        flagDuck = 0;
    }

    private static void tryTrap() throws GameActionException {
        if(!rc.isActionReady()) return;

        if (rc.canBuild(TrapType.STUN, rc.getLocation())) {
            rc.build(TrapType.STUN, rc.getLocation());
        }
        Direction dir = rc.getLocation().directionTo(Map.center);

        if (rc.canBuild(TrapType.WATER, rc.getLocation().add(dir))) {
            rc.build(TrapType.WATER, rc.getLocation().add(dir));
        }
        else if (rc.canBuild(TrapType.WATER, rc.getLocation().add(dir.rotateLeft()))) {
            rc.build(TrapType.WATER, rc.getLocation().add(dir.rotateLeft()));
        }
        else if (rc.canBuild(TrapType.WATER, rc.getLocation().add(dir.rotateRight()))) {
            rc.build(TrapType.WATER, rc.getLocation().add(dir.rotateRight()));
        }
    }

}
