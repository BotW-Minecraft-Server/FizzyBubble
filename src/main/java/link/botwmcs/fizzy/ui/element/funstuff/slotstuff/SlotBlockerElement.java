package link.botwmcs.fizzy.ui.element.funstuff.slotstuff;

import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.ui.host.FizzyMenuScreenHost;
import link.botwmcs.fizzy.ui.host.FizzyScreenHost;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvents;
import com.mojang.blaze3d.platform.NativeImage;

import java.io.IOException;
import java.io.InputStream;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SlotBlockerElement implements ElementPainter {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/glass.png");
    private static final int GLASS_SIZE = 16;
    private static final float ANIM_SPEED = 4.0f; // progress per second

    private boolean openTarget;
    private float openProgress;
    private long lastUpdateMs;

    private final Map<AbstractWidget, Boolean> storedActive = new IdentityHashMap<>();
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
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        if (this.widget == null) {
            return;
        }
        this.widget.setX(leftPx);
        this.widget.setY(topPx);
        this.widget.setWidth(widthPx);
        this.widget.setHeight(heightPx);
    }

    @Override
    public ElementType type() {
        return ElementType.CUSTOM;
    }

    private void tickAnimation() {
        long now = Util.getMillis();
        if (lastUpdateMs == 0L) {
            lastUpdateMs = now;
            return;
        }
        float dt = (now - lastUpdateMs) / 1000.0f;
        lastUpdateMs = now;
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
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            int w = this.getWidth();
            int h = this.getHeight();
            if (w <= 0 || h <= 0) {
                return;
            }

            tickAnimation();
            updateUnderlyingButtons();

            TextureSize size = SIZE_CACHE.computeIfAbsent(TEXTURE, SlotBlockerElement::resolveTextureSize);
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
        public void onClick(double mouseX, double mouseY) {
            if (this.active && this.visible) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
            }
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            soundManager.play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HARP.value(), 1.0F));
        }
    }

    private record TextureSize(int w, int h) {
        static final TextureSize FALLBACK = new TextureSize(16, 16);
    }

    private static final Map<ResourceLocation, TextureSize> SIZE_CACHE = new ConcurrentHashMap<>();

    private static TextureSize resolveTextureSize(ResourceLocation tex) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return TextureSize.FALLBACK;
        }
        try {
            var resourceOpt = mc.getResourceManager().getResource(tex);
            if (resourceOpt.isEmpty()) {
                return TextureSize.FALLBACK;
            }

            Resource resource = resourceOpt.get();
            try (InputStream stream = resource.open(); NativeImage image = NativeImage.read(stream)) {
                int width = image.getWidth();
                int height = image.getHeight();
                if (width <= 0 || height <= 0) {
                    return TextureSize.FALLBACK;
                }
                return new TextureSize(width, height);
            }
        } catch (IOException e) {
            return TextureSize.FALLBACK;
        }
    }

    private void updateUnderlyingButtons() {
        if (openTarget) {
            if (!storedActive.isEmpty()) {
                for (var entry : storedActive.entrySet()) {
                    entry.getKey().active = entry.getValue();
                }
                storedActive.clear();
            }
            return;
        }

        int cx = widget.getX() + widget.getWidth() / 2;
        int cy = widget.getY() + widget.getHeight() / 2;
        List<ElementPainter> elements = elementsAtPx(cx, cy);
        if (elements.isEmpty()) {
            return;
        }

        for (ElementPainter element : elements) {
            if (element == this || element.type() != ElementType.BUTTON) {
                continue;
            }
            for (AbstractWidget button : element.widgets()) {
                storedActive.putIfAbsent(button, button.active);
                button.active = false;
            }
        }
    }

    private List<ElementPainter> elementsAtPx(int x, int y) {
        var screen = Minecraft.getInstance().screen;
        if (screen instanceof FizzyScreenHost host) {
            return host.elementsAtPx(x, y);
        }
        if (screen instanceof FizzyMenuScreenHost<?> host) {
            return host.elementsAtPx(x, y);
        }
        return List.of();
    }

    private static void blitClipped(GuiGraphics g,
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
        g.blit(TEXTURE, visX, visY, u, v, visW, visH, texW, texH);
    }
}
