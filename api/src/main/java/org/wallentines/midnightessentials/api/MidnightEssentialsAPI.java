package org.wallentines.midnightessentials.api;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.lang.LangModule;
import org.wallentines.midnightcore.api.module.lang.LangProvider;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightessentials.api.config.MainConfig;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;

import java.io.File;
import java.util.Map;

public class MidnightEssentialsAPI {

    private static MidnightEssentialsAPI instance;

    private final LangProvider langProvider;
    private final MainConfig config;

    private final File dataFolder;

    public MidnightEssentialsAPI(MidnightCoreAPI api, File dataFolder, ConfigSection langDefaults, ConfigSection configDefaults) {

        instance = this;

        this.dataFolder = dataFolder;

        LangModule mod = api.getModuleManager().getModule(LangModule.class);

        File langFolder = new File(dataFolder, "lang");
        langProvider = mod.createProvider(langFolder.toPath(), langDefaults);

        config = new MainConfig(configDefaults, FileConfig.findOrCreate("config", dataFolder));
    }

    public LangProvider getLangProvider() {
        return langProvider;
    }

    public MainConfig getConfig() {
        return config;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public void onFirstJoin(MPlayer player) {

        if(config.getSpawnLocation() != null && config.shouldTeleportFirstJoin()) {
            Location loc = config.getSpawnLocation();
            if(loc == null) return;
            player.teleport(loc);
        }

        for(Map.Entry<Integer, MItemStack> ent : config.getFirstJoinItems().entrySet()) {
            player.giveItem(ent.getValue(), ent.getKey());
        }

    }

    public void onJoin(MPlayer player) {

        if(config.getSpawnLocation() != null && config.shouldTeleportEachJoin()) {
            Location loc = config.getSpawnLocation();
            if(loc == null) return;
            player.teleport(loc);
        }

    }

    public static MidnightEssentialsAPI getInstance() {
        return instance;
    }
}
