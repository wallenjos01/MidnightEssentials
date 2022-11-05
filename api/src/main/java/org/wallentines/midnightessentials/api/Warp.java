package org.wallentines.midnightessentials.api;

import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;

public class Warp {

    private final UIDisplay display;
    private final Location location;

    public Warp(UIDisplay display, Location location) {
        this.display = display;
        this.location = location;
    }

    public UIDisplay getDisplay() {
        return display;
    }

    public Location getLocation() {
        return location;
    }

    public static final ConfigSerializer<Warp> SERIALIZER = ConfigSerializer.create(
            UIDisplay.SERIALIZER.entry("id", Warp::getDisplay),
            Location.SERIALIZER.entry("location", Warp::getLocation),
            Warp::new
    );

}
