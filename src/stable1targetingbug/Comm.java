package stable1targetingbug;

import battlecode.common.FlagInfo;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Team;
import stable1targetingbug.utils.FastIterableIntSet;

/**
 * The following code is partially
 * from https://github.com/carlguo866/battlecode23-gonefishin/tree/main/src/submit24_US_qual and is
 * not ours
 */

/***
 *
 * to represent coord, the value in the shared array is 1 more than usual
 * so that (0,0) in shared array means null
 *
 * Shared array
 * 0-35: 3*2 6bits int specifying the coord of friendly HQs
 * 48-50: 3 bit whether symmetries of the map have been eliminated:
 * [ROTATIONAL, VERTIAL, HORIZONTAL]
 * 64-67:
 *
 *
 *
 */
public class Comm extends Globals {


    final static int FLAG_SETUP_COMM = 0;
    final static int EXPLORER_COMM = 1;
    final static int ENEMY_FLAG_HELD = 10;
    final static int FRIENDLY_FLAG_INDEX_HELPER = 39;
    final static int ENEMY_FLAG_INDEX_HELPER = 42;
    final static int FRIENDLY_FLAG_SAFE = 45;
    final static int ENEMY_FLAG_FIRST = 48;
    final static int ENEMY_FLAG_LAST = 50;
    final static int ENEMY_FLAGS_NUM_IDX = 51;
    public static int symmetry;
    public static boolean isSymmetryConfirmed;
    public static boolean needSymmetryReport;
    public static boolean[] isSymEliminated = new boolean[3];
    // public static int[] flagIDs = new int[6];

    private static final int ARRAY_LENGTH = 64; // this is how much we use rn
    private static final int SYM_BIT = 48;
    private static final int NUM_FLAGS = 3;
    private static int NUM_FLAGS_CAPTURED = 0;
    public static int numFlagsReported = 0;

    private static boolean needFlagUpdate = false;
    public static MapLocation[] enemyFlagsInitial = new MapLocation[NUM_FLAGS];

    public static MapLocation[] allyFlagLocs = new MapLocation[NUM_FLAGS];


    private static int[] buffered_share_array = new int[ARRAY_LENGTH];
    private static FastIterableIntSet changedIndexes = new FastIterableIntSet(ARRAY_LENGTH);


    public static void turn_starts() throws GameActionException {
        boolean needSymUpdate = false;
        for (int i = 0; i < ARRAY_LENGTH; i++) {
            if (rc.readSharedArray(i) != buffered_share_array[i]) {
                if (i == 3 && !isSymmetryConfirmed) {
                    needSymUpdate = true;
                }
                buffered_share_array[i] = rc.readSharedArray(i);
            }
        }
        if (rc.isSpawned()) {
            for (FlagInfo flag : rc.senseNearbyFlags(-1, rc.getTeam().opponent())) {
                int index = flagIDToIdx(flag.getID(), flag.getTeam());
                // Debug.log("INDEX " + index + " " + rc.readSharedArray(index));
                if (rc.readSharedArray(index) == 0) {
                    updateFlagInfo(flag.getLocation(), flag.isPickedUp(), index);
                }
            }
        }
        for (int i = ENEMY_FLAG_FIRST; i <= ENEMY_FLAG_LAST; i++) {
            if (enemyFlagsInitial[i-ENEMY_FLAG_FIRST] == null && rc.readSharedArray(i) != 0)  {
                enemyFlagsInitial[i-ENEMY_FLAG_FIRST] = int2loc(rc.readSharedArray(i) >> 1);
            }
        }
        if (needSymUpdate || Globals.turnCount == 0) {
            updateSym();
        }
    }

    public static int flagIDToIdx(int flagID, Team team) throws GameActionException {
        // printArray(flagIDs);
        int j = 0;
        if (rc.getTeam().equals(team)) {
            j = FRIENDLY_FLAG_INDEX_HELPER;
        } else {
            j = ENEMY_FLAG_INDEX_HELPER;
        }
        for (int i = j; i < j+NUM_FLAGS; i++) {
            int num = rc.readSharedArray(i);
            if (num == 0) {
                rc.writeSharedArray(i, flagID);
                return i+NUM_FLAGS*2;
            } else if (num == flagID) {
                return i+NUM_FLAGS*2;
            }
        }
        return -1;
    }

    public static void printArray(int[] a) {
        String s = "[";
        for (int i = 0; i < a.length; i++) {
            s += a[i] + ",";
        }
        System.out.println(s + "]");
    }


    public static void updateFlagInfo(MapLocation loc, boolean isCarried, int idx) throws GameActionException {
        int locInt = loc2int(loc);
        locInt = locInt << 1;
        if (isCarried) {
            locInt += 1;
        }
        int readArray = rc.readSharedArray(idx);
        if (readArray != locInt) {
            rc.writeSharedArray(idx, locInt);
        }

    }

    public static void unCarry(int idx) throws GameActionException {
        int value = rc.readSharedArray(idx);
        value = value >> 1;
        value = value << 1;
        rc.writeSharedArray(idx, value);
    }


    public static MapLocation getLocation(int idx) throws GameActionException {
        int val = rc.readSharedArray(idx);
        val = val >> 1;
        return int2loc(val);
    }

    public static boolean isCarried(int idx) throws GameActionException {
        int val = rc.readSharedArray(idx);
        if (val == 0)
            return false;
        return val % 2 == 1;
    }

    public static void increaseFlagsCaptured() throws GameActionException {
        int add = rc.readSharedArray(ENEMY_FLAGS_NUM_IDX) + 1;
        rc.writeSharedArray(ENEMY_FLAGS_NUM_IDX, add);
        NUM_FLAGS_CAPTURED = add;
    }

    // IMPORTANT: always ensure that any write op is performed when writable
    public static void commit_write() throws GameActionException {
        if (changedIndexes.size > 0) {
            changedIndexes.updateIterable();
            int[] indexes = changedIndexes.ints;
            for (int i = changedIndexes.size; --i>=0;) {
                rc.writeSharedArray(indexes[i], buffered_share_array[indexes[i]]);
            }
            changedIndexes.clear();
        }
    }

    public static void eliminateSym(int sym) throws GameActionException {
        isSymEliminated[sym] = true;
        if (rc.canWriteSharedArray(0, 0)) {
            writeBits(SYM_BIT + sym, 1, 1);
            commit_write();
        } else {
            needSymmetryReport = true;
        }
        guessSym();
    }

    public static void updateSym() {
        int bits = readBits(SYM_BIT, 3);
        needSymmetryReport = false;
        for (int sym = 3; --sym >= 0; ) {
            if (!isSymEliminated[sym] && (bits & (1 << (2 - sym))) > 0) {
                isSymEliminated[sym] = true;
            } else if (isSymEliminated[sym] && (bits & (1 << (2 - sym))) == 0) {
                needSymmetryReport = true;
            }
        }
        guessSym();
    }

    public static void reportSym() throws GameActionException {
        if (!needSymmetryReport)
            return;
        int bits = readBits(SYM_BIT, 3);
        for (int sym = 3; --sym >= 0; ) {
            if (isSymEliminated[sym] && (bits & (1 << (2 - sym))) == 0) {
                writeBits(SYM_BIT + sym, 1, 1);
            }
        }
        needSymmetryReport = false;
    }

    public static void guessSym() {
        int numPossible = 0;
        for (int sym = 3; --sym >=0; ) {
            if (!isSymEliminated[sym]) {
                numPossible++;
                symmetry = sym;
            }
        }
        if (numPossible == 0) {
            Debug.log("impossible that no sym is correct, guess rotation");
            symmetry = 0;
            numPossible = 1;
        }
        if (numPossible == 1) {
            isSymmetryConfirmed = true;
        } else {
            isSymmetryConfirmed = false;
        }
        for (int i = Map.allySpawnLocations.length; --i >= 0;) {
            MapLocation loc = Map.allySpawnLocations[i];
            Map.enemySpawnLocations[i] = new MapLocation(
                    (symmetry & 1) == 0? Map.mapWidth - loc.x - 1 : loc.x,
                    (symmetry & 2) == 0? Map.mapHeight - loc.y - 1 : loc.y);
        }

        Map.enemyFlagSpawnLocations = Map.getCenters(Map.enemySpawnLocations);
    }

    public static void commFlagLocation(MapLocation location, int flagDuck) throws GameActionException {
        rc.writeSharedArray((flagDuck +60) ,loc2int(location));
    }

    public static void commFlagLocationDropped(int flagDuck) throws GameActionException {
        FlagInfo[] flags = rc.senseNearbyFlags(-1);
        if (flags.length > 0) {
            allyFlagLocs[flagDuck - 1] = flags[0].getLocation();
            rc.writeSharedArray((flagDuck +60) ,loc2int(flags[0].getLocation()));
        }
    }

    public static void readFlagLocation() throws GameActionException {
        for (int i = 0; i < NUM_FLAGS; i++) {
            allyFlagLocs[i] = int2loc(rc.readSharedArray(i+61));
        }
    }




    // helper funcs
    private static int readBits(int left, int length) {
        int endingBitIndex = left + length - 1;
        int rv = 0;
        while (left <= endingBitIndex) {
            int right = Math.min(left | 0xF, endingBitIndex);
            rv = (rv << (right - left + 1)) + ((buffered_share_array[left/16] % (1 << (16 - left%16))) >> (15 - right % 16));
            left = right + 1;
        }
        return rv;
    }

    private static void writeBits(int startingBitIndex, int length, int value) {
        assert value < (1 << length);
        int current_length = length;
        while (current_length > 0){
            int current_ending = startingBitIndex + current_length - 1;
            int len = Math.min(current_ending%16+1, current_length);
            int left = current_ending - len + 1;
            int original_value = (buffered_share_array[left/16] % (1 << (16 - left%16))) >> (15 - current_ending % 16);
            int new_value = value % (1 << len);
            value >>= len;
            if (new_value != original_value){
                changedIndexes.add(current_ending / 16);
                buffered_share_array[current_ending / 16] ^= (new_value^original_value) << (15 - current_ending % 16);
            }
            current_length -= len;
        }
    }

    public static MapLocation int2loc(int val) {
        if (val == 0) {
            return null;
        }
        return new MapLocation(val / 64 - 1, val % 64 - 1);
    }

    public static int loc2int(MapLocation loc) {
        if (loc == null)
            return 0;
        return ((loc.x + 1) * 64) + (loc.y + 1);
    }
}
