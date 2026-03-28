package link.botwmcs.fizzy.proxy.api;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public final class HostStageCapabilities {
    private final EnumSet<HostRenderStage> supportedStages;

    private HostStageCapabilities(EnumSet<HostRenderStage> supportedStages) {
        this.supportedStages = EnumSet.copyOf(supportedStages);
    }

    public static HostStageCapabilities all() {
        return new HostStageCapabilities(EnumSet.allOf(HostRenderStage.class));
    }

    public static HostStageCapabilities of(HostRenderStage... stages) {
        if (stages == null || stages.length == 0) {
            return new HostStageCapabilities(EnumSet.noneOf(HostRenderStage.class));
        }
        return new HostStageCapabilities(EnumSet.copyOf(Arrays.asList(stages)));
    }

    public boolean supports(HostRenderStage stage) {
        return supportedStages.contains(stage);
    }

    public Set<HostRenderStage> stages() {
        return Set.copyOf(supportedStages);
    }
}

