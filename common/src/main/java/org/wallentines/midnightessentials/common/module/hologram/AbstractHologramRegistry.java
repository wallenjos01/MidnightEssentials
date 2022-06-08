package org.wallentines.midnightessentials.common.module.hologram;

import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightessentials.api.module.hologram.Hologram;
import org.wallentines.midnightessentials.api.module.hologram.HologramRegistry;
import org.wallentines.midnightlib.config.ConfigSection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractHologramRegistry implements HologramRegistry {

    protected final Set<Hologram> loaded = new HashSet<>();
    protected final AbstractHologramModule module;

    public AbstractHologramRegistry(AbstractHologramModule module) {
        this.module = module;

        module.registerHologramParser("text", c -> TextHologram.load(this, c));
        module.registerHologramParser("lang", c -> LangHologram.load(this, c));

    }

    @Override
    public Hologram createHologram(List<MComponent> components, Location location) {
        return new TextHologram(this, location, components);
    }

    @Override
    public void addHologram(Hologram hologram) {
        loaded.add(hologram);
        doLoad(hologram);
    }

    @Override
    public void loadFromConfig(ConfigSection config) {

        for(ConfigSection sec : config.getListFiltered("holograms", ConfigSection.class)) {
            addHologram(module.parseHologram(sec));
        }
    }

    @Override
    public ConfigSection saveToConfig() {

        List<ConfigSection> serialized = new ArrayList<>();
        loaded.forEach(hg -> serialized.add(hg.save()));

        return new ConfigSection().with("holograms", serialized);
    }

    @Override
    public void unloadHologram(Hologram hologram) {

        loaded.remove(hologram);
        doUnload(hologram);
    }

    @Override
    public void unloadAll() {

        loaded.forEach(Hologram::unload);
    }

    protected abstract void doLoad(Hologram hologram);
    protected abstract void doUnload(Hologram hologram);
}
