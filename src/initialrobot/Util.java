package initialrobot;


import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.Team;


public class Util {
    static boolean hurt(int h){
        return h < Constants.CRITICAL_HEALTH;
    }

}
