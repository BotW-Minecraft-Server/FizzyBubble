package link.botwmcs.fizzy.client.util;

public final class BossbarRenderProbe {
    private static final int BAR_HEIGHT = 12; // 原版条高=10px
    private static int lastY;
    private static boolean drewAny;
    private static int bottomY; // 暴露给外界读取

    private BossbarRenderProbe() {}

    public static void beginFrame() {
        drewAny = false;
        lastY = 0;
        bottomY = 0;
    }

    /** 在每次绘制单条bossbar时，捕获其Y */
    public static void onDrawBarAt(int y) {
        drewAny = true;
        lastY = y + 20;
    }

    public static void endFrame() {
        bottomY = drewAny ? (lastY + BAR_HEIGHT) : 0;
    }

    public static int getBottomY() {
        return bottomY;
    }

}
