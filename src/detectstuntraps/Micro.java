package detectstuntraps;

import battlecode.common.*;

import static battlecode.common.GameConstants.*;

public class Micro extends Globals {

    final int INF = 1000000;
    Direction[] dirs = Direction.values();
    boolean shouldPlaySafe = false;
    boolean alwaysInRange = false;
    boolean hurt = false;
    static int myRange;
    static int myVisionRange;
    static double myDPS;
    static double myAttackCooldown;

    static double myHPS;

    static double myHealCooldown;

    boolean severelyHurt = false;

    double baseDamage = Constants.BASE_DAMAGE;

    double baseHeal = Constants.BASE_HEAL;

    double[] DPS = new double[]{baseDamage,baseDamage*1.05,baseDamage*1.07,baseDamage*1.1,baseDamage*1.30,baseDamage*1.35, baseDamage*1.6};

    double[] HPS = new double[]{baseHeal, baseHeal*1.03, baseHeal*1.05, baseHeal*1.07, baseHeal*1.10, baseHeal*1.15, baseHeal*1.25};
    double[] ATTACK_COOLDOWN_COST = new double[]{ATTACK_COOLDOWN, ATTACK_COOLDOWN*0.95, ATTACK_COOLDOWN*0.93, ATTACK_COOLDOWN*0.9, ATTACK_COOLDOWN*0.8, ATTACK_COOLDOWN*0.65, ATTACK_COOLDOWN * 0.4};
    double[] HEAL_COOLDOWN_COST = new double[]{HEAL_COOLDOWN, HEAL_COOLDOWN*0.95, HEAL_COOLDOWN*0.9,HEAL_COOLDOWN*0.85, HEAL_COOLDOWN*0.85, HEAL_COOLDOWN*0.85, HEAL_COOLDOWN* 0.75};

    static boolean opponentAttackUpgrade;
    static boolean opponentHealingUpgrade;

    Micro(){
        GlobalUpgrade[] opponentGlobalUpgrades = rc.getGlobalUpgrades(rc.getTeam().opponent());

        for (GlobalUpgrade upgrade: opponentGlobalUpgrades){
            if (upgrade.equals(GlobalUpgrade.ATTACK)){
                opponentAttackUpgrade = true;
            } else if(upgrade.equals(GlobalUpgrade.HEALING)){
                opponentHealingUpgrade = true;
            }
        }

        myRange = ATTACK_RADIUS_SQUARED;
        myVisionRange = VISION_RADIUS_SQUARED;
        myDPS = rc.getAttackDamage();
        myHPS = rc.getHealAmount();
        myAttackCooldown = ATTACK_COOLDOWN_COST[rc.getLevel(SkillType.ATTACK)];
        myHealCooldown = HEAL_COOLDOWN_COST[rc.getLevel(SkillType.HEAL)];

    }
    static double currentDPS = 0;

    static double currentOpponentDPS = 0;

    static double currentHPS = 0;

    static double currentOpponentHPS = 0;
    static boolean canAttack;

    static MapLocation[] stunTrapsWentOff() throws GameActionException {
        if (BotMainRoundAttackDuck.prevStunTrap == null || BotMainRoundAttackDuck.prevStunTrap.length == 0) {
            return null;
        }
        MapLocation[] ret = new MapLocation[BotMainRoundAttackDuck.prevStunTrap.length];
        int i =0;
        for (MapLocation stun : BotMainRoundAttackDuck.prevStunTrap) {
            if (rc.canSenseLocation(stun)) {
                MapInfo loc = rc.senseMapInfo(stun);
                if (loc.getTrapType() != TrapType.STUN) {
                    ret[i] = stun;
                    i++;
                }
            }
        }
        return ret;
    }

    boolean doMicro(){try{
        if (!rc.isMovementReady()) return false;
        shouldPlaySafe = false;
        severelyHurt = Util.hurt(rc.getHealth());
        RobotInfo[] units = rc.senseNearbyRobots(myVisionRange, rc.getTeam().opponent());
        if(units.length == 0) return false;
        canAttack = rc.isActionReady();

        int uIndex = units.length;
        if (uIndex-- > 0){
            RobotInfo r = units[uIndex];
            if (!r.hasFlag()){
                shouldPlaySafe = true;
            }
        }
        if (!shouldPlaySafe) return false;


        alwaysInRange = false;
        if(!canAttack) alwaysInRange = true;
        if(severelyHurt) alwaysInRange = true;

        MicroInfo[] microInfo = new MicroInfo[9];
        for (int i = 0; i < 9; ++i) microInfo[i] = new MicroInfo(dirs[i]);
        MapLocation[] stuns = stunTrapsWentOff();
        for (RobotInfo unit : units) {
            if(unit.hasFlag()){
                continue;
            }
            boolean stunned = false;
            if (stuns != null) {
                for (MapLocation trap : stuns) {

                    if (trap != null && trap.isWithinDistanceSquared(unit.getLocation(), 13)) {
                        stunned = true;
                        break;
                    }
                }
            }

            if (stunned) {
                continue;
            }
            currentOpponentDPS = DPS[unit.getAttackLevel()] / ATTACK_COOLDOWN_COST[unit.getAttackLevel()];
            if(opponentAttackUpgrade){
                currentOpponentDPS += Constants.GLOBAL_ATTACK_UPGRADE;
            }
            currentOpponentHPS = HPS[unit.getHealLevel()] / HEAL_COOLDOWN_COST[unit.getHealLevel()];
            if(opponentHealingUpgrade){
                currentOpponentHPS += Constants.GLOBAL_HEALING_UPGRADE;
            }
            microInfo[0].updateEnemy(unit);
            microInfo[1].updateEnemy(unit);
            microInfo[2].updateEnemy(unit);
            microInfo[3].updateEnemy(unit);
            microInfo[4].updateEnemy(unit);
            microInfo[5].updateEnemy(unit);
            microInfo[6].updateEnemy(unit);
            microInfo[7].updateEnemy(unit);
            microInfo[8].updateEnemy(unit);
        }

        units = rc.senseNearbyRobots(myVisionRange, rc.getTeam());
        for (RobotInfo unit : units) {
            if (unit.hasFlag()){
                continue;
            }
            currentDPS = myDPS;
            currentHPS = myHPS;
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


        MicroInfo bestMicro = microInfo[8];
        for (int i = 0; i < 8; ++i) {
            if (microInfo[i].isBetter(bestMicro)) bestMicro = microInfo[i];
        }

        if (bestMicro.dir == Direction.CENTER) return true;

        if (rc.canMove(bestMicro.dir)) {
            rc.setIndicatorString("Moving back: " + bestMicro.dir);
            rc.move(bestMicro.dir);
            return true;
        }

    }catch(Exception e){
        e.printStackTrace();
    }
        return false;
    }


    class MicroInfo{
        Direction dir;
        MapLocation location;
        int minDistanceToEnemy = INF;
        double DPSreceived = 0;
        double enemiesTargeting = 0;
        double alliesTargeting = 0;
        boolean canMove = true;

        public MicroInfo(Direction dir){
            this.dir = dir;
            this.location = rc.getLocation().add(dir);
            if(dir != Direction.CENTER && !rc.canMove(dir)) canMove = false;
            else{
                if(!hurt){
                    if(canAttack){
                        this.DPSreceived -= myDPS / myAttackCooldown;
                        this.alliesTargeting += myDPS / myAttackCooldown;
                    }
                    minDistanceToEnemy = VISION_RADIUS_SQUARED;
                } else minDistanceToEnemy = INF;
            }
        }

        void updateEnemy(RobotInfo unit){
            if(!canMove) return;
            int dist = unit.getLocation().distanceSquaredTo(location);
            if (dist < minDistanceToEnemy)  minDistanceToEnemy = dist;
            if (dist <= ATTACK_RADIUS_SQUARED) DPSreceived += currentOpponentDPS;
            if (dist <= VISION_RADIUS_SQUARED) enemiesTargeting += currentOpponentDPS;
        }

        void updateAlly(RobotInfo unit){
            if (!canMove) return;
            int dist = unit.getLocation().distanceSquaredTo(location);
            if (dist <= ATTACK_RADIUS_SQUARED) DPSreceived -= currentHPS;
            if (dist <= VISION_RADIUS_SQUARED) alliesTargeting += currentDPS;
        }

        int safe(){
            if (!canMove) return -1;
            if (DPSreceived > 0) return 0;
            if (enemiesTargeting > alliesTargeting) return 1;
            return 2;
        }

        boolean inRange(){
            if (alwaysInRange) return true;
            return minDistanceToEnemy <= myRange;
        }

        //equal => true
        boolean isBetter(MicroInfo M){

            if (safe() > M.safe()) return true;
            if (safe() < M.safe()) return false;

            if (inRange() && !M.inRange()) return true;
            if (!inRange() && M.inRange()) return false;

//            if(!severelyHurt){
//                if (alliesTargeting > M.alliesTargeting) return true;
//                if (alliesTargeting < M.alliesTargeting) return false;
//            }

            if (inRange()) return minDistanceToEnemy >= M.minDistanceToEnemy;
            else return minDistanceToEnemy <= M.minDistanceToEnemy;
        }
    }


}