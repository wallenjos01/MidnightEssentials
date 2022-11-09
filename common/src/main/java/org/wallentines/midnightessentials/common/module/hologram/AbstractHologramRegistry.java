package org.wallentines.midnightessentials.common.module.hologram;

import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
import org.wallentines.midnightessentials.api.module.hologram.Hologram;
import org.wallentines.midnightessentials.api.module.hologram.HologramRegistry;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractHologramRegistry implements HologramRegistry {

    protected final HashMap<String, Hologram> loaded = new HashMap<>();
    protected final AbstractHologramModule module;
    protected final Identifier worldId;

    public AbstractHologramRegistry(AbstractHologramModule module, Identifier worldId) {
        this.module = module;
        this.worldId = worldId;
    }

    @Override
    public void addHologram(String id, Hologram hologram) {
        onLoad(id, hologram);
        loaded.put(id, hologram);
    }

    @Override
    public void loadFromConfig(ConfigSection config) {

        for(String key : config.getKeys()) {
            try {
                ConfigSection sec = config.getSection(key);
                addHologram(key, HologramImpl.SERIALIZER.deserialize(sec));
            } catch (Exception ex) {
                MidnightEssentialsAPI.getLogger().warn("An error occurred while parsing a Hologram!");
                ex.printStackTrace();
            }
        }
    }

    @Override
    public ConfigSection saveToConfig() {

        ConfigSection out = new ConfigSection();
        for(Map.Entry<String, Hologram> hg : loaded.entrySet()) {
            out.set(hg.getKey(), HologramImpl.SERIALIZER.serialize(hg.getValue()));
        }

        return out;
    }

    @Override
    public void unloadHologram(String key) {

        Hologram hg = loaded.remove(key);
        onUnload(key, hg);
    }

    @Override
    public void unloadAll() {

        for(Map.Entry<String, Hologram> hg : loaded.entrySet()) {
            onUnload(hg.getKey(), hg.getValue());
        }
        loaded.clear();
    }

    @Override
    public Collection<String> getHologramIds() {
        return loaded.keySet();
    }

    @Override
    public Hologram getHologram(String id) {
        return loaded.get(id);
    }

    protected abstract void onLoad(String key, Hologram hologram);
    protected abstract void onUnload(String key, Hologram hologram);

}
