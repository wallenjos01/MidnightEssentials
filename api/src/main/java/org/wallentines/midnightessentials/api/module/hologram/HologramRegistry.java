package org.wallentines.midnightessentials.api.module.hologram;

import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.ConfigSection;

import java.util.List;

public interface HologramRegistry {

    Hologram createHologram(List<MComponent> components, Location location);

    void addHologram(Hologram hologram);

    void loadFromConfig(ConfigSection config);

    ConfigSection saveToConfig();

    void unloadHologram(Hologram hologram);

    void unloadAll();

}
