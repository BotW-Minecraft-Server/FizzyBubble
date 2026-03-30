package link.botwmcs.fizzy.ui.element.funstuff.slotstuff;

import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.client.util.BlockingElementSupport;
import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;

import java.util.List;
import java.util.Map;

public final class TransparentBlockerElement implements ElementPainter {
    private boolean openTarget;
    private final Map<AbstractWidget, Boolean> storedActive = BlockingElementSupport.newWidgetStateMap();
    private BlockerWidget widget;

    public TransparentBlockerElement() {
        this(false);
    }

    public TransparentBlockerElement(boolean open) {
        this.openTarget = open;
    }

    /** true: 允许点击下方按钮；false: 阻挡点击 */
    public void setOpen(boolean open) {
        this.openTarget = open;
        if (this.widget != null) {
            this.widget.setBlocking(!open);
        }
        if (open) {
            restoreUnderlyingButtons();
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
            updateUnderlyingButtons();
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
            restoreUnderlyingButtons();
            return;
        }
        if (widget == null) {
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

    private void restoreUnderlyingButtons() {
        BlockingElementSupport.restoreWidgets(storedActive);
    }
}
