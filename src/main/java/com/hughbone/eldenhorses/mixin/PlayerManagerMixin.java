package com.hughbone.eldenhorses.mixin;

import com.hughbone.eldenhorses.interfaces.ServerPlayerExt;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method="respawnPlayer", at=@At("RETURN"))
    public void respawnPlayer(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        try {
            HorseEntity horse = ((ServerPlayerExt) player).getHorse();
            if (horse != null) {
                ((ServerPlayerExt) cir.getReturnValue()).storeHorse(horse); // Transfer horse data to respawned player
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
