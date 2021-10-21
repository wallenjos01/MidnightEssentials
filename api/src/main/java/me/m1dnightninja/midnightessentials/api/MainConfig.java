package me.m1dnightninja.midnightessentials.api;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.FileConfig;
import me.m1dnightninja.midnightcore.api.math.Vec3d;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;

public class MainConfig {

    private final FileConfig config;
    private final ConfigSection defaults;

    public MainConfig(ConfigSection defaults, FileConfig config) {
        this.config = config;
        this.defaults = defaults;

        loadConfig();
    }

    public Vec3d getSpawnLocation() {
        return config.getRoot().has("spawn", ConfigSection.class) && config.getRoot().getSection("spawn").has("location", Vec3d.class) ?
                config.getRoot().getSection("spawn").get("location", Vec3d.class) :
                null;
    }

    public float getSpawnYaw() {
        return config.getRoot().has("spawn", ConfigSection.class) && config.getRoot().getSection("spawn").has("yaw", Number.class) ?
                config.getRoot().getSection("spawn").getFloat("yaw") :
                0.0f;
    }

    public float getSpawnPitch() {
        return config.getRoot().has("spawn", ConfigSection.class) && config.getRoot().getSection("spawn").has("pitch", Number.class) ?
                config.getRoot().getSection("spawn").getFloat("pitch") :
                0.0f;
    }

    public MIdentifier getSpawnDimension() {
        return config.getRoot().has("spawn", ConfigSection.class) && config.getRoot().getSection("spawn").has("dimension", MIdentifier.class) ?
                config.getRoot().getSection("spawn").get("dimension", MIdentifier.class) :
                MIdentifier.create("minecraft", "overworld");
    }

    public boolean shouldTeleportFirstJoin() {
        return config.getRoot().getSection("spawn").getBoolean("teleport_first_join", true);
    }

    public boolean shouldTeleportEachJoin() {
        return config.getRoot().getSection("spawn").getBoolean("teleport_each_join");
    }

    public void setSpawnLocation(Vec3d spawnLocation) {

        ConfigSection spawn = config.getRoot().getOrCreateSection("spawn");
        spawn.set("location", spawnLocation);
    }

    public void setSpawnYaw(float spawnYaw) {

        ConfigSection spawn = config.getRoot().getOrCreateSection("spawn");
        spawn.set("yaw", spawnYaw);
    }

    public void setSpawnPitch(float spawnPitch) {

        ConfigSection spawn = config.getRoot().getOrCreateSection("spawn");
        spawn.set("pitch", spawnPitch);
    }

    public void setSpawnDimension(MIdentifier spawnDimension) {

        ConfigSection spawn = config.getRoot().getOrCreateSection("spawn");
        spawn.set("dimension", spawnDimension);
    }

    public void setTeleportFirstJoin(boolean teleportFirstJoin) {

        ConfigSection spawn = config.getRoot().getOrCreateSection("spawn");
        spawn.set("teleport_first_join", teleportFirstJoin);
    }

    public void setTeleportEachJoin(boolean teleportEachJoin) {

        ConfigSection spawn = config.getRoot().getOrCreateSection("spawn");
        spawn.set("teleport_each_join", teleportEachJoin);
    }

    public void reload() {

        config.reload();
        loadConfig();

    }

    public void save() {

        config.save();

    }

    private void loadConfig() {

        config.getRoot().fill(defaults);
        config.save();

    }


}
