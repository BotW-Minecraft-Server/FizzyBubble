package link.botwmcs.fizzy.client.bossbar;

import link.botwmcs.fizzy.client.util.BossbarRenderProbe;
import link.botwmcs.fizzy.mixin.client.BossHealthOverlayAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.UUID;

public final class AnnounceMessageManager {
    private static final AnnounceMessage INSTANCE = new AnnounceMessage();

    // 布局参数
    private static int marginTop = 30;   // 无 BossBar 时的默认顶部 Y
    private static int gapToBoss = 4;    // 与 BossBar 底边的垂直间距

    // 原版 BossBar 的行距估算（1.21.1）
    private static final int BOSSBAR_START_Y = 12; // 第一条顶部 Y
    private static final int BOSSBAR_STEP    = 19; // 每条占用高度（含间距）

    private AnnounceMessageManager() {}

    /** 显示（如果正在显示会覆盖） */
    public static void show(Component text, int ticks) {
        INSTANCE.show(text, ticks);
    }

    /** 统一渲染（由外部事件调用） */
    public static void render(GuiGraphics g, int sw, int sh, float pt) {
        // 居中 X
        int centerX = sw / 2;

        // 计算 BossBar 占用底边 Y，决定基准 Y
        int bossBottom = calcBossBarBottomY();
        int baseY = (bossBottom > 0) ? (bossBottom + gapToBoss) : marginTop;

        float dt = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
        INSTANCE.updateFromRender(dt);
        INSTANCE.render(g, pt, baseY);
    }

    /** 全局隐藏（风格上对应 hideAll） */
    public static void hideAll() {
        INSTANCE.hide();
    }

    /* ========== BossBar 占用计算 ========== */
    private static int calcBossBarBottomY() {
        return BossbarRenderProbe.getBottomY();


    }

}
