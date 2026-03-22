package link.botwmcs.fizzy.client.util;

public final class TooltipVisibilityCoordinator {
    private static int suppressionDepth;

    private TooltipVisibilityCoordinator() {
    }

    public static void pushSuppression() {
        suppressionDepth++;
    }

    public static void popSuppression() {
        if (suppressionDepth > 0) {
            suppressionDepth--;
        }
    }

    public static boolean isSuppressed() {
        return suppressionDepth > 0;
    }
}
