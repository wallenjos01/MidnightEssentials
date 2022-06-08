package org.wallentines.midnightessentials.api.module.hologram;

import org.wallentines.midnightlib.config.ConfigSection;

import java.util.function.Function;

public interface HologramModule {

    HologramRegistry getGlobalRegistry();

    HologramRegistry createRegistry();

    void registerHologramParser(String key, Function<ConfigSection, Hologram> func);

    Hologram parseHologram(ConfigSection section);

}
