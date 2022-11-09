package org.wallentines.midnightessentials.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.wallentines.midnightcore.api.module.lang.CustomPlaceholderInline;
import org.wallentines.midnightcore.fabric.event.entity.EntityDismountVehicleEvent;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
import org.wallentines.midnightessentials.fabric.util.ArmorStandUtil;
import org.wallentines.midnightlib.event.Event;

import java.util.*;

public class SitCommand {

    private static final Set<Entity> SITTING = new HashSet<>();


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("sit")
            .requires(Permissions.require("midnightessentials.command.sit", 2))
            .executes(ctx -> execute(ctx, List.of(ctx.getSource().getPlayerOrException()), null, null))
            .then(Commands.argument("targets", EntityArgument.entities())
                .requires(Permissions.require("midnightessentials.command.others", 2))
                .executes(ctx -> execute(ctx, EntityArgument.getEntities(ctx, "targets"), null, null))
                .then(Commands.argument("location", Vec3Argument.vec3())
                    .executes(ctx -> execute(
                        ctx,
                        EntityArgument.getEntities(ctx, "targets"),
                        Vec3Argument.getCoordinates(ctx, "location").getPosition(ctx.getSource()), null
                    ))
                    .then(Commands.argument("rotation", FloatArgumentType.floatArg(-180.0f, 180.0f))
                        .executes(ctx -> execute(
                            ctx,
                            EntityArgument.getEntities(ctx, "targets"),
                            Vec3Argument.getCoordinates(ctx, "location").getPosition(ctx.getSource()),
                            ctx.getArgument("rotation", Float.class)
                        ))
                    )
                )
            )
        );

        Event.register(EntityDismountVehicleEvent.class, SitCommand.class, ev -> {

            if(SITTING.contains(ev.getRider())) {

                Vec3 loc = new Vec3(ev.getVehicle().getX(), ev.getVehicle().getY() + 0.25d, ev.getVehicle().getZ());

                ev.getVehicle().remove(Entity.RemovalReason.KILLED);
                ev.getRider().teleportTo(loc.x, loc.y, loc.z);

                SITTING.remove(ev.getRider());
            }
        });

    }


    private static int execute(CommandContext<CommandSourceStack> ctx, Collection<? extends Entity> entities, Vec3 location, Float rotation) {

        int sat = 0;
        for(Entity ent : entities) {

            if(!(ent instanceof LivingEntity) || SITTING.contains(ent) && location == null) {
                continue;
            }

            ent.stopRiding();

            Vec3 loc = Objects.requireNonNullElseGet(location, () -> {

                int floored = ent.getBlockY();
                double offset = ent.getY() - floored >= 0.5d ? 0.5d : 0.0d;

                return new Vec3(ent.getX(), (ent.getBlockY() - 0.15d) + offset, ent.getZ());
            });

            float rot = Objects.requireNonNullElse(rotation, ent.getYRot());

            ArmorStand armorStand = ArmorStandUtil.createInvisibleArmorStand(ent.getLevel(), loc.x, loc.y, loc.z);
            armorStand.setYHeadRot(rot);
            armorStand.setYRot(rot);

            ent.getLevel().addFreshEntity(armorStand);
            ent.startRiding(armorStand);

            SITTING.add(ent);

            sat++;
        }

        CommandUtil.sendCommandSuccess(ctx, MidnightEssentialsAPI.getInstance().getLangProvider(), true, "command.sit.success", CustomPlaceholderInline.create("count", sat + ""));
        return entities.size();
    }

}
