package org.wallentines.midnightessentials.api.module.blockcommand;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.math.Vec3i;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.registry.Identifier;

public interface BlockCommandModule extends Module<MidnightCoreAPI> {

    BlockCommandRegistry createRegistry(String id);

    void reloadDefaultRegistry();
    boolean execute(Vec3i loc, BlockCommandRegistry.InteractionType type, MPlayer player);

    void unloadRegistry(String id);
    BlockCommandRegistry getRegistry(String id);

    BlockCommandRegistry findRegistry(Identifier worldId);

}
