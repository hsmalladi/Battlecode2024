package sprintv1;

import battlecode.common.*;
public class GlobalUpgrades {

    public static void useGlobalUpgrade(RobotController rc) throws GameActionException {
        if(rc.canBuyGlobal(GlobalUpgrade.ACTION)) {
            rc.buyGlobal(GlobalUpgrade.ACTION);
            System.out.println("BOUGHT ACTION UPGRADE");
        }
        else if(rc.canBuyGlobal(GlobalUpgrade.HEALING)) {
            rc.buyGlobal(GlobalUpgrade.HEALING);
            System.out.println("BOUGHT HEALING UPGRADE");
        }
    }
}
