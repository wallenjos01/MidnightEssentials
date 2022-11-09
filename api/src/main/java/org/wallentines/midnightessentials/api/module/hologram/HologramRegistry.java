package org.wallentines.midnightessentials.api.module.hologram;

import org.wallentines.midnightlib.config.ConfigSection;

import java.util.Collection;

public interface HologramRegistry {

    void addHologram(String id, Hologram hologram);

    void loadFromConfig(ConfigSection config);

    ConfigSection saveToConfig();

    void unloadHologram(String id);

    void unloadAll();

    Collection<String> getHologramIds();

    Hologram getHologram(String id);

}
