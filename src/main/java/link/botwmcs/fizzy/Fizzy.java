package link.botwmcs.fizzy;

import link.botwmcs.fizzy.client.bossbar.AnnounceMessageManager;
import link.botwmcs.fizzy.client.overlay.OverlayManager;
import link.botwmcs.fizzy.client.overlay.content.SimpleTextPage;
import link.botwmcs.fizzy.command.AnnounceCommand;
import link.botwmcs.fizzy.command.GuiCommand;
import link.botwmcs.fizzy.command.OverlayCommand;
import link.botwmcs.fizzy.menu.FizzyMenus;
import link.botwmcs.fizzy.menu.FizzyTestMenu;
import link.botwmcs.fizzy.network.c2s.FizzyMenuPingC2SPayload;
import link.botwmcs.fizzy.network.s2c.AnnouncePayload;
import link.botwmcs.fizzy.network.s2c.FizzyMenuSyncS2CPayload;
import link.botwmcs.fizzy.network.s2c.HudOverlayPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Fizzy.MODID)
public class Fizzy {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "fizzy";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Fizzy(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);
        FizzyMenus.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Fizzy) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "fizzy/fizzy-common.toml");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
//        ImageServices.initImageClient();
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        var r = event.registrar(MODID);
        r.playToServer(FizzyMenuPingC2SPayload.TYPE, FizzyMenuPingC2SPayload.CODEC, (payload, ctx) -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            if (player.containerMenu instanceof FizzyTestMenu menu
                    && menu.containerId == payload.containerId()) {
                menu.handleClientPing(player);
            }
        });

        // Client payloads (s2c)
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            r.playToClient(HudOverlayPayload.TYPE, HudOverlayPayload.CODEC, (payload, ctx) -> {
                        switch (payload.action()) {
                                case SHOW -> OverlayManager.create()
                                        .setTitle(Component.literal(payload.title()))
                                        .setSlidingText(Component.literal(payload.scrollingText()))
                                        .setContent(new SimpleTextPage(Component.literal(payload.text())))
                                        .setScale(1.0F)
                                        .show();
                                case HIDE -> OverlayManager.hideAll();
                        }
            });

            r.playToClient(AnnouncePayload.TYPE, AnnouncePayload.CODEC, (payload, ctx) -> {
                AnnounceMessageManager.show(Component.literal(payload.context()), payload.ticks());
            });

            r.playToClient(FizzyMenuSyncS2CPayload.TYPE, FizzyMenuSyncS2CPayload.CODEC, (payload, ctx) -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null) {
                    return;
                }
                if (mc.player.containerMenu instanceof FizzyTestMenu menu
                        && menu.containerId == payload.containerId()) {
                    menu.applyClientSync(payload.progress(), payload.text());
                }
            });
        }
    }

    @SubscribeEvent
    private void shutdown(GameShuttingDownEvent event) {
//        ImageServices.shutdown();
    }

    @SubscribeEvent
    private void onRegisterCommands(RegisterCommandsEvent event) {
        OverlayCommand.register(event.getDispatcher());
        AnnounceCommand.register(event.getDispatcher());
        GuiCommand.register(event.getDispatcher());
    }

    public static Identifier resourceLocation(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
