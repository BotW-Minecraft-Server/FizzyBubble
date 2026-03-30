package link.botwmcs.fizzy.proxy.rule;

import link.botwmcs.fizzy.proxy.api.KernelAttachSpec;
import net.minecraft.resources.Identifier;

import java.util.List;

public record ProxyResolutionResult(
        KernelAttachSpec mergedSpec,
        List<Identifier> appliedRuleIds,
        String debugSummary
) {
}

