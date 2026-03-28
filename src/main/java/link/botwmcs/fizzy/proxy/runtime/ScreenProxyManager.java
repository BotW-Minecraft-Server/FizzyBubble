package link.botwmcs.fizzy.proxy.runtime;

import link.botwmcs.fizzy.proxy.api.KernelAttachSpec;
import link.botwmcs.fizzy.proxy.host.HostAdapter;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public final class ScreenProxyManager {
    private final Map<Screen, ScreenProxySession> sessions = new WeakHashMap<>();

    public synchronized ScreenProxySession ensure(Screen screen, Supplier<ScreenProxySession> factory) {
        Objects.requireNonNull(screen, "screen");
        Objects.requireNonNull(factory, "factory");

        ScreenProxySession existing = sessions.get(screen);
        if (existing != null && !existing.isClosed()) {
            return existing;
        }

        ScreenProxySession created = factory.get();
        sessions.put(screen, created);
        return created;
    }

    public synchronized ScreenProxySession ensure(Screen screen, HostAdapter hostAdapter, KernelAttachSpec spec) {
        return ensure(screen, () -> new ScreenProxySession(screen, hostAdapter, spec));
    }

    public synchronized @Nullable ScreenProxySession get(Screen screen) {
        return sessions.get(screen);
    }

    public synchronized void remove(Screen screen) {
        ScreenProxySession removed = sessions.remove(screen);
        if (removed != null) {
            removed.close();
        }
    }

    public synchronized void clear() {
        List<ScreenProxySession> snapshot = new ArrayList<>(sessions.values());
        sessions.clear();
        for (ScreenProxySession session : snapshot) {
            session.close();
        }
    }

    public synchronized int activeSessionCount() {
        int count = 0;
        for (ScreenProxySession session : sessions.values()) {
            if (!session.isClosed()) {
                count++;
            }
        }
        return count;
    }
}
