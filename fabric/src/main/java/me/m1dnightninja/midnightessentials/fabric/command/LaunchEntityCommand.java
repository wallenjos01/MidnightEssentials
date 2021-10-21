package me.m1dnightninja.midnightessentials.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.m1dnightninja.midnightcore.api.module.lang.CustomPlaceholderInline;
import me.m1dnightninja.midnightcore.fabric.util.PermissionUtil;
import me.m1dnightninja.midnightcore.fabric.module.lang.LangModule;
import me.m1dnightninja.midnightessentials.api.MidnightEssentialsAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LaunchEntityCommand {

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("launchentity")
            .requires(stack -> PermissionUtil.checkOrOp(stack, "midnightessentials.command.launchentity", 2))
            .then(Commands.argument("target", EntityArgument.entities())
                .then(Commands.argument("velocity", Vec3Argument.vec3(false))
                    .executes(context -> execute(context, context.getArgument("target", EntitySelector.class), context.getArgument("velocity", Coordinates.class), null))
                    .then(Commands.argument("origin", EntityArgument.entity())
                        .executes(context -> execute(context, context.getArgument("target", EntitySelector.class), context.getArgument("velocity", Coordinates.class), context.getArgument("origin", EntitySelector.class).findSingleEntity(context.getSource())))
                    )
                )
            )
        );

    }

    private int execute(CommandContext<CommandSourceStack> context, EntitySelector selector, Coordinates coords, Entity origin) throws CommandSyntaxException {

        List<? extends Entity> targets = selector.findEntities(context.getSource());

        for(Entity ent : targets) {

            Vec3 xyz = coords.getPosition(context.getSource());
            double dx = xyz.x, dy = xyz.y, dz = xyz.z;

            if(coords.isXRelative()) {
                dx += (ent.getDeltaMovement().x - ent.getX());
            }

            if(coords.isYRelative()) {
                dy += (ent.getDeltaMovement().y - ent.getY());
            }

            if(coords.isZRelative()) {
                dz += (ent.getDeltaMovement().z - ent.getZ());
            }

            if(origin != null) {

                Vec3 offset = ent.position().subtract(origin.position()).normalize();

                dx *= offset.x;
                dy *= offset.y;
                dz *= offset.z;

            }

            ent.setDeltaMovement(dx, dy, dz);

            if(ent instanceof ServerPlayer) {
                ((ServerPlayer) ent).connection.send(new ClientboundSetEntityMotionPacket(ent));
            }
        }

        LangModule.sendCommandSuccess(context, MidnightEssentialsAPI.getInstance().getLangProvider(), true, "command.launchentity.success", new CustomPlaceholderInline("count", targets.size()+""));

        return targets.size();

    }


}
