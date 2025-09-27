package link.botwmcs.fizzy.network;

import link.botwmcs.fizzy.network.s2c.HudOverlayPayload;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;

public final class FizzyNet {
    private FizzyNet() {}

    public static void sendShowOverlay(ServerPlayer sp, String l1, String l2, String l3, float x, float y) {
        var payload = new HudOverlayPayload(HudOverlayPayload.Action.SHOW, x, y, l1, l2, l3);
        sp.connection.send(new ClientboundCustomPayloadPacket(payload));
    }

    public static void sendHideOverlay(ServerPlayer sp) {
        var payload = new HudOverlayPayload(HudOverlayPayload.Action.HIDE, 0f, 0f, "", "", "");
        sp.connection.send(new ClientboundCustomPayloadPacket(payload));
    }

}
