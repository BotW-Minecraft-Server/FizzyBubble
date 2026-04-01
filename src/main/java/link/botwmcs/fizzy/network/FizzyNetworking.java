package link.botwmcs.fizzy.network;

import link.botwmcs.fizzy.network.s2c.AnnouncePayload;
import link.botwmcs.fizzy.network.s2c.HudOverlayPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class FizzyNetworking {
    private FizzyNetworking() {}

    public static void registerClientboundPayloads(
            PayloadRegistrar registrar,
            IPayloadHandler<HudOverlayPayload> hudOverlayHandler,
            IPayloadHandler<AnnouncePayload> announceHandler
    ) {
        registrar.playToClient(HudOverlayPayload.TYPE, HudOverlayPayload.CODEC, hudOverlayHandler);
        registrar.playToClient(AnnouncePayload.TYPE, AnnouncePayload.CODEC, announceHandler);
    }

    public static boolean canSendHudOverlay(ServerPlayer player) {
        return NetworkRegistry.hasChannel(player.connection, HudOverlayPayload.ID);
    }

    public static boolean canSendAnnounce(ServerPlayer player) {
        return NetworkRegistry.hasChannel(player.connection, AnnouncePayload.ID);
    }
}
