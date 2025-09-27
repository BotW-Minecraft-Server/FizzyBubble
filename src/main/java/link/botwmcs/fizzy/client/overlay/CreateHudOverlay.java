package link.botwmcs.fizzy.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.api.IOverlayContent;
import link.botwmcs.fizzy.client.util.animate.LerpedFloat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CreateHudOverlay {
    // ==== 面板资源 & 几何 ====
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/components/hud/create_style.png"); // 需提供一张226x118的贴图或改尺寸
    private static final int GUI_W = 226, GUI_H = 118;
    private static final int TITLE_X = 6, TITLE_Y = 2; // 上移标题
    private static final int TIME_PAD_X = 6;
    private static final float TEXT_SCALE = 1.0f / 0.75f; // 你用的文字缩放
    private static final int   LEFT_PAD   = 2;            // 左内边距
    private static final int   RIGHT_GAP  = 20;           // 从右侧外起点进入时的间隔

    // 滚动文字区域(上移 + 加大高度，避免裁剪掉大字的上缘)
    private static final int SLIDE_AREA_X = 3;
    private static final int SLIDE_AREA_Y = 14;  // 从16→14
    private static final int SLIDE_AREA_W = 220;
    private static final int SLIDE_AREA_H = 22;  // 从20→22 更宽松

    // API
    private IOverlayContent content;

    // ==== 状态 ====
    private final Font font = Minecraft.getInstance().font;

    private final LerpedFloat xPos = LerpedFloat.linear().startWithValue(0);
    private final LerpedFloat yPos = LerpedFloat.linear().startWithValue(0);
    private final LerpedFloat alpha = LerpedFloat.linear().startWithValue(0);     // 整体不透明度
    private final LerpedFloat textAlpha = LerpedFloat.linear().startWithValue(0); // 文字淡入

    private Anchor anchor = Anchor.TOP_LEFT;

    private float uiScale = 1.0f;
    private int targetX, targetY;  // 由布局器赋值
    private boolean targetVisible = false; // “目标可见性”（驱动淡入/淡出）
    private boolean active = false;        // 仍需参与渲染/更新（alpha>0 或 正在显示）

    private Component title = Component.literal("Fizzy Create HUD").withStyle(ChatFormatting.GRAY);
    private Component slidingText = Component.literal("Hello, Animated HUD Overlay. このテキストは横スクロールします。");
    private int slidingTextWidth = 0;
    private float slidingOffset = 0;
    private boolean shouldScroll = false;
    private int effectiveTextWidth = 0; // 考虑缩放后的真实宽度

    // ========== 构造 ==========
    public CreateHudOverlay(IOverlayContent content) {
        this.content = content;
        // 初始滚动文本宽度
        setSlidingText(slidingText);
    }

    // ========== 对外 API ==========
    public CreateHudOverlay setScale(float scale) {
        this.uiScale = Mth.clamp(scale, 0.5f, 3.0f);
        return this;
    }

    public CreateHudOverlay setContent(IOverlayContent c) {
        this.content = c;
        return this;
    }

    public CreateHudOverlay setAnchor(Anchor a) {
        this.anchor = a == null ? Anchor.TOP_RIGHT : a;
        return this;
    }

    public Anchor getAnchor() { return anchor; }


    public CreateHudOverlay setTitle(Component title) {
        this.title = title == null ? Component.empty() : title;
        return this;
    }

    public CreateHudOverlay setSlidingText(Component c) {
        this.slidingText = c == null ? Component.empty() : c;
        this.slidingTextWidth = font.width(this.slidingText);
        this.effectiveTextWidth = (int) Math.ceil(this.slidingTextWidth * TEXT_SCALE);
        this.shouldScroll = this.effectiveTextWidth > (SLIDE_AREA_W - 2 * LEFT_PAD);
        if (this.shouldScroll) {
            // 从“右侧窗口外”进入：左边缘 = 可视区域宽度 + 右侧间隔
            this.slidingOffset = SLIDE_AREA_W + RIGHT_GAP;
        } else {
            // 不滚动：始终左对齐
            this.slidingOffset = LEFT_PAD;
        }

        this.textAlpha.chase(1.0, 0.35, LerpedFloat.Chaser.EXP);
        return this;

//        this.slidingOffset = (slidingTextWidth > SLIDE_AREA_W * 0.75f)
//                ? (SLIDE_AREA_W + slidingTextWidth / 2f) + 20f
//                : (SLIDE_AREA_W * 0.75f) / 2f;
//        this.textAlpha.chase(1.0, 0.35, LerpedFloat.Chaser.EXP);
//        return this;
    }

    /** 请求显示（淡入） */
    public void show() {
        this.targetVisible = true;
        this.active = true;
        this.alpha.chase(1.0, 0.25, LerpedFloat.Chaser.EXP);
        this.textAlpha.chase(1.0, 0.30, LerpedFloat.Chaser.EXP);
        resetFromScreenCenter();
    }

    /** 请求隐藏（淡出）；等 alpha 到 0 后自动停止渲染 */
    public void hide() {
        this.targetVisible = false;
        this.alpha.chase(0.0, 0.25, LerpedFloat.Chaser.EXP);
        this.textAlpha.chase(0.0, 0.35, LerpedFloat.Chaser.EXP);
    }

    public boolean isActive() { return active; }

    /** 由布局器设置最终目标位置（像素，未乘缩放） */
    public void setTargetPos(int x, int y) {
        this.targetX = x;
        this.targetY = y;
    }

    public int getWidthPx()  { return (int) (GUI_W * uiScale); }
    public int getHeightPx() { return (int) (GUI_H * uiScale); }

    // ========== 更新 & 渲染 ==========
    public void render(GuiGraphics g, float pt) {
        if (!active) return;

        // 更新追踪
        xPos.chase(targetX, 0.20, LerpedFloat.Chaser.EXP);
        yPos.chase(targetY, 0.20, LerpedFloat.Chaser.EXP);
        xPos.tickChaser();
        yPos.tickChaser();
        alpha.tickChaser();
        textAlpha.tickChaser();

        float a = Mth.clamp(alpha.getValue(pt), 0f, 1f);
        if (!targetVisible && a <= 0.01f) {
            // 完成淡出 → 不再渲染
            active = false;
            return;
        }

        // 背板
        g.pose().pushPose();
        g.pose().translate((int) xPos.getValue(pt), (int) yPos.getValue(pt), 0);
        g.pose().scale(uiScale, uiScale, uiScale);

        RenderSystem.setShaderTexture(0, BG);
        g.blit(BG, 0, 0, 0, 0, GUI_W, GUI_H, 256, 256);

        // 标题 & 时间
        String timeStr = formatTickTime(Minecraft.getInstance().level.getDayTime());
        int ttlColor = withAlpha(0xFF4F4F4F, a);
        g.drawString(font, title, TITLE_X, TITLE_Y, ttlColor, false);
        g.drawString(font, timeStr, GUI_W - TIME_PAD_X - font.width(timeStr), TITLE_Y, ttlColor, false);

        // 滚动文字（调高裁剪窗口，避免吃字）
        int scLeft   = (int) (xPos.getValue(pt) + SLIDE_AREA_X * uiScale);
        int scTop    = (int) (yPos.getValue(pt) + (SLIDE_AREA_Y) * uiScale);
        int scRight  = (int) (xPos.getValue(pt) + (SLIDE_AREA_X + SLIDE_AREA_W) * uiScale);
        int scBottom = (int) (yPos.getValue(pt) + (SLIDE_AREA_Y + SLIDE_AREA_H) * uiScale);
        g.enableScissor(scLeft, scTop, scRight, scBottom);

        g.pose().pushPose();
        // 维持你原来的 1/0.75 视觉（略放大文字）
        float scale = 1f / 0.75f;
        g.pose().scale(scale, scale, scale);

        tickMarquee(2f * Minecraft.getInstance().getTimer().getGameTimeDeltaTicks());
        int txtColor = withAlpha(0xFFFF9900, a * Mth.clamp(textAlpha.getValue(pt), 0f, 1f));
        int drawX = (int) ((SLIDE_AREA_X + slidingOffset) / (1f / TEXT_SCALE)); // 等价于 /0.75f
        int drawY = (int) ((SLIDE_AREA_Y) / (1f / TEXT_SCALE));
        g.drawString(font, slidingText, drawX, drawY, txtColor, false);

        g.pose().popPose();
        g.disableScissor();

//        g.enableScissor((int) (xPos.getValue(pt) + 3 * uiScale),
//                (int) (yPos.getValue(pt) + 40 * uiScale),
//                (int) (xPos.getValue(pt) + (3 + 220) * uiScale),
//                (int) (yPos.getValue(pt) + (40 + 62) * uiScale));
//        g.drawString(font, Component.literal("Main layer content..."),
//                6, 44, withAlpha(0xFFCCCCCC, a), false);
        renderContent(g, a);
//        g.disableScissor();

        g.pose().popPose();
    }

    private void renderContent(GuiGraphics g, float a) {
        if (content == null) return;

        // 内容区裁剪区域
        g.enableScissor(
                (int) (xPos.getValue() + 3 * uiScale),
                (int) (yPos.getValue() + 40 * uiScale),
                (int) (xPos.getValue() + (3 + 220) * uiScale),
                (int) (yPos.getValue() + (40 + 62) * uiScale)
        );

        g.pose().pushPose();
        g.pose().translate(3, 40, 0);
        content.render(g, 0, 0, 220, 62, Minecraft.getInstance().getTimer().getGameTimeDeltaTicks());
        g.pose().popPose();

        g.disableScissor();
    }


    // ========== 内部 ==========
    private void resetFromScreenCenter() {
        var mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        xPos.startWithValue(sw / 2f - (GUI_W * uiScale) / 2f);
        yPos.startWithValue(sh / 2f - (GUI_H * uiScale) / 2f);
    }

    private void tickMarquee(float delta) {
        if (!shouldScroll) return;

        // 匀速向左
        this.slidingOffset -= delta;

        // 复位条件： (左内边距 + 当前偏移 + 文本宽) <= 0  → 尾巴也完全越过左边界
        if (LEFT_PAD + this.slidingOffset + this.effectiveTextWidth <= 0) {
            // 复位到右侧窗口外一点点
            this.slidingOffset = SLIDE_AREA_W + RIGHT_GAP;
        }

    }

    private static String formatTickTime(long dayTime) {
        long ticks = (dayTime % 24000L + 24000L) % 24000L;
        int totalSeconds = (int) (ticks / 20);
        int h = (totalSeconds / 3600);
        int m = (totalSeconds / 60) % 60;
        return String.format("%02d:%02d", h, m);
    }

    private static int withAlpha(int rgb, float a) {
        int alpha = (int) (Mth.clamp(a, 0f, 1f) * 255f) & 0xFF;
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

}
