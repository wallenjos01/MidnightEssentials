package org.wallentines.midnightessentials.common.module.hologram;

import org.wallentines.midnightessentials.api.module.hologram.Hologram;
import org.wallentines.midnightessentials.api.module.hologram.HologramModule;
import org.wallentines.midnightlib.config.ConfigSection;

import java.util.HashMap;
import java.util.function.Function;

public abstract class AbstractHologramModule implements HologramModule {

    protected HashMap<String, Function<ConfigSection, Hologram>> parsers = new HashMap<>();


    @Override
    public void registerHologramParser(String key, Function<ConfigSection, Hologram> func) {
        parsers.put(key, func);
    }

    public Hologram parseHologram(ConfigSection section) {

        String type = section.getOrDefault("type", "text", String.class);
        return parsers.get(type).apply(section);
    }
}
