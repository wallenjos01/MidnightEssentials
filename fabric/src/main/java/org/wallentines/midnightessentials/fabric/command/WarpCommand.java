package org.wallentines.midnightessentials.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec2;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.lang.CustomPlaceholderInline;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightcore.api.text.TextColor;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
import org.wallentines.midnightessentials.api.UIDisplay;
import org.wallentines.midnightessentials.api.Warp;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.math.Vec3d;

import java.util.ArrayList;

public class WarpCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("warp")
            .requires(Permissions.require("midnightessentials.command.warp", 2))
            .executes(ctx -> executeWarp(ctx, null))
            .then(Commands.argument("warp", StringArgumentType.string())
                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(MidnightEssentialsAPI.getInstance().getConfig().getWarpRegistry().getIds(), builder))
                .executes(ctx -> executeWarp(ctx, ctx.getArgument("warp", String.class)))
            )
        );

        dispatcher.register(Commands.literal("setwarp")
            .requires(Permissions.require("midnightessentials.command.setwarp", 2))
            .then(Commands.argument("warp", StringArgumentType.string())
                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(MidnightEssentialsAPI.getInstance().getConfig().getWarpRegistry().getIds(), builder))
                .executes(WarpCommand::executeSetwarp)
            )
        );

    }

    private static int executeWarp(CommandContext<CommandSourceStack> ctx, String warpId) throws CommandSyntaxException {

        MPlayer mpl = FabricPlayer.wrap(ctx.getSource().getPlayerOrException());

        if(warpId == null) {

            MComponent comp = MidnightEssentialsAPI.getInstance().getLangProvider().getMessage("gui.warp.title", mpl);
            InventoryGUI gui = MidnightCoreAPI.getInstance().createGUI(comp);

            int slot = 0;
            for(Warp w : MidnightEssentialsAPI.getInstance().getConfig().getWarpRegistry()) {
                gui.setItem(slot, w.getDisplay().getItem(), (click, clicker) -> {
                    clicker.teleport(w.getLocation());
                    gui.close(clicker);
                });
                slot++;
            }

            gui.open(mpl, 0);
            return 1;
        }

        Warp w = MidnightEssentialsAPI.getInstance().getConfig().getWarpRegistry().get(warpId);
        mpl.teleport(w.getLocation());

        CommandUtil.sendCommandSuccess(ctx, MidnightEssentialsAPI.getInstance().getLangProvider(), false, "command.warp.success", CustomPlaceholderInline.create("warp_id", warpId));
        return 1;
    }

    private static int executeSetwarp(CommandContext<CommandSourceStack> ctx) {

        String warpId = ctx.getArgument("warp", String.class);

        BlockPos pos = new BlockPos(ctx.getSource().getPosition());
        Level lvl = ctx.getSource().getLevel();
        Block block = lvl.getBlockState(pos).getBlock();

        MComponent id = new MTextComponent(warpId);
        MItemStack is = block == Blocks.AIR ? null : MItemStack.Builder.of(ConversionUtil.toIdentifier(Registry.BLOCK.getKey(block))).withName(id).build();

        Vec3d coords = new Vec3d(ctx.getSource().getPosition().x(), ctx.getSource().getPosition().y(), ctx.getSource().getPosition().z());
        Vec2 rot = ctx.getSource().getRotation();

        Location loc = new Location(ConversionUtil.toIdentifier(lvl.dimension().location()), coords, rot.x, rot.y);
        UIDisplay display = new UIDisplay(id, new TextColor(Color.WHITE), new ArrayList<>(), is);
        Warp w = new Warp(display, loc);

        MidnightEssentialsAPI.getInstance().getConfig().getWarpRegistry().register(warpId, w);

        CommandUtil.sendCommandSuccess(ctx, MidnightEssentialsAPI.getInstance().getLangProvider(), false, "command.setwarp.success", CustomPlaceholderInline.create("warp_id", warpId));

        return 1;
    }

}
