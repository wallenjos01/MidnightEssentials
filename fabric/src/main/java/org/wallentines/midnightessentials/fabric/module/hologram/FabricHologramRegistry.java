package org.wallentines.midnightessentials.fabric.module.hologram;

import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightcore.fabric.MidnightCore;
import org.wallentines.midnightcore.fabric.event.player.PlayerChangeSettingsEvent;
import org.wallentines.midnightessentials.api.module.hologram.Hologram;
import org.wallentines.midnightessentials.common.module.hologram.AbstractHologramModule;
import org.wallentines.midnightessentials.common.module.hologram.AbstractHologramRegistry;
import org.wallentines.midnightlib.event.Event;

public class FabricHologramRegistry extends AbstractHologramRegistry {


    public FabricHologramRegistry(AbstractHologramModule module) {
        super(module);

        Event.register(PlayerChangeSettingsEvent.class, this, event -> {
            loaded.forEach(hg -> unloadFor(hg, event.getPlayer()));
            loaded.forEach(hg -> loadFor(hg, event.getPlayer()));
        });

    }

    @Override
    protected void doLoad(Hologram hologram) {
        for(ServerPlayer sp : MidnightCore.getInstance().getServer().getPlayerList().getPlayers()) {
            loadFor(hologram, sp);
        }
    }

    @Override
    protected void doUnload(Hologram hologram) {
        for(ServerPlayer sp : MidnightCore.getInstance().getServer().getPlayerList().getPlayers()) {
            unloadFor(hologram, sp);
        }
    }

    private void loadFor(Hologram hg, ServerPlayer player) {



    }

    private void unloadFor(Hologram hg, ServerPlayer player) {

    }

}
