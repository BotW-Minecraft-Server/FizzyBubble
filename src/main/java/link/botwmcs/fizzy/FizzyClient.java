package link.botwmcs.fizzy;

import link.botwmcs.fizzy.client.bossbar.AnnounceMessageManager;
import link.botwmcs.fizzy.client.formatting.emoji.builtin.IconEmojiPack;
import link.botwmcs.fizzy.client.overlay.OverlayManager;
import link.botwmcs.fizzy.menu.FizzyMenus;
import link.botwmcs.fizzy.menu.FizzyTestMenuScreen;
import link.botwmcs.fizzy.proxy.api.HostRenderStage;
import link.botwmcs.fizzy.proxy.runtime.ScreenProxyRuntime;
import link.botwmcs.fizzy.util.EnvDetector;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Fizzy.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = Fizzy.MODID, value = Dist.CLIENT)
public class FizzyClient {
    public FizzyClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        Fizzy.LOGGER.info("HELLO FROM CLIENT SETUP");
        Fizzy.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        AnnounceMessageManager.ensureRegistered();
        IconEmojiPack.registerBuiltin();
        if (EnvDetector.isLTSX()) {
            Fizzy.LOGGER.info("LTS-X detected, enabling compatibility mode.");
        }
    }

    @SubscribeEvent
    static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(FizzyMenus.FIZZY_TEST_MENU.get(), FizzyTestMenuScreen::new);
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
}
