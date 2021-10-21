package me.m1dnightninja.midnightessentials.api;

import me.m1dnightninja.midnightcore.api.math.Vec3i;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.player.MPlayer;

public interface IBlockCommandModule extends IModule {

    BlockCommandRegistry createRegistry(String id);

    void reloadDefaultRegistry();
    boolean execute(Vec3i loc, BlockCommandRegistry.InteractionType type, MPlayer player);

    void unloadRegistry(String id);
    BlockCommandRegistry getRegistry(String id);

}
