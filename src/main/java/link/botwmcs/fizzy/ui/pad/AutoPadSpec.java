package link.botwmcs.fizzy.ui.pad;

import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import link.botwmcs.fizzy.ui.split.PixelSplitSpec;
import link.botwmcs.fizzy.ui.split.SlotSplitSpec;
import link.botwmcs.fizzy.ui.split.SplitMetrics;
import link.botwmcs.fizzy.ui.split.SplitSpec;
import link.botwmcs.fizzy.ui.split.SplitType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record AutoPadSpec(
        int rowStart,
        int colStart,
        int rowEnd,
        int colEnd,
        int baseInsetPx,
        List<SplitRect> splitRects,
        List<ElementPainter> elements
) implements PadSpec {
    public AutoPadSpec {
        if (rowStart < 1) {
            throw new IllegalArgumentException("rowStart must be >= 1");
        }
        if (colStart < 1) {
            throw new IllegalArgumentException("colStart must be >= 1");
        }
        if (rowEnd < rowStart) {
            throw new IllegalArgumentException("rowEnd must be >= rowStart");
        }
        if (colEnd < colStart) {
            throw new IllegalArgumentException("colEnd must be >= colStart");
        }
        if (baseInsetPx < 0) {
            throw new IllegalArgumentException("baseInsetPx must be >= 0");
        }
        splitRects = List.copyOf(splitRects);
        elements = List.copyOf(elements);
    }

    public static AutoPadSpec of(int rowStart,
                                 int colStart,
                                 int rowEnd,
                                 int colEnd,
                                 PadBuildContext context,
                                 List<ElementPainter> elements) {
        Objects.requireNonNull(context, "context");
        if (rowEnd > context.rows()) {
            throw new IllegalStateException("Auto pad rowEnd exceeds configured rows");
        }
        if (colEnd > context.cols()) {
            throw new IllegalStateException("Auto pad colEnd exceeds configured cols");
        }

        int baseInsetPx = Math.max(0, UiUnit.SLOT_BORDER_PX);
        List<SplitRect> splitRects = resolveSplitRects(context.splits(), context.splitMetrics());
        return new AutoPadSpec(rowStart, colStart, rowEnd, colEnd, baseInsetPx, splitRects, elements);
    }

    @Override
    public PadBounds resolve(FramePainter frame, FramePainter.SlotArea slotArea) {
        if (slotArea == null) {
            throw new IllegalStateException("Slot area is not available for auto pad.");
        }

        int localLeft = (colStart - 1) * UiUnit.SLOT_PX;
        int localTop = (rowStart - 1) * UiUnit.SLOT_PX;
        int localWidth = (colEnd - colStart + 1) * UiUnit.SLOT_PX;
        int localHeight = (rowEnd - rowStart + 1) * UiUnit.SLOT_PX;
        int localRight = localLeft + localWidth;
        int localBottom = localTop + localHeight;

        int insetLeft = baseInsetPx;
        int insetTop = baseInsetPx;
        int insetRight = baseInsetPx;
        int insetBottom = baseInsetPx;

        for (SplitRect splitRect : splitRects) {
            if (overlaps(splitRect.top(), splitRect.bottom(), localTop, localBottom)) {
                if (splitRect.left() < localLeft && splitRect.right() > localLeft) {
                    insetLeft = Math.max(insetLeft, splitRect.right() - localLeft);
                }
                if (splitRect.left() < localRight && splitRect.right() > localRight) {
                    insetRight = Math.max(insetRight, localRight - splitRect.left());
                }
            }

            if (overlaps(splitRect.left(), splitRect.right(), localLeft, localRight)) {
                if (splitRect.top() < localTop && splitRect.bottom() > localTop) {
                    insetTop = Math.max(insetTop, splitRect.bottom() - localTop);
                }
                if (splitRect.top() < localBottom && splitRect.bottom() > localBottom) {
                    insetBottom = Math.max(insetBottom, localBottom - splitRect.top());
                }
            }
        }

        int applyLeft = Math.min(Math.max(0, insetLeft), localWidth);
        int applyRight = Math.min(Math.max(0, insetRight), Math.max(0, localWidth - applyLeft));
        int applyTop = Math.min(Math.max(0, insetTop), localHeight);
        int applyBottom = Math.min(Math.max(0, insetBottom), Math.max(0, localHeight - applyTop));

        int finalLeft = slotArea.x() + localLeft + applyLeft;
        int finalTop = slotArea.y() + localTop + applyTop;
        int finalWidth = Math.max(0, localWidth - applyLeft - applyRight);
        int finalHeight = Math.max(0, localHeight - applyTop - applyBottom);

        return new PadBounds(finalLeft, finalTop, finalWidth, finalHeight);
    }

    private static boolean overlaps(int startA, int endA, int startB, int endB) {
        return Math.min(endA, endB) > Math.max(startA, startB);
    }

    private static List<SplitRect> resolveSplitRects(List<SplitSpec> splits, SplitMetrics metrics) {
        if (splits.isEmpty() || metrics == null) {
            return List.of();
        }

        List<SplitRect> out = new ArrayList<>(splits.size());
        for (SplitSpec split : splits) {
            if (split instanceof PixelSplitSpec pixelSplit) {
                resolvePixelSplitRect(pixelSplit, metrics, out);
                continue;
            }
            if (split instanceof SlotSplitSpec slotSplit) {
                resolveSlotSplitRect(slotSplit, metrics, out);
            }
        }
        return List.copyOf(out);
    }

    private static void resolvePixelSplitRect(PixelSplitSpec split, SplitMetrics metrics, List<SplitRect> out) {
        if (split.type() == SplitType.VERTICAL) {
            addRect(out, split.offsetX(), split.offsetY(), metrics.splitorWidth(SplitType.VERTICAL), split.lengthPx());
            return;
        }
        addRect(out, split.offsetX(), split.offsetY(), split.lengthPx(), metrics.splitorHeight(SplitType.HORIZONTAL));
    }

    private static void resolveSlotSplitRect(SlotSplitSpec split, SplitMetrics metrics, List<SplitRect> out) {
        if (split.colStart() == split.colEnd()) {
            int minRow = Math.min(split.rowStart(), split.rowEnd());
            int maxRow = Math.max(split.rowStart(), split.rowEnd());
            int top = (minRow - 1) * UiUnit.SLOT_PX;
            int bottom = maxRow * UiUnit.SLOT_PX;
            int anchor = split.colStart() * UiUnit.SLOT_PX - 1;
            int x = anchor - metrics.anchorOffset(SplitType.VERTICAL);
            addRect(out, x, top, metrics.splitorWidth(SplitType.VERTICAL), bottom - top);
            return;
        }

        if (split.rowStart() == split.rowEnd()) {
            int minCol = Math.min(split.colStart(), split.colEnd());
            int maxCol = Math.max(split.colStart(), split.colEnd());
            int left = (minCol - 1) * UiUnit.SLOT_PX;
            int right = maxCol * UiUnit.SLOT_PX;
            int anchor = split.rowStart() * UiUnit.SLOT_PX - 1;
            int y = anchor - metrics.anchorOffset(SplitType.HORIZONTAL);
            addRect(out, left, y, right - left, metrics.splitorHeight(SplitType.HORIZONTAL));
        }
    }

    private static void addRect(List<SplitRect> out, int left, int top, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        out.add(new SplitRect(left, top, width, height));
    }

    public record SplitRect(int left, int top, int width, int height) {
        public SplitRect {
            if (width <= 0) {
                throw new IllegalArgumentException("width must be > 0");
            }
            if (height <= 0) {
                throw new IllegalArgumentException("height must be > 0");
            }
        }

        public int right() {
            return left + width;
        }

        public int bottom() {
            return top + height;
        }
    }
}
