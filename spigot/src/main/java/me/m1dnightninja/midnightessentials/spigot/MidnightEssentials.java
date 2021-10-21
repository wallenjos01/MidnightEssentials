package me.m1dnightninja.midnightessentials.spigot;

import me.m1dnightninja.midnightcore.spigot.event.MidnightCoreLoadModulesEvent;
import me.m1dnightninja.midnightessentials.spigot.module.ArmorStandModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


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
        event.registerModule(new ArmorStandModule());
    }



}
