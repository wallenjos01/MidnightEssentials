package org.wallentines.midnightessentials.api.module.blockcommand;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.math.Vec3i;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.requirement.Requirement;

import java.util.*;

public class BlockCommandRegistry {

    private final HashMap<Vec3i, BlockData> commands = new HashMap<>();
    private Identifier activeWorld = new Identifier("minecraft", "overworld");

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

    public void removeCommand(Vec3i block, int i) {

        BlockData dt = commands.get(block);
        if(dt == null) return;

        dt.commands.remove(i);
    }

    public Collection<BlockCommand> getCommands(Vec3i block) {

        BlockData dt = commands.get(block);
        if(dt == null) return new ArrayList<>();

        return dt.commands;
    }


    public boolean executeCommands(MPlayer player, InteractionType type, Vec3i block) {

        BlockData data = commands.get(block);
        if(data == null || data.commands.isEmpty()) {
            return false;
        }

        int count = 0;
        for(BlockCommand cmd : data.commands) {

            if(cmd.execute(player, type)) count++;
        }

        return count > 0 && !data.passthrough;
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

    public void clear(Vec3i block) {

        BlockData dt = commands.get(block);
        if(dt == null) return;

        dt.commands.clear();
    }

    public Identifier getActiveWorld() {
        return activeWorld;
    }

    public void setActiveWorld(Identifier activeWorld) {
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
        private final List<Requirement<MPlayer>> requirements = new ArrayList<>();

        public BlockCommand(CommandType type, String command) {
            this.commandType = type;
            this.command = command;
        }

        public void addActuationMethod(InteractionType type) {
            actuation.add(type);
        }

        public void addRequirement(Requirement<MPlayer> req) {
            requirements.add(req);
        }

        public String getCommand() {
            return command;
        }

        public boolean execute(MPlayer clicker, InteractionType type) {

            if (!actuation.contains(type)) return false;

            for (Requirement<MPlayer> req : requirements) {
                if (!req.check(clicker)) return false;
            }

            switch (commandType) {
                case PLAYER_COMMAND -> clicker.executeCommand(PlaceholderManager.INSTANCE.parseText(command, clicker).getAllContent());
                case CONSOLE_COMMAND -> MidnightCoreAPI.getInstance().executeConsoleCommand(PlaceholderManager.INSTANCE.parseText(command, clicker).getAllContent());
                case MESSAGE -> clicker.sendMessage(PlaceholderManager.INSTANCE.applyPlaceholders(MComponent.parse(command), clicker));
                case ACTION_BAR -> clicker.sendActionBar(PlaceholderManager.INSTANCE.applyPlaceholders(MComponent.parse(command), clicker));
                case TITLE -> clicker.sendTitle(PlaceholderManager.INSTANCE.applyPlaceholders(MComponent.parse(command), clicker), 20, 80, 20);
                case SUBTITLE -> clicker.sendSubtitle(PlaceholderManager.INSTANCE.applyPlaceholders(MComponent.parse(command), clicker), 20, 80, 20);
            }

            return true;
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
                    for (Requirement<MPlayer> req : section.getListFiltered("requirements", Requirement.class)) {
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
