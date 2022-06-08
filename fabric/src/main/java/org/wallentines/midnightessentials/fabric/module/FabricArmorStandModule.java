package org.wallentines.midnightessentials.fabric.module;

import org.wallentines.midnightessentials.common.module.AbstractArmorStandModule;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.fabric.event.player.PlayerInteractEntityEvent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;

public class FabricArmorStandModule extends AbstractArmorStandModule {

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

    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(FabricArmorStandModule::new, ID, new ConfigSection());

}
