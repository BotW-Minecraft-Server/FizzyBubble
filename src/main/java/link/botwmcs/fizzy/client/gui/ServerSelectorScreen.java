package link.botwmcs.fizzy.client.gui;

import link.botwmcs.fizzy.client.elements.FizzyButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ServerSelectorScreen extends Screen {
    private final Screen lastScreen;
    private static final String ADDR_DIRECT  = "server.botwmcs.link"; // 北京直连
    private static final String ADDR_SOUTH   = "cdn1.botwmcs.link";   // 南部地区
    private static final String ADDR_WEST    = "cdn2.botwmcs.link";   // 西部地区

    private final ServerStatusPinger pinger = new ServerStatusPinger();
    private final Map<FizzyButton, ServerData> buttonData = new HashMap<>();

    private FizzyButton btnDirect, btnSouth, btnWest, btnBack;


    protected ServerSelectorScreen(Screen lastScreen) {
        super(Component.literal("SERVERSELECTOR"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int w  = 220;
        int h  = 20;
        int gap = 26;

        // 北京直连
        btnDirect = this.addRenderableWidget(FizzyButton.builder(Component.translatable("fizzy.gui.serverselector.addr1"),
                        b -> connect(ADDR_DIRECT, Component.translatable("fizzy.gui.serverselector.addr1").toString()))
                .pos(cx - w / 2, cy - gap - h)
                .size(w, h)
                .tooltip(Tooltip.create(Component.translatable("fizzy.gui.serverselector.addr1.region" + "\n" + ADDR_DIRECT)))
                .build());

        // 南部地区
        btnSouth = this.addRenderableWidget(FizzyButton.builder(Component.translatable("fizzy.gui.serverselector.addr2"),
                        b -> connect(ADDR_SOUTH, Component.translatable("fizzy.gui.serverselector.addr2").toString()))
                .pos(cx - w / 2, cy)
                .size(w, h)
                .tooltip(Tooltip.create(Component.literal("fizzy.gui.serverselector.addr2.region" + "\n" + ADDR_SOUTH)))
                .build());

        // 西部地区
        btnWest = this.addRenderableWidget(FizzyButton.builder(Component.translatable("fizzy.gui.serverselector.addr3"),
                        b -> connect(ADDR_WEST, Component.translatable("fizzy.gui.serverselector.addr3").toString()))
                .pos(cx - w / 2, cy + gap + h)
                .size(w, h)
                .tooltip(Tooltip.create(Component.literal("fizzy.gui.serverselector.addr3.region" + "\n" + ADDR_WEST)))
                .build());
        super.init();
    }


    private void connect(String addr, String displayName) {
        Minecraft mc = Minecraft.getInstance();
        ServerAddress parsed = ServerAddress.parseString(addr);
        ServerData data = new ServerData(displayName, addr, ServerData.Type.OTHER);
        ConnectScreen.startConnecting(this, mc, parsed, data, false, null);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
//        g.drawCenteredString(this.font, "请选择一个服务器入口", this.width / 2, this.height / 2 - 60, 0xFFFFFF);
        super.render(g, mouseX, mouseY, delta);
    }


    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void tick() {
        super.tick();
        this.pinger.tick(); // 驱动 pinger
    }

    @Override
    public void removed() {
        super.removed();
        this.pinger.removeAll(); // 防止泄露
    }

}
