package me.m1dnightninja.midnightessentials.fabric;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.api.MidnightCoreModInitializer;
import me.m1dnightninja.midnightessentials.fabric.command.LaunchEntityCommand;
import me.m1dnightninja.midnightessentials.fabric.command.SendCommand;
import me.m1dnightninja.midnightessentials.fabric.module.ArmorStandModule;
import me.m1dnightninja.midnightessentials.fabric.module.SignEditModule;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import java.util.Arrays;
import java.util.Collection;

public class MidnightEssentials implements MidnightCoreModInitializer {

    @Override
    public void onInitialize() {

        CommandRegistrationCallback.EVENT.register((commandDispatcher, b) -> {
            new LaunchEntityCommand().register(commandDispatcher);
            new SendCommand().register(commandDispatcher);
        });

    }

    @Override
    public Collection<IModule> getModules() {
        return Arrays.asList(new ArmorStandModule(), new SignEditModule());
    }

    @Override
    public void onAPICreated(MidnightCore midnightCore, MidnightCoreAPI midnightCoreAPI) { }
}
