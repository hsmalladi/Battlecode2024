package initialrobot;

<<<<<<< HEAD
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.Team;

=======
>>>>>>> main
public class Util {
    static boolean hurt(int h){
        return h < Constants.CRITICAL_HEALTH;
    }
<<<<<<< HEAD

    static Team opponentTeam(RobotController rc) throws GameActionException {
        if (rc.getTeam().equals(Team.A))
            return Team.B;
        return Team.A;
    }
=======
>>>>>>> main
}
