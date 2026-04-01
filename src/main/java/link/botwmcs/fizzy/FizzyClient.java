package link.botwmcs.fizzy;

import link.botwmcs.fizzy.client.bossbar.AnnounceMessageManager;
import link.botwmcs.fizzy.client.formatting.emoji.builtin.IconEmojiPack;
import link.botwmcs.fizzy.client.overlay.OverlayManager;
import link.botwmcs.fizzy.client.overlay.content.SimpleTextPage;
import link.botwmcs.fizzy.network.FizzyNetworking;
import link.botwmcs.fizzy.network.s2c.AnnouncePayload;
import link.botwmcs.fizzy.network.s2c.HudOverlayPayload;
import link.botwmcs.fizzy.proxy.api.HostRenderStage;
import link.botwmcs.fizzy.proxy.runtime.ScreenProxyRuntime;
import link.botwmcs.fizzy.util.EnvDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Mod(value = Fizzy.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Fizzy.MODID, value = Dist.CLIENT)
public class FizzyClient {
    public FizzyClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(this::registerPayloads);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        FizzyNetworking.registerClientboundPayloads(
                event.registrar(Fizzy.MODID).optional(),
                FizzyClient::handleHudOverlay,
                FizzyClient::handleAnnounce
        );
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        Fizzy.LOGGER.info("HELLO FROM CLIENT SETUP");
        Fizzy.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        AnnounceMessageManager.ensureRegistered();
        IconEmojiPack.registerBuiltin();
        if (EnvDetector.isLTSX()) {
            Fizzy.LOGGER.info("LTS-X detected, enabling compatibility mode.");
        }
    }

    @SubscribeEvent
    static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        float pt = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        OverlayManager.renderAllLayers(event.getGuiGraphics(), sw, sh, pt);
    }

    @SubscribeEvent
    static void onScreenInitPost(ScreenEvent.Init.Post event) {
        ScreenProxyRuntime.instance().onScreenInit(event.getScreen());
    }

    @SubscribeEvent
    static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        ScreenProxyRuntime.instance().onRenderStage(
                event.getScreen(),
                HostRenderStage.SCREEN_PRE,
                event.getGuiGraphics(),
                (int) event.getMouseX(),
                (int) event.getMouseY(),
                0.0f
        );
    }

    @SubscribeEvent
    static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        ScreenProxyRuntime.instance().onRenderStage(
                event.getScreen(),
                HostRenderStage.SCREEN_POST,
                event.getGuiGraphics(),
                (int) event.getMouseX(),
                (int) event.getMouseY(),
                0.0f
        );
        OverlayManager.onMouseMoved(event.getMouseX(), event.getMouseY());
    }

    @SubscribeEvent
    static void onScreenMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (OverlayManager.onMouseClicked(event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
            return;
        }
        if (ScreenProxyRuntime.instance().onMouseClicked(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onScreenMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (OverlayManager.onMouseReleased(event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
            return;
        }
        if (ScreenProxyRuntime.instance().onMouseReleased(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onScreenMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        if (OverlayManager.onMouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton(), event.getDragX(), event.getDragY())) {
            event.setCanceled(true);
            return;
        }
        if (ScreenProxyRuntime.instance().onMouseDragged(
                event.getScreen(),
                event.getMouseX(),
                event.getMouseY(),
                event.getMouseButton(),
                event.getDragX(),
                event.getDragY()
        )) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onScreenMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (OverlayManager.onMouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDeltaX(), event.getScrollDeltaY())) {
            event.setCanceled(true);
            return;
        }
        if (ScreenProxyRuntime.instance().onMouseScrolled(
                event.getScreen(),
                event.getMouseX(),
                event.getMouseY(),
                event.getScrollDeltaX(),
                event.getScrollDeltaY()
        )) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onScreenClosing(ScreenEvent.Closing event) {
        ScreenProxyRuntime.instance().onScreenClosing(event.getScreen());
    }

    private static void handleHudOverlay(HudOverlayPayload payload, IPayloadContext context) {
        switch (payload.action()) {
            case SHOW -> OverlayManager.create()
                    .setTitle(Component.literal(payload.title()))
                    .setSlidingText(Component.literal(payload.scrollingText()))
                    .setContent(new SimpleTextPage(Component.literal(payload.text())))
                    .setScale(1.0F)
                    .show();
            case HIDE -> OverlayManager.hideAll();
        }
    }

    private static void handleAnnounce(AnnouncePayload payload, IPayloadContext context) {
        AnnounceMessageManager.show(Component.literal(payload.context()), payload.ticks());
    }
}
