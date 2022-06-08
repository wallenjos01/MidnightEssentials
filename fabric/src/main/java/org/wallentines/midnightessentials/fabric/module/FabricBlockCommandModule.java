package org.wallentines.midnightessentials.fabric.module;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.fabric.event.player.PlayerInteractEvent;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightessentials.api.module.blockcommand.BlockCommandRegistry;
import org.wallentines.midnightessentials.common.module.AbstractBlockCommandModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.math.Vec3i;
import org.wallentines.midnightlib.module.ModuleInfo;

public class FabricBlockCommandModule extends AbstractBlockCommandModule {

    @Override
    protected void registerCommands() {

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

    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(FabricBlockCommandModule::new, ID, DEFAULT_CONFIG);
}
