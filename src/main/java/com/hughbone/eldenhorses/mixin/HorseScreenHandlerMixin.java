package com.hughbone.eldenhorses.mixin;

import com.hughbone.eldenhorses.EldenExt;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.HorseScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.hughbone.eldenhorses.EldenHorses.NETHERITE_HORSE_ARMOR;

@Mixin(HorseScreenHandler.class)
public abstract class HorseScreenHandlerMixin {

    @Inject(method = "close", at = @At("HEAD"))
    public void transferSlot(PlayerEntity player, CallbackInfo ci) {

        if (player.getVehicle() instanceof HorseEntity horse) {
            if (horse.getArmorType().getItem().equals(NETHERITE_HORSE_ARMOR.asItem())) {
                ((EldenExt) horse).setEldenArmor(true);
            } else {
                ((EldenExt) horse).setEldenArmor(false);
            }
        }

    }

}
