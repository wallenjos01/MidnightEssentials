package org.wallentines.midnightessentials.fabric.module.hologram;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.fabric.MidnightCore;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
import org.wallentines.midnightessentials.api.module.hologram.Hologram;
import org.wallentines.midnightessentials.common.module.hologram.AbstractHologramModule;
import org.wallentines.midnightessentials.common.module.hologram.AbstractHologramRegistry;
import org.wallentines.midnightessentials.fabric.util.ArmorStandUtil;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FabricHologramRegistry extends AbstractHologramRegistry {

    private final HashMap<String, HashMap<ServerPlayer, IntList>> entityIds = new HashMap<>();
    private final ResourceLocation level;

    private boolean isWithin(Vec3d vec, ChunkPos pos) {

        return vec.getX() >= pos.getMinBlockX() && vec.getX() < pos.getMaxBlockX() + 1 &&
                vec.getZ() >= pos.getMinBlockZ() && vec.getZ() < pos.getMaxBlockZ() + 1;

    }

    private void sendHologram(ServerPlayer player, String hologramId, Hologram hg, Level lvl) {

        HashMap<ServerPlayer, IntList> ids = entityIds.computeIfAbsent(hologramId, id -> new HashMap<>());
        if(ids.containsKey(player)) {
            player.connection.send(new ClientboundRemoveEntitiesPacket(ids.get(player)));
        }

        FabricPlayer fp = FabricPlayer.wrap(player);
        List<MComponent> text = hg.getMessage(fp);

        double yOffset = 0;

        IntList list = new IntArrayList();
        for(MComponent line : text) {

            Component cmp = ConversionUtil.toComponent(line);

            ArmorStand armorStand = ArmorStandUtil.createInvisibleArmorStand(lvl, hg.getLocation().getX(), hg.getLocation().getY() + yOffset, hg.getLocation().getZ());
            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName(cmp);

            yOffset -= 0.25;

            Packet<?> spawnPacket = armorStand.getAddEntityPacket();
            ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(armorStand.getId(), armorStand.getEntityData(), true);

            list.add(armorStand.getId());
            armorStand.kill();

            player.connection.send(spawnPacket);
            player.connection.send(dataPacket);
        }

        ids.put(player, list);
    }


    public FabricHologramRegistry(AbstractHologramModule module, Identifier worldId) {
        super(module, worldId);
        level = ConversionUtil.toResourceLocation(worldId);
    }

    public void playerLoadedChunk(ServerPlayer player, LevelChunk chunk) {

        for(Map.Entry<String, Hologram> hg : loaded.entrySet()) {
            if(isWithin(hg.getValue().getLocation(), chunk.getPos())) {

                sendHologram(player, hg.getKey(), hg.getValue(), chunk.getLevel());
            }
        }
    }

    @Override
    protected void onLoad(String id, Hologram hologram) {

        // Send new hologram to players
        MinecraftServer server = MidnightCore.getInstance().getServer();
        if(server == null) return;

        Level lvl = server.getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, level));
        if(lvl == null || lvl.isClientSide) return;

        int trackingRange = EntityType.ARMOR_STAND.clientTrackingRange() * 16;
        int scaledRange = server.getScaledTrackingDistance(trackingRange);

        for(Player spl : lvl.players()) {

            Vec3 holoPos = new Vec3(hologram.getLocation().getX(), hologram.getLocation().getY(), hologram.getLocation().getZ());
            Vec3 distance = spl.position().subtract(holoPos);

            double distanceSquared = distance.x * distance.x + distance.z * distance.z;
            double viewDistance = Math.min(scaledRange, (server.getPlayerList().getViewDistance() - 1) * 16);
            double viewDistanceSquared = viewDistance * viewDistance;

            if(distanceSquared <= viewDistanceSquared) {
                sendHologram((ServerPlayer) spl, id, hologram, lvl);
            }
        }
    }

    @Override
    protected void onUnload(String key, Hologram hologram) {
        // Remove hologram from players' screens
        if(entityIds.containsKey(key)) {

            for(Map.Entry<ServerPlayer, IntList> ent : entityIds.get(key).entrySet()) {
                ent.getKey().connection.send(new ClientboundRemoveEntitiesPacket(ent.getValue()));
            }
            entityIds.remove(key);
        }
    }

}
