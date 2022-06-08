package org.wallentines.midnightessentials.common.module.hologram;

import org.wallentines.midnightcore.api.module.lang.LangProvider;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
import org.wallentines.midnightessentials.api.module.hologram.Hologram;
import org.wallentines.midnightlib.config.ConfigSection;

import java.util.List;

public class LangHologram implements Hologram {

    private final String key;
    private final Location location;
    private final LangProvider provider;
    private final AbstractHologramRegistry registry;

    public LangHologram(AbstractHologramRegistry reg, Location location, String key) {
        this.registry = reg;
        this.key = key;
        this.location = location;
        this.provider = MidnightEssentialsAPI.getInstance().getLangProvider();
    }

    @Override
    public List<MComponent> getMessage(MPlayer player) {
        return List.of(provider.getMessage(key, player));
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void unload() {
        registry.unloadHologram(this);
    }

    @Override
    public ConfigSection save() {
        return new ConfigSection().with("key", key).with("location", location);
    }

    public static LangHologram load(AbstractHologramRegistry reg, ConfigSection section) {
        return new LangHologram(reg, section.get("location", Location.class), section.getString("key"));
    }
}
