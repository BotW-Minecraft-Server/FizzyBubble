package link.botwmcs.fizzy.menu;

import link.botwmcs.fizzy.network.c2s.FizzyMenuPingC2SPayload;
import link.botwmcs.fizzy.ui.behind.VanillaBehind;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.core.FizzyGuiBuilder;
import link.botwmcs.fizzy.ui.core.HostType;
import link.botwmcs.fizzy.ui.element.below.DoubleButtonBelow;
import link.botwmcs.fizzy.ui.element.slot.SlotElement;
import link.botwmcs.fizzy.ui.frame.FizzyFrame;
import link.botwmcs.fizzy.ui.host.FizzyMenuScreenHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.entity.player.Inventory;

public final class FizzyTestMenuScreen extends FizzyMenuScreenHost<FizzyTestMenu> {
    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 10;
    private static final int BAR_X = 28;
    private static final int BAR_Y = 8;

    public FizzyTestMenuScreen(FizzyTestMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, createGui(title));
    }

    private static FizzyGui createGui(Component title) {
        int rows = FizzyTestMenu.CONTAINER_ROWS;
        FizzyFrame frame = new FizzyFrame(title);
        int width = frame.panelWidthPx();
        int height = frame.computeHeightPx(rows, false, true);
        DoubleButtonBelow belowButtons = new DoubleButtonBelow(
                Component.literal("Sync"),
                btn -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null && mc.getConnection() != null) {
                        mc.getConnection().send(new ServerboundCustomPayloadPacket(new FizzyMenuPingC2SPayload(mc.player.containerMenu.containerId)));
                    }
                },
                Component.literal("Close"),
                btn -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null) {
                        mc.player.closeContainer();
                    }
                }
        );

        return FizzyGuiBuilder.start()
                .host(HostType.MENU)
                .sizeSlots(rows)
                .behind(new VanillaBehind())
                .frame(frame)
                .overrideSizePx(width, height)
                .pad(1, 1, rows, 9)
                .element(new SlotElement()).done()
                .below(belowButtons) // No-below test: comment this line to verify default chin fallback rendering.
                .build();
    }

    @Override
    protected void init() {
        super.init();
        requestServerSync();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.tickCount % 40 == 0) {
            requestServerSync();
        }
    }

    @Override
    protected void renderCustomMenuBackground(GuiGraphicsExtractor g, float partialTick, int mouseX, int mouseY) {
        int barLeft = this.leftPos + BAR_X;
        int barTop = this.topPos + BAR_Y;
        int barRight = barLeft + BAR_WIDTH;
        int barBottom = barTop + BAR_HEIGHT;
//        g.fill(barLeft, barTop, barRight, barBottom, 0xFF242424);
//        g.fill(barLeft + 1, barTop + 1, barRight - 1, barBottom - 1, 0xFF101010);

        int progress = this.menu.progress();
        int max = this.menu.maxProgress();
        int fillWidth = Math.max(0, Math.min(BAR_WIDTH - 2, (BAR_WIDTH - 2) * progress / max));
        if (fillWidth > 0) {
//            g.fill(barLeft + 1, barTop + 1, barLeft + 1 + fillWidth, barBottom - 1, 0xFF45B54A);
        }
    }

    @Override
    protected void renderCustomMenuForeground(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int progress = this.menu.progress();
        int max = this.menu.maxProgress();
        String progressText = "Progress: " + progress + " / " + max;
//        g.drawString(this.font, progressText, 8, 22, 0xFFFFFF, false);
//        g.drawString(this.font, this.menu.serverStatusText(), 8, 34, 0xFFCCE6FF, false);
    }

    private void requestServerSync() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new FizzyMenuPingC2SPayload(this.menu.containerId)));
        }
    }
}
