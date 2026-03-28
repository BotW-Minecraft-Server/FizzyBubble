package link.botwmcs.fizzy.proxy.host;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class HostAdapterRegistry {
    private static final Comparator<HostAdapter> PRIORITY_DESC = Comparator
            .comparingInt(HostAdapter::priority)
            .reversed()
            .thenComparing(adapter -> adapter.id().toString());

    private final Map<ResourceLocation, HostAdapter> adapters = new LinkedHashMap<>();

    public synchronized void register(HostAdapter adapter) {
        Objects.requireNonNull(adapter, "adapter");
        adapters.put(adapter.id(), adapter);
    }

    public synchronized void unregister(ResourceLocation id) {
        adapters.remove(id);
    }

    public synchronized List<HostAdapter> snapshot() {
        return List.copyOf(adapters.values());
    }

    public synchronized Optional<HostAdapter> resolve(Screen screen) {
        List<HostAdapter> candidates = new ArrayList<>(adapters.values());
        candidates.sort(PRIORITY_DESC);
        for (HostAdapter candidate : candidates) {
            if (candidate.supports(screen)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }
}

