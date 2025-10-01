package link.botwmcs.fizzy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import link.botwmcs.fizzy.api.OverlayAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class OverlayCommand {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("overlay")
                .requires(src -> src.hasPermission(2)) // 按需调整权限
                .then(Commands.literal("create")
                        .then(Commands.argument("title", StringArgumentType.string())
                                .then(Commands.argument("scrolling", StringArgumentType.string())
                                        .then(Commands.argument("components", StringArgumentType.string())
                                                .executes(ctx -> {
                                                    ServerPlayer serverPlayer = ctx.getSource().getPlayer();
                                                    if (serverPlayer == null) {
                                                        ctx.getSource().sendFailure(Component.literal("This subcommand can only be used by a player."));
                                                        return 0;
                                                    }
                                                    OverlayAPI.sendTo(serverPlayer, StringArgumentType.getString(ctx, "title"), StringArgumentType.getString(ctx, "scrolling"), StringArgumentType.getString(ctx, "components"));
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
                .then(Commands.literal("clear")
                        .executes(ctx -> {
                            ServerPlayer serverPlayer = ctx.getSource().getPlayer();
                            if (serverPlayer == null) {
                                ctx.getSource().sendFailure(Component.literal("This subcommand can only be used by a player."));
                                return 0;
                            }
                            OverlayAPI.hide(serverPlayer);
                            return 1;
                        })
                )
        );
    }
}
