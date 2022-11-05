package org.wallentines.midnightessentials.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import org.wallentines.midnightcore.api.module.lang.CustomPlaceholderInline;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public class DamageCommand {

    private static final HashMap<String, DamageSource> allSources = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("damage")
            .requires(Permissions.require("midnightessentials.command.damage", 2))
            .then(Commands.argument("targets", EntityArgument.entities())
                .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0f))
                    .then(Commands.argument("source", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(allSources.keySet(), builder))
                        .executes(context -> executeDamage(context, context.getArgument("targets", EntitySelector.class).findEntities(context.getSource()), context.getArgument("amount", Float.class), context.getArgument("source", String.class)))
                    )
                    .executes(context -> executeDamage(context, context.getArgument("targets", EntitySelector.class).findEntities(context.getSource()), context.getArgument("amount", Float.class), null))
                )
            )
        );
    }

    private static int executeDamage(CommandContext<CommandSourceStack> context, List<? extends Entity> entities, float amount, String damageSource) {

        DamageSource src = DamageSource.GENERIC;

        if(damageSource != null) {
            src = allSources.get(damageSource);
        }

        for(Entity ent : entities) {

            ent.hurt(src, amount);
        }

        CommandUtil.sendCommandSuccess(context, MidnightEssentialsAPI.getInstance().getLangProvider(), true, "command.damage.success", CustomPlaceholderInline.create("count", entities.size()+""));

        return entities.size();
    }

    static {

        for(Field f : DamageSource.class.getDeclaredFields()) {
            if(f.getType() == DamageSource.class) {
                try {
                    DamageSource src = (DamageSource) f.get(DamageSource.class);
                    allSources.put(src.getMsgId(), src);
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }

    }

}
