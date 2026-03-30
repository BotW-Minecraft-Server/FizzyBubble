package link.botwmcs.fizzy.ui.kernel.layout;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class LayoutContainerNode extends LayoutNode {
    private final LayoutDirection direction;
    private final int gap;
    private final LayoutAlign alignItems;
    private final List<LayoutNode> children;

    LayoutContainerNode(
            LayoutDirection direction,
            int gap,
            LayoutAlign alignItems,
            List<LayoutNode> children,
            LayoutModifier modifier
    ) {
        super(modifier);
        this.direction = Objects.requireNonNull(direction, "direction");
        this.gap = Math.max(0, gap);
        this.alignItems = Objects.requireNonNull(alignItems, "alignItems");
        this.children = List.copyOf(children);
    }

    @Override
    protected void onMount(LayoutMountContext context, LayoutRect bounds) {
        List<LayoutPlacement> placements = layoutChildren(bounds, false);
        for (LayoutPlacement placement : placements) {
            placement.node().mount(context, placement.bounds());
        }
    }

    @Override
    protected void onRender(LayoutMountContext context, GuiGraphicsExtractor graphics, float partialTick, LayoutRect bounds) {
        if (!modifier().isVisible()) {
            return;
        }
        List<LayoutPlacement> placements = layoutChildren(bounds, true);
        for (LayoutPlacement placement : placements) {
            placement.node().render(context, graphics, partialTick, placement.bounds());
        }
    }

    private List<LayoutPlacement> layoutChildren(LayoutRect bounds, boolean visibleOnly) {
        if (children.isEmpty()) {
            return List.of();
        }

        boolean horizontal = direction == LayoutDirection.ROW;
        int availableMain = horizontal ? bounds.width() : bounds.height();
        int availableCross = horizontal ? bounds.height() : bounds.width();
        if (availableMain <= 0 || availableCross <= 0) {
            return List.of();
        }

        List<LayoutNode> participants = new ArrayList<>(children.size());
        for (LayoutNode child : children) {
            if (visibleOnly && !child.modifier().isVisible()) {
                continue;
            }
            participants.add(child);
        }
        if (participants.isEmpty()) {
            return List.of();
        }

        int count = participants.size();
        int[] mainSizes = new int[count];
        int[] crossSizes = new int[count];
        float[] grows = new float[count];

        int fixedMain = gap * Math.max(0, count - 1);
        float growSum = 0.0f;

        for (int i = 0; i < count; i++) {
            LayoutModifier modifier = participants.get(i).modifier();

            LayoutDimension mainDim = horizontal ? modifier.width() : modifier.height();
            LayoutDimension crossDim = horizontal ? modifier.height() : modifier.width();

            int minMain = horizontal ? modifier.minWidth() : modifier.minHeight();
            int minCross = horizontal ? modifier.minHeight() : modifier.minWidth();

            if (mainDim.type() == LayoutDimensionType.FILL) {
                float grow = modifier.growValue();
                if (grow <= 0.0f) {
                    grow = 1.0f;
                }
                grows[i] = grow;
                growSum += grow;
            } else {
                int resolvedMain = resolveWithMin(mainDim, availableMain, minMain);
                mainSizes[i] = resolvedMain;
                fixedMain += resolvedMain;
            }

            int resolvedCross;
            if (alignItems == LayoutAlign.STRETCH && crossDim.type() == LayoutDimensionType.AUTO) {
                resolvedCross = availableCross;
            } else {
                resolvedCross = resolveWithMin(crossDim, availableCross, minCross);
            }
            crossSizes[i] = clamp(resolvedCross, 0, availableCross);
        }

        int remaining = Math.max(0, availableMain - fixedMain);
        if (growSum > 0.0f && remaining > 0) {
            int distributed = 0;
            for (int i = 0; i < count; i++) {
                if (grows[i] <= 0.0f) {
                    continue;
                }
                int share;
                if (i == count - 1) {
                    share = Math.max(0, remaining - distributed);
                } else {
                    share = Math.round(remaining * (grows[i] / growSum));
                    share = Math.max(0, Math.min(share, remaining - distributed));
                }
                mainSizes[i] = share;
                distributed += share;
            }
        }

        List<LayoutPlacement> result = new ArrayList<>(count);
        int cursor = horizontal ? bounds.x() : bounds.y();
        int crossBase = horizontal ? bounds.y() : bounds.x();

        for (int i = 0; i < count; i++) {
            LayoutNode child = participants.get(i);
            int main = Math.max(0, mainSizes[i]);
            int cross = Math.max(0, crossSizes[i]);

            int crossPos;
            if (alignItems == LayoutAlign.STRETCH) {
                crossPos = crossBase;
                cross = availableCross;
            } else if (alignItems == LayoutAlign.CENTER) {
                crossPos = crossBase + (availableCross - cross) / 2;
            } else if (alignItems == LayoutAlign.END) {
                crossPos = crossBase + (availableCross - cross);
            } else {
                crossPos = crossBase;
            }

            LayoutRect childRect;
            if (horizontal) {
                childRect = LayoutRect.of(cursor, crossPos, main, cross);
            } else {
                childRect = LayoutRect.of(crossPos, cursor, cross, main);
            }
            result.add(new LayoutPlacement(child, childRect));
            cursor += main + gap;
        }

        return result;
    }

    private static int resolveWithMin(LayoutDimension dimension, int parent, int minValue) {
        int fallback = Math.max(0, minValue);
        int resolved = dimension.resolve(parent, fallback);
        return Math.max(fallback, resolved);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private record LayoutPlacement(LayoutNode node, LayoutRect bounds) {
    }
}
