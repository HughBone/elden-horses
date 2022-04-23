package com.hughbone.eldenhorses.mixin;

import com.hughbone.eldenhorses.interfaces.EldenExt;
import com.hughbone.eldenhorses.interfaces.EntityExt;
import com.hughbone.eldenhorses.interfaces.ServerPlayerExt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin implements ServerPlayerExt {

    @Shadow public abstract void sendMessage(Text message, boolean actionBar);
    @Shadow public abstract ServerWorld getWorld();
    private HorseEntity eldenHorse = null;

    public void summonHorse(boolean mountPlayer) {
        if (eldenHorse != null) {
            if (!((EldenExt)eldenHorse).hasEldenArmor())
                eldenHorse = null;
        }
        if (eldenHorse == null) {
            this.sendMessage(Text.of("No Horse Found!"), true);
            return;
        }

        HorseEntity eldenHorse2 = eldenHorse;
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        eldenHorse2.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        if (mountPlayer) player.startRiding(eldenHorse2, true);
        eldenHorse2.setVelocity(player.getVelocity());
        player.getWorld().tryLoadEntity(eldenHorse2);
    }

    public void storeHorse(HorseEntity horse) {
        if (eldenHorse != null) {
            if (!eldenHorse.equals(horse))
                summonHorse(false);
        }
        if (horse.getRemovalReason() != null) {
            eldenHorse = null;
            return;
        }
        eldenHorse = horse;
        horse.remove(Entity.RemovalReason.DISCARDED);
        ((EntityExt) eldenHorse).undoRemove();

    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound horseNbt = nbt.getCompound("Elden_Horse");
        if (!horseNbt.isEmpty()) {
            eldenHorse = (HorseEntity) EntityType.getEntityFromNbt(horseNbt, this.getWorld()).get();
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if (eldenHorse != null) {
            if (!((EldenExt)eldenHorse).hasEldenArmor()) {
                eldenHorse = null;
                return;
            }
            NbtCompound tag = new NbtCompound();
            eldenHorse.saveSelfNbt(tag);
            nbt.put("Elden_Horse", tag);
        }
    }

    @Inject(method = "stopRiding", at = @At("HEAD"))
    public void stopRiding(CallbackInfo ci) {
        ServerPlayerEntity player = ((ServerPlayerEntity)(Object)this);
        Entity mountedEntity = player.getVehicle();

        if (mountedEntity instanceof HorseEntity horse) {
            if (((EldenExt) horse).hasEldenArmor()) {
                (new Thread(() -> {
                    while (player.hasVehicle()) {
                        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                    }
                    storeHorse(horse);
                })).start();
            }
        }
    }


}
