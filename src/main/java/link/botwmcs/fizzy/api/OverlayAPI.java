package link.botwmcs.fizzy.api;

import link.botwmcs.fizzy.network.s2c.AnnouncePayload;
import link.botwmcs.fizzy.network.s2c.HudOverlayPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.function.Predicate;

public final class OverlayAPI {
    private OverlayAPI() {}

    /** 发给单个玩家 */
    public static void sendTo(ServerPlayer player, String title, String scrolling, String context) {
        var payload = new HudOverlayPayload(HudOverlayPayload.Action.SHOW, title, scrolling, context);
        player.connection.send(new ClientboundCustomPayloadPacket(payload));
    }

    public static void sendTo(Collection<? extends ServerPlayer> players, String title, String scrolling, String context) {
        var payload = new HudOverlayPayload(HudOverlayPayload.Action.SHOW, title, scrolling, context);
        for (ServerPlayer p : players) p.connection.send(new ClientboundCustomPayloadPacket(payload));
    }

    /** 条件筛选后发送（例如同一队、同一阵营、有权限节点等） */
    public static void sendToIf(ServerLevel level, Predicate<ServerPlayer> filter, String title, String scrolling, String context) {
        var payload = new HudOverlayPayload(HudOverlayPayload.Action.SHOW, title, scrolling, context);
        for (ServerPlayer p : level.players()) if (filter.test(p)) p.connection.send(new ClientboundCustomPayloadPacket(payload));

    }

    /** 指定范围内的玩家（例如事件点附近 64 格内） */
    public static void sendToNear(ServerLevel level, BlockPos pos, double radius, String title, String scrolling, String context) {
        double r2 = radius * radius;
        var payload = new HudOverlayPayload(HudOverlayPayload.Action.SHOW, title, scrolling, context);
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= r2) {
                p.connection.send(new ClientboundCustomPayloadPacket(payload));
            }
        }
    }

    /** 全服广播 */
    public static void broadcast(ServerLevel level, String title, String scrolling, String context) {
        sendTo(level.players(), title, scrolling, context);
    }

    /** hide */
    public static void hide(ServerPlayer serverPlayer) {
        serverPlayer.connection.send(new ClientboundCustomPayloadPacket(new HudOverlayPayload(HudOverlayPayload.Action.HIDE, "", "", "")));
    }

}
