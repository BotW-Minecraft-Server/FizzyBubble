package link.botwmcs.fizzy.ui.frame;

import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.ui.element.component.FizzyComponentElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.Consumer;

public class FizzyFrame implements FramePainter {
    private static final Identifier DEFAULT_PANEL_TEXTURE =
            Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/panel_default.png");
    private static final Identifier DEFAULT_DARK_PANEL_TEXTURE =
            Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/panel_default_dark.png");

    private final Identifier tex;
    private final FrameMetrics m;
    private final int panelWidthPx;
    private final FizzyComponentElement titleElement;
    private final boolean dark;
    private Layout layout;

    public FizzyFrame() {
        this(Component.empty());
    }

    public FizzyFrame(Component title) {
        this(DEFAULT_PANEL_TEXTURE, FizzyFrameMetrics.ofDefault256x256(), title);
    }

    public FizzyFrame(FizzyComponentElement titleElement) {
        this(DEFAULT_PANEL_TEXTURE, FizzyFrameMetrics.ofDefault256x256(), titleElement);
    }

    public FizzyFrame(Component title, boolean dark) {
        this(
                DEFAULT_PANEL_TEXTURE,
                FizzyFrameMetrics.ofDefault256x256(),
                FizzyFrameMetrics.ofDefault256x256().panelW(),
                title,
                dark
        );
    }

    public FizzyFrame(FizzyComponentElement titleElement, boolean dark) {
        this(
                DEFAULT_PANEL_TEXTURE,
                FizzyFrameMetrics.ofDefault256x256(),
                FizzyFrameMetrics.ofDefault256x256().panelW(),
                titleElement,
                dark
        );
    }

    public FizzyFrame(Component title, Consumer<FizzyComponentElement.Builder> titleCustomizer) {
        this(
                DEFAULT_PANEL_TEXTURE,
                FizzyFrameMetrics.ofDefault256x256(),
                FizzyFrameMetrics.ofDefault256x256().panelW(),
                FrameTitleSupport.defaultTitle(title, false, titleCustomizer),
                false
        );
    }

    public FizzyFrame(Component title, boolean dark, Consumer<FizzyComponentElement.Builder> titleCustomizer) {
        this(
                DEFAULT_PANEL_TEXTURE,
                FizzyFrameMetrics.ofDefault256x256(),
                FizzyFrameMetrics.ofDefault256x256().panelW(),
                FrameTitleSupport.defaultTitle(title, dark, titleCustomizer),
                dark
        );
    }

    public FizzyFrame(Identifier tex, FrameMetrics metrics, Component title) {
        this(tex, metrics, metrics.panelW(), title, false);
    }

    public FizzyFrame(Identifier tex, FrameMetrics metrics, FizzyComponentElement titleElement) {
        this(tex, metrics, metrics.panelW(), titleElement, false);
    }

    public FizzyFrame(Identifier tex, FrameMetrics metrics, int panelWidthPx, Component title, boolean dark) {
        this(tex, metrics, panelWidthPx, FrameTitleSupport.defaultTitle(title, dark), dark);
    }

    public FizzyFrame(Identifier tex, FrameMetrics metrics, int panelWidthPx, FizzyComponentElement titleElement, boolean dark) {
        this.dark = dark;
        this.tex = tex;
        this.m = metrics;
        this.panelWidthPx = panelWidthPx;
        this.titleElement = Objects.requireNonNull(titleElement, "titleElement");
    }

    @Override
    public void paint(GuiGraphicsExtractor g, int left, int top, int w, int h, boolean drawBottomEdge, boolean hasBelow) {
        final int texW = m.texW();
        final int texH = m.texH();
        final int drawW = panelWidthPx;
        int y = top;

        int expectedRowsPart = h - m.slotStartTopPx() - m.bottomPadHeight()
                - (hasBelow ? m.buttomInvExtraHeight() : 0)
                - (drawBottomEdge ? m.bottomEdgeHeight() : 0);
        if (expectedRowsPart % m.slotSizePx() != 0) {
            expectedRowsPart -= expectedRowsPart % m.slotSizePx();
        }
        int rows = Math.max(0, expectedRowsPart / m.slotSizePx());

        blit(g, left, y, 0, 0, drawW, m.slotStartTopPx(), texW, texH);
        y += m.slotStartTopPx();

        if (rows > 0) {
            blit(g, left, y, 0, m.topBorderY(), drawW, 1, texW, texH);
            y += 1;

            int middleH = rows * m.slotSizePx() - 2;
            if (middleH > 0) {
                int rest = middleH;
                while (rest > 0) {
                    int hStep = Math.min(rest, m.slotInnerHeight());
                    blit(g, left, y, 0, m.slotInnerStartY(), drawW, hStep, texW, texH);
                    y += hStep;
                    rest -= hStep;
                }
            }

            blit(g, left, y, 0, m.bottomBorderY(), drawW, 1, texW, texH);
            y += 1;
        }

        blit(g, left, y, 0, m.bottomPadStartY(), drawW, m.bottomPadHeight(), texW, texH);
        y += m.bottomPadHeight();

        if (hasBelow) {
            blit(g, left, y, 0, m.buttomInvExtraStartY(), drawW, m.buttomInvExtraHeight(), texW, texH);
            y += m.buttomInvExtraHeight();
        }

        if (drawBottomEdge) {
            blit(g, left, y, 0, m.bottomEdgeStartY(), drawW, m.bottomEdgeHeight(), texW, texH);
        }

        FrameTitleSupport.render(g, m, left, top, drawW, this.titleElement);
    }

    private void blit(GuiGraphicsExtractor g, int x, int y, int u, int v, int w, int h, int texW, int texH) {
        g.blit(
                RenderPipelines.GUI_TEXTURED,
                this.dark ? DEFAULT_DARK_PANEL_TEXTURE : this.tex,
                x,
                y,
                (float) u,
                (float) v,
                w,
                h,
                w,
                h,
                texW,
                texH
        );
    }

    public int computeHeightPx(int rows, boolean includeBottomEdge) {
        return computeHeightPx(rows, includeBottomEdge, false);
    }

    public int computeHeightPx(int rows, boolean includeBottomEdge, boolean includeBelow) {
        return m.totalHeightForRows(rows, includeBottomEdge, includeBelow);
    }

    public int gridOriginX(int panelLeftPx) {
        return m.gridOriginX(panelLeftPx);
    }

    public int gridOriginY(int panelTopPx) {
        return m.gridOriginY(panelTopPx);
    }

    public int panelWidthPx() {
        return panelWidthPx;
    }

    public FizzyComponentElement titleElement() {
        return titleElement;
    }

    @Override
    public FrameMetrics metrics() {
        return m;
    }

    @Override
    public void setLayout(int left, int top, int w, int h, boolean drawBottomEdge, boolean hasBelow) {
        this.layout = new Layout(left, top, w, h, drawBottomEdge, hasBelow);
    }

    @Override
    public Layout layout() {
        return layout;
    }
}
