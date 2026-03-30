package link.botwmcs.fizzy.network.s2c;

import link.botwmcs.fizzy.Fizzy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HudOverlayPayload(Action action, String title, String scrollingText, String text) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Fizzy.MODID, "hud_overlay");
    public static final Type<HudOverlayPayload> TYPE = new Type<>(ID);
    public enum Action { SHOW, HIDE }

    public static final StreamCodec<FriendlyByteBuf, HudOverlayPayload.Action> ACTION_CODEC =
            StreamCodec.of(
                    (buf, value) -> buf.writeUtf(value.name()),       // 写入
                    buf -> HudOverlayPayload.Action.valueOf(buf.readUtf()) // 读取
            );

    public static final StreamCodec<FriendlyByteBuf, HudOverlayPayload> CODEC = StreamCodec.composite(
            ACTION_CODEC, HudOverlayPayload::action,
            ByteBufCodecs.STRING_UTF8, HudOverlayPayload::title,
            ByteBufCodecs.STRING_UTF8, HudOverlayPayload::scrollingText,
            ByteBufCodecs.STRING_UTF8, HudOverlayPayload::text,
            HudOverlayPayload::new
    );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
