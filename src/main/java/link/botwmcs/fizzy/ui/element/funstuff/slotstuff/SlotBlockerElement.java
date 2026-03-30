package link.botwmcs.fizzy.ui.element.funstuff.slotstuff;

import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.client.util.AnimationClock;
import link.botwmcs.fizzy.client.util.BlockingElementSupport;
import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

import java.util.List;
import java.util.Map;

public final class SlotBlockerElement implements ElementPainter {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/block/glass.png");
    private static final int GLASS_SIZE = 16;
    private static final float ANIM_SPEED = 4.0f; // progress per second

    private boolean openTarget;
    private float openProgress;
    private final AnimationClock animationClock = new AnimationClock();

    private final Map<AbstractWidget, Boolean> storedActive = BlockingElementSupport.newWidgetStateMap();
    private BlockerWidget widget;

    public SlotBlockerElement() {
        this(false);
    }

    public SlotBlockerElement(boolean open) {
        this.openTarget = open;
        this.openProgress = open ? 1.0f : 0.0f;
    }

    /** true: 展开(允许点击下方按钮); false: 关闭(阻挡点击) */
    public void setOpen(boolean open) {
        this.openTarget = open;
        if (this.widget != null) {
            this.widget.setBlocking(!open);
        }
    }

    public boolean isOpen() {
        return this.openTarget;
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        this.widget = new BlockerWidget(leftPx, topPx, widthPx, heightPx);
        this.widget.setBlocking(!openTarget);
        context.addRenderableWidget(this.widget);
    }

    @Override
    public void render(GuiGraphicsExtractor g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        FizzyGuiUtils.syncWidgetBounds(this.widget, leftPx, topPx, widthPx, heightPx);
    }

    @Override
    public ElementType type() {
        return ElementType.CUSTOM;
    }

    private void tickAnimation() {
        AnimationClock.TickDelta delta = animationClock.tick(false);
        float dt = delta.seconds();
        float target = openTarget ? 1.0f : 0.0f;
        if (openProgress < target) {
            openProgress = Math.min(target, openProgress + ANIM_SPEED * dt);
        } else if (openProgress > target) {
            openProgress = Math.max(target, openProgress - ANIM_SPEED * dt);
        }
    }

    private final class BlockerWidget extends AbstractWidget {
        private BlockerWidget(int x, int y, int width, int height) {
            super(x, y, width, height, net.minecraft.network.chat.Component.empty());
        }

        private void setBlocking(boolean blocking) {
            this.active = blocking;
            this.visible = true;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
            int w = this.getWidth();
            int h = this.getHeight();
            if (w <= 0 || h <= 0) {
                return;
            }

            tickAnimation();
            updateUnderlyingButtons();

            FizzyGuiUtils.TextureSize size = FizzyGuiUtils.textureSize(TEXTURE);
            int texW = Math.max(1, size.w());
            int texH = Math.max(1, size.h());

            int panelW = Math.min(texW, GLASS_SIZE);
            int panelH = Math.min(texH, GLASS_SIZE);

            int clipX = this.getX() + (w - panelW) / 2;
            int clipY = this.getY() + (h - panelH) / 2;

            int leftW = panelW / 2;
            int rightW = panelW - leftW;
            int srcLeftU = 0;
            int srcRightU = panelW - rightW;

            float offset = openProgress * (panelW / 2.0f + 1.0f);
            int leftX = Math.round(clipX - offset);
            int rightX = Math.round(clipX + leftW + offset);

            // Left glass panel (clipped to the slot inner area)
            blitClipped(g, leftX, clipY, leftW, panelH, clipX, clipY, panelW, panelH, srcLeftU, 0, texW, texH);
            // Right glass panel (clipped to the slot inner area)
            blitClipped(g, rightX, clipY, rightW, panelH, clipX, clipY, panelW, panelH, srcRightU, 0, texW, texH);

            // Middle split lines (use texture's leftmost/rightmost columns)
            int baseLineLeftX = leftX + leftW - 1;
            int baseLineRightX = rightX;

            int topH = panelH / 2;
            int bottomH = panelH - topH;

            int leftTopX = baseLineLeftX - 1;
            int leftBottomX = baseLineLeftX + 1;
            int rightTopX = baseLineRightX - 1;
            int rightBottomX = baseLineRightX + 1;

            // Vertical split lines with S-shaped offsets.
            blitClipped(g, leftTopX, clipY, 1, topH + 1, clipX, clipY, panelW, panelH, panelW - 1, 0, texW, texH);
            blitClipped(g, leftBottomX, clipY + topH, 1, bottomH, clipX, clipY, panelW, panelH, panelW - 1, 0, texW, texH);

            blitClipped(g, rightTopX, clipY, 1, topH, clipX, clipY, panelW, panelH, 0, 0, texW, texH);
            blitClipped(g, rightBottomX, clipY + topH - 1, 1, bottomH + 1, clipX, clipY, panelW, panelH, 0, 0, texW, texH);

            // Horizontal step line using the darker bottom edge near the right-bottom corner.
            int stepY = clipY + topH - 1;
            int stepW = 3;
            int stepU = panelW - 1;
            int stepV = panelH - 1;

            blitClipped(g, leftTopX, stepY, stepW, 2, clipX, clipY, panelW, panelH, stepU, stepV, texW, texH);
            blitClipped(g, rightTopX, stepY, stepW, 2, clipX, clipY, panelW, panelH, stepU, stepV, texW, texH);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            if (this.active && this.visible) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
            }
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            soundManager.play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HARP.value(), 1.0F));
        }
    }

    private void updateUnderlyingButtons() {
        if (openTarget) {
            BlockingElementSupport.restoreWidgets(storedActive);
            return;
        }

        int cx = widget.getX() + widget.getWidth() / 2;
        int cy = widget.getY() + widget.getHeight() / 2;
        List<ElementPainter> elements = BlockingElementSupport.elementsAtCurrentScreenPx(cx, cy);
        if (elements.isEmpty()) {
            return;
        }
        BlockingElementSupport.disableUnderlyingWidgets(elements, this, storedActive);
    }

    private static void blitClipped(GuiGraphicsExtractor g,
                                    int destX, int destY, int destW, int destH,
                                    int clipX, int clipY, int clipW, int clipH,
                                    int srcU, int srcV, int texW, int texH) {
        int clipRight = clipX + clipW;
        int clipBottom = clipY + clipH;
        int destRight = destX + destW;
        int destBottom = destY + destH;

        int visX = Math.max(destX, clipX);
        int visY = Math.max(destY, clipY);
        int visRight = Math.min(destRight, clipRight);
        int visBottom = Math.min(destBottom, clipBottom);
        int visW = visRight - visX;
        int visH = visBottom - visY;

        if (visW <= 0 || visH <= 0) {
            return;
        }

        int u = srcU + (visX - destX);
        int v = srcV + (visY - destY);
        g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, visX, visY, u, v, visW, visH, texW, texH);
    }
}
