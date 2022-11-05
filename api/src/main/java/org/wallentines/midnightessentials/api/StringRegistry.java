package org.wallentines.midnightessentials.api;

import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.config.serialization.InlineSerializer;

import java.util.*;

public class StringRegistry<T> implements Iterable<T> {

    private final List<T> data = new ArrayList<>();

    private final HashMap<String, Integer> indicesById = new HashMap<>();
    private final HashMap<T, String> idsByValue = new HashMap<>();

    public final InlineSerializer<T> serializer = InlineSerializer.of(this::getId, this::get);

    public StringRegistry() { }

    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }

    public void register(String id, T value) {

        int index = data.size();
        if(indicesById.containsKey(id)) {
            index = indicesById.get(id);
            data.set(index, value);
        } else {
            data.add(value);
        }

        indicesById.put(id, index);
        idsByValue.put(value, id);
    }

    public void clear() {
        data.clear();
        indicesById.clear();
        idsByValue.clear();
    }

    public T get(String id) {
        return data.get(indicesById.get(id));
    }

    public String getId(T value) {
        return idsByValue.get(value);
    }

    public Collection<String> getIds() {
        return indicesById.keySet();
    }
}
