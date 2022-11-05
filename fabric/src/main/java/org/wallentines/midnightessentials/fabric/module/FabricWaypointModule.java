package org.wallentines.midnightessentials.fabric.module;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.lang.CustomPlaceholder;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MStyle;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.common.util.MojangUtil;
import org.wallentines.midnightcore.fabric.event.entity.EntityLoadedEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerInteractEntityEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerLeaveEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerTickEvent;
import org.wallentines.midnightcore.fabric.event.world.BlockBreakEvent;
import org.wallentines.midnightcore.fabric.event.world.BlockPlacedEvent;
import org.wallentines.midnightcore.fabric.event.world.ExplosionEvent;
import org.wallentines.midnightcore.fabric.item.FabricItem;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.config.serialization.PrimitiveSerializers;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.math.Vec3i;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.lang.ref.WeakReference;
import java.util.*;


public class FabricWaypointModule implements Module<MidnightCoreAPI> {

    private static final String REGISTERED_KEY = "registered_waypoints";
    private final HashMap<UUID, WaypointData> allWaypoints = new HashMap<>();
    private final HashMap<WeakReference<Entity>, UUID> waypointEntities = new HashMap<>();

    private Block baseBlock;
    private Skin coreSkin;
    private Skin headSkin;
    private MComponent coreName;
    private FileConfig config;

    private String defaultName;


    @Override
    public void disable() {

        Event.unregisterAll(this);
    }

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        baseBlock = Registry.BLOCK.get(ResourceLocation.tryParse(section.getString("base_block")));
        coreSkin = section.get("core_skin", Skin.class);
        coreName = section.get("core_name", MComponent.class);
        headSkin = section.get("head_skin", Skin.class);
        config = FileConfig.findOrCreate(section.getString("waypoint_file"), MidnightEssentialsAPI.getInstance().getDataFolder());
        defaultName = section.getString("default_waypoint_name");

        Event.register(EntityLoadedEvent.class, this, this::onLoad);
        Event.register(ServerTickEvent.class, this, this::onTick);
        Event.register(BlockPlacedEvent.class, this, this::onPlace);
        Event.register(BlockBreakEvent.class, this, this::onBreak);
        Event.register(PlayerInteractEntityEvent.class, this, this::onInteract);
        Event.register(PlayerLeaveEvent.class, this, this::onLeave);

        Event.register(ExplosionEvent.class, this, event -> event.getAffectedBlocks().removeIf(pos -> isCoreBlock(event.getLevel().getBlockState(pos), pos, event.getLevel())));

        // Load Waypoint data
        for(String key : config.getRoot().getKeys()) {
            UUID u;
            try {
                u = UUID.fromString(key);
            } catch (IllegalArgumentException ex) {
                MidnightEssentialsAPI.getLogger().warn("Cannot convert " + key + " to a UUID!");
                continue;
            }

            WaypointData wd = WaypointData.SERIALIZER.deserialize(config.getRoot().getSection(key));
            allWaypoints.put(u, wd);
        }

        return true;
    }


    // Cache entities on load
    private void onLoad(EntityLoadedEvent event) {
        if(event.getEntity().getType() != EntityType.ARMOR_STAND) return;

        for(Map.Entry<UUID, WaypointData> ent : allWaypoints.entrySet()) {
            if(event.getEntity().getUUID().equals(ent.getValue().entityId)) {

                // Cleanup all weak references
                for(WeakReference<Entity> ref : new ArrayList<>(waypointEntities.keySet())) {
                    if(ref.get() == null) waypointEntities.remove(ref);
                }

                event.getEntity().teleportTo(ent.getValue().location.getX() + 0.5d, ent.getValue().location.getY() - 0.5d, ent.getValue().location.getZ() + 0.5d);

                waypointEntities.put(new WeakReference<>(event.getEntity()), ent.getKey());
                break;
            }
        }
    }

    // Deal with entities every tick
    private void onTick(ServerTickEvent event) {

        for(Map.Entry<WeakReference<Entity>, UUID> entry : new HashSet<>(waypointEntities.entrySet())) {

            WeakReference<Entity> ref = entry.getKey();
            Entity ent = ref.get();
            WaypointData dt = allWaypoints.get(entry.getValue());

            if(ent == null || dt == null) {
                waypointEntities.remove(entry.getKey());
                continue;
            }

            BlockPos pos = new BlockPos(dt.location.getX(), dt.location.getY(), dt.location.getZ());

            if(ent.level.getBlockState(pos).getBlock() != Blocks.LODESTONE) {

                ent.kill();

                ent.level.playSound(null, ent.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);

                FabricItem it = (FabricItem) MItemStack.Builder.headWithSkin(coreSkin).withName(coreName).build();
                Block.popResource(ent.getLevel(), pos.above(), it.getInternal());

                allWaypoints.remove(entry.getValue());

                config.getRoot().set(entry.getValue().toString(), null);
                config.save();

                continue;
            }

            // Animate Head
            ent.setYRot(ent.getYRot() + 4.0f);

            float t = (float) (event.getTickCount() % 120) / 60.0f;
            ent.teleportTo(ent.getX(), dt.location.getY() + 0.5f + Math.sin(t * 2 * Math.PI) / 6.0f, ent.getZ());

        }
    }


    // Handle Placing Waypoint Cores
    private void onPlace(BlockPlacedEvent event) {

        if(event.getPlacedState() == null || event.getPlacedState().getBlock() != Blocks.PLAYER_HEAD) return;

        ServerLevel level = event.getPlayer().getLevel();

        BlockState state = level.getBlockState(event.getPos().below());
        if(state.getBlock() != baseBlock) return;

        ItemStack is = event.getItemStack();

        String texture = getHeadTexture(is);

        if(!coreSkin.getValue().equals(texture)) {
            return;
        }

        event.setPlacedState(Blocks.AIR.defaultBlockState());

        MItemStack mis = MItemStack.Builder.headWithSkin(headSkin).build();

        ArmorStand armorStand = new ArmorStand(level, event.getPos().getX() + 0.5d, event.getPos().getY() - 0.5d, event.getPos().getZ() + 0.5d);

        // Setup Armor Stand NBT
        CompoundTag data = new CompoundTag();
        armorStand.save(data);
        data.putInt("DisabledSlots", 0b11111);

        UUID u = armorStand.getUUID();
        armorStand.load(data);

        armorStand.setUUID(u);
        armorStand.setInvulnerable(true);
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.setHeadPose(new Rotations(180.0f, 0.0f, 0.0f));
        armorStand.setItemSlot(EquipmentSlot.HEAD, ((FabricItem) mis).getInternal());

        level.addFreshEntity(armorStand);

        // Determine block to use for GUI
        Block b = level.getBlockState(event.getPos().below().below()).getBlock();
        if(b == null || b == Blocks.AIR) {
            Vec3 loc = findTeleportPosition(event.getPos().below(), FabricPlayer.wrap(event.getPlayer()));
            if (loc != null) {
                b = level.getBlockState(new BlockPos(loc).below()).getBlock();
            }
        }
        if(b == null) {
            b = Blocks.GRASS_BLOCK;
        }

        Vec3i location = new Vec3i(event.getPos().getX(), event.getPos().getY() - 1, event.getPos().getZ());

        WaypointData dt = new WaypointData(
                event.getPlayer().getUUID(),
                location,
                new MTextComponent(defaultName),
                armorStand.getUUID(),
                new FabricItem(new ItemStack(b.asItem())),
                false
        );

        UUID uid = UUID.randomUUID();
        while(allWaypoints.containsKey(uid)) {
            uid = UUID.randomUUID();
        }

        allWaypoints.put(uid, dt);
        waypointEntities.put(new WeakReference<>(armorStand), uid);

        level.playSound(null, event.getPos(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);

        FabricPlayer fpl = FabricPlayer.wrap(event.getPlayer());
        if(registerWaypoint(fpl, uid)) {
            MComponent comp = MidnightEssentialsAPI.getInstance().getLangProvider().getMessage("waypoint.registered.new", fpl, CustomPlaceholder.create("name", dt.name));
            fpl.sendMessage(comp);
        }

        config.getRoot().set(uid.toString(), WaypointData.SERIALIZER.serialize(dt));
        config.save();
    }

    // Handle Breaking Core Blocks
    private void onBreak(BlockBreakEvent event) {

        if(event.getPlayer().getAbilities().instabuild) return;
        if(!isCoreBlock(event.getState(), event.getPosition(), event.getLevel())) return;

        FabricItem it = (FabricItem) MItemStack.Builder.headWithSkin(coreSkin).withName(coreName).build();

        event.setCancelled(true);
        event.getLevel().setBlock(event.getPosition(), Blocks.AIR.defaultBlockState(), 11);

        Block.popResource(event.getLevel(), event.getPosition(), it.getInternal());
    }


    // Handle clicking on waypoints
    private void onInteract(PlayerInteractEntityEvent event) {

        if(event.getClicked().getType() != EntityType.ARMOR_STAND) return;

        UUID waypointId = null;
        WaypointData waypointData = null;
        for(Map.Entry<WeakReference<Entity>, UUID> ent : waypointEntities.entrySet()) {
            if(ent.getKey().get() == event.getClicked()) {
                waypointId = ent.getValue();
                waypointData = allWaypoints.get(waypointId);
                break;
            }
        }

        if(waypointData == null) return;

        event.setCancelled(true);

        FabricPlayer fpl = FabricPlayer.wrap(event.getPlayer());
        if(waypointData.isLocked(fpl)) {

            MComponent comp = MidnightEssentialsAPI.getInstance().getLangProvider().getMessage("waypoint.locked", fpl);
            fpl.sendActionBar(comp);
            return;
        }

        if(event.getPlayer().getUUID().equals(waypointData.owner)) {

            ItemStack is = event.getPlayer().getItemInHand(event.getHand());
            if(is.getItem() == Items.NAME_TAG && is.hasCustomHoverName()) {
                waypointData.name = ConversionUtil.toMComponent(is.getHoverName());

                config.getRoot().set(waypointId.toString(), WaypointData.SERIALIZER.serialize(waypointData));
                config.save();

                if(!event.getPlayer().getAbilities().instabuild) {
                    is.shrink(1);
                }
                return;
            }
        }

        if(registerWaypoint(fpl, waypointId)) {
            MComponent comp = MidnightEssentialsAPI.getInstance().getLangProvider().getMessage("waypoint.registered", fpl, CustomPlaceholder.create("name", waypointData.name));
            fpl.sendMessage(comp);
        }

        InventoryGUI gui = MidnightCoreAPI.getInstance().createGUI(waypointData.name.withStyle(new MStyle()));

        int i = 0;
        for(WaypointData dt : getRegisteredWaypoints(fpl)) {

            MItemStack.Builder bld = MItemStack.Builder.of(dt.display.getType()).withName(dt.name);
            if(dt == waypointData) {
                MComponent comp = MidnightEssentialsAPI.getInstance().getLangProvider().getMessage("waypoint.current", fpl);
                bld.withLore(List.of(comp));
            }
            MItemStack is = bld.build();

            gui.setItem(i, is, (clickType, player) -> {
                teleportToWaypoint(player, dt);
                gui.close(player);
            });
            i++;
        }

        gui.open(fpl, 0);
    }

    // Update player data on leave
    private void onLeave(PlayerLeaveEvent event) {

        FabricPlayer fpl = FabricPlayer.wrap(event.getPlayer());
        ConfigSection pdt = MidnightEssentialsAPI.getInstance().getDataProvider().getData(fpl);
        if(!pdt.has(REGISTERED_KEY, List.class)) return;

        List<UUID> out = new ArrayList<>();
        for(UUID u : pdt.getList(REGISTERED_KEY, UUID.class)) {
            if(allWaypoints.containsKey(u)) {
                out.add(u);
            }
        }
        pdt.set(REGISTERED_KEY, out);
        MidnightEssentialsAPI.getInstance().getDataProvider().saveData(fpl);
    }


    // Utility Methods
    private boolean isCoreBlock(BlockState state, BlockPos pos, Level level) {

        if (state.getBlock() != Blocks.PLAYER_HEAD && state.getBlock() != Blocks.PLAYER_WALL_HEAD) return false;

        SkullBlockEntity be = (SkullBlockEntity) level.getBlockEntity(pos);
        if(be == null) return false;

        GameProfile prof = be.getOwnerProfile();
        if(prof == null) return false;

        Skin sk = MojangUtil.getSkinFromProfile(prof);
        return sk.getValue().equals(coreSkin.getValue());
    }

    private String getHeadTexture(ItemStack is) {

        CompoundTag nbt = is.getTag();
        if(nbt == null || !nbt.contains("SkullOwner", 10)) {
            return null;
        }

        CompoundTag skullOwner = nbt.getCompound("SkullOwner");
        if(!skullOwner.contains("Properties", 10)) {
            return null;
        }

        CompoundTag properties = skullOwner.getCompound("Properties");
        if(!properties.contains("textures",9)) {
            return null;
        }

        ListTag textures = properties.getList("textures", 10);
        if(textures.size() == 0) {
            return null;
        }

        return ((CompoundTag) textures.get(0)).getString("Value");
    }


    private void teleportToWaypoint(MPlayer mpl, WaypointData data) {

        try {

            if (data.isLocked(mpl)) {
                MComponent comp = MidnightEssentialsAPI.getInstance().getLangProvider().getMessage("waypoint.locked", mpl);
                mpl.sendActionBar(comp);
                return;
            }

            BlockPos outPos = new BlockPos(data.location.getX(), data.location.getY(), data.location.getZ());
            Vec3 teleport = findTeleportPosition(outPos, mpl);
            if (teleport == null) {
                MComponent comp = MidnightEssentialsAPI.getInstance().getLangProvider().getMessage("waypoint.teleport.obstructed", mpl);
                mpl.sendMessage(comp);
                return;
            }

            ServerPlayer spl = ((FabricPlayer) mpl).getInternal();
            ServerLevel lvl = spl.getLevel();

            BlockPos prevPos = spl.blockPosition();

            // Spawn particle effects
            lvl.gameEvent(GameEvent.TELEPORT, spl.position(), GameEvent.Context.of(spl));

            mpl.teleport(new Location(mpl.getLocation().getWorldId(), new Vec3d(teleport.x, teleport.y, teleport.z), mpl.getLocation().getYaw(), mpl.getLocation().getPitch()));

            // Play sounds at source and destination
            lvl.playSound(null, prevPos, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
            lvl.playSound(null, outPos, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);

        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private Vec3 findTeleportPosition(BlockPos pos, MPlayer player) {

        ServerPlayer spl = ((FabricPlayer) player).getInternal();
        ServerLevel lvl = spl.getLevel();

        BlockPos[] toTest = new BlockPos[] {
                pos.north(), pos.south(), pos.east(), pos.west(),
                pos.north().east(), pos.north().west(),
                pos.south().east(), pos.south().west(),
                pos.above()
        };

        for(BlockPos bp : toTest) {
            Vec3 vec = DismountHelper.findSafeDismountLocation(EntityType.PLAYER, lvl, bp, false);
            if(vec == null) continue;

            return vec;
        }

        return null;
    }



    private List<WaypointData> getRegisteredWaypoints(MPlayer mpl) {

        ConfigSection data = MidnightEssentialsAPI.getInstance().getDataProvider().getData(mpl);
        if(!data.has(REGISTERED_KEY, List.class)) return new ArrayList<>();

        List<UUID> availableWaypoints = data.getList(REGISTERED_KEY, UUID.class);

        List<WaypointData> dt = new ArrayList<>();

        for(UUID u : availableWaypoints) {
            if(!allWaypoints.containsKey(u)) continue;
            dt.add(allWaypoints.get(u));
        }

        return dt;
    }

    private boolean registerWaypoint(MPlayer player, UUID waypoint) {

        ConfigSection data = MidnightEssentialsAPI.getInstance().getDataProvider().getData(player);
        List<UUID> registered = data.has(REGISTERED_KEY, List.class) ? data.getList(REGISTERED_KEY, UUID.class) : new ArrayList<>();

        if(registered.contains(waypoint)) return false;
        registered.add(waypoint);

        data.set(REGISTERED_KEY, registered);
        MidnightEssentialsAPI.getInstance().getDataProvider().saveData(player);

        return true;
    }


    private static class WaypointData {
        private final UUID owner;
        private final UUID entityId;
        private final Vec3i location;
        MComponent name;
        MItemStack display;
        boolean locked;

        public WaypointData(UUID owner, Vec3i location, MComponent name, UUID entityId, MItemStack display, boolean locked) {
            this.owner = owner;
            this.name = name;
            this.display = display;
            this.entityId = entityId;
            this.locked = locked;
            this.location = location;
        }

        public boolean isLocked(MPlayer player) {
            return locked && !player.getUUID().equals(owner);
        }

        public static final ConfigSerializer<WaypointData> SERIALIZER = ConfigSerializer.create(
            PrimitiveSerializers.UUID.<WaypointData>entry("owner", wd -> wd.owner).orDefault(null),
            Vec3i.SERIALIZER.entry("location", wd -> wd.location),
            MComponent.INLINE_SERIALIZER.entry("name", wd -> wd.name),
            PrimitiveSerializers.UUID.<WaypointData>entry("entity_id", wd -> wd.entityId).orDefault(null),
            MItemStack.SERIALIZER.<WaypointData>entry("item", wd -> wd.display).orDefault(new FabricItem(new ItemStack(Items.GRASS_BLOCK))),
            PrimitiveSerializers.BOOLEAN.<WaypointData>entry("locked", wd -> wd.locked).orDefault(false),
            WaypointData::new
        );
    }

    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "waypoint");
    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(FabricWaypointModule::new, ID, new ConfigSection()
            .with("enabled", false)
            .with("waypoint_file", "waypoints")
            .with("base_block", "minecraft:lodestone")
            .with("core_skin", new Skin(
                    UUID.fromString("bc4ea7fc-63c3-415a-b5b9-204e5acadd5c"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTRkODQ0ZmVlMjRkNWYyN2RkYjY2OTQzODUyOGQ4M2I2ODRkOTAxYjc1YTY4ODlmZTc0ODhkZmM0Y2Y3YTFjIn19fQ==", ""))
            .with("core_name", new MTextComponent("Waypoint Core"))
            .with("head_skin", new Skin(
                    UUID.fromString("fdfad7cf-4977-4676-b1d0-6a6cea993f39"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRiNjYyZDNiNTI5YTE4NzI2MWNhYjg2YzZlNTY0MjNiZjg3NmFhMjQ5ZDAzMGZhZWFmMzQzNjJmMzQ0NzI3NyJ9fX0=", ""))
            .with("default_waypoint_name", "Waypoint")
    );

}
