package me.m1dnightninja.midnightessentials.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.m1dnightninja.midnightcore.api.math.Vec3d;
import me.m1dnightninja.midnightcore.api.module.lang.CustomPlaceholderInline;
import me.m1dnightninja.midnightcore.api.player.Location;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.util.LocationUtil;
import me.m1dnightninja.midnightcore.fabric.util.PermissionUtil;
import me.m1dnightninja.midnightcore.fabric.module.lang.LangModule;
import me.m1dnightninja.midnightcore.fabric.util.ConversionUtil;
import me.m1dnightninja.midnightessentials.api.MainConfig;
import me.m1dnightninja.midnightessentials.api.MidnightEssentialsAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;

public class SpawnCommand {

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("spawn")
            .requires(stack -> PermissionUtil.checkOrOp(stack, "midnightessentials.command.spawn", 2))
            .executes(context -> execute(context, null))
            .then(Commands.argument("target", EntityArgument.entities())
                .requires(stack -> PermissionUtil.checkOrOp(stack, "midnightessentials.command.spawn.others", 2))
                .executes(context -> execute(context, context.getArgument("target", EntitySelector.class)))
            )
        );

        dispatcher.register(Commands.literal("setspawn")
            .requires(stack -> PermissionUtil.checkOrOp(stack, "midnightessentials.command.setspawn", 2))
            .then(Commands.argument("location", Vec3Argument.vec3(true))
                .executes(context -> executeSet(context, context.getArgument("location", Coordinates.class), null, context.getSource().getLevel().dimension().location()))
                .then(Commands.argument("rotation", RotationArgument.rotation())
                    .executes(context -> executeSet(context, context.getArgument("location", Coordinates.class), context.getArgument("rotation", Coordinates.class), context.getSource().getLevel().dimension().location()))
                    .then(Commands.argument("world", DimensionArgument.dimension())
                        .executes(context -> executeSet(context, context.getArgument("location", Coordinates.class), context.getArgument("rotation", Coordinates.class), context.getArgument("world", ResourceLocation.class)))
                    )
                )
            )
        );

    }

    private int execute(CommandContext<CommandSourceStack> context, EntitySelector selector) throws CommandSyntaxException {

        List<? extends Entity> entities;
        if(selector == null) {
            entities = Collections.singletonList(context.getSource().getEntity());
            LangModule.sendCommandSuccess(context, MidnightEssentialsAPI.getInstance().getLangProvider(), false, "command.spawn.success");

        } else {
            entities = selector.findEntities(context.getSource());
            LangModule.sendCommandSuccess(context, MidnightEssentialsAPI.getInstance().getLangProvider(), false, "command.spawn.success.other", new CustomPlaceholderInline("count", entities.size()+""));
        }

        MainConfig config = MidnightEssentialsAPI.getInstance().getConfig();

        Location l = config.getSpawnLocation() == null ?
                LocationUtil.getSpawnLocation(MidnightCore.getServer().overworld()) :
                new Location(
                        config.getSpawnDimension(),
                        config.getSpawnLocation().getX(),
                        config.getSpawnLocation().getY(),
                        config.getSpawnLocation().getZ(),
                        config.getSpawnYaw(),
                        config.getSpawnPitch());

        for (Entity ent : entities) {
            LocationUtil.teleport(ent, l);
        }

        return entities.size();
    }

    private int executeSet(CommandContext<CommandSourceStack> context, Coordinates coords, Coordinates rotation, ResourceLocation level) {

        MainConfig config = MidnightEssentialsAPI.getInstance().getConfig();

        Vec3 loc = coords.getPosition(context.getSource());
        Vec2 rot = rotation == null ? new Vec2(0,0) : rotation.getRotation(context.getSource());

        config.setSpawnLocation(new Vec3d(loc.x, loc.y, loc.z));
        config.setSpawnYaw(rot.x);
        config.setSpawnPitch(rot.y);
        config.setSpawnDimension(ConversionUtil.fromResourceLocation(level));

        config.save();

        LangModule.sendCommandSuccess(context, MidnightEssentialsAPI.getInstance().getLangProvider(), false, "command.setspawn.success");

        return 1;
    }
}
