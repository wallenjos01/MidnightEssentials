package org.wallentines.midnightessentials.common.module.hologram;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
import org.wallentines.midnightessentials.api.module.hologram.HologramModule;
import org.wallentines.midnightessentials.api.module.hologram.HologramRegistry;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractHologramModule implements HologramModule {

    protected final HashMap<Identifier, HologramRegistry> registries = new HashMap<>();
    private FileConfig config;

    @Override
    public HologramRegistry getHologramRegistry(Identifier world) {
        return registries.computeIfAbsent(world, this::createRegistry);
    }

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        config = FileConfig.findOrCreate("holograms", MidnightEssentialsAPI.getInstance().getDataFolder());
        for(String key : config.getRoot().getKeys()) {
            Identifier id = Identifier.parseOrDefault(key, "minecraft");
            getHologramRegistry(id).loadFromConfig(config.getRoot().getSection(key));
        }

        return true;
    }

    @Override
    public void disable() {

        for(Map.Entry<Identifier, HologramRegistry> ent : registries.entrySet()) {
            config.getRoot().set(ent.getKey().toString(), ent.getValue().saveToConfig());
            ent.getValue().unloadAll();
        }

        registries.clear();

        config.save();

    }

    protected abstract HologramRegistry createRegistry(Identifier world);

    public static final Identifier ID = new Identifier("midnightessentials","hologram");
}
