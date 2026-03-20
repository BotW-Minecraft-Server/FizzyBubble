package link.botwmcs.fizzy.ui.kernel.overlay;

import link.botwmcs.fizzy.client.overlay.Anchor;

import java.util.List;

public final class OverlayLayoutEngine {
    private OverlayLayoutEngine() {
    }

    public static void layout(
            int screenWidth,
            int screenHeight,
            List<? extends OverlayRenderable> overlays,
            Anchor anchor,
            OverlayLayoutConfig layout,
            boolean forceAnchorIntoInstance
    ) {
        if (overlays.isEmpty()) {
            return;
        }

        int margin = layout.margin();
        int verticalGap = layout.verticalGap();
        int horizontalGap = layout.horizontalGap();
        int maxColumns = layout.maxColumns();

        int columnEdgeX = switch (anchor) {
            case TOP_LEFT, BOTTOM_LEFT -> margin;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - margin;
        };
        int currentY = switch (anchor) {
            case TOP_LEFT, TOP_RIGHT -> margin;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - margin;
        };
        int columnDirection = switch (anchor) {
            case TOP_LEFT, BOTTOM_LEFT -> 1;
            case TOP_RIGHT, BOTTOM_RIGHT -> -1;
        };
        int rowDirection = switch (anchor) {
            case TOP_LEFT, TOP_RIGHT -> 1;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> -1;
        };

        int column = 0;
        int currentColumnWidth = 0;

        for (OverlayRenderable overlay : overlays) {
            if (!overlay.isActive()) {
                continue;
            }
            if (forceAnchorIntoInstance) {
                overlay.assignAnchor(anchor);
            }

            int width = overlay.getWidthPx();
            int height = overlay.getHeightPx();

            boolean overflow = switch (anchor) {
                case TOP_LEFT, TOP_RIGHT -> currentY + rowDirection * height > screenHeight - margin;
                case BOTTOM_LEFT, BOTTOM_RIGHT -> currentY + rowDirection * height < margin;
            };
            if (overflow) {
                column++;
                if (column >= maxColumns) {
                    column = maxColumns - 1;
                }
                columnEdgeX += columnDirection * (currentColumnWidth + horizontalGap);
                currentY = switch (anchor) {
                    case TOP_LEFT, TOP_RIGHT -> margin;
                    case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - margin;
                };
                currentColumnWidth = 0;
            }

            currentColumnWidth = Math.max(currentColumnWidth, width);
            int x = switch (anchor) {
                case TOP_LEFT, BOTTOM_LEFT -> columnEdgeX;
                case TOP_RIGHT, BOTTOM_RIGHT -> columnEdgeX - width;
            };
            int y = switch (anchor) {
                case TOP_LEFT, TOP_RIGHT -> currentY;
                case BOTTOM_LEFT, BOTTOM_RIGHT -> currentY - height;
            };

            overlay.setTargetPos(x, y);
            currentY += rowDirection * (height + verticalGap);
        }
    }
}
