package link.botwmcs.fizzy.ui.pad;

import link.botwmcs.fizzy.ui.core.FizzyGuiBuilder;
import link.botwmcs.fizzy.ui.element.ElementPainter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PadBuilder {
    private final FizzyGuiBuilder parent;
    public final int rowStart;
    public final int colStart;
    public final int rowEnd;
    public final int colEnd;
    public final List<ElementPainter> elements = new ArrayList<>();

    public PadBuilder(FizzyGuiBuilder parent, int rowStart, int colStart, int rowEnd, int colEnd) {
        this.parent = parent;
        this.rowStart = rowStart;
        this.colStart = colStart;
        this.rowEnd = rowEnd;
        this.colEnd = colEnd;
    }

    public PadBuilder element(ElementPainter element) {
        elements.add(Objects.requireNonNull(element, "element"));
        return this;
    }

    public FizzyGuiBuilder done() {
        return parent;
    }
}
