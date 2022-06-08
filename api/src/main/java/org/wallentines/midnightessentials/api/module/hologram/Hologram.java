package org.wallentines.midnightessentials.api.module.hologram;

import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.ConfigSection;

import java.util.List;

public interface Hologram {

    List<MComponent> getMessage(MPlayer player);

    Location getLocation();

    void unload();

    ConfigSection save();

}
