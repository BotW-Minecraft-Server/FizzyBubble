package link.botwmcs.fizzy.proxy.rule;

import link.botwmcs.fizzy.proxy.api.KernelAttachSpec;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ProxyRuleResolver {
    private static final Comparator<ProxyRule> RULE_ORDER = Comparator
            .comparingInt(ProxyRule::priority)
            .thenComparing(rule -> rule.id().toString());

    private final ProxyRuleRegistry registry;

    public ProxyRuleResolver(ProxyRuleRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public ProxyResolutionResult resolve(ProxyBuildContext context) {
        List<ProxyRule> matchedRules = new ArrayList<>();
        List<KernelAttachSpec> patches = new ArrayList<>();

        for (ProxyRule rule : sortedRules()) {
            if (!rule.matches(context)) {
                continue;
            }
            matchedRules.add(rule);
            KernelAttachSpec patch = rule.build(context);
            if (patch != null && !patch.isEmpty()) {
                patches.add(patch);
            }
        }

        KernelAttachSpec merged = KernelSpecMerger.merge(patches);
        List<Identifier> ids = matchedRules.stream().map(ProxyRule::id).toList();
        return new ProxyResolutionResult(merged, ids, buildDebugSummary(matchedRules, merged));
    }

    private List<ProxyRule> sortedRules() {
        List<ProxyRule> rules = new ArrayList<>(registry.snapshot());
        rules.sort(RULE_ORDER);
        return rules;
    }

    private static String buildDebugSummary(List<ProxyRule> matchedRules, KernelAttachSpec mergedSpec) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("ProxyRuleResolver Debug").append('\n');
        sb.append("Matched rules: ").append(matchedRules.size()).append('\n');
        for (ProxyRule matchedRule : matchedRules) {
            sb.append("- ")
                    .append(matchedRule.id())
                    .append(" (priority=")
                    .append(matchedRule.priority())
                    .append(')')
                    .append('\n');
        }

        sb.append("Resolved policy: phase=")
                .append(policyName(mergedSpec.phasePolicy()))
                .append(", tooltip=")
                .append(mergedSpec.tooltipPolicy())
                .append(", input=")
                .append(mergedSpec.inputPolicy())
                .append('\n');

        sb.append("Resolved ui: baseKernel=")
                .append(mergedSpec.uiSpec().baseKernel() != null)
                .append(", frame=")
                .append(mergedSpec.uiSpec().frame() != null)
                .append(", bg=")
                .append(mergedSpec.uiSpec().background() != null)
                .append(", behind=")
                .append(mergedSpec.uiSpec().behind() != null)
                .append(", splitPainter=")
                .append(mergedSpec.uiSpec().splitPainter() != null)
                .append(", below=")
                .append(mergedSpec.uiSpec().below() != null)
                .append(", pads=")
                .append(mergedSpec.uiSpec().pads().size())
                .append(", splits=")
                .append(mergedSpec.uiSpec().splits().size());
        return sb.toString();
    }

    private static String policyName(Object policy) {
        return policy == null ? "null" : policy.getClass().getSimpleName();
    }
}

