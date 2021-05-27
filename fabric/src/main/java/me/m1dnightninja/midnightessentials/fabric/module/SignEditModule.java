package me.m1dnightninja.midnightessentials.fabric.module;

import me.m1dnightninja.midnightcore.fabric.api.event.PlayerInteractEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightessentials.common.module.AbstractSignEditModule;
import me.m1dnightninja.midnightessentials.fabric.mixin.AccessorSignEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public class SignEditModule extends AbstractSignEditModule {

    @Override
    protected void registerListeners() {

        Event.register(PlayerInteractEvent.class, this, this::onInteract);
    }

    private void onInteract(PlayerInteractEvent event) {

        if(event.getBlockHit() == null || event.getBlockHit().getType() == HitResult.Type.MISS || !event.getPlayer().isShiftKeyDown() || !event.getPlayer().getItemInHand(event.getHand()).isEmpty()) return;


        GameType type = event.getPlayer().gameMode.getGameModeForPlayer();
        if(type == GameType.SPECTATOR) return;


        BlockState b = event.getPlayer().getLevel().getBlockState(event.getBlockHit().getBlockPos());
        if(!(b.getBlock() instanceof SignBlock)) return;

        SignBlockEntity ent = (SignBlockEntity) event.getPlayer().getLevel().getBlockEntity(event.getBlockHit().getBlockPos());
        if(ent == null) return;

        CompoundTag tag = ent.getUpdateTag();

        boolean hasTag = tag.contains("Editable");
        boolean editable = tag.getBoolean("Editable");

        if(type == GameType.ADVENTURE) {
            if(!editable) return;
        }

        if(!hasTag || editable) {

            ent.setAllowedPlayerEditor(event.getPlayer());
            ((AccessorSignEntity) ent).setIsEditable(true);

            event.getPlayer().connection.send(new ClientboundEntityEventPacket(event.getPlayer(), (byte) 9));
            event.getPlayer().openTextEdit(ent);

            event.setCancelled(true);
            event.setShouldSwingArm(true);
        }

    }
}
