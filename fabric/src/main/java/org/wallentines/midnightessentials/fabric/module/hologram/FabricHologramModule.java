package org.wallentines.midnightessentials.fabric.module.hologram;

import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.phys.Vec3;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.fabric.event.player.PlayerLoadChunkEvent;
import org.wallentines.midnightcore.fabric.event.server.CommandLoadEvent;
import org.wallentines.midnightcore.fabric.util.CommandUtil;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightessentials.api.MidnightEssentialsAPI;
import org.wallentines.midnightessentials.api.module.hologram.HologramRegistry;
import org.wallentines.midnightessentials.common.module.hologram.AbstractHologramModule;
import org.wallentines.midnightessentials.common.module.hologram.HologramImpl;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

public class FabricHologramModule extends AbstractHologramModule {

    @Override
    protected HologramRegistry createRegistry(Identifier world) {
        return new FabricHologramRegistry(this, world);
    }


    public static final ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>> MODULE_INFO = new ModuleInfo<>(FabricHologramModule::new, ID, new ConfigSection());

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        super.initialize(section, data);

        Event.register(PlayerLoadChunkEvent.class, this, ev -> {
            Identifier id = ConversionUtil.toIdentifier(ev.getChunk().getLevel().dimension().location());

            HologramRegistry reg = getHologramRegistry(id);
            ((FabricHologramRegistry) reg).playerLoadedChunk(ev.getPlayer(), ev.getChunk());
        });


        Event.register(CommandLoadEvent.class, this, ev -> {

            WeakReference<FabricHologramModule> mod = new WeakReference<>(FabricHologramModule.this);
            ev.getDispatcher().register(Commands.literal("hologram")
                .requires(Permissions.require("midnightessentials.commands.hologram", 2))
                .then(Commands.literal("create")
                    .then(Commands.argument("id", StringArgumentType.string())
                        .then(Commands.argument("lines", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                FabricHologramModule module = mod.get();
                                if(module == null) {
                                    CommandUtil.sendCommandFailure(ctx, MidnightEssentialsAPI.getInstance().getLangProvider(), "command.hologram.error");
                                    return 0;
                                }

                                Vec3 loc = ctx.getSource().getPosition();
                                Vec3d location = new Vec3d(loc.x, loc.y, loc.z);

                                String allLines = ctx.getArgument("lines", String.class);

                                List<String> lines = Arrays.asList(allLines.split("\\\\n"));
                                HologramImpl hg = new HologramImpl(location, lines);

                                Identifier worldId = ConversionUtil.toIdentifier(ctx.getSource().getLevel().dimension().location());
                                module.getHologramRegistry(worldId).addHologram(ctx.getArgument("id", String.class), hg);

                                CommandUtil.sendCommandSuccess(ctx, MidnightEssentialsAPI.getInstance().getLangProvider(), false,"command.hologram.create");

                                return lines.size();
                            })
                        )
                    )
                )
                .then(Commands.literal("remove")
                    .then(Commands.argument("id", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            FabricHologramModule module = mod.get();
                            if(module == null) return builder.buildFuture();
                            HologramRegistry reg = getHologramRegistry(ConversionUtil.toIdentifier(ctx.getSource().getLevel().dimension().location()));
                            return SharedSuggestionProvider.suggest(reg.getHologramIds(), builder);
                        })
                        .executes(ctx -> {
                            FabricHologramModule module = mod.get();
                            if(module == null) {
                                CommandUtil.sendCommandFailure(ctx, MidnightEssentialsAPI.getInstance().getLangProvider(), "command.hologram.error");
                                return 0;
                            }
                            HologramRegistry reg = getHologramRegistry(ConversionUtil.toIdentifier(ctx.getSource().getLevel().dimension().location()));
                            String id = ctx.getArgument("id", String.class);
                            reg.unloadHologram(id);

                            CommandUtil.sendCommandSuccess(ctx, MidnightEssentialsAPI.getInstance().getLangProvider(), false,"command.hologram.delete");

                            return 1;
                        })
                    )
                )
            );
        });

        return true;
    }
}
