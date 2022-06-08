package org.wallentines.midnightessentials.fabric.module.hologram;

import org.wallentines.midnightessentials.api.module.hologram.HologramRegistry;
import org.wallentines.midnightessentials.common.module.hologram.AbstractHologramModule;

public class FabricHologramModule extends AbstractHologramModule {

    private final FabricHologramRegistry global = new FabricHologramRegistry(this);

    @Override
    public HologramRegistry getGlobalRegistry() {
        return global;
    }

    @Override
    public HologramRegistry createRegistry() {
        return new FabricHologramRegistry(this);
    }
}
