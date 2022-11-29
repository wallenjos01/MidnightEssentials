package org.wallentines.midnightessentials.fabric;

import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.api.text.LangRegistry;
import org.wallentines.midnightessentials.common.MidnightEssentialsImpl;
import org.wallentines.midnightessentials.fabric.command.*;
import org.wallentines.midnightessentials.fabric.module.*;
import net.fabricmc.api.ModInitializer;
import org.wallentines.midnightcore.fabric.event.server.CommandLoadEvent;
import org.wallentines.midnightessentials.fabric.listener.PlayerListener;
import org.wallentines.midnightessentials.fabric.module.hologram.FabricHologramModule;
import org.wallentines.midnightlib.config.ConfigProvider;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.event.Event;

import java.io.File;

public class MidnightEssentials implements ModInitializer {

    @Override
    public void onInitialize() {

        Registries.MODULE_REGISTRY.register(FabricArmorStandModule.ID, FabricArmorStandModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricBlockCommandModule.ID, FabricBlockCommandModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricSignEditModule.ID, FabricSignEditModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricAutoElytraModule.ID, FabricAutoElytraModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricWaypointModule.ID, FabricWaypointModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricHologramModule.ID, FabricHologramModule.MODULE_INFO);

        ConfigProvider prov = JsonConfigProvider.INSTANCE;
        ConfigSection lang = prov.loadFromStream(getClass().getResourceAsStream("/midnightessentials/lang/en_us.json"));

        MidnightEssentialsImpl api = new MidnightEssentialsImpl(new File("config/MidnightEssentials"), new ConfigSection(), lang);

        // Load Spanish Entries
        ConfigSection esp = prov.loadFromStream(getClass().getResourceAsStream("/midnightessentials/lang/es_mx.json"));
        api.getLangProvider().loadEntries("es_mx", LangRegistry.fromConfigSection(esp));

        Event.register(CommandLoadEvent.class, this, event -> {
            LaunchEntityCommand.register(event.getDispatcher());
            SendCommand.register(event.getDispatcher());
            SpawnCommand.register(event.getDispatcher());
            ChatAsCommand.register(event.getDispatcher());
            DamageCommand.register(event.getDispatcher());
            WarpCommand.register(event.getDispatcher());
            SitCommand.register(event.getDispatcher());

            FabricBlockCommandModule.registerCommands(event.getDispatcher());
        });


        PlayerListener.registerEvents();

    }
}
