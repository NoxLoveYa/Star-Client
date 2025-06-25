package com.starclient.mixin.client;

import com.starclient.utils.CheatOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EspForceMixin {
    @Shadow @Final protected EntityRenderDispatcher dispatcher;

    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void forceBlToTrue(Entity entity, EntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (!CheatOptions.NameEnabled || !(entity instanceof PlayerEntity || entity instanceof HostileEntity)){
            return;
        }
        state.displayName = entity.getDisplayName();
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void idk(Entity entity, CallbackInfoReturnable<Text> cir) {
        if (entity instanceof PlayerEntity || entity instanceof HostileEntity) {
            cir.setReturnValue(entity.getStyledDisplayName());
            cir.cancel();
        }
    }

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void idk2(EntityRenderState state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        Vec3d vec3d = state.nameLabelPos;
        if (vec3d == null) {
            ci.cancel();
            return;
        }

        matrices.push();
        matrices.translate(vec3d.x, vec3d.y + 0.5, vec3d.z);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        TextRenderer textRenderer = this.getTextRenderer();
        float f = -textRenderer.getWidth(text) / 2.0F;
        int j = (int)(MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F) * 255.0F) << 24;
        textRenderer.draw(
                text, f, (float)0, Colors.WHITE, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, j, light
        ); 

        matrices.pop();

        ci.cancel();
    }

}
