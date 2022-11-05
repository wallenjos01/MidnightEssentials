package org.wallentines.midnightessentials.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;

import java.util.List;

public class ChatAsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
            Commands.literal("chatas")
                .requires(Permissions.require("mindightessentials.command.chatas", 2))
                .then(Commands.argument("players", EntityArgument.players())
                    .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(context -> execute(context, context.getArgument("players", EntitySelector.class).findPlayers(context.getSource()), context.getArgument("message", String.class)))
                    )
                )
        );

    }

    private static int execute(CommandContext<CommandSourceStack> context, List<ServerPlayer> players, String message) {

        for(ServerPlayer pl : players) {

            FabricPlayer.wrap(pl).sendChatMessage(message);
        }

        return players.size();
    }


}
