package link.botwmcs.fizzy.ui.frame;

import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.ui.element.component.FizzyComponentElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.Consumer;

public class MotiveFrame implements FramePainter {
    private static final FrameMetrics DEFAULT_METRICS = MotiveFrameMetrics.ofDefault256x256();
    private static final Identifier DEFAULT_PANEL_TEXTURE =
            Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/blank_sharp.png");
    private static final Identifier DEFAULT_DARK_PANEL_TEXTURE =
            Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/blank_sharp_dark.png");
    private static final int TOP_CAP_HEIGHT = 3;
    private static final int BOTTOM_CAP_HEIGHT = 3;

    private final Identifier tex;
    private final FrameMetrics m;
    private final int panelWidthPx;
    private final FizzyComponentElement titleElement;
    private final boolean dark;
    private Layout layout;

    public MotiveFrame() {
        this(Component.empty());
    }

    public MotiveFrame(Component title) {
        this(title, false);
    }

    public MotiveFrame(FizzyComponentElement titleElement) {
        this(titleElement, false);
    }

    public MotiveFrame(Component title, boolean dark) {
        this(DEFAULT_PANEL_TEXTURE, DEFAULT_METRICS, DEFAULT_METRICS.panelW(), title, dark);
    }

    public MotiveFrame(FizzyComponentElement titleElement, boolean dark) {
        this(DEFAULT_PANEL_TEXTURE, DEFAULT_METRICS, DEFAULT_METRICS.panelW(), titleElement, dark);
    }

    public MotiveFrame(Component title, Consumer<FizzyComponentElement.Builder> titleCustomizer) {
        this(
                DEFAULT_PANEL_TEXTURE,
                DEFAULT_METRICS,
                DEFAULT_METRICS.panelW(),
                FrameTitleSupport.defaultTitle(title, false, titleCustomizer),
                false
        );
    }

    public MotiveFrame(Component title, boolean dark, Consumer<FizzyComponentElement.Builder> titleCustomizer) {
        this(
                DEFAULT_PANEL_TEXTURE,
                DEFAULT_METRICS,
                DEFAULT_METRICS.panelW(),
                FrameTitleSupport.defaultTitle(title, dark, titleCustomizer),
                dark
        );
    }

    public MotiveFrame(Identifier tex, FrameMetrics metrics, Component title) {
        this(tex, metrics, metrics.panelW(), title, false);
    }

    public MotiveFrame(Identifier tex, FrameMetrics metrics, FizzyComponentElement titleElement) {
        this(tex, metrics, metrics.panelW(), titleElement, false);
    }

    public MotiveFrame(Identifier tex, FrameMetrics metrics, int panelWidthPx, Component title, boolean dark) {
        this(tex, metrics, panelWidthPx, FrameTitleSupport.defaultTitle(title, dark), dark);
    }

    public MotiveFrame(Identifier tex, FrameMetrics metrics, int panelWidthPx, FizzyComponentElement titleElement, boolean dark) {
        this.tex = tex;
        this.m = metrics;
        this.panelWidthPx = panelWidthPx;
        this.titleElement = Objects.requireNonNull(titleElement, "titleElement");
        this.dark = dark;
    }

    @Override
    public void paint(GuiGraphicsExtractor g, int left, int top, int w, int h, boolean drawBottomEdge, boolean hasBelow) {
        final int texW = m.texW();
        final int texH = m.texH();
        final int drawW = panelWidthPx;

        if (h <= TOP_CAP_HEIGHT + BOTTOM_CAP_HEIGHT) {
            blit(g, left, top, 0, 0, drawW, h, texW, texH);
        } else {
            blit(g, left, top, 0, 0, drawW, TOP_CAP_HEIGHT, texW, texH);
            int y = top + TOP_CAP_HEIGHT;
            int rest = h - TOP_CAP_HEIGHT - BOTTOM_CAP_HEIGHT;
            int middleV = TOP_CAP_HEIGHT;
            int middleSrcH = Math.max(1, texH - TOP_CAP_HEIGHT - BOTTOM_CAP_HEIGHT);
            while (rest > 0) {
                int step = Math.min(rest, middleSrcH);
                blit(g, left, y, 0, middleV, drawW, step, texW, texH);
                y += step;
                rest -= step;
            }
            blit(g, left, y, 0, texH - BOTTOM_CAP_HEIGHT, drawW, BOTTOM_CAP_HEIGHT, texW, texH);
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
