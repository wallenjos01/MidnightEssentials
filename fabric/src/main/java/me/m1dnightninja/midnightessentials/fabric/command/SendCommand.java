package me.m1dnightninja.midnightessentials.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.pluginmessage.IPluginMessageModule;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import me.m1dnightninja.midnightcore.fabric.util.PermissionUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

public class SendCommand {

    private static final MIdentifier sendId = MIdentifier.create("midnightcore", "send");

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("send")
            .requires(stack -> PermissionUtil.checkOrOp(stack, "midnightessentials.command.send", 4))
            .then(Commands.argument("target", EntityArgument.players())
                .then(Commands.argument("server", StringArgumentType.word())
                    .executes(context -> execute(context, context.getArgument("target", EntitySelector.class), context.getArgument("server", String.class)))
                )
            )
        );

    }

    private int execute(CommandContext<CommandSourceStack> context, EntitySelector selector, String server) throws CommandSyntaxException {

        IPluginMessageModule module = MidnightCoreAPI.getInstance().getModule(IPluginMessageModule.class);

        ConfigSection data = new ConfigSection();
        data.set("server", server);

        for(ServerPlayer pl : selector.findPlayers(context.getSource())) {
            module.sendMessage(FabricPlayer.wrap(pl), sendId, data);
        }

        return 0;
    }

}
