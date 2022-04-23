package com.hughbone.eldenhorses.mixin;

import com.hughbone.eldenhorses.interfaces.EldenExt;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.hughbone.eldenhorses.EldenHorses.NETHERITE_HORSE_ARMOR;

@Mixin(HorseBaseEntity.class)
public abstract class HorseBaseEntityMixin implements EldenExt{

    @Shadow protected float jumpStrength;
    @Shadow protected abstract void setHorseFlag(int bitmask, boolean flag);

    private boolean doubleJumped = false;
    private boolean eldenArmor = false;

    public boolean hasEldenArmor() {
        return eldenArmor;
    }

    public void updateEldenArmor() {
        eldenArmor = ((HorseEntity)(Object)this).getArmorType().getItem().equals(NETHERITE_HORSE_ARMOR.asItem());
    }

    @Inject(method="isAngry", at = @At("RETURN"), cancellable = true)
    public void isAngry(CallbackInfoReturnable<Boolean> cir) {
        if (hasEldenArmor()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method="writeCustomDataToNbt", at = @At("HEAD"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("eldenHorseArmor", eldenArmor);
    }

    @Inject(method="readCustomDataFromNbt", at = @At("HEAD"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        eldenArmor = nbt.getBoolean("eldenHorseArmor");
    }

    @Inject(method = "computeFallDamage", at = @At("RETURN"), cancellable = true)
    private void computeFallDamage(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> cir) {
        if (hasEldenArmor()) {
            cir.setReturnValue(cir.getReturnValue() - 4);
        }
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void test(CallbackInfo ci) {
        HorseBaseEntity hbe = (HorseBaseEntity)(Object)this;

        if (hbe.hasPlayerRider() && hasEldenArmor()) {
            if (hbe.isOnGround() && !hbe.isInAir()) doubleJumped = false;

            if (!doubleJumped && jumpStrength > 0.0F && hbe.isInAir() && !hbe.isOnGround())  {
                hbe.setVelocity(hbe.getVelocity().x, hbe.getJumpStrength(), hbe.getVelocity().z);
                float h = MathHelper.sin(hbe.getYaw() * 0.017453292F);
                float i = MathHelper.cos(hbe.getYaw() * 0.017453292F);
                hbe.setVelocity(hbe.getVelocity().add((double)(-0.4F * h * jumpStrength), 0.0, (double)(0.4F * i * jumpStrength)));

                doubleJumped = true;
            }
        }
    }

}
