package link.botwmcs.fizzy.api;

import java.util.Collection;
import java.util.function.Predicate;
import link.botwmcs.fizzy.network.FizzyNetworking;
import link.botwmcs.fizzy.network.s2c.AnnouncePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class AnnounceAPI {
    private AnnounceAPI() {}

    public static void sendTo(ServerPlayer player, String msg, int ticks) {
        if (!FizzyNetworking.canSendAnnounce(player)) {
            return;
        }
        player.connection.send(new ClientboundCustomPayloadPacket(new AnnouncePayload(msg, ticks)));
    }

    public static void sendTo(Collection<? extends ServerPlayer> players, String msg, int ticks) {
        var pkt = new ClientboundCustomPayloadPacket(new AnnouncePayload(msg, ticks));
        for (ServerPlayer player : players) {
            if (FizzyNetworking.canSendAnnounce(player)) {
                player.connection.send(pkt);
            }
        }
    }

    public static void sendToIf(ServerLevel level, Predicate<ServerPlayer> filter, String msg, int ticks) {
        var pkt = new ClientboundCustomPayloadPacket(new AnnouncePayload(msg, ticks));
        for (ServerPlayer player : level.players()) {
            if (filter.test(player) && FizzyNetworking.canSendAnnounce(player)) {
                player.connection.send(pkt);
            }
        }
    }

    public static void sendToNear(ServerLevel level, BlockPos pos, double radius, String msg, int ticks) {
        double radiusSquared = radius * radius;
        var pkt = new ClientboundCustomPayloadPacket(new AnnouncePayload(msg, ticks));
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= radiusSquared
                    && FizzyNetworking.canSendAnnounce(player)) {
                player.connection.send(pkt);
            }
        }
    }

    public static void broadcast(ServerLevel level, String msg, int ticks) {
        sendTo(level.players(), msg, ticks);
    }

    public static void hide(ServerPlayer player) {
        if (!FizzyNetworking.canSendAnnounce(player)) {
            return;
        }
        player.connection.send(new ClientboundCustomPayloadPacket(new AnnouncePayload("", 1)));
    }
}
