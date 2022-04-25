package com.hughbone.eldenhorses.mixin;

import com.hughbone.eldenhorses.interfaces.EldenExt;
import com.hughbone.eldenhorses.interfaces.EntityExt;
import com.hughbone.eldenhorses.interfaces.ServerPlayerExt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin implements ServerPlayerExt {

    @Shadow public abstract void sendMessage(Text message, boolean actionBar);
    @Shadow public abstract ServerWorld getWorld();
    private HorseEntity eldenHorse = null;

    public HorseEntity getHorse() { return eldenHorse; }

    public void summonHorse(boolean mountPlayer) {
        HorseEntity eldenHorse2 = eldenHorse; // Prevent concurrent modification crash
        if (eldenHorse2 == null) {
            this.sendMessage(Text.of("No Horse Found!"), true);
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        eldenHorse2.world = player.world; // Update dimension
        eldenHorse2.fallDistance = player.fallDistance;

        // Spawn elden horse
        eldenHorse2.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        if (mountPlayer) player.startRiding(eldenHorse2, true);
        eldenHorse2.setVelocity(player.getVelocity());
        player.getWorld().tryLoadEntity(eldenHorse2);
        // Add glowing effect to old horse if replaced
        if (!mountPlayer) eldenHorse2.addStatusEffect(
                new StatusEffectInstance(StatusEffects.GLOWING,60,0,false,false));
    }

    public void storeHorse(HorseEntity horse) {
        if (eldenHorse != null) {
            if (eldenHorse.getUuid().equals(horse.getUuid())) return;

            this.sendMessage(Text.of("[Elden Horses]: Replaced Old Horse."), false);
            summonHorse(false); // Summon old horse if new one found
        }
        if (horse.getRemovalReason() != null) {
            eldenHorse = null;
            return;
        }
        eldenHorse = horse;
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound horseNbt = nbt.getCompound("Elden_Horse");
        if (!horseNbt.isEmpty()) {
            eldenHorse = (HorseEntity) EntityType.getEntityFromNbt(horseNbt, this.getWorld()).get();
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if (eldenHorse != null && !((ServerPlayerEntity)(Object)this).hasVehicle()) {
            NbtCompound tag = new NbtCompound();
            eldenHorse.saveSelfNbt(tag);
            nbt.put("Elden_Horse", tag);
        }
    }

    @Inject(method = "startRiding", at = @At("TAIL"))
    public void startRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof HorseEntity horse) {
            if (((EldenExt) horse).hasEldenArmor()) {
                storeHorse(horse);
            }
        }
    }

    @Inject(method = "stopRiding", at = @At("TAIL"))
    public void stopRiding(CallbackInfo ci) {
        if (eldenHorse == null) return;

        if (!((EldenExt) eldenHorse).hasEldenArmor() || eldenHorse.getRemovalReason() != null) {
            eldenHorse = null;
            return;
        }
        eldenHorse.remove(Entity.RemovalReason.DISCARDED);
        ((EntityExt) eldenHorse).undoRemove();  // Set removalReason to null so that horse can be spawned
        ((ServerPlayerEntity)(Object)this).fallDistance = eldenHorse.fallDistance;
    }

}
