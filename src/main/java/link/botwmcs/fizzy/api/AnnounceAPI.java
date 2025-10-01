package link.botwmcs.fizzy.api;

import link.botwmcs.fizzy.network.s2c.AnnouncePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.function.Predicate;

public final class AnnounceAPI {
    private AnnounceAPI() {}

    /** 发给单个玩家 */
    public static void sendTo(ServerPlayer player, String msg, int ticks) {
        player.connection.send(new ClientboundCustomPayloadPacket(new AnnouncePayload(msg, ticks)));
    }

    /** 发给一组玩家 */
    public static void sendTo(Collection<? extends ServerPlayer> players, String msg, int ticks) {
        var pkt = new ClientboundCustomPayloadPacket(new AnnouncePayload(msg, ticks));
        for (ServerPlayer p : players) p.connection.send(pkt);
    }

    /** 条件筛选后发送（例如同一队、同一阵营、有权限节点等） */
    public static void sendToIf(ServerLevel level, Predicate<ServerPlayer> filter, String msg, int ticks) {
        var pkt = new ClientboundCustomPayloadPacket(new AnnouncePayload(msg, ticks));
        for (ServerPlayer p : level.players()) if (filter.test(p)) p.connection.send(pkt);
    }

    /** 指定范围内的玩家（例如事件点附近 64 格内） */
    public static void sendToNear(ServerLevel level, BlockPos pos, double radius, String msg, int ticks) {
        double r2 = radius * radius;
        var pkt = new ClientboundCustomPayloadPacket(new AnnouncePayload(msg, ticks));
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= r2) {
                p.connection.send(pkt);
            }
        }
    }

    /** 全服广播 */
    public static void broadcast(ServerLevel level, String msg, int ticks) {
        sendTo(level.players(), msg, ticks);
    }

    /** hide（直接send一个1 tick的空内容就可以带动画隐藏） */
    public static void hide(ServerPlayer serverPlayer) {
        serverPlayer.connection.send(new ClientboundCustomPayloadPacket(new AnnouncePayload("", 1)));
    }

}
