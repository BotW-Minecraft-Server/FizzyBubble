package link.botwmcs.fizzy.ui.kernel.state;

public interface EffectHandle extends AutoCloseable {
    boolean isDisposed();

    @Override
    void close();
}
