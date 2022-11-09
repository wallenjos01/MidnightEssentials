package org.wallentines.midnightessentials.fabric.module;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.text.CustomPlaceholderInline;
import org.wallentines.midnightcore.fabric.event.player.PlayerInteractEvent;
import org.wallentines.midnightcore.fabric.event.server.CommandLoadEvent;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.CommandUtil;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
import org.wallentines.midnightessentials.api.module.blockcommand.BlockCommandRegistry;
import org.wallentines.midnightessentials.common.module.AbstractBlockCommandModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.math.Vec3i;
import org.wallentines.midnightlib.module.ModuleInfo;

public class FabricBlockCommandModule extends AbstractBlockCommandModule {

    @Override
    protected void registerCommands() { }

    @Override
    protected void registerListeners() {

        Event.register(PlayerInteractEvent.class, this, event -> {

            if(event.getBlockHit() == null || event.getHand() == InteractionHand.OFF_HAND) return;

            BlockPos pos = event.getBlockHit().getBlockPos();
            Vec3i loc = new Vec3i(pos.getX(), pos.getY(), pos.getZ());

            BlockCommandRegistry.InteractionType type = event.isLeftClick() ?
                event.getPlayer().isShiftKeyDown() ? BlockCommandRegistry.InteractionType.SHIFT_LEFT_CLICK : BlockCommandRegistry.InteractionType.LEFT_CLICK :
                event.getPlayer().isShiftKeyDown() ? BlockCommandRegistry.InteractionType.SHIFT_RIGHT_CLICK : BlockCommandRegistry.InteractionType.RIGHT_CLICK;


            if(execute(loc, type, FabricPlayer.wrap(event.getPlayer()))) {
                event.setCancelled(true);
                event.setShouldSwingArm(true);
            }
        });

        Event.register(CommandLoadEvent.class, this, event ->
            event.getDispatcher().register(Commands.literal("blockcommands")
                .requires(Permissions.require("midnightessentials.command.blockcommands", 2))
                .then(Commands.argument("location", BlockPosArgument.blockPos())
                    .then(Commands.literal("add")
                        .then(Commands.argument("command", StringArgumentType.greedyString())
                            .executes(ctx -> {

                                try {
                                    BlockCommandRegistry reg = findRegistry(ConversionUtil.toIdentifier(ctx.getSource().getLevel().dimension().location()));
                                    if (reg == null) {
                                        CommandUtil.sendCommandFailure(ctx, MidnightEssentialsAPI.getInstance().getLangProvider(), "command.blockcommand.error");
                                        return 0;
                                    }

                                    BlockPos bp = BlockPosArgument.getLoadedBlockPos(ctx, "location");
                                    BlockCommandRegistry.BlockCommand bc = new BlockCommandRegistry.BlockCommand(BlockCommandRegistry.CommandType.CONSOLE_COMMAND, ctx.getArgument("command", String.class));
                                    bc.addActuationMethod(BlockCommandRegistry.InteractionType.RIGHT_CLICK);
                                    bc.addActuationMethod(BlockCommandRegistry.InteractionType.SHIFT_RIGHT_CLICK);

                                    reg.addCommand(new Vec3i(bp.getX(), bp.getY(), bp.getZ()), bc);

                                    CommandUtil.sendCommandSuccess(ctx, MidnightEssentialsAPI.getInstance().getLangProvider(), false, "command.blockcommand.add");

                                    return 1;
                                } catch (Throwable th) {
                                    th.printStackTrace();
                                    throw th;
                                }
                            })
                        )
                    )
                    .then(Commands.literal("remove")
                        .then(Commands.argument("index", IntegerArgumentType.integer(0))
                            .executes(ctx -> {

                                BlockCommandRegistry reg = findRegistry(ConversionUtil.toIdentifier(ctx.getSource().getLevel().dimension().location()));
                                if(reg == null) {
                                    CommandUtil.sendCommandFailure(ctx, MidnightEssentialsAPI.getInstance().getLangProvider(), "command.blockcommand.error");
                                    return 0;
                                }

                                int index = ctx.getArgument("index", Integer.class);
                                BlockPos bp = BlockPosArgument.getLoadedBlockPos(ctx, "location");

                                reg.removeCommand(new Vec3i(bp.getX(), bp.getY(), bp.getZ()), index);

                                CommandUtil.sendCommandSuccess(ctx, MidnightEssentialsAPI.getInstance().getLangProvider(), false, "command.blockcommand.remove");

                                return 1;
                            })
                        )
                    )
                    .then(Commands.literal("list")
                        .executes(ctx -> {

                            BlockCommandRegistry reg = findRegistry(ConversionUtil.toIdentifier(ctx.getSource().getLevel().dimension().location()));
                            if(reg == null) {
                                CommandUtil.sendCommandFailure(ctx, MidnightEssentialsAPI.getInstance().getLangProvider(), "command.blockcommand.error");
                                return 0;
                            }

                            BlockPos bp = BlockPosArgument.getLoadedBlockPos(ctx, "location");

                            CommandUtil.sendCommandSuccess(ctx, MidnightEssentialsAPI.getInstance().getLangProvider(), false, "command.blockcommand.list", CustomPlaceholderInline.create("block", bp.toShortString()));

                            for(BlockCommandRegistry.BlockCommand cmd : reg.getCommands(new Vec3i(bp.getX(), bp.getY(), bp.getZ()))) {
                                ctx.getSource().sendSuccess(Component.literal(" - " + cmd.getCommand()), false);
                            }

                            return 1;
                        })
                    )
                    .then(Commands.literal("clear")
                        .executes(ctx -> {

                            BlockCommandRegistry reg = findRegistry(ConversionUtil.toIdentifier(ctx.getSource().getLevel().dimension().location()));
                            if(reg == null) {
                                CommandUtil.sendCommandFailure(ctx, MidnightEssentialsAPI.getInstance().getLangProvider(), "command.blockcommand.error");
                                return 0;
                            }

                            BlockPos bp = BlockPosArgument.getLoadedBlockPos(ctx, "location");
                            reg.clear(new Vec3i(bp.getX(), bp.getY(), bp.getZ()));

                            CommandUtil.sendCommandSuccess(ctx, MidnightEssentialsAPI.getInstance().getLangProvider(), false, "command.blockcommand.clear");

                            return 1;
                        })
                    )
                )
            )
        );

    }

    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(FabricBlockCommandModule::new, ID, DEFAULT_CONFIG);
}
