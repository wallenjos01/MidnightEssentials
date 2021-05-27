package me.m1dnightninja.midnightessentials.fabric.module;

import me.m1dnightninja.midnightcore.fabric.api.event.PlayerInteractEntityEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightessentials.common.module.AbstractArmorStandModule;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ArmorStandModule extends AbstractArmorStandModule {

    @Override
    protected void registerListeners() {

        Event.register(PlayerInteractEntityEvent.class, this, event -> {

            if(event.getClicked().getType() != EntityType.ARMOR_STAND) return;

            ArmorStand as = (ArmorStand) event.getClicked();
            if(as.isShowArms()) return;

            ItemStack is = event.getPlayer().getItemInHand(event.getHand());
            if(is.getItem() == Items.STICK) {
                is.shrink(1);

                CompoundTag tag = new CompoundTag();
                as.addAdditionalSaveData(tag);

                tag.put("ShowArms", ByteTag.valueOf((byte) 1));
                as.readAdditionalSaveData(tag);

                ClientboundSetEntityDataPacket pck = new ClientboundSetEntityDataPacket(as.getId(), as.getEntityData(), true);
                for(ServerPlayer pl : event.getPlayer().getLevel().players()) {
                    pl.connection.send(pck);
                }

                event.getPlayer().playSound(SoundEvents.ARMOR_STAND_HIT, 1.0f, 1.0f);
                event.setCancelled(true);
                event.setShouldSwingArm(true);
            }
        });

    }

}
