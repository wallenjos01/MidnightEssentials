package me.m1dnightninja.midnightessentials.common.module;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.FileConfig;
import me.m1dnightninja.midnightcore.api.math.Vec3i;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightessentials.api.BlockCommandRegistry;
import me.m1dnightninja.midnightessentials.api.IBlockCommandModule;
import me.m1dnightninja.midnightessentials.api.MidnightEssentialsAPI;

import java.io.File;
import java.util.HashMap;

public abstract class AbstractBlockCommandModule implements IBlockCommandModule {

    public static final MIdentifier ID = MIdentifier.create("midnightessentials", "block_commands");

    protected HashMap<String, BlockCommandRegistry> registries = new HashMap<>();

    private String fileName;
    private FileConfig config;

    @Override
    public boolean initialize(ConfigSection configuration) {

        if(configuration.getBoolean("register_command")) {
            registerCommands();
        }

        fileName = configuration.getString("file_name");

        MidnightCoreAPI.getInstance().getConfigRegistry().registerSerializer(BlockCommandRegistry.BlockCommand.class, BlockCommandRegistry.BlockCommand.SERIALIZER);

        registerListeners();
        return true;
    }

    @Override
    public MIdentifier getId() {
        return ID;
    }

    @Override
    public ConfigSection getDefaultConfig() {

        ConfigSection sec = new ConfigSection();
        sec.set("register_command", true);
        sec.set("file_name", "block_commands" + MidnightCoreAPI.getInstance().getDefaultConfigProvider().getFileExtension());

        return sec;
    }

    @Override
    public BlockCommandRegistry createRegistry(String id) {
        BlockCommandRegistry out = new BlockCommandRegistry();
        registries.put(id, out);

        return out;
    }

    @Override
    public void reloadDefaultRegistry() {

        if(config == null) {
            config = new FileConfig(new File(MidnightEssentialsAPI.getInstance().getDataFolder(), fileName));
        } else {
            config.reload();
        }

        for(String s : config.getRoot().getKeys()) {

            BlockCommandRegistry reg = createRegistry(s);
            reg.setActiveWorld(MIdentifier.parseOrDefault(s));
            reg.loadFromConfig(config.getRoot().getSection(s));
        }
    }

    @Override
    public boolean execute(Vec3i loc, BlockCommandRegistry.InteractionType type, MPlayer player) {
        for(BlockCommandRegistry reg : registries.values()) {

            if(reg.getActiveWorld().equals(player.getDimension())) return reg.executeCommands(player, type, loc);
        }
        return false;
    }

    @Override
    public void unloadRegistry(String id) {
        registries.remove(id);
    }

    protected abstract void registerCommands();
    protected abstract void registerListeners();

}
