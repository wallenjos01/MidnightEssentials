package org.wallentines.midnightessentials.fabric.module;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightessentials.common.module.AbstractSignEditModule;
import org.wallentines.midnightessentials.fabric.mixin.AccessorSignEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.wallentines.midnightcore.fabric.event.player.PlayerInteractEvent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;

public class FabricSignEditModule extends AbstractSignEditModule {

    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(FabricSignEditModule::new, ID, new ConfigSection());

    @Override
    protected void registerListeners() {

        Event.register(PlayerInteractEvent.class, this, this::onInteract);
    }

    private void onInteract(PlayerInteractEvent event) {

        if(event.getBlockHit() == null || !event.getPlayer().isShiftKeyDown() || !event.getPlayer().getItemInHand(event.getHand()).isEmpty()) return;


        GameType type = event.getPlayer().gameMode.getGameModeForPlayer();
        if(type == GameType.SPECTATOR) return;


        BlockPos pos = new BlockPos(event.getBlockHit().getBlockPos().getX(), event.getBlockHit().getBlockPos().getY(), event.getBlockHit().getBlockPos().getZ());
        BlockState b = event.getPlayer().getLevel().getBlockState(pos);
        if(!(b.getBlock() instanceof SignBlock)) return;

        SignBlockEntity ent = (SignBlockEntity) event.getPlayer().getLevel().getBlockEntity(pos);
        if(ent == null) return;

        CompoundTag tag = ent.getUpdateTag();

        boolean hasTag = tag.contains("Editable");
        boolean editable = tag.getBoolean("Editable");

        if(type == GameType.ADVENTURE) {
            if(!editable) return;
        }

        if(!hasTag || editable) {

            ent.setAllowedPlayerEditor(event.getPlayer().getUUID());
            ((AccessorSignEntity) ent).setIsEditable(true);

            event.getPlayer().connection.send(new ClientboundEntityEventPacket(event.getPlayer(), (byte) 9));
            event.getPlayer().openTextEdit(ent);

            event.setCancelled(true);
            event.setShouldSwingArm(true);
        }

    }
}
