package microgod2;

import battlecode.common.*;

import static battlecode.common.GameConstants.*;

public class HealMicro extends Globals {

    final int INF = 1000000;
    Direction[] dirs = Direction.values();

    HealMicro(){
    }
    boolean doMicro(){try{
        if (!rc.isMovementReady()) return false;
        RobotInfo[] units = rc.senseNearbyRobots(-1, rc.getTeam());
        boolean hurtAlliesNearby = false;
        for(RobotInfo unit : units){
            if (unit.getHealth() < DEFAULT_HEALTH){
                hurtAlliesNearby = true;
            }
        }
        if(!hurtAlliesNearby) return false;

        HealMicroInfo[] microInfo = new HealMicroInfo[9];
        for (int i = 0; i < 9; ++i) microInfo[i] = new HealMicroInfo(dirs[i]);
        for (RobotInfo unit : units) {
            if (unit.hasFlag() || unit.getHealth() >= DEFAULT_HEALTH) {
                continue;
            }
            microInfo[0].updateAlly(unit);
            microInfo[1].updateAlly(unit);
            microInfo[2].updateAlly(unit);
            microInfo[3].updateAlly(unit);
            microInfo[4].updateAlly(unit);
            microInfo[5].updateAlly(unit);
            microInfo[6].updateAlly(unit);
            microInfo[7].updateAlly(unit);
            microInfo[8].updateAlly(unit);
        }

        HealMicroInfo bestMicro = microInfo[8];
        for (int i = 0; i < 8; ++i) {
            if (microInfo[i].isBetter(bestMicro)) bestMicro = microInfo[i];
        }

        if (bestMicro.dir == Direction.CENTER) return true;

        if (rc.canMove(bestMicro.dir)) {
            rc.setIndicatorString("Healing Micro--Moving back: " + bestMicro.dir);
            rc.move(bestMicro.dir);
            return true;
        }
    }catch(Exception e){
        e.printStackTrace();
    }
        return false;
    }
    class HealMicroInfo{
        Direction dir;
        MapLocation location;
        double HPSGiven = 0;
        int alliesTargeting = 0;
        boolean canMove = true;

        int minDistanceToAlly = INF;

        public HealMicroInfo(Direction dir){
            this.dir = dir;
            this.location = rc.getLocation().add(dir);
            if(dir != Direction.CENTER && !rc.canMove(dir)) canMove = false;
            minDistanceToAlly = INF;
        }
        void updateAlly(RobotInfo unit){
            if (!canMove) return;
            int dist = unit.getLocation().distanceSquaredTo(location);
            if (dist < minDistanceToAlly) minDistanceToAlly = dist;
            if (dist <= ATTACK_RADIUS_SQUARED) HPSGiven += (DEFAULT_HEALTH - unit.getHealth());
            if (dist <= VISION_RADIUS_SQUARED) alliesTargeting += (DEFAULT_HEALTH - unit.getHealth());
        }

        //equal => true
        boolean isBetter(HealMicroInfo M){
            if (HPSGiven > M.HPSGiven) return true;
            if (M.HPSGiven < HPSGiven) return false;

            if (alliesTargeting > M.alliesTargeting) return true;
            if (alliesTargeting < M.alliesTargeting) return false;

            return minDistanceToAlly <= M.minDistanceToAlly;
        }
    }




}