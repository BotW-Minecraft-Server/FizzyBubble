package link.botwmcs.fizzy.client.bossbar;

import com.mojang.blaze3d.vertex.PoseStack;
import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.client.util.animate.LerpedFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;

public class AnnounceMessage {
    private Component currentText;
    private boolean enabled;
    private int remainTicks = 0;
    private float lifeAcc;

    private final LerpedFloat barSize = LerpedFloat.linear();
    private final LerpedFloat barY = LerpedFloat.linear();
    // 视觉参数
    private static final int PADDING = 17;
    private static final int BASE_Y = 30; // 理论顶边（会根据 BossBar 往下让位）
    private static final int BAR_HEIGHT = 20;
    private static final int SIDE_CAP_W = 6;
    private static final int CENTER_MIN_TO_DRAW = 1;

    private static final float WIDTH_SPEED_PER_TICK = 0.30f;
    private static final float Y_SPEED_PER_TICK = 0.25f;

    private static final ResourceLocation WIDGETS = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/components/widgets.png");

    public AnnounceMessage() {
        barSize.startWithValue(0);
        barY.startWithValue(0);
    }

    public void show(Component text, int showTicks) {
        this.currentText = text;
        this.remainTicks = Math.max(1, showTicks);
        this.lifeAcc = 0f;

        int target = Minecraft.getInstance().font.width(text) + PADDING;
        // 只设置目标 & 追踪函数；速度不要写死在这里，后面每帧按dt折算
        barSize.updateChaseTarget(target);
        barSize.chase(target, WIDTH_SPEED_PER_TICK, LerpedFloat.Chaser.EXP);

    }

    public void updateFromRender(float dt) {
        if (!Minecraft.getInstance().isPaused() && dt > 0f) {
            lifeAcc += dt;
            int dec = (int) lifeAcc;      // 累满>=1tick才扣一次
            if (dec > 0) {
                lifeAcc -= dec;
                remainTicks = Math.max(0, remainTicks - dec);
                if (remainTicks == 0) currentText = null; // 到期清空 → 宽度会追到0
            }
        }

        // 根据当前文本更新目标宽度
        int target = (currentText != null) ? Minecraft.getInstance().font.width(currentText) + PADDING : 0;
        barSize.updateChaseTarget(target);

        // ===== 2) chase 用 dt；把“每tick速度”折算成“本帧等效速度”=====
        // EXP 追踪：s_frame = 1 - (1 - s_tick)^(dtTicks)
        float sTick = WIDTH_SPEED_PER_TICK;
        float sFrame = (dt <= 0f) ? 0f
                : 1f - (float) Math.pow(1f - sTick, dt);

        // 线性追踪（如果你换成 LINEAR）：s_frame = s_tick * dtTicks
        // float sFrame = sTick * dtTicks;

        barSize.updateChaseSpeed(sFrame);
        barSize.tickChaser();
    }

    /**
     * @param guiGraphics Forge/NeoForge 的 HUD 绘制上下文
     * @param partialTicks 插值
     * @param bossBarBottomY 传入 BossBar 占用到底的 Y（像素）
     */
    public void render(GuiGraphics guiGraphics, float partialTicks, int bossBarBottomY) {
        Minecraft mc = Minecraft.getInstance();
//        if (mc.options.hideGui || mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
//            enabled = false;
//            return;
//        }

        PoseStack ps = guiGraphics.pose();
        ps.pushPose();

        int screenW = mc.getWindow().getGuiScaledWidth();
        int safeY = Math.max(BASE_Y, bossBarBottomY + 4);        // 计算避让后的 Y

        // 移到屏幕上方中间附近
        ps.translate(screenW / 2f - 91, safeY, 0);

        int size = (int) barSize.getValue(partialTicks);
        if (size > CENTER_MIN_TO_DRAW) {
            enabled = true;

            // 背景条（左右端帽 + 中心拉伸）
            ps.pushPose();
            ps.translate(size / -2f + 91, -27, 100);

            // 左端帽
            guiGraphics.blit(WIDGETS, -SIDE_CAP_W, 0, 0, 0, SIDE_CAP_W, BAR_HEIGHT, 256, 256);
            // 右端帽
            guiGraphics.blit(WIDGETS, size, 0, SIDE_CAP_W, 0, SIDE_CAP_W, BAR_HEIGHT, 256, 256);
            // 中段（根据 size 拉伸；这里假设 widgets.png 的 U/V 里有一条可横向拉伸的 20px 高条）
            // 下面这行示例用法：将 U 偏移动态映射到纹理中部。若你的贴图不是拼图式，可直接用固定 U/V。
            guiGraphics.blit(WIDGETS, 0, 0, 0, 128 - size / 2f, BAR_HEIGHT, size, 20, 256, 256);

            ps.popPose();

            // 文本
            if (currentText != null) {
                Font font = mc.font;
                if (font.width(currentText) < size - 10) {
                    ps.pushPose();
                    ps.translate(font.width(currentText) / 2f + 82, -27, 100);
                    guiGraphics.drawCenteredString(font, currentText, 9 - font.width(currentText) / 2, 6, 0xFFFFFF);
                    ps.popPose();
                }
            }
        } else {
            enabled = false;
        }

        // 微调缩放（如果你想保留原类中那点缩放）
        ps.translate(91, -9, 0);
        ps.scale(0.925f, 0.925f, 1);

        ps.popPose();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isActive() {
        return (remainTicks > 0) || (currentText != null) || !barSize.settled();
    }

    public Component getCurrentText() {
        return currentText;
    }

    public void hide() {
        this.remainTicks = 0;
        this.currentText = null;
        barSize.chase(0, 0.2, LerpedFloat.Chaser.EXP);
    }





}
