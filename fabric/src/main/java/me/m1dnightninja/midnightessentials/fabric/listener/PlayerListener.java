package me.m1dnightninja.midnightessentials.fabric.listener;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.playerdata.IPlayerDataModule;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.event.PlayerJoinedEvent;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import me.m1dnightninja.midnightessentials.api.MidnightEssentialsAPI;

public class PlayerListener {

    public void registerEvents() {

        Event.register(PlayerJoinedEvent.class, this, this::onJoin);

    }

    private void onJoin(PlayerJoinedEvent event) {

        IPlayerDataModule mod = MidnightCoreAPI.getInstance().getModule(IPlayerDataModule.class);

        ConfigSection sec = mod.getGlobalProvider().getPlayerData(event.getPlayer().getUUID()).getOrCreateSection("midnightessentials");
        if(!sec.getBoolean("joined")) {

            MidnightEssentialsAPI.getInstance().onFirstJoin(FabricPlayer.wrap(event.getPlayer()));
            sec.set("joined", true);

            mod.getGlobalProvider().savePlayerData(event.getPlayer().getUUID());
        }

        MidnightEssentialsAPI.getInstance().onJoin(FabricPlayer.wrap(event.getPlayer()));

    }

}
