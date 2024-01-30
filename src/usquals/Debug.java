package usquals;

public class Debug extends Globals {

    public static boolean DEBUGGING = false;


    public static void log(String s) {
        if (DEBUGGING)
            System.out.println(s);
    }

    public static void log(int robotID, String s) {
        if (DEBUGGING) {
            System.out.println(String.format("Robot %d: %s", robotID, s));
        }
    }

    public static void log(int robotID, int roundNum, String s) {
        if (DEBUGGING) {
            System.out.println(String.format("Robot %d (Round %d): %s", robotID, roundNum, s));
        }
    }


}
