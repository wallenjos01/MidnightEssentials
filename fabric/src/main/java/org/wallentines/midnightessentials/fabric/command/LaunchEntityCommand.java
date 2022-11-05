package org.wallentines.midnightessentials.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.world.phys.Vec2;
import org.wallentines.midnightcore.api.module.lang.CustomPlaceholderInline;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
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

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("launchentity")
            .requires(Permissions.require("midnightessentials.command.launchentity", 2))
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

    private static int execute(CommandContext<CommandSourceStack> context, EntitySelector selector, Coordinates coords, Entity origin) throws CommandSyntaxException {

        try {
            List<? extends Entity> targets = selector.findEntities(context.getSource());

            for (Entity ent : targets) {

                Vec3 xyz = coords.getPosition(context.getSource());
                double dx = xyz.x, dy = xyz.y, dz = xyz.z;
                Vec2 rot = ent.getRotationVector();

                if (coords.isXRelative()) {
                    dx -= ent.getX();
                    dx *= Math.sin(Math.toRadians(rot.y)) * -1;
                }

                if (coords.isYRelative()) {
                    dy -= ent.getY();
                    dy *= Math.sin(Math.toRadians(rot.x)) * -1;
                }

                if (coords.isZRelative()) {
                    dz -= ent.getZ();
                    dz *= Math.cos(Math.toRadians(rot.y));
                }

                if (origin != null) {

                    Vec3 offset = ent.position().subtract(origin.position()).normalize();

                    dx *= offset.x;
                    dy *= offset.y;
                    dz *= offset.z;
                }

                ent.setDeltaMovement(dx, dy, dz);

                if (ent instanceof ServerPlayer) {
                    ((ServerPlayer) ent).connection.send(new ClientboundSetEntityMotionPacket(ent));
                }
            }

            CommandUtil.sendCommandSuccess(context, MidnightEssentialsAPI.getInstance().getLangProvider(), true, "command.launchentity.success", CustomPlaceholderInline.create("count", targets.size() + ""));
            return targets.size();

        } catch (Throwable th) {
            th.printStackTrace();
            throw th;
        }

    }

}
