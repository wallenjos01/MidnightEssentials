package org.wallentines.midnightessentials.api;

import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.TextColor;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UIDisplay {

    private final MComponent name;
    private final TextColor color;
    private final MItemStack item;
    private final List<MComponent> description = new ArrayList<>();

    public UIDisplay(MComponent name, TextColor color, Collection<MComponent> description) {
        this(name, color, description, null);
    }

    public UIDisplay(MComponent name, TextColor color, Collection<MComponent> description, MItemStack item) {
        this.name = name;
        this.color = color;
        this.item = item == null ? generateItem() : item;
        this.description.addAll(description);
    }

    public MComponent getName() {
        return name;
    }

    public TextColor getColor() {
        return color;
    }

    public MItemStack getItem() {
        return item;
    }

    public Collection<MComponent> getDescription() {
        return description;
    }

    private MItemStack generateItem() {

        return MItemStack.Builder.woolWithColor(color).withName(name).withLore(description).build();
    }

    public static final ConfigSerializer<UIDisplay> SERIALIZER = ConfigSerializer.create(
            MComponent.INLINE_SERIALIZER.entry( "name",  UIDisplay::getName),
            TextColor.SERIALIZER.entry( "color",  UIDisplay::getColor),
            MComponent.INLINE_SERIALIZER.listOf().entry( "description",  UIDisplay::getDescription),
            MItemStack.SERIALIZER.entry( "item",  UIDisplay::getItem).orDefault(null),
            UIDisplay::new
    );

}
