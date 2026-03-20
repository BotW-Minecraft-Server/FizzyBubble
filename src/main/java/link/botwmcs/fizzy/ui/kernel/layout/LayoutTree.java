package link.botwmcs.fizzy.ui.kernel.layout;

import java.util.Objects;

public final class LayoutTree {
    private final LayoutNode root;

    LayoutTree(LayoutNode root) {
        this.root = Objects.requireNonNull(root, "root");
    }

    LayoutNode root() {
        return root;
    }
}
