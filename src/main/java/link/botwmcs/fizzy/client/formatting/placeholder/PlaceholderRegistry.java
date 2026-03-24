package link.botwmcs.fizzy.client.formatting.placeholder;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class PlaceholderRegistry {
    private static final Map<String, PlaceholderResolver> REGISTRY = new ConcurrentHashMap<>();
    private static final AtomicLong VERSION = new AtomicLong(1L);
    private static final AtomicBoolean DEFAULTS_REGISTERED = new AtomicBoolean(false);

    private PlaceholderRegistry() {
    }

    public static void register(PlaceholderResolver resolver) {
        if (resolver == null) {
            return;
        }
        String id = normalize(resolver.id());
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("PlaceholderResolver id must not be blank");
        }
        REGISTRY.put(id, resolver);
        VERSION.incrementAndGet();
    }

    public static void unregister(String id) {
        String normalized = normalize(id);
        if (normalized == null || normalized.isEmpty()) {
            return;
        }
        if (REGISTRY.remove(normalized) != null) {
            VERSION.incrementAndGet();
        }
    }

    public static Optional<PlaceholderResolver> find(String id) {
        String normalized = normalize(id);
        if (normalized == null || normalized.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(REGISTRY.get(normalized));
    }

    public static Collection<PlaceholderResolver> allResolvers() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static long version() {
        return VERSION.get();
    }

    public static void ensureDefaults() {
        DEFAULTS_REGISTERED.compareAndSet(false, true);
    }

    private static String normalize(String id) {
        return id == null ? null : id.trim().toLowerCase();
    }
}
