package me.m1dnightninja.midnightessentials.api;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.midnightcore.api.config.FileConfig;
import me.m1dnightninja.midnightcore.api.player.MPlayer;

import java.io.File;

public class MidnightEssentialsAPI {

    private static MidnightEssentialsAPI instance;

    private final ILangProvider langProvider;
    private final MainConfig config;

    private final File dataFolder;

    public MidnightEssentialsAPI(MidnightCoreAPI api, File dataFolder, ConfigSection langDefaults, ConfigSection configDefaults) {

        instance = this;

        this.dataFolder = dataFolder;

        ILangModule mod = api.getModule(ILangModule.class);

        File langFolder = new File(dataFolder, "lang");
        langProvider = mod.createLangProvider(langFolder, langDefaults);

        config = new MainConfig(configDefaults, new FileConfig(new File(dataFolder, "config" + api.getDefaultConfigProvider().getFileExtension())));
    }

    public ILangProvider getLangProvider() {
        return langProvider;
    }

    public MainConfig getConfig() {
        return config;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public void onFirstJoin(MPlayer player) {

        System.out.println(player.getName().allContent() + " joined for the first time");

        if(config.getSpawnLocation() != null && config.shouldTeleportFirstJoin()) {
            player.teleport(config.getSpawnDimension(), config.getSpawnLocation(), config.getSpawnYaw(), config.getSpawnPitch());
        }

    }

    public void onJoin(MPlayer player) {

        if(config.getSpawnLocation() != null && config.shouldTeleportEachJoin()) {
            player.teleport(config.getSpawnDimension(), config.getSpawnLocation(), config.getSpawnYaw(), config.getSpawnPitch());
        }

    }

    public static MidnightEssentialsAPI getInstance() {
        return instance;
    }
}
