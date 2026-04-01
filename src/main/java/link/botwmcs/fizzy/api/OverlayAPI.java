package link.botwmcs.fizzy.api;

import java.util.Collection;
import java.util.function.Predicate;
import link.botwmcs.fizzy.network.FizzyNetworking;
import link.botwmcs.fizzy.network.s2c.HudOverlayPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class OverlayAPI {
    private OverlayAPI() {}

    public static void sendTo(ServerPlayer player, String title, String scrolling, String context) {
        if (!FizzyNetworking.canSendHudOverlay(player)) {
            return;
        }
        var payload = new HudOverlayPayload(HudOverlayPayload.Action.SHOW, title, scrolling, context);
        player.connection.send(new ClientboundCustomPayloadPacket(payload));
    }

    public static void sendTo(Collection<? extends ServerPlayer> players, String title, String scrolling, String context) {
        var payload = new HudOverlayPayload(HudOverlayPayload.Action.SHOW, title, scrolling, context);
        for (ServerPlayer player : players) {
            if (FizzyNetworking.canSendHudOverlay(player)) {
                player.connection.send(new ClientboundCustomPayloadPacket(payload));
            }
        }
    }

    public static void sendToIf(ServerLevel level, Predicate<ServerPlayer> filter, String title, String scrolling, String context) {
        var payload = new HudOverlayPayload(HudOverlayPayload.Action.SHOW, title, scrolling, context);
        for (ServerPlayer player : level.players()) {
            if (filter.test(player) && FizzyNetworking.canSendHudOverlay(player)) {
                player.connection.send(new ClientboundCustomPayloadPacket(payload));
            }
        }
    }

    public static void sendToNear(ServerLevel level, BlockPos pos, double radius, String title, String scrolling, String context) {
        double radiusSquared = radius * radius;
        var payload = new HudOverlayPayload(HudOverlayPayload.Action.SHOW, title, scrolling, context);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= radiusSquared
                    && FizzyNetworking.canSendHudOverlay(player)) {
                player.connection.send(new ClientboundCustomPayloadPacket(payload));
            }
        }
    }

    public static void broadcast(ServerLevel level, String title, String scrolling, String context) {
        sendTo(level.players(), title, scrolling, context);
    }

    public static void hide(ServerPlayer player) {
        if (!FizzyNetworking.canSendHudOverlay(player)) {
            return;
        }
        player.connection.send(new ClientboundCustomPayloadPacket(
                new HudOverlayPayload(HudOverlayPayload.Action.HIDE, "", "", "")
        ));
    }
}
