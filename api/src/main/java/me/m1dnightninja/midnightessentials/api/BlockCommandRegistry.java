package me.m1dnightninja.midnightessentials.api;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.ConfigSerializer;
import me.m1dnightninja.midnightcore.api.math.Vec3i;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.player.Requirement;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MActionBar;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MTitle;

import java.util.*;

public class BlockCommandRegistry {

    private final HashMap<Vec3i, BlockData> commands = new HashMap<>();
    private MIdentifier activeWorld = MIdentifier.create("minecraft", "overworld");

    public void addCommand(Vec3i block, BlockCommand cmd) {

        commands.compute(block, (k,v) -> {
            BlockData data = v;
            if(v == null) {
                data = new BlockData();
            }
            data.commands.add(cmd);
            return data;
        });

    }

    public boolean executeCommands(MPlayer player, InteractionType type, Vec3i block) {

        BlockData data = commands.get(block);
        if(data == null) {
            return false;
        }

        for(BlockCommand cmd : data.commands) {
            cmd.execute(player, type);
        }

        return !data.passthrough;
    }

    public void loadFromConfig(ConfigSection sec) {

        commands.clear();
        for(String s : sec.getKeys()) {

            Vec3i vector = Vec3i.parse(s);
            if(vector == null) return;

            commands.put(vector, BlockData.SERIALIZER.deserialize(sec.getSection(s)));
        }
    }

    public void clear() {
        commands.clear();
    }


    public MIdentifier getActiveWorld() {
        return activeWorld;
    }

    public void setActiveWorld(MIdentifier activeWorld) {
        this.activeWorld = activeWorld;
    }

    public ConfigSection save() {

        ConfigSection out = new ConfigSection();
        for(Map.Entry<Vec3i, BlockData> ent : commands.entrySet()) {

            out.set(ent.getKey().toString(), BlockData.SERIALIZER.serialize(ent.getValue()));
        }

        return out;
    }

    private static class BlockData {

        boolean passthrough = false;
        final List<BlockCommand> commands = new ArrayList<>();

        static final ConfigSerializer<BlockData> SERIALIZER = new ConfigSerializer<>() {
            @Override
            public BlockData deserialize(ConfigSection section) {

                BlockData out = new BlockData();
                if(section.has("passthrough", Boolean.class)) {
                    out.passthrough = section.getBoolean("passthrough");
                }

                out.commands.addAll(section.getListFiltered("commands", BlockCommand.class));

                return out;
            }

            @Override
            public ConfigSection serialize(BlockData object) {

                ConfigSection out = new ConfigSection();
                out.set("passthough", object.passthrough);
                out.set("commands", object.commands);

                return out;
            }
        };

    }


    public enum InteractionType {

        LEFT_CLICK,
        RIGHT_CLICK,
        SHIFT_LEFT_CLICK,
        SHIFT_RIGHT_CLICK
    }

    public enum CommandType {

        PLAYER_COMMAND,
        CONSOLE_COMMAND,
        MESSAGE,
        ACTION_BAR,
        TITLE,
        SUBTITLE
    }

    public static class BlockCommand {

        private final CommandType commandType;
        private final String command;
        private final List<InteractionType> actuation = new ArrayList<>();
        private final List<Requirement> requirements = new ArrayList<>();

        public BlockCommand(CommandType type, String command) {
            this.commandType = type;
            this.command = command;
        }

        public void addActuationMethod(InteractionType type) {
            actuation.add(type);
        }

        public void addRequirement(Requirement req) {
            requirements.add(req);
        }

        public void execute(MPlayer clicker, InteractionType type) {

            if (!actuation.contains(type)) return;

            for (Requirement req : requirements) {
                if (!req.check(clicker)) return;
            }

            ILangModule mod = MidnightCoreAPI.getInstance().getModule(ILangModule.class);

            switch (commandType) {
                case PLAYER_COMMAND -> clicker.executeCommand(mod.applyPlaceholdersFlattened(command, clicker));
                case CONSOLE_COMMAND -> MidnightCoreAPI.getInstance().executeConsoleCommand(mod.applyPlaceholdersFlattened(command, clicker));
                case MESSAGE -> clicker.sendMessage(mod.applyPlaceholders(MComponent.Serializer.parse(command), clicker));
                case ACTION_BAR -> clicker.sendActionBar(new MActionBar(mod.applyPlaceholders(MComponent.Serializer.parse(command), clicker), new MActionBar.ActionBarOptions()));
                case TITLE -> clicker.sendTitle(new MTitle(mod.applyPlaceholders(MComponent.Serializer.parse(command), clicker), MTitle.TITLE));
                case SUBTITLE -> clicker.sendTitle(new MTitle(mod.applyPlaceholders(MComponent.Serializer.parse(command), clicker), MTitle.SUBTITLE));
            }
        }

        public static final ConfigSerializer<BlockCommand> SERIALIZER = new ConfigSerializer<>() {
            @Override
            public BlockCommand deserialize(ConfigSection section) {

                BlockCommand cmd = new BlockCommand(CommandType.valueOf(section.getString("type").toUpperCase(Locale.ROOT)), section.getString("value"));

                if(section.has("actions")) {
                    for (String s : section.getListFiltered("actions", String.class)) {
                        cmd.addActuationMethod(InteractionType.valueOf(s.toUpperCase(Locale.ROOT)));
                    }
                } else {
                    cmd.addActuationMethod(InteractionType.RIGHT_CLICK);
                }

                if(section.has("requirements")) {
                    for (Requirement req : section.getListFiltered("requirements", Requirement.class)) {
                        cmd.addRequirement(req);
                    }
                }

                return cmd;
            }

            @Override
            public ConfigSection serialize(BlockCommand object) {

                ConfigSection out = new ConfigSection();
                out.set("type", object.commandType.name());
                out.set("value", object.command);
                out.set("actions", object.actuation);
                out.set("requirements", object.requirements);

                return out;
            }
        };

    }


}
