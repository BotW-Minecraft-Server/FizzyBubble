package link.botwmcs.fizzy.client.overlay;

import link.botwmcs.fizzy.api.IOverlayContent;
import link.botwmcs.fizzy.client.overlay.content.SimpleTextPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class OverlayManager {
    private static final List<CreateHudOverlay> INSTANCES = new ArrayList<>();
    // 布局参数
    private static int margin = 8;     // 边距
    private static int vGap = 6;       // 垂直间距
    private static int hGap = 6;       // 列间距
    private static int maxColumns = 3; // 最多列数

    public static CreateHudOverlay create() {
        CreateHudOverlay o = new CreateHudOverlay(new SimpleTextPage(Component.literal("Hello Overlay Content!")));
        INSTANCES.add(o);
        return o;
    }

    public static CreateHudOverlay create(IOverlayContent content) {
        CreateHudOverlay o = new CreateHudOverlay(content);
        INSTANCES.add(o);
        return o;
    }

    public static void remove(CreateHudOverlay o) {
        INSTANCES.remove(o);
    }

    // 不要直接用 removeAll，改成调用 hide()
    public static void hideAll() {
        for (CreateHudOverlay o : INSTANCES) {
            o.hide();
        }
    }

    private static void sweepInactive() {
        INSTANCES.removeIf(o -> {
            boolean dead = !o.isActive();
            if (dead) o.dispose();
            return dead;
        });

    }

    public static void clear() {
        INSTANCES.clear();
    }

    /** 情况 A：使用“全局 Anchor”统一排版 */
    public static void renderAll(GuiGraphics g, int sw, int sh, float pt, Anchor anchor) {
        layoutAndRender(g, sw, sh, pt, INSTANCES, anchor, true);
    }

    /** 情况 B：每实例 Anchor（同 Anchor 分组独立排版） */
    public static void renderAllPerInstance(GuiGraphics g, int sw, int sh, float pt) {
        // 四个角各排一次
        layoutAndRender(g, sw, sh, pt, INSTANCES.stream().filter(o -> o.getAnchor()==Anchor.TOP_LEFT).toList(),     Anchor.TOP_LEFT,     false);
        layoutAndRender(g, sw, sh, pt, INSTANCES.stream().filter(o -> o.getAnchor()==Anchor.TOP_RIGHT).toList(),    Anchor.TOP_RIGHT,    false);
        layoutAndRender(g, sw, sh, pt, INSTANCES.stream().filter(o -> o.getAnchor()==Anchor.BOTTOM_LEFT).toList(),  Anchor.BOTTOM_LEFT,  false);
        layoutAndRender(g, sw, sh, pt, INSTANCES.stream().filter(o -> o.getAnchor()==Anchor.BOTTOM_RIGHT).toList(), Anchor.BOTTOM_RIGHT, false);
    }

    // ===== 核心：按指定 Anchor 堆叠并自动换列 =====
    private static void layoutAndRender(
            GuiGraphics g, int sw, int sh, float pt,
            List<CreateHudOverlay> list,
            Anchor anchor, boolean forceAnchorIntoInstance
    ) {
        if (list.isEmpty()) return;

        // 初始列“边界 X”
        int colEdgeX = switch (anchor) {
            case TOP_LEFT, BOTTOM_LEFT -> margin;           // 左侧列的左边缘
            case TOP_RIGHT, BOTTOM_RIGHT -> sw - margin;    // 右侧列的右边缘
        };

        // 当前列内“起始 Y”
        int curY = switch (anchor) {
            case TOP_LEFT, TOP_RIGHT     -> margin;         // 从上往下
            case BOTTOM_LEFT, BOTTOM_RIGHT -> sh - margin;  // 从下往上
        };

        // 列移动方向（左右）
        int colDir = switch (anchor) {
            case TOP_LEFT, BOTTOM_LEFT   -> +1; // 新列向右
            case TOP_RIGHT, BOTTOM_RIGHT -> -1; // 新列向左
        };

        // 行堆叠方向（上下）
        int rowDir = switch (anchor) {
            case TOP_LEFT, TOP_RIGHT     -> +1; // 往下叠
            case BOTTOM_LEFT, BOTTOM_RIGHT -> -1; // 往上叠
        };

        int col = 0;
        int colWidth = 0; // 当前列内最大宽度（用于下一列定位）

        for (CreateHudOverlay o : list) {
            if (!o.isActive()) continue; // 未激活跳过
            if (forceAnchorIntoInstance) o.setAnchor(anchor);

            int w = o.getWidthPx();
            int h = o.getHeightPx();

            // 判断是否需要换列
            boolean overflow = switch (anchor) {
                case TOP_LEFT, TOP_RIGHT -> (curY + (rowDir * (h))) > (sh - margin);        // 下边越界
                case BOTTOM_LEFT, BOTTOM_RIGHT -> (curY + (rowDir * (h))) < margin;         // 上边越界（向上堆叠）
            };

            if (overflow) {
                col++;
                if (col >= maxColumns) col = maxColumns - 1; // 到顶后可以选择覆盖最后一列

                // 列移动：以上一列的“列宽 + 间距”为步长
                colEdgeX += colDir * (colWidth + hGap);
                // 新列重置
                curY = switch (anchor) {
                    case TOP_LEFT, TOP_RIGHT     -> margin;
                    case BOTTOM_LEFT, BOTTOM_RIGHT -> sh - margin;
                };
                colWidth = 0;
            }

            // 更新当前列的最大宽度
            colWidth = Math.max(colWidth, w);

            // 计算该 overlay 的左上角坐标
            int x = switch (anchor) {
                case TOP_LEFT, BOTTOM_LEFT   -> colEdgeX;      // 左列：x 从列左边缘开始
                case TOP_RIGHT, BOTTOM_RIGHT -> colEdgeX - w;  // 右列：x = 列右边缘 - 自身宽
            };

            int y = switch (anchor) {
                case TOP_LEFT, TOP_RIGHT     -> curY;          // 从上往下：y 直接用 curY
                case BOTTOM_LEFT, BOTTOM_RIGHT -> curY - h;    // 从下往上：y = curY - 高度
            };

            // 设置目标位置并推进行指针
            o.setTargetPos(x, y);
            curY += rowDir * (h + vGap);
        }

        // 最后统一渲染
        for (CreateHudOverlay o : list) {
            o.render(g, pt);
        }
    }

    // 可选：外部调整布局参数
    public static void setLayout(int marginPx, int vGapPx, int hGapPx, int maxCols) {
        margin = Math.max(0, marginPx);
        vGap = Math.max(0, vGapPx);
        hGap = Math.max(0, hGapPx);
        maxColumns = Math.max(1, maxCols);
    }

}
