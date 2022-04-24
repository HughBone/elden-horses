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

    public HorseEntity getHorse() {
        return eldenHorse;
    }

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
        eldenHorse2.world = player.world; // Update dimension
        eldenHorse2.fallDistance = player.fallDistance;

        // Spawn elden horse
        eldenHorse2.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        if (mountPlayer) player.startRiding(eldenHorse2, true);
        eldenHorse2.setVelocity(player.getVelocity());
        player.getWorld().tryLoadEntity(eldenHorse2);
    }

    public void updatePlayerHorse(HorseEntity horse) {
        if (eldenHorse != null) {
            // Set to null if has armor but no rider
            if (((EldenExt) eldenHorse).hasEldenArmor() && !eldenHorse.hasPlayerRider() && eldenHorse.equals(horse)) {
                eldenHorse = null;
            }
        }
    }

    public void storeHorse(HorseEntity horse) {
        if (eldenHorse != null) {
            if (!eldenHorse.equals(horse)) {
                this.sendMessage(Text.of("Replaced Old Horse!"), true);
                summonHorse(false);
            }
        }
        if (horse.getRemovalReason() != null) {
            eldenHorse = null;
            return;
        }
        eldenHorse = horse;
        ((ServerPlayerEntity)(Object)this).fallDistance = eldenHorse.fallDistance;
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
                    while (true) {
                        if (player.hasVehicle()) {
                            if (player.getVehicle().equals(eldenHorse)) {
                                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                                continue;
                            }
                        }
                        break;
                    }
                    storeHorse(horse);
                })).start();
            }
        }
    }


}
