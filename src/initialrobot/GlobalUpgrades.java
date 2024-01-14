package initialrobot;

import battlecode.common.*;
public class GlobalUpgrades {

    public static void useGlobalUpgrade(RobotController rc) throws GameActionException {
        if(rc.canBuyGlobal(GlobalUpgrade.ACTION)) {
            rc.buyGlobal(GlobalUpgrade.ACTION);
        } else if(rc.canBuyGlobal(GlobalUpgrade.HEALING)) {
            rc.canBuyGlobal(GlobalUpgrade.HEALING);
        }
    }
}
