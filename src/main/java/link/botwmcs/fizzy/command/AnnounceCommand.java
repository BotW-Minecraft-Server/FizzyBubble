package link.botwmcs.fizzy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import link.botwmcs.fizzy.api.AnnounceAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AnnounceCommand {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("announce")
                .then(Commands.literal("create")
                        .then(Commands.argument("context", StringArgumentType.string())
                                .then(Commands.argument("ticks", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                                    ServerPlayer serverPlayer = ctx.getSource().getPlayer();
                                                    if (serverPlayer == null) {
                                                        ctx.getSource().sendFailure(Component.literal("This subcommand can only be used by a player."));
                                                        return 0;
                                                    }
                                                    AnnounceAPI.sendTo(serverPlayer, StringArgumentType.getString(ctx, "context"), IntegerArgumentType.getInteger(ctx, "ticks"));
                                                    return 1;
                                                }
                                        )
                                )
                        )
                )
        );
    }
}
