package link.botwmcs.fizzy.util;

public final class EnvDetector {
    private static Boolean cached;
    private static Boolean test = false;

    private static final String MARKER_MODID = "ezfix";

    public static boolean isLTSX() {
//        if (test) return true;
//        if (cached != null) return cached;
//        boolean detected;
//        try {
//            Class.forName("link.botwmcs.ezfix.EzFix");
//            detected = true;
//            cached = true;
//        } catch (ClassNotFoundException e) {
//            detected = false;
//        }
//        return detected;

        // Fizzy V2 is no needed.
        return false;
    }
}
