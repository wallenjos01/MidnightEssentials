package org.wallentines.midnightessentials.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.data.DataModule;
import org.wallentines.midnightcore.api.module.data.DataProvider;
import org.wallentines.midnightcore.api.module.lang.LangModule;
import org.wallentines.midnightcore.api.module.lang.LangProvider;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightessentials.api.config.MainConfig;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;

import java.io.File;
import java.util.Map;

public abstract class MidnightEssentialsAPI {

    private static MidnightEssentialsAPI INSTANCE;
    private static final Logger LOGGER = LogManager.getLogger("MidnightEssentials");

    public MidnightEssentialsAPI() {
        INSTANCE = this;
    }

    public abstract MainConfig getConfig();
    public abstract LangProvider getLangProvider();
    public abstract DataProvider getDataProvider();

    public abstract File getDataFolder();

    public static MidnightEssentialsAPI getInstance() {
        return INSTANCE;
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
