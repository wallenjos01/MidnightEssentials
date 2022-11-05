package org.wallentines.midnightessentials.fabric;

import org.wallentines.midnightessentials.common.MidnightEssentialsImpl;
import org.wallentines.midnightessentials.fabric.command.*;
import org.wallentines.midnightessentials.fabric.module.*;
import net.fabricmc.api.ModInitializer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.fabric.event.MidnightCoreAPICreatedEvent;
import org.wallentines.midnightcore.fabric.event.MidnightCoreLoadModulesEvent;
import org.wallentines.midnightcore.fabric.event.server.CommandLoadEvent;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
import org.wallentines.midnightessentials.fabric.listener.PlayerListener;
import org.wallentines.midnightlib.config.ConfigProvider;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.event.Event;

import java.io.File;

public class MidnightEssentials implements ModInitializer {

    @Override
    public void onInitialize() {

        MidnightEssentialsImpl api = new MidnightEssentialsImpl(new File("config/MidnightEssentials"), new ConfigSection());

        Event.register(CommandLoadEvent.class, this, event -> {
            new LaunchEntityCommand().register(event.getDispatcher());
            new SendCommand().register(event.getDispatcher());
            new SpawnCommand().register(event.getDispatcher());
            new ChatAsCommand().register(event.getDispatcher());
            new DamageCommand().register(event.getDispatcher());
            SitCommand.register(event.getDispatcher());
        });

        Event.register(MidnightCoreLoadModulesEvent.class, this, event -> {

            event.getModuleRegistry().register(FabricArmorStandModule.ID, FabricArmorStandModule.MODULE_INFO);
            event.getModuleRegistry().register(FabricBlockCommandModule.ID, FabricBlockCommandModule.MODULE_INFO);
            event.getModuleRegistry().register(FabricSignEditModule.ID, FabricSignEditModule.MODULE_INFO);
            event.getModuleRegistry().register(FabricAutoElytraModule.ID, FabricAutoElytraModule.MODULE_INFO);
            event.getModuleRegistry().register(FabricWaypointModule.ID, FabricWaypointModule.MODULE_INFO);

        });

        Event.register(MidnightCoreAPICreatedEvent.class, this, event -> {

            ConfigProvider prov = JsonConfigProvider.INSTANCE;
            ConfigSection lang = prov.loadFromStream(getClass().getResourceAsStream("/midnightessentials/lang/en_us.json"));
            ConfigSection esp = prov.loadFromStream(getClass().getResourceAsStream("/midnightessentials/lang/es_us.json"));

            api.initialize(event.getAPI(), lang);
            api.getLangProvider().loadEntries(esp, "es_us");

            if(MidnightCoreAPI.getInstance().getModuleManager().isModuleLoaded(FabricBlockCommandModule.ID)) {
                MidnightCoreAPI.getInstance().getModuleManager().getModule(FabricBlockCommandModule.class).reloadDefaultRegistry();
            }

        });

        new PlayerListener().registerEvents();

    }
}
