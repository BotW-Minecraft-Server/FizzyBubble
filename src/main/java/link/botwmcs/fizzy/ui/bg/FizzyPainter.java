package link.botwmcs.fizzy.ui.bg;

import link.botwmcs.fizzy.Fizzy;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class FizzyPainter implements BackgroundPainter {
    private final ResourceLocation tex;
    private final PanelMetrics m;
    private final int panelWidthPx;  // 面板显示宽度（通常等于纹理宽）
    private final boolean dark;

    public FizzyPainter() {
        this(ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/panel_default.png"), PanelMetrics.ofDefault256x256());
    }

    public FizzyPainter(ResourceLocation tex, PanelMetrics metrics) {
        this(tex, metrics, metrics.texW(), false);
    }

    public FizzyPainter(ResourceLocation tex, PanelMetrics metrics, int panelWidthPx, boolean dark) {
        this.dark = dark;
        this.tex = tex;
        this.m = metrics;
        this.panelWidthPx = panelWidthPx;

    }

    /**
     * @param left 面板左上角 X（像素）
     * @param top  面板左上角 Y（像素）
     * @param w    期望绘制宽度（建议与 panelWidthPx 相同）
     * @param h    期望绘制高度 = metrics.totalHeightForRows(rows, drawBottomEdge)
     * @param drawBottomEdge true=Screen 绘底边；false=Menu 不绘底边
     */
    @Override
    public void paint(GuiGraphics g, int left, int top, int w, int h, boolean drawBottomEdge) {
        final int texW = m.texW(), texH = m.texH();
        final int drawW = panelWidthPx; // 固定使用面板宽，避免横向拉伸失真
        int y = top;

        // 计算行数（严格基于目标高度反推，防止调用方传错）
        int expectedRowsPart = h - m.slotStartTopPx() - m.bottomPadHeight() - (drawBottomEdge ? m.bottomEdgeHeight() : 0);
        if (expectedRowsPart % m.slotSizePx() != 0) {
            // 不整除就“就近取整”，防止溢出（也可抛错看你偏好）
            expectedRowsPart -= expectedRowsPart % m.slotSizePx();
        }
        int rows = Math.max(0, expectedRowsPart / m.slotSizePx());

        // 1) 顶部装饰（不含首行顶线）
        blit(g, left, y, 0, 0, drawW, m.slotStartTopPx(), texW, texH);
        y += m.slotStartTopPx();

        // 2) rows 行 slot：顶线(1) + 内容(16) + 底线(1)
        for (int i = 0; i < rows; i++) {
            // 顶线
            blit(g, left, y, 0, m.topBorderY(), drawW, 1, texW, texH);
            y += 1;
            // 内容（可重复）
            blit(g, left, y, 0, m.slotInnerStartY(), drawW, m.slotInnerHeight(), texW, texH);
            y += m.slotInnerHeight();
            // 底线
            blit(g, left, y, 0, m.bottomBorderY(), drawW, 1, texW, texH);
            y += 1;
        }

        // 3) slot 区域后的固定留白
        blit(g, left, y, 0, m.bottomPadStartY(), drawW, m.bottomPadHeight(), texW, texH);
        y += m.bottomPadHeight();

        // 4) 底边（仅 Screen）
        if (drawBottomEdge) {
            blit(g, left, y, 0, m.bottomEdgeStartY(), drawW, m.bottomEdgeHeight(), texW, texH);
            y += m.bottomEdgeHeight();
        }
        // 至此 y 应等于 top + h
    }

    private void blit(GuiGraphics g, int x, int y, int u, int v, int w, int h, int texW, int texH) {
//        g.blit(/*texture*/ null, 0,0,0,0,0,0,0,0); // 占位避免导入顺序警告
        // 正式调用（1.21.1 MojMaps 常用签名）：
        g.blit(this.tex, x, y, u, v, w, h, texW, texH);
    }

    /** 供宿主用来计算最终面板高度（避免魔法数字） */
    public int computeHeightPx(int rows, boolean includeBottomEdge) {
        return m.totalHeightForRows(rows, includeBottomEdge);
    }

    /** 供后续对齐 slot 网格使用 */
    public int gridOriginX(int panelLeftPx) { return m.gridOriginX(panelLeftPx); }
    public int gridOriginY(int panelTopPx)  { return m.gridOriginY(panelTopPx); }

    public int panelWidthPx() { return panelWidthPx; }

}
