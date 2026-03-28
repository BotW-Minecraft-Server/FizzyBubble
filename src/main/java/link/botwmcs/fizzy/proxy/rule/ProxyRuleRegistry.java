package link.botwmcs.fizzy.proxy.rule;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ProxyRuleRegistry {
    private final Map<ResourceLocation, ProxyRule> rulesById = new LinkedHashMap<>();

    public synchronized void register(ProxyRule rule) {
        Objects.requireNonNull(rule, "rule");
        rulesById.put(rule.id(), rule);
    }

    public synchronized void unregister(ResourceLocation id) {
        rulesById.remove(id);
    }

    public synchronized Optional<ProxyRule> get(ResourceLocation id) {
        return Optional.ofNullable(rulesById.get(id));
    }

    public synchronized List<ProxyRule> snapshot() {
        return List.copyOf(rulesById.values());
    }

    public synchronized int size() {
        return rulesById.size();
    }

    public synchronized void clear() {
        rulesById.clear();
    }

    public synchronized List<ResourceLocation> ids() {
        return new ArrayList<>(rulesById.keySet());
    }
}

