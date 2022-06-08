package org.wallentines.midnightessentials.spigot.module;

import org.wallentines.midnightessentials.common.module.AbstractArmorStandModule;
import org.wallentines.midnightessentials.spigot.MidnightEssentials;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;

public class SpigotArmorStandModule extends AbstractArmorStandModule implements Listener {

    @Override
    protected void registerListeners() {

        MidnightEssentials.getInstance().getServer().getPluginManager().registerEvents(this, MidnightEssentials.getInstance());
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {

        Entity ent = event.getRightClicked();
        ItemStack is = event.getPlayer().getInventory().getItem(event.getHand());

        if(!(ent instanceof ArmorStand as) || is.getType() != Material.STICK) return;

        if(is.getAmount() == 1) {
            event.getPlayer().getInventory().setItem(event.getHand(), null);
        } else {
            is.setAmount(is.getAmount() - 1);
        }

        as.setArms(true);

        event.setCancelled(true);
    }

    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(SpigotArmorStandModule::new, ID, new ConfigSection());
}
