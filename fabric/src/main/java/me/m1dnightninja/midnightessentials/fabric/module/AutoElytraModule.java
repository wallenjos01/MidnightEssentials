package me.m1dnightninja.midnightessentials.fabric.module;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.event.PlayerDisconnectEvent;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;

public class AutoElytraModule implements IModule {

    private static final MIdentifier ID = MIdentifier.create("midnightessentials", "auto_elytra");

    private final HashMap<FabricPlayer, PlayerItems> playerData = new HashMap<>();

    @Override
    public boolean initialize(ConfigSection configuration) {

        Event.register(PlayerDisconnectEvent.class, this, this::onLeave);

        return true;
    }

    @Override
    public MIdentifier getId() {
        return ID;
    }

    @Override
    public ConfigSection getDefaultConfig() {

        ConfigSection section = new ConfigSection();
        section.set("enabled", false);

        return section;
    }

    @Override
    public void onDisable() {

        for(FabricPlayer pl : playerData.keySet())  {
            if(pl.isOffline()) continue;

            playerData.get(pl).restore(pl.getMinecraftPlayer());
        }

        playerData.clear();
    }

    private void onLeave(PlayerDisconnectEvent event) {

        FabricPlayer pl = (FabricPlayer) FabricPlayer.wrap(event.getPlayer());
        playerData.computeIfPresent(pl, (k,v) -> {
            v.restore(pl.getMinecraftPlayer());
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
