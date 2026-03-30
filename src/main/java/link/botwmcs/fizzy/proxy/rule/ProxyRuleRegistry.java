package link.botwmcs.fizzy.proxy.rule;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ProxyRuleRegistry {
    private final Map<Identifier, ProxyRule> rulesById = new LinkedHashMap<>();

    public synchronized void register(ProxyRule rule) {
        Objects.requireNonNull(rule, "rule");
        rulesById.put(rule.id(), rule);
    }

    public synchronized void unregister(Identifier id) {
        rulesById.remove(id);
    }

    public synchronized Optional<ProxyRule> get(Identifier id) {
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

    public synchronized List<Identifier> ids() {
        return new ArrayList<>(rulesById.keySet());
    }
}

