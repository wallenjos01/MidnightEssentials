package org.wallentines.midnightessentials.fabric.module;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.fabric.event.player.PlayerLeaveEvent;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;

public class FabricAutoElytraModule implements Module<MidnightCoreAPI> {

    public static final Identifier ID = new Identifier("midnightessentials", "auto_elytra");
    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(FabricAutoElytraModule::new, ID, new ConfigSection().with("enabled", false));

    private final HashMap<FabricPlayer, PlayerItems> playerData = new HashMap<>();

    @Override
    public boolean initialize(ConfigSection configuration, MidnightCoreAPI api) {

        Event.register(PlayerLeaveEvent.class, this, this::onLeave);

        return true;
    }

    @Override
    public void disable() {

        for(FabricPlayer pl : playerData.keySet())  {
            if(pl.isOffline()) continue;

            playerData.get(pl).restore(pl.getInternal());
        }

        playerData.clear();
    }

    private void onLeave(PlayerLeaveEvent event) {

        FabricPlayer pl = (FabricPlayer) FabricPlayer.wrap(event.getPlayer());
        playerData.computeIfPresent(pl, (k,v) -> {
            v.restore(pl.getInternal());
            return null;
        });
    }

    private static class PlayerItems {

        final ItemStack handItem;
        final ItemStack chestplateItem;

        public PlayerItems(Player player) {
            this(player.getItemInHand(InteractionHand.OFF_HAND), player.getItemBySlot(EquipmentSlot.CHEST));
        }

        public PlayerItems(ItemStack hand, ItemStack chestplate) {
            this.handItem = hand;
            this.chestplateItem = chestplate;
        }

        public void restore(Player player) {

            if(player == null) return;

            player.setItemInHand(InteractionHand.OFF_HAND, handItem);
            player.setItemSlot(EquipmentSlot.CHEST, chestplateItem);
        }

    }

}
