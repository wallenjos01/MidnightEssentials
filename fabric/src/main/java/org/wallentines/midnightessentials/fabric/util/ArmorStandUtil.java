package org.wallentines.midnightessentials.fabric.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class ArmorStandUtil {

    public static ArmorStand createInvisibleArmorStand(Level lvl, double x, double y, double z) {

        ArmorStand armorStand = new ArmorStand(lvl, x, y, z);

        CompoundTag data = new CompoundTag();
        armorStand.save(data);
        data.putBoolean("Invulnerable", true);
        data.putBoolean("NoGravity", true);
        data.putBoolean("Invisible", true);
        data.putBoolean("Marker", true);

        UUID u = armorStand.getUUID();
        armorStand.load(data);
        armorStand.setUUID(u);

        return armorStand;
    }

}
