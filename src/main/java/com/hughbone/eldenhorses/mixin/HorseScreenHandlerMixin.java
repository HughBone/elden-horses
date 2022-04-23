package com.hughbone.eldenhorses.mixin;

import com.hughbone.eldenhorses.interfaces.EldenExt;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.HorseScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseScreenHandler.class)
public abstract class HorseScreenHandlerMixin {
    @Shadow @Final private HorseBaseEntity entity;

    @Inject(method = "close", at = @At("HEAD"))
    public void transferSlot(PlayerEntity player, CallbackInfo ci) {
        if (entity instanceof HorseEntity horse) {
            ((EldenExt) horse).updateEldenArmor();
        }
    }

}