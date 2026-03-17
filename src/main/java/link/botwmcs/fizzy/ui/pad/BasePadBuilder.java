package link.botwmcs.fizzy.ui.pad;

import link.botwmcs.fizzy.ui.core.FizzyGuiBuilder;
import link.botwmcs.fizzy.ui.element.ElementPainter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class BasePadBuilder<T extends BasePadBuilder<T>> {
    protected final FizzyGuiBuilder parent;
    protected final List<ElementPainter> elements = new ArrayList<>();

    protected BasePadBuilder(FizzyGuiBuilder parent) {
        this.parent = parent;
    }

    public T element(ElementPainter element) {
        elements.add(Objects.requireNonNull(element, "element"));
        return self();
    }

    public T elements(ElementPainter... elements) {
        Objects.requireNonNull(elements, "elements");
        for (ElementPainter element : elements) {
            this.elements.add(Objects.requireNonNull(element, "element"));
        }
        return self();
    }

    protected abstract T self();

    public FizzyGuiBuilder done() {
        return parent;
    }

    public abstract PadSpec toSpec(PadBuildContext context);
}
