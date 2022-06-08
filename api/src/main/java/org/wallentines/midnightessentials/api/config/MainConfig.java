package org.wallentines.midnightessentials.api.config;

import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightlib.config.ConfigRegistry;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;

import java.util.HashMap;

public class MainConfig {

    private final FileConfig config;
    private final ConfigSection defaults;

    private Location spawnLocation;
    private boolean teleportFirstJoin;
    private boolean teleportEachJoin;

    private boolean disableJoinLeaveMessages;

    private final HashMap<Integer, MItemStack> firstJoinItems = new HashMap<>();

    public MainConfig(ConfigSection defaults, FileConfig config) {
        this.config = config;
        this.defaults = defaults;

        loadConfig();
    }

    public Location getSpawnLocation() {
       return spawnLocation;
    }

    public boolean shouldTeleportFirstJoin() {
        return teleportFirstJoin;
    }

    public boolean shouldTeleportEachJoin() {
        return teleportEachJoin;
    }

    public void setSpawnLocation(Location spawnLocation) {

        ConfigSection spawn = config.getRoot().getOrCreateSection("spawn");
        spawn.set("location", spawnLocation);
        this.spawnLocation = spawnLocation;
    }

    public void setTeleportFirstJoin(boolean teleportFirstJoin) {

        ConfigSection spawn = config.getRoot().getOrCreateSection("spawn");
        spawn.set("teleport_first_join", teleportFirstJoin);
        this.teleportFirstJoin = teleportFirstJoin;
    }

    public void setTeleportEachJoin(boolean teleportEachJoin) {

        ConfigSection spawn = config.getRoot().getOrCreateSection("spawn");
        spawn.set("teleport_each_join", teleportEachJoin);
        this.teleportEachJoin = teleportEachJoin;
    }

    public boolean shouldDisableJoinLeaveMessages() {
        return disableJoinLeaveMessages;
    }

    public void reload() {

        config.reload();
        loadConfig();

    }

    public void save() {

        config.save();

    }

    public HashMap<Integer, MItemStack> getFirstJoinItems() {
        return firstJoinItems;
    }

    private void loadConfig() {

        config.getRoot().fill(defaults);
        config.save();

        firstJoinItems.clear();

        if(config.getRoot().has("spawn")) {

            ConfigSection spawn = config.getRoot().getOrDefault("spawn", null, ConfigSection.class);

            spawnLocation = spawn.getOrDefault("location", null, Location.class);
            teleportFirstJoin = spawn.getBoolean("teleport_first_join", true);
            teleportEachJoin = spawn.getBoolean("teleport_each_join", false);
        }

        disableJoinLeaveMessages = config.getRoot().getBoolean("disable_join_messages", false);

        if(!config.getRoot().has("first_join_items")) return;
        for(ConfigSection sec : config.getRoot().getListFiltered("first_join_items", ConfigSection.class)) {

            MItemStack mis = ConfigRegistry.INSTANCE.getSerializer(MItemStack.class).deserialize(sec);
            int slot = sec.getOrDefault("slot", -1, Integer.class);

            firstJoinItems.put(slot, mis);
        }

    }


}
