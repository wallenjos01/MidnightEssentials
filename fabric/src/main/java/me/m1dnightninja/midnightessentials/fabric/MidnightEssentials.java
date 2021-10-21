package me.m1dnightninja.midnightessentials.fabric;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.common.config.JsonConfigProvider;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.MidnightCoreModInitializer;
import me.m1dnightninja.midnightessentials.api.MidnightEssentialsAPI;
import me.m1dnightninja.midnightessentials.fabric.command.ChatAsCommand;
import me.m1dnightninja.midnightessentials.fabric.command.DamageCommand;
import me.m1dnightninja.midnightessentials.fabric.command.LaunchEntityCommand;
import me.m1dnightninja.midnightessentials.fabric.command.SendCommand;
import me.m1dnightninja.midnightessentials.fabric.command.SpawnCommand;
import me.m1dnightninja.midnightessentials.fabric.listener.PlayerListener;
import me.m1dnightninja.midnightessentials.fabric.module.ArmorStandModule;
import me.m1dnightninja.midnightessentials.fabric.module.BlockCommandModule;
import me.m1dnightninja.midnightessentials.fabric.module.SignEditModule;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

public class MidnightEssentials implements MidnightCoreModInitializer {

    @Override
    public void onInitialize() {

        CommandRegistrationCallback.EVENT.register((commandDispatcher, b) -> {
            new LaunchEntityCommand().register(commandDispatcher);
            new SendCommand().register(commandDispatcher);
            new SpawnCommand().register(commandDispatcher);
            new ChatAsCommand().register(commandDispatcher);
            new DamageCommand().register(commandDispatcher);
        });

        new PlayerListener().registerEvents();

    }

    @Override
    public Collection<IModule> getModules() {
        return Arrays.asList(new ArmorStandModule(), new SignEditModule(), new BlockCommandModule());
    }

    @Override
    public void onAPICreated(MidnightCore midnightCore, MidnightCoreAPI midnightCoreAPI) {

        ConfigProvider prov = JsonConfigProvider.INSTANCE;
        ConfigSection config = prov.loadFromStream(getClass().getResourceAsStream("/config.json"));
        ConfigSection lang = prov.loadFromStream(getClass().getResourceAsStream("/assets/midnightessentials/lang/en_us.json"));

        new MidnightEssentialsAPI(midnightCoreAPI, new File("config/MidnightEssentials"), lang, config);

        if(MidnightCoreAPI.getInstance().isModuleLoaded(BlockCommandModule.ID)) {
            MidnightCoreAPI.getInstance().getModule(BlockCommandModule.class).reloadDefaultRegistry();
        }

    }
}
