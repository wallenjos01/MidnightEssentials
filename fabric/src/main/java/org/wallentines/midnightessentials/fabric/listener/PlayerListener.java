package org.wallentines.midnightessentials.fabric.listener;

import net.minecraft.network.chat.Component;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.data.DataModule;
import org.wallentines.midnightcore.fabric.event.player.PlayerJoinEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerLeaveEvent;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;

public class PlayerListener {

    public void registerEvents() {

        Event.register(PlayerJoinEvent.class, this, this::onJoin);
        Event.register(PlayerLeaveEvent.class, this, this::onLeave);

    }

    private void onJoin(PlayerJoinEvent event) {

        DataModule mod = MidnightCoreAPI.getInstance().getModuleManager().getModule(DataModule.class);

        FabricPlayer fp = FabricPlayer.wrap(event.getPlayer());
        ConfigSection sec = mod.getGlobalProvider().getData(fp).getOrCreateSection("midnightessentials");
        if(!sec.getBoolean("joined")) {

            MidnightEssentialsAPI.getInstance().onFirstJoin(fp);
            sec.set("joined", true);

            mod.getGlobalProvider().saveData(fp);
        }

        if(MidnightEssentialsAPI.getInstance().getConfig().shouldDisableJoinLeaveMessages()) {
            event.setJoinMessage((Component) null);
        }
        MidnightEssentialsAPI.getInstance().onJoin(fp);

    }

    private void onLeave(PlayerLeaveEvent event) {
        if(MidnightEssentialsAPI.getInstance().getConfig().shouldDisableJoinLeaveMessages()) {
            event.setLeaveMessage(null);
        }
    }

}
