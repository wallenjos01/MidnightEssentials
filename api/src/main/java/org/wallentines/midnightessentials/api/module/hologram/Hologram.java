package org.wallentines.midnightessentials.api.module.hologram;

import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.math.Vec3d;

import java.util.List;

public interface Hologram {

    List<String> getLines();

    List<MComponent> getMessage(MPlayer player);

    Vec3d getLocation();

}
