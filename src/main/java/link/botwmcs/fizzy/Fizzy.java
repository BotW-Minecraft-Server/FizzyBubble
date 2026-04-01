package link.botwmcs.fizzy;

import com.mojang.logging.LogUtils;
import link.botwmcs.fizzy.network.FizzyNetworking;
import link.botwmcs.fizzy.network.s2c.AnnouncePayload;
import link.botwmcs.fizzy.network.s2c.HudOverlayPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

@Mod(Fizzy.MODID)
public class Fizzy {
    public static final String MODID = "fizzy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Fizzy(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        if (FMLEnvironment.getDist() != Dist.CLIENT) {
            modEventBus.addListener(this::registerPayloads);
        }

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "fizzy/fizzy-common.toml");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        FizzyNetworking.registerClientboundPayloads(
                event.registrar(MODID).optional(),
                Fizzy::ignoreHudOverlay,
                Fizzy::ignoreAnnounce
        );
    }

    public static Identifier resourceLocation(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    private static void ignoreHudOverlay(HudOverlayPayload payload, IPayloadContext context) {}

    private static void ignoreAnnounce(AnnouncePayload payload, IPayloadContext context) {}
}
