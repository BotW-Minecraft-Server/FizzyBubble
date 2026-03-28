package link.botwmcs.fizzy.proxy.host;

import link.botwmcs.fizzy.proxy.api.HostStageCapabilities;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

public interface HostAdapter {
    ResourceLocation id();

    int priority();

    boolean supports(Screen screen);

    HostGeometry resolveGeometry(Screen screen);

    HostStageCapabilities stageCapabilities(Screen screen);
}

