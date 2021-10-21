package me.m1dnightninja.midnightessentials.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.m1dnightninja.midnightcore.fabric.util.PermissionUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class ChatAsCommand {

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
            Commands.literal("chatas")
                .requires(context -> PermissionUtil.checkOrOp(context, "mindightessentials.command.chatas", 2))
                .then(Commands.argument("players", EntityArgument.players())
                    .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(context -> execute(context, context.getArgument("players", EntitySelector.class).findPlayers(context.getSource()), context.getArgument("message", String.class)))
                    )
                )
        );

    }

    private int execute(CommandContext<CommandSourceStack> context, List<ServerPlayer> players, String message) {

        for(ServerPlayer pl : players) {

            ServerboundChatPacket packet = new ServerboundChatPacket(message);
            pl.connection.handleChat(packet);
        }

        return players.size();
    }


}
