package link.botwmcs.fizzy.ui.frame;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class EasyFrame implements FramePainter {
    private static final FrameMetrics METRICS = new FrameMetrics() {
        @Override
        public int texW() {
            return 0;
        }

        @Override
        public int texH() {
            return 0;
        }

        @Override
        public int panelW() {
            return 1;
        }

        @Override
        public int titleStartH() {
            return 0;
        }

        @Override
        public int slotStartTopPx() {
            return 0;
        }

        @Override
        public int slotStartLeftPx() {
            return 0;
        }

        @Override
        public int slotSizePx() {
            return 1;
        }

        @Override
        public int slotInnerStartY() {
            return 0;
        }

        @Override
        public int slotInnerHeight() {
            return 0;
        }

        @Override
        public int topBorderY() {
            return 0;
        }

        @Override
        public int bottomBorderY() {
            return 0;
        }

        @Override
        public int bottomPadStartY() {
            return 0;
        }

        @Override
        public int bottomPadHeight() {
            return 0;
        }

        @Override
        public int bottomEdgeStartY() {
            return 0;
        }

        @Override
        public int bottomEdgeHeight() {
            return 0;
        }

        @Override
        public int buttomInvExtraStartY() {
            return 0;
        }

        @Override
        public int buttomInvExtraHeight() {
            return 0;
        }
    };

    private Layout layout = new Layout(0, 0, 1, 1, false, false);

    @Override
    public void paint(GuiGraphicsExtractor g, int left, int top, int w, int h, boolean drawBottomEdge, boolean hasBelow) {
    }

    @Override
    public FrameMetrics metrics() {
        return METRICS;
    }

    @Override
    public void setLayout(int left, int top, int w, int h, boolean drawBottomEdge, boolean hasBelow) {
        Minecraft mc = Minecraft.getInstance();
        int resolvedLeft = left;
        int resolvedTop = top;
        int resolvedWidth = Math.max(1, w);
        int resolvedHeight = Math.max(1, h);
        if (mc != null && mc.getWindow() != null) {
            resolvedLeft = 0;
            resolvedTop = 0;
            resolvedWidth = Math.max(1, mc.getWindow().getGuiScaledWidth());
            resolvedHeight = Math.max(1, mc.getWindow().getGuiScaledHeight());
        }
        this.layout = new Layout(resolvedLeft, resolvedTop, resolvedWidth, resolvedHeight, drawBottomEdge, hasBelow);
    }

    @Override
    public Layout layout() {
        return this.layout;
    }

    @Override
    public SlotArea currentSlotArea() {
        Layout current = this.layout;
        return new SlotArea(current.left(), current.top(), current.w(), current.h());
    }

    @Override
    public SlotArea currentBackgroundArea() {
        return currentSlotArea();
    }

    @Override
    public BelowArea currentBelowArea() {
        Layout current = this.layout;
        if (!current.hasBelow()) {
            return null;
        }
        return new BelowArea(current.left(), current.top(), current.w(), current.h());
    }
}
