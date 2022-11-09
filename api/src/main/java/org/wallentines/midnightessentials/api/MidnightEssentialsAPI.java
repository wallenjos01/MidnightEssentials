package org.wallentines.midnightessentials.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wallentines.midnightcore.api.module.data.DataProvider;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightessentials.api.config.MainConfig;

import java.io.File;

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
