package link.botwmcs.fizzy.proxy.host;

import link.botwmcs.fizzy.proxy.api.HostRenderStage;
import link.botwmcs.fizzy.proxy.api.HostStageCapabilities;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;

public final class GenericScreenHostAdapter implements HostAdapter {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("fizzy", "generic_screen");
    private static final HostStageCapabilities STAGES = HostStageCapabilities.of(
            HostRenderStage.SCREEN_PRE,
            HostRenderStage.SOURCE_CONTENT_PRE,
            HostRenderStage.SOURCE_CONTENT_POST,
            HostRenderStage.SCREEN_POST
    );

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public int priority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean supports(Screen screen) {
        return screen != null;
    }

    @Override
    public HostGeometry resolveGeometry(Screen screen) {
        return new HostGeometry(
                0,
                0,
                Math.max(0, screen.width),
                Math.max(0, screen.height),
                null
        );
    }

    @Override
    public HostStageCapabilities stageCapabilities(Screen screen) {
        return STAGES;
    }
}
