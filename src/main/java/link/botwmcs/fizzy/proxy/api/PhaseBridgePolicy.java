package link.botwmcs.fizzy.proxy.api;

import link.botwmcs.fizzy.ui.kernel.render.UiRenderPhase;

public interface PhaseBridgePolicy {
    HostRenderStage map(UiRenderPhase fizzyPhase, HostStageCapabilities capabilities);
}

