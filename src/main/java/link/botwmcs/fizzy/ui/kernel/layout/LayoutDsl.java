package link.botwmcs.fizzy.ui.kernel.layout;

import link.botwmcs.fizzy.ui.element.ElementPainter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class LayoutDsl {
    private LayoutDsl() {
    }

    public static LayoutModifier modifier() {
        return LayoutModifier.DEFAULT;
    }

    public static LayoutTreePainter painter(LayoutTree tree) {
        return new LayoutTreePainter(tree);
    }

    public static LayoutTree row(Consumer<ContainerBuilder> block) {
        return row(LayoutModifier.DEFAULT, 0, LayoutAlign.START, block);
    }

    public static LayoutTree row(LayoutModifier modifier, int gap, LayoutAlign alignItems, Consumer<ContainerBuilder> block) {
        return new LayoutTree(buildContainer(LayoutDirection.ROW, modifier, gap, alignItems, block));
    }

    public static LayoutTree column(Consumer<ContainerBuilder> block) {
        return column(LayoutModifier.DEFAULT, 0, LayoutAlign.START, block);
    }

    public static LayoutTree column(LayoutModifier modifier, int gap, LayoutAlign alignItems, Consumer<ContainerBuilder> block) {
        return new LayoutTree(buildContainer(LayoutDirection.COLUMN, modifier, gap, alignItems, block));
    }

    private static LayoutContainerNode buildContainer(
            LayoutDirection direction,
            LayoutModifier modifier,
            int gap,
            LayoutAlign alignItems,
            Consumer<ContainerBuilder> block
    ) {
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(modifier, "modifier");
        Objects.requireNonNull(alignItems, "alignItems");
        Objects.requireNonNull(block, "block");

        ContainerBuilder builder = new ContainerBuilder();
        block.accept(builder);
        return new LayoutContainerNode(direction, gap, alignItems, builder.children, modifier);
    }

    public static final class ContainerBuilder {
        private final List<LayoutNode> children = new ArrayList<>();

        public ContainerBuilder element(ElementPainter element) {
            return element(element, LayoutModifier.DEFAULT.autoWidth().autoHeight().grow(0.0f));
        }

        public ContainerBuilder element(ElementPainter element, LayoutModifier modifier) {
            children.add(new LayoutLeafNode(element, Objects.requireNonNull(modifier, "modifier")));
            return this;
        }

        public ContainerBuilder spacer() {
            return spacer(LayoutModifier.DEFAULT.autoWidth().autoHeight().grow(1.0f));
        }

        public ContainerBuilder spacer(LayoutModifier modifier) {
            children.add(new LayoutSpacerNode(Objects.requireNonNull(modifier, "modifier")));
            return this;
        }

        public ContainerBuilder row(Consumer<ContainerBuilder> block) {
            return row(LayoutModifier.DEFAULT, 0, LayoutAlign.START, block);
        }

        public ContainerBuilder row(LayoutModifier modifier, int gap, LayoutAlign alignItems, Consumer<ContainerBuilder> block) {
            children.add(buildContainer(LayoutDirection.ROW, modifier, gap, alignItems, block));
            return this;
        }

        public ContainerBuilder column(Consumer<ContainerBuilder> block) {
            return column(LayoutModifier.DEFAULT, 0, LayoutAlign.START, block);
        }

        public ContainerBuilder column(LayoutModifier modifier, int gap, LayoutAlign alignItems, Consumer<ContainerBuilder> block) {
            children.add(buildContainer(LayoutDirection.COLUMN, modifier, gap, alignItems, block));
            return this;
        }
    }
}
