package me.m1dnightninja.midnightessentials.fabric.command;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.m1dnightninja.midnightcore.fabric.api.PermissionHelper;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class SendCommand {

    private static final ResourceLocation sendId = new ResourceLocation("midnightcore", "send");

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("send")
            .requires(stack -> PermissionHelper.checkOrOp(stack, "midnightessentials.send", 4))
            .then(Commands.argument("target", EntityArgument.players())
                .then(Commands.argument("server", StringArgumentType.word())
                    .executes(context -> execute(context, context.getArgument("target", EntitySelector.class), context.getArgument("server", String.class)))
                )
            )
        );

    }

    private int execute(CommandContext<CommandSourceStack> context, EntitySelector selector, String server) throws CommandSyntaxException {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(server);

        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBytes(out.toByteArray());

        for(ServerPlayer pl : selector.findPlayers(context.getSource())) {

            ServerPlayNetworking.send(pl, sendId, buf);
        }

        return 0;
    }

}
