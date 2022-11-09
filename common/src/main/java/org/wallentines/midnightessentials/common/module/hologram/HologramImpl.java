package org.wallentines.midnightessentials.common.module.hologram;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
import org.wallentines.midnightessentials.api.module.hologram.Hologram;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.config.serialization.PrimitiveSerializers;
import org.wallentines.midnightlib.math.Vec3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HologramImpl implements Hologram {

    private final List<String> lines = new ArrayList<>();
    private final Vec3d location;

    public HologramImpl(Vec3d location, Collection<String> lines) {
        this.location = location;
        this.lines.addAll(lines);
    }

    @Override
    public List<String> getLines() {
        return lines;
    }

    @Override
    public List<MComponent> getMessage(MPlayer player) {

        LangProvider provider = MidnightEssentialsAPI.getInstance().getLangProvider();

        List<MComponent> out = new ArrayList<>();
        for(String line : lines) {
            out.add(PlaceholderManager.INSTANCE.parseText(line, player, this, provider));
        }

        return out;
    }

    @Override
    public Vec3d getLocation() {
        return location;
    }

    public static final ConfigSerializer<Hologram> SERIALIZER = ConfigSerializer.create(
        Vec3d.SERIALIZER.entry("location", Hologram::getLocation),
        PrimitiveSerializers.STRING.listOf().entry("lines", Hologram::getLines),
        HologramImpl::new
    );

}
