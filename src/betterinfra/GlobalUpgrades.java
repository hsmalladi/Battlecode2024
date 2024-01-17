package betterinfra;

import battlecode.common.GameActionException;
import battlecode.common.GlobalUpgrade;

public class GlobalUpgrades extends Globals {

    public static void useGlobalUpgrade() throws GameActionException {
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