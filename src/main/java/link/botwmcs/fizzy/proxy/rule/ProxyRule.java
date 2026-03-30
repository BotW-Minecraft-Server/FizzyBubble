package link.botwmcs.fizzy.proxy.rule;

import link.botwmcs.fizzy.proxy.api.KernelAttachSpec;
import net.minecraft.resources.Identifier;

public interface ProxyRule {
    Identifier id();

    int priority();

    boolean matches(ProxyBuildContext context);

    KernelAttachSpec build(ProxyBuildContext context);
}

