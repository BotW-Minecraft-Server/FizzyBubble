package link.botwmcs.fizzy.proxy.rule;

import link.botwmcs.fizzy.proxy.host.HostAdapter;
import link.botwmcs.fizzy.proxy.host.HostGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

public record ProxyBuildContext(
        Minecraft minecraft,
        Screen screen,
        HostAdapter hostAdapter,
        HostGeometry geometry,
        @Nullable String sourceModId
) {
}

