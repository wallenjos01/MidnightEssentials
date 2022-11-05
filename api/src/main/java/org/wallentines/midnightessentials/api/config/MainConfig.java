package org.wallentines.midnightessentials.api.config;

import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
import org.wallentines.midnightessentials.api.StringRegistry;
import org.wallentines.midnightessentials.api.Warp;
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

    private final StringRegistry<Warp> warpRegistry = new StringRegistry<>();

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

    public StringRegistry<Warp> getWarpRegistry() {
        return warpRegistry;
    }

    private void loadConfig() {

        config.getRoot().fill(defaults);
        config.save();

        warpRegistry.clear();
        firstJoinItems.clear();

        if(config.getRoot().has("spawn")) {

            ConfigSection spawn = config.getRoot().getOrDefault("spawn", null, ConfigSection.class);

            spawnLocation = spawn.getOrDefault("location", null, Location.class);
            teleportFirstJoin = spawn.getBoolean("teleport_first_join", true);
            teleportEachJoin = spawn.getBoolean("teleport_each_join", false);
        }

        disableJoinLeaveMessages = config.getRoot().getBoolean("disable_join_messages", false);

        if(config.getRoot().has("first_join_items")) {
            for (ConfigSection sec : config.getRoot().getListFiltered("first_join_items", ConfigSection.class)) {

                MItemStack mis = ConfigRegistry.INSTANCE.getSerializer(MItemStack.class, ConfigRegistry.Direction.DESERIALIZE).deserialize(sec);
                int slot = sec.getOrDefault("slot", -1, Integer.class);

                firstJoinItems.put(slot, mis);
            }
        }

        if(config.getRoot().has("warps")) {

            ConfigSection warps = config.getRoot().getSection("warps");
            for(String key : warps.getKeys()) {
                try {
                    Warp warp = Warp.SERIALIZER.deserialize(warps.getSection(key));
                    warpRegistry.register(key, warp);

                } catch (Exception ex) {
                    MidnightEssentialsAPI.getLogger().warn("An error occurred while parsing a Warp!");
                    ex.printStackTrace();
                }
            }
        }


    }


}
