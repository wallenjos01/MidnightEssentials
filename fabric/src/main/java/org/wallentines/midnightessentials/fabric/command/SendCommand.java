package org.wallentines.midnightessentials.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.messaging.MessagingModule;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public class SendCommand {

    private static final Identifier SEND_ID = new Identifier("midnightcore", "send");

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("send")
            .requires(Permissions.require("midnightessentials.command.send", 4))
            .then(Commands.argument("target", EntityArgument.players())
                .then(Commands.argument("server", StringArgumentType.word())
                    .executes(context -> execute(context, context.getArgument("target", EntitySelector.class), context.getArgument("server", String.class)))
                )
            )
        );

    }

    private int execute(CommandContext<CommandSourceStack> context, EntitySelector selector, String server) throws CommandSyntaxException {

        MessagingModule module = MidnightCoreAPI.getInstance().getModuleManager().getModule(MessagingModule.class);

        ConfigSection data = new ConfigSection();
        data.set("server", server);

        for(ServerPlayer pl : selector.findPlayers(context.getSource())) {
            module.sendMessage(FabricPlayer.wrap(pl), SEND_ID, data);
        }

        return 0;
    }

}
