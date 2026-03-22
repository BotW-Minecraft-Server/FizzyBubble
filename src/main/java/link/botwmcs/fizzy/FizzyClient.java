package link.botwmcs.fizzy;

import link.botwmcs.fizzy.client.bossbar.AnnounceMessageManager;
import link.botwmcs.fizzy.client.overlay.OverlayManager;
import link.botwmcs.fizzy.menu.FizzyMenus;
import link.botwmcs.fizzy.menu.FizzyTestMenuScreen;
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
        AnnounceMessageManager.render(event.getGuiGraphics(), sw, sh, pt);
    }
}
