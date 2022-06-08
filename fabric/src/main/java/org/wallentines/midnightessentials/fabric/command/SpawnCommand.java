package org.wallentines.midnightessentials.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import org.wallentines.midnightcore.api.module.lang.CustomPlaceholderInline;
import org.wallentines.midnightcore.api.module.lang.LangProvider;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.fabric.MidnightCore;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightcore.fabric.util.LocationUtil;
import org.wallentines.midnightessentials.api.config.MainConfig;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
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
import org.wallentines.midnightlib.math.Vec3d;

import java.util.Collections;
import java.util.List;

public class SpawnCommand {

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("spawn")
            .requires(Permissions.require("midnightessentials.command.spawn", 2))
            .executes(context -> execute(context, null))
            .then(Commands.argument("target", EntityArgument.entities())
                .requires(Permissions.require("midnightessentials.command.spawn.others", 2))
                .executes(context -> execute(context, context.getArgument("target", EntitySelector.class)))
            )
        );

        dispatcher.register(Commands.literal("setspawn")
            .requires(Permissions.require("midnightessentials.command.setspawn", 2))
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
            sendCommandSuccess(context, MidnightEssentialsAPI.getInstance().getLangProvider(), false, "command.spawn.success");

        } else {
            entities = selector.findEntities(context.getSource());
            sendCommandSuccess(context, MidnightEssentialsAPI.getInstance().getLangProvider(), false, "command.spawn.success.other", CustomPlaceholderInline.create("count", entities.size()+""));
        }

        MainConfig config = MidnightEssentialsAPI.getInstance().getConfig();

        Location l = config.getSpawnLocation();
        if(l == null) l = LocationUtil.getSpawnLocation(MidnightCore.getInstance().getServer().overworld());

        for (Entity ent : entities) {
            LocationUtil.teleport(ent, l);
        }

        return entities.size();
    }

    private int executeSet(CommandContext<CommandSourceStack> context, Coordinates coords, Coordinates rotation, ResourceLocation level) {

        MainConfig config = MidnightEssentialsAPI.getInstance().getConfig();

        Vec3 loc = coords.getPosition(context.getSource());
        Vec2 rot = rotation == null ? new Vec2(0,0) : rotation.getRotation(context.getSource());

        Location newSpawn = new Location(ConversionUtil.toIdentifier(level), new Vec3d(loc.x, loc.y, loc.z), rot.x, rot.y);
        config.setSpawnLocation(newSpawn);

        config.save();

        sendCommandSuccess(context, MidnightEssentialsAPI.getInstance().getLangProvider(), false, "command.setspawn.success");

        return 1;
    }

    private static void sendCommandSuccess(CommandContext<CommandSourceStack> context, LangProvider langProvider, boolean notify, String key, Object... args) {

        MPlayer u = null;
        try {
            u = FabricPlayer.wrap(context.getSource().getPlayerOrException());

        } catch(CommandSyntaxException ex) {
            // Ignore
        }

        context.getSource().sendSuccess(ConversionUtil.toComponent(langProvider.getMessage(key, u, args)), notify);
    }

    private static void sendCommandFailure(CommandContext<CommandSourceStack> context, LangProvider langProvider, String key, Object... args) {

        MPlayer u = null;
        try {
            u = FabricPlayer.wrap(context.getSource().getPlayerOrException());

        } catch(CommandSyntaxException ex) {
            // Ignore
        }

        context.getSource().sendFailure(ConversionUtil.toComponent(langProvider.getMessage(key, u, args)));

    }
}
