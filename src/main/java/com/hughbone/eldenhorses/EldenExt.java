package com.hughbone.eldenhorses;

import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface EldenExt {
    boolean hasEldenArmor();
    void setEldenArmor(boolean ea);
}
