package link.botwmcs.fizzy.ui.element.animate;

import link.botwmcs.fizzy.ui.element.ElementPainter;

public interface AnimatableElement extends ElementPainter {
    default ElementPainter animated(ElementAnimation... animations) {
        return AnimatedElement.builder(this).addAll(animations).build();
    }
}
