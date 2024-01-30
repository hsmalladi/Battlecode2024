package microgod2;

import battlecode.common.Direction;


public class Constants {
    static double BASE_DAMAGE = 150;

    static double BASE_HEAL = 80;

    static double GLOBAL_ATTACK_UPGRADE = 60;

    static double GLOBAL_HEALING_UPGRADE = 50;

    final static int CRITICAL_HEALTH = 150;

    final static int EXPLORE_ROUNDS = 125;

    final static int BUILD_COOLDOWN = 5;
    final static double[] BUILD_COOLDOWN_COST = new double[]{BUILD_COOLDOWN, BUILD_COOLDOWN*0.95, BUILD_COOLDOWN*0.9,BUILD_COOLDOWN*0.85, BUILD_COOLDOWN*0.80, BUILD_COOLDOWN*0.7, BUILD_COOLDOWN* 0.5};

    /** Array containing all the possible movement directions. */
    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public static final String ONE_HUNDRED_LEN_STRING = "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0";
    public static final String SIX_HUNDRED_LEN_STRING = ONE_HUNDRED_LEN_STRING + ONE_HUNDRED_LEN_STRING + ONE_HUNDRED_LEN_STRING + ONE_HUNDRED_LEN_STRING + ONE_HUNDRED_LEN_STRING + ONE_HUNDRED_LEN_STRING;
    public static final String MAP_LEN_STRING = SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING;


    final static int MAX_BROADCAST_DISTANCE = 10;
}
