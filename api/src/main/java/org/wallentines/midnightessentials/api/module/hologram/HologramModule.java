package org.wallentines.midnightessentials.api.module.hologram;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.registry.Identifier;

public interface HologramModule extends Module<MidnightCoreAPI> {

    HologramRegistry getHologramRegistry(Identifier world);

}
