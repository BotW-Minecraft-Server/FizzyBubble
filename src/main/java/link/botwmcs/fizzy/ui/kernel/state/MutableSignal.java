package link.botwmcs.fizzy.ui.kernel.state;

import java.util.Objects;
import java.util.function.UnaryOperator;

public interface MutableSignal<T> extends Signal<T> {
    void set(T value);

    default void update(UnaryOperator<T> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        set(mapper.apply(get()));
    }
}
