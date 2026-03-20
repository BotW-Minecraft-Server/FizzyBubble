package link.botwmcs.fizzy.ui.kernel.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Scope implements AutoCloseable {
    private final List<AutoCloseable> resources = new ArrayList<>();
    private boolean closed;

    public synchronized <T extends AutoCloseable> T hold(T resource) {
        Objects.requireNonNull(resource, "resource");
        if (closed) {
            try {
                resource.close();
            } catch (Exception ignored) {
            }
            throw new IllegalStateException("Scope is already closed");
        }
        resources.add(resource);
        return resource;
    }

    public synchronized boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        RuntimeException first = null;
        for (int i = resources.size() - 1; i >= 0; i--) {
            AutoCloseable resource = resources.get(i);
            try {
                resource.close();
            } catch (RuntimeException ex) {
                if (first == null) {
                    first = ex;
                } else {
                    first.addSuppressed(ex);
                }
            } catch (Exception ex) {
                RuntimeException wrapped = new RuntimeException(ex);
                if (first == null) {
                    first = wrapped;
                } else {
                    first.addSuppressed(wrapped);
                }
            }
        }
        resources.clear();
        if (first != null) {
            throw first;
        }
    }
}
