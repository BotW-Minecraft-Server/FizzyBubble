package link.botwmcs.fizzy.ui.element.funstuff.slotstuff;

import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.ui.host.FizzyMenuScreenHost;
import link.botwmcs.fizzy.ui.host.FizzyScreenHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class TransparentBlockerElement implements ElementPainter {
    private boolean openTarget;
    private final Map<AbstractWidget, Boolean> storedActive = new IdentityHashMap<>();
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
            updateUnderlyingButtons();
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

    private void restoreUnderlyingButtons() {
        if (storedActive.isEmpty()) {
            return;
        }
        for (var entry : storedActive.entrySet()) {
            entry.getKey().active = entry.getValue();
        }
        storedActive.clear();
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
}
