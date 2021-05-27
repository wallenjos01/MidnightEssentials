package me.m1dnightninja.midnightessentials.fabric.mixin;

import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SignBlockEntity.class)
public interface AccessorSignEntity {

    @Accessor
    void setIsEditable(boolean editable);
}
