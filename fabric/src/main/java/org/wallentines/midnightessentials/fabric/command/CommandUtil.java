package org.wallentines.midnightessentials.fabric.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import org.wallentines.midnightcore.api.module.lang.LangProvider;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;

public class CommandUtil {

    public static void sendCommandSuccess(CommandContext<CommandSourceStack> context, LangProvider langProvider, boolean notify, String key, Object... args) {

        MPlayer u = null;
        try {
            u = FabricPlayer.wrap(context.getSource().getPlayerOrException());

        } catch(CommandSyntaxException ex) {
            // Ignore
        }

        context.getSource().sendSuccess(ConversionUtil.toComponent(langProvider.getMessage(key, u, args)), notify);

    }

}
