package smurfv2;

import battlecode.common.*;

/**
 * The following code is from https://github.com/carlguo866/battlecode23-gonefishin/tree/main/src/submit24_US_qual and is
 * not ours
 */


public class MapRecorder extends Globals {
    // perform bit hack to reduce init cost
    public static final char SEEN_BIT = 1 << 4;
    public static final char WALL_BIT = 1 << 5;
    public static final char DAM_BIT = 1 << 6;
    public static final char PASSABLE_BIT = 1 << 7;

    public static char[] vals = Constants.MAP_LEN_STRING.toCharArray();

    public static boolean check(MapLocation loc, Direction targetDir) throws GameActionException {
        if (!rc.onTheMap(loc))
            return false;
        int val = vals[loc.x * Map.mapHeight + loc.y];
        if ((val & SEEN_BIT) == 0)
            return true;
        if ((val & PASSABLE_BIT) == 0 && (val & DAM_BIT) == 0)
            return false;
        else {
            return true;
        }
    }

    public static void recordSym(int leaveBytecodeCnt) throws GameActionException {
        MapInfo[] infos = rc.senseNearbyMapInfos();

        for (int i = infos.length; --i >= 0; ) {
            if (Clock.getBytecodesLeft() <= leaveBytecodeCnt) {
                return;
            }

            if ((vals[infos[i].getMapLocation().x * Map.mapHeight + infos[i].getMapLocation().y] & SEEN_BIT) != 0) {

                continue;
            }
            MapInfo info = infos[i];
            int x = info.getMapLocation().x;
            int y = info.getMapLocation().y;
            char val = SEEN_BIT;
            if (!info.isWall())
                val |= PASSABLE_BIT;
            if (!Comm.isSymmetryConfirmed) {
                if (info.isWall())
                    val |= WALL_BIT;
                if (info.isDam()) {
                    val |= DAM_BIT;
                }

                int symVal;
                boolean isSym;
                for (int sym = 3; --sym >= 0; ) {
                    if (Comm.isSymEliminated[sym])
                        continue;
                    symVal = vals[((sym & 1) == 0 ? Map.mapWidth - x - 1 : x) * Map.mapHeight + ((sym & 2) == 0 ? Map.mapHeight - y - 1 : y)];
                    if ((symVal & SEEN_BIT) == 0) {
                        continue;
                    }
                    isSym = (val) == (symVal);
                    if (!isSym) {
                        Comm.eliminateSym(sym);
                    }
                }
            }
            vals[x * Map.mapHeight + y] = val;
        }
    }
}
