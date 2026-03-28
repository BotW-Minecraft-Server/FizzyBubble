package link.botwmcs.fizzy.proxy.api;

import link.botwmcs.fizzy.ui.kernel.render.UiRenderPhase;

public final class DefaultPhaseBridgePolicy implements PhaseBridgePolicy {
    public static final DefaultPhaseBridgePolicy INSTANCE = new DefaultPhaseBridgePolicy();

    private DefaultPhaseBridgePolicy() {
    }

    @Override
    public HostRenderStage map(UiRenderPhase fizzyPhase, HostStageCapabilities capabilities) {
        return switch (fizzyPhase) {
            case BEHIND, BACKGROUND -> firstSupported(
                    capabilities,
                    HostRenderStage.SOURCE_BG_PRE,
                    HostRenderStage.SOURCE_CONTENT_PRE,
                    HostRenderStage.SCREEN_PRE,
                    HostRenderStage.SCREEN_POST
            );
            case FRAME, SPLIT, ELEMENT, WIDGET -> firstSupported(
                    capabilities,
                    HostRenderStage.SOURCE_CONTENT_POST,
                    HostRenderStage.SOURCE_CONTENT_PRE,
                    HostRenderStage.SCREEN_POST,
                    HostRenderStage.SCREEN_PRE
            );
            case TOOLTIP -> firstSupported(
                    capabilities,
                    HostRenderStage.SOURCE_TOOLTIP_POST,
                    HostRenderStage.SOURCE_CONTENT_POST,
                    HostRenderStage.SCREEN_POST
            );
            case OVERLAY -> firstSupported(
                    capabilities,
                    HostRenderStage.SCREEN_POST,
                    HostRenderStage.SOURCE_TOOLTIP_POST,
                    HostRenderStage.SOURCE_CONTENT_POST
            );
        };
    }

    private static HostRenderStage firstSupported(
            HostStageCapabilities capabilities,
            HostRenderStage... candidates
    ) {
        for (HostRenderStage candidate : candidates) {
            if (capabilities.supports(candidate)) {
                return candidate;
            }
        }
        return HostRenderStage.SCREEN_POST;
    }
}

