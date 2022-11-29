package org.wallentines.midnightessentials.fabric.module;

import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.fabric.event.entity.BlockEntityLoadDataEvent;
import org.wallentines.midnightcore.fabric.event.entity.BlockEntitySaveDataEvent;
import org.wallentines.midnightessentials.common.module.AbstractSignEditModule;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.wallentines.midnightcore.fabric.event.player.PlayerInteractEvent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;

import java.util.UUID;

public class FabricSignEditModule extends AbstractSignEditModule {

    public static final ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>> MODULE_INFO = new ModuleInfo<>(FabricSignEditModule::new, ID, new ConfigSection());

    @Override
    protected void registerListeners() {

        Event.register(PlayerInteractEvent.class, this, this::onInteract);
        Event.register(BlockEntitySaveDataEvent.class, this, this::onSave);
        Event.register(BlockEntityLoadDataEvent.class, this, this::onLoad);
    }

    private void onSave(BlockEntitySaveDataEvent ev) {

        BlockEntity ent = ev.getEntity();
        if(ent.getType() == BlockEntityType.SIGN) {

            SignBlockEntity sb = (SignBlockEntity) ev.getEntity();

            UUID uid = sb.getPlayerWhoMayEdit();
            if(uid != null) ev.getTag().putUUID("Editor", sb.getPlayerWhoMayEdit());

            ev.getTag().putBoolean("Editable", sb.isEditable());
        }

    }

    private void onLoad(BlockEntityLoadDataEvent ev) {
        BlockEntity ent = ev.getEntity();
        if(ent.getType() == BlockEntityType.SIGN) {

            SignBlockEntity sb = (SignBlockEntity) ev.getEntity();

            if(ev.getTag().contains("Editable")) sb.setEditable(ev.getTag().getBoolean("Editable"));
            if(ev.getTag().contains("Editor")) sb.setAllowedPlayerEditor(ev.getTag().getUUID("Editor"));
        }
    }

    private void onInteract(PlayerInteractEvent event) {

        if(event.getBlockHit() == null || !event.getPlayer().isShiftKeyDown() || !event.getPlayer().getItemInHand(event.getHand()).isEmpty()) return;

        GameType type = event.getPlayer().gameMode.getGameModeForPlayer();
        if(type == GameType.SPECTATOR || type == GameType.ADVENTURE) return;

        BlockPos pos = new BlockPos(event.getBlockHit().getBlockPos().getX(), event.getBlockHit().getBlockPos().getY(), event.getBlockHit().getBlockPos().getZ());
        BlockState b = event.getPlayer().getLevel().getBlockState(pos);
        if(!(b.getBlock() instanceof SignBlock)) return;

        SignBlockEntity ent = (SignBlockEntity) event.getPlayer().getLevel().getBlockEntity(pos);
        if(ent == null) return;

        if(type == GameType.CREATIVE || ent.isEditable() && ent.getPlayerWhoMayEdit() == null || event.getPlayer().getUUID().equals(ent.getPlayerWhoMayEdit())) {

            event.getPlayer().connection.send(new ClientboundEntityEventPacket(event.getPlayer(), (byte) 9));
            event.getPlayer().connection.send(new ClientboundBlockUpdatePacket(event.getPlayer().getLevel(), ent.getBlockPos()));
            event.getPlayer().connection.send(new ClientboundOpenSignEditorPacket(ent.getBlockPos()));

            event.setCancelled(true);
            event.setShouldSwingArm(true);
        }
    }
}
