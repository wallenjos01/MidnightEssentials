package me.m1dnightninja.midnightessentials.spigot.module;

import me.m1dnightninja.midnightessentials.common.module.AbstractArmorStandModule;
import me.m1dnightninja.midnightessentials.spigot.MidnightEssentials;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ArmorStandModule extends AbstractArmorStandModule implements Listener {

    @Override
    protected void registerListeners() {

        MidnightEssentials.getInstance().getServer().getPluginManager().registerEvents(this, MidnightEssentials.getInstance());
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {

        Entity ent = event.getRightClicked();
        ItemStack is = event.getPlayer().getInventory().getItem(event.getHand());

        if(!(ent instanceof ArmorStand) || is.getType() != Material.STICK) return;

        if(is.getAmount() == 1) {
            event.getPlayer().getInventory().setItem(event.getHand(), null);
        } else {
            is.setAmount(is.getAmount() - 1);
        }

        ArmorStand as = (ArmorStand) ent;
        as.setArms(true);

        event.setCancelled(true);

    }
}
