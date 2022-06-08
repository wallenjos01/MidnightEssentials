package org.wallentines.midnightessentials.common.module;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.registry.Identifier;

public abstract class AbstractSignEditModule implements Module<MidnightCoreAPI> {

    public static final Identifier ID = new Identifier("midnightessentials","editable_signs");

    @Override
    public boolean initialize(ConfigSection configuration, MidnightCoreAPI api) {

        registerListeners();
        return true;
    }

    protected abstract void registerListeners();


}
