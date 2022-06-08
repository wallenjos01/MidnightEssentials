package org.wallentines.midnightessentials.common.module.hologram;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightessentials.api.module.hologram.Hologram;
import org.wallentines.midnightlib.config.ConfigSection;

import java.util.List;

public class TextHologram implements Hologram {

    private final List<MComponent> messages;
    private final Location location;
    private final AbstractHologramRegistry registry;

    public TextHologram(AbstractHologramRegistry reg, Location location, List<MComponent> messages) {
        this.registry = reg;
        this.location = location;
        this.messages = messages;
    }

    @Override
    public List<MComponent> getMessage(MPlayer player) {
        return messages;
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
        return new ConfigSection().with("message", messages).with("location", location);
    }

    public static TextHologram load(AbstractHologramRegistry reg, ConfigSection sec) {
        return new TextHologram(reg, sec.get("location", Location.class), sec.getListFiltered("message", MComponent.class));
    }
}
