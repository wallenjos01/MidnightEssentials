package org.wallentines.midnightessentials.common;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.data.DataModule;
import org.wallentines.midnightcore.api.module.data.DataProvider;
import org.wallentines.midnightcore.api.module.lang.LangModule;
import org.wallentines.midnightcore.api.module.lang.LangProvider;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
import org.wallentines.midnightessentials.api.config.MainConfig;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;

import java.io.File;
import java.util.Map;

public class MidnightEssentialsImpl extends MidnightEssentialsAPI {

    private LangProvider langProvider;
    private DataProvider dataProvider;
    private final MainConfig config;
    private final File dataFolder;

    public MidnightEssentialsImpl(File dataFolder, ConfigSection configDefaults) {

        this.dataFolder = dataFolder;
        config = new MainConfig(configDefaults, FileConfig.findOrCreate("config", dataFolder));
    }

    @Override
    public LangProvider getLangProvider() {
        return langProvider;
    }

    @Override
    public MainConfig getConfig() {
        return config;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public DataProvider getDataProvider() {
        return dataProvider;
    }

    public void initialize(MidnightCoreAPI api, ConfigSection langDefaults) {

        LangModule mod = api.getModuleManager().getModule(LangModule.class);
        DataModule dMod = api.getModuleManager().getModule(DataModule.class);

        File langFolder = new File(dataFolder, "lang");
        langProvider = mod.createProvider(langFolder.toPath(), langDefaults);
        dataProvider = dMod.getOrCreateProvider(dataFolder.toPath().resolve("data"));
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

}
