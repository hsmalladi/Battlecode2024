package smurfv2;

import battlecode.common.GameActionException;
import battlecode.common.GlobalUpgrade;

public class GlobalUpgrades extends Globals {

    public static void useGlobalUpgrade() throws GameActionException {
        if(rc.canBuyGlobal(GlobalUpgrade.ATTACK)) {
            rc.buyGlobal(GlobalUpgrade.ATTACK);
            Debug.log("BOUGHT ATTACK UPGRADE");
        }
        else if(rc.canBuyGlobal(GlobalUpgrade.HEALING)) {
            rc.buyGlobal(GlobalUpgrade.HEALING);
            Debug.log("BOUGHT HEALING UPGRADE");
        }
        else if(rc.canBuyGlobal(GlobalUpgrade.CAPTURING)) {
            rc.buyGlobal(GlobalUpgrade.CAPTURING);
            Debug.log("BOUGHT CAPTURING UPGRADE");
        }
    }
}
