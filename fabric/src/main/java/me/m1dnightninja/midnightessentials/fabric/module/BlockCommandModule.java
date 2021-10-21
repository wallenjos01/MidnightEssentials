package me.m1dnightninja.midnightessentials.fabric.module;

import me.m1dnightninja.midnightcore.api.math.Vec3i;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.event.PlayerInteractEvent;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import me.m1dnightninja.midnightessentials.api.BlockCommandRegistry;
import me.m1dnightninja.midnightessentials.common.module.AbstractBlockCommandModule;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;

public class BlockCommandModule extends AbstractBlockCommandModule {

    @Override
    protected void registerCommands() {

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {

            // TODO: Command for managing BlockCommands in game
        });

    }

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
    }

    @Override
    public BlockCommandRegistry getRegistry(String id) {
        return registries.get(id);
    }
}
