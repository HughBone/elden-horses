package com.hughbone.eldenhorses.mixin;

import com.hughbone.eldenhorses.interfaces.EldenExt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.HorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {
    protected LivingRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    private boolean lowerHorseOpacity = false;

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("STORE"), ordinal = 1)
    private boolean lowerEldenHorseOpacity(boolean bl2) {
        if (lowerHorseOpacity)  {
            lowerHorseOpacity = false;
            return true;
        }
        return bl2;
    }

    @Inject(method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at=@At("HEAD"))
    private void render(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (livingEntity instanceof HorseEntity horse) {
            /* Lower elden horse opacity if:
            1. Client is riding
            2. Client is in first person
            3. Horse has netherite armor
             */
            if (!horse.hasPassenger(MinecraftClient.getInstance().player)) return;
            if (!MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) return;
            if (!((EldenExt) horse).hasEldenArmor()) return;

            lowerHorseOpacity = true;
        }
    }

}
