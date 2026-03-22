package link.botwmcs.fizzy.client.bossbar;

import com.mojang.blaze3d.vertex.PoseStack;
import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.client.overlay.Anchor;
import link.botwmcs.fizzy.client.util.BossbarRenderProbe;
import link.botwmcs.fizzy.client.util.animate.LerpedFloat;
import link.botwmcs.fizzy.ui.kernel.overlay.OverlayRenderable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class AnnounceMessage implements OverlayRenderable {
    private static final int PADDING = 17;
    private static final int BASE_Y = 30;
    private static final int BAR_HEIGHT = 20;
    private static final int SIDE_CAP_W = 6;
    private static final int CENTER_MIN_TO_DRAW = 1;
    private static final float WIDTH_SPEED_PER_TICK = 0.30f;
    private static final ResourceLocation WIDGETS =
            ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/components/widgets.png");

    private final LerpedFloat barSize = LerpedFloat.linear();

    private Component currentText;
    private boolean enabled;
    private int remainTicks;
    private float lifeAccumulator;

    private Anchor anchor = Anchor.TOP_LEFT;
    private int screenWidth;
    private int targetX;
    private int targetY;

    public AnnounceMessage() {
        barSize.startWithValue(0);
    }

    public void show(Component text, int showTicks) {
        this.currentText = text;
        this.remainTicks = Math.max(1, showTicks);
        this.lifeAccumulator = 0.0f;

        int targetWidth = Minecraft.getInstance().font.width(text) + PADDING;
        barSize.updateChaseTarget(targetWidth);
        barSize.chase(targetWidth, WIDTH_SPEED_PER_TICK, LerpedFloat.Chaser.EXP);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Component getCurrentText() {
        return currentText;
    }

    @Override
    public void beforeLayout(int screenWidth, int screenHeight) {
        this.screenWidth = Math.max(0, screenWidth);
        int bossBottom = BossbarRenderProbe.getBottomY();
        int baseY = bossBottom > 0 ? bossBottom + 4 : BASE_Y;
        this.targetX = this.screenWidth / 2;
        this.targetY = Math.max(BASE_Y, baseY);
    }

    @Override
    public boolean isActive() {
        return remainTicks > 0 || currentText != null || !barSize.settled();
    }

    @Override
    public void hide() {
        remainTicks = 0;
        currentText = null;
        barSize.chase(0.0, 0.2, LerpedFloat.Chaser.EXP);
    }

    @Override
    public void dispose() {
        hide();
    }

    @Override
    public int getWidthPx() {
        return Math.max(182, screenWidth);
    }

    @Override
    public int getHeightPx() {
        return BAR_HEIGHT;
    }

    @Override
    public Anchor getAnchor() {
        return anchor;
    }

    @Override
    public void assignAnchor(Anchor anchor) {
        this.anchor = anchor == null ? Anchor.TOP_LEFT : anchor;
    }

    @Override
    public void setTargetPos(int x, int y) {
        this.targetX = x;
        this.targetY = y;
    }

    @Override
    public void render(GuiGraphics graphics, float partialTick) {
        float deltaTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
        updateFromRender(deltaTicks);
        renderAt(graphics, partialTick, targetX, targetY);
    }

    private void updateFromRender(float deltaTicks) {
        if (!Minecraft.getInstance().isPaused() && deltaTicks > 0.0f) {
            lifeAccumulator += deltaTicks;
            int consumed = (int) lifeAccumulator;
            if (consumed > 0) {
                lifeAccumulator -= consumed;
                remainTicks = Math.max(0, remainTicks - consumed);
                if (remainTicks == 0) {
                    currentText = null;
                }
            }
        }

        int targetWidth = currentText == null ? 0 : Minecraft.getInstance().font.width(currentText) + PADDING;
        barSize.updateChaseTarget(targetWidth);
        float speedPerFrame = deltaTicks <= 0.0f
                ? 0.0f
                : 1.0f - (float) Math.pow(1.0f - WIDTH_SPEED_PER_TICK, deltaTicks);
        barSize.updateChaseSpeed(speedPerFrame);
        barSize.tickChaser();
    }

    private void renderAt(GuiGraphics graphics, float partialTick, int centerX, int baseY) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack pose = graphics.pose();
        pose.pushPose();

        int safeCenterX = centerX > 0
                ? centerX
                : (screenWidth > 0 ? screenWidth : mc.getWindow().getGuiScaledWidth()) / 2;
        int safeY = Math.max(BASE_Y, baseY > 0 ? baseY : BASE_Y);

        pose.translate(safeCenterX - 91, safeY, 0.0f);

        int size = (int) barSize.getValue(partialTick);
        if (size > CENTER_MIN_TO_DRAW) {
            enabled = true;

            pose.pushPose();
            pose.translate(size / -2.0f + 91.0f, -27.0f, 100.0f);
            graphics.blit(WIDGETS, -SIDE_CAP_W, 0, 0, 0, SIDE_CAP_W, BAR_HEIGHT, 256, 256);
            graphics.blit(WIDGETS, size, 0, SIDE_CAP_W, 0, SIDE_CAP_W, BAR_HEIGHT, 256, 256);
            graphics.blit(WIDGETS, 0, 0, 0, 128 - size / 2.0f, BAR_HEIGHT, size, 20, 256, 256);
            pose.popPose();

            if (currentText != null) {
                Font font = mc.font;
                if (font.width(currentText) < size - 10) {
                    pose.pushPose();
                    pose.translate(font.width(currentText) / 2.0f + 82.0f, -27.0f, 100.0f);
                    graphics.drawCenteredString(font, currentText, 9 - font.width(currentText) / 2, 6, 0xFFFFFF);
                    pose.popPose();
                }
            }
        } else {
            enabled = false;
        }

        pose.translate(91.0f, -9.0f, 0.0f);
        pose.scale(0.925f, 0.925f, 1.0f);
        pose.popPose();
    }
}
