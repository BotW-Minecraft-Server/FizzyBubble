package link.botwmcs.fizzy.ui.element.component;

import link.botwmcs.fizzy.client.util.TextRenderer;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Objects;

public final class FizzyTooltipElement implements ElementPainter {
    private static final int TOOLTIP_Z = 400;
    private static final TooltipColors DEFAULT_COLORS = new TooltipColors(
            -267386864,
            -267386864,
            1347420415,
            1344798847
    ); // FROM VANILLA
    private static final ClientTooltipPositioner DEFAULT_POSITIONER = (screenW, screenH, mouseX, mouseY, tooltipW, tooltipH) -> {
        int x = mouseX + 12;
        int y = mouseY - 12;

        if (x + tooltipW > screenW) {
            x = mouseX - 12 - tooltipW;
        }
        if (x < 4) {
            x = 4;
        }

        if (y + tooltipH + 6 > screenH) {
            y = screenH - tooltipH - 6;
        }
        if (y < 4) {
            y = 4;
        }

        return new Vector2i(x, y);
    };

    private final TextRenderer renderer;
    private final TooltipColors colors;
    private final ClientTooltipPositioner positioner;
    private final int maxWidthPx;

    private TooltipWidget widget;

    private FizzyTooltipElement(Builder builder) {
        this.renderer = builder.buildRenderer();
        this.colors = builder.colors != null ? builder.colors : DEFAULT_COLORS;
        this.positioner = builder.positioner != null ? builder.positioner : DEFAULT_POSITIONER;
        this.maxWidthPx = Math.max(0, builder.maxWidthPx);
    }

    public static Builder builder(Component text) {
        return new Builder(text);
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        this.widget = new TooltipWidget(leftPx, topPx, widthPx, heightPx);
        this.widget.active = false;
        this.widget.visible = true;
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

    private final class TooltipWidget extends AbstractWidget {
        private TooltipWidget(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            if (!this.visible || !isPointInside(mouseX, mouseY)) {
                return;
            }
            renderTooltip(g, mouseX, mouseY, partialTick);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
        }

        @Override
        public void playDownSound(net.minecraft.client.sounds.SoundManager soundManager) {
        }

        private boolean isPointInside(double mouseX, double mouseY) {
            int w = this.getWidth();
            int h = this.getHeight();
            if (w <= 0 || h <= 0) {
                return false;
            }
            return mouseX >= this.getX()
                    && mouseX < this.getX() + w
                    && mouseY >= this.getY()
                    && mouseY < this.getY() + h;
        }
    }

    private void renderTooltip(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options.hideGui) {
            return;
        }

        float scale = Math.max(0.01f, renderer.textScale());
        int wrapWidth = resolveWrapWidth(scale);
        var metrics = renderer.measure(mc.font, wrapWidth);
        if (metrics.lines().isEmpty()) {
            return;
        }

        int textWidth = Math.max(1, (int) Math.ceil(metrics.maxWidth() * scale));
        int textHeight = Math.max(1, (int) Math.ceil(metrics.totalHeight() * scale));

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        Vector2ic pos = positioner.positionTooltip(screenW, screenH, mouseX, mouseY, textWidth, textHeight);
        int x = pos.x();
        int y = pos.y();

        TooltipRenderUtil.renderTooltipBackground(
                g,
                x, y,
                textWidth, textHeight,
                TOOLTIP_Z,
                colors.bgColor1(),
                colors.bgColor2(),
                colors.edgeColor1(),
                colors.edgeColor2()
        );
        g.pose().pushPose();
        g.pose().translate(0.0f, 0.0f, TOOLTIP_Z);
        renderer.render(g, x, y, textWidth, textHeight, partialTick);
        g.pose().popPose();
    }

    private int resolveWrapWidth(float scale) {
        if (!renderer.wrap()) {
            return Integer.MAX_VALUE;
        }
        if (maxWidthPx <= 0) {
            return Integer.MAX_VALUE;
        }
        return Math.max(1, (int) Math.floor(maxWidthPx / scale));
    }

    private record TooltipColors(int bgColor1, int bgColor2, int edgeColor1, int edgeColor2) {
    }

    public static final class Builder extends TextRenderer.Builder<Builder> {
        private TooltipColors colors;
        private ClientTooltipPositioner positioner;
        private int maxWidthPx;

        private Builder(Component text) {
            super(text);
        }

        public Builder maxWidthPx(int maxWidthPx) {
            this.maxWidthPx = maxWidthPx;
            return this;
        }

        public Builder tooltipColors(int bgColor1, int bgColor2, int edgeColor1, int edgeColor2) {
            this.colors = new TooltipColors(bgColor1, bgColor2, edgeColor1, edgeColor2);
            return this;
        }

        public Builder positioner(ClientTooltipPositioner positioner) {
            this.positioner = Objects.requireNonNull(positioner, "positioner");
            return this;
        }

        public FizzyTooltipElement build() {
            return new FizzyTooltipElement(this);
        }
    }
}
