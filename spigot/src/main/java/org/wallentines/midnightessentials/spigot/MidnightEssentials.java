package org.wallentines.midnightessentials.spigot;

import org.wallentines.midnightessentials.spigot.module.SpigotArmorStandModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.wallentines.midnightcore.spigot.event.MidnightCoreLoadModulesEvent;


public class MidnightEssentials extends JavaPlugin implements Listener {

    private static MidnightEssentials instance;

    @Override
    public void onLoad() {

        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onEnable() {

        instance = this;

    }

    public static MidnightEssentials getInstance() {
        return instance;
    }

    @EventHandler
    public void onLoadModules(MidnightCoreLoadModulesEvent event) {
        event.getRegistry().register(SpigotArmorStandModule.ID, SpigotArmorStandModule.MODULE_INFO);
    }



}
