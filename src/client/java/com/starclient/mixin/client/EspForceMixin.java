package com.starclient.mixin.client;

import com.starclient.interfaces.IEntityRenderState;
import com.starclient.utils.CheatOptions;
import com.starclient.utils.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(EntityRenderer.class)
public abstract class EspForceMixin {
    @Shadow @Final protected EntityRenderDispatcher dispatcher;

    @Shadow public abstract TextRenderer getTextRenderer();

    @Redirect(method = "updateRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;getSquaredDistanceToCamera(Lnet/minecraft/entity/Entity;)D"))
    private double forceBlToTrue(EntityRenderDispatcher instance, Entity entity) {
        if (!CheatOptions.NameEnabled || !(entity instanceof PlayerEntity || entity instanceof HostileEntity)){
            return instance.camera.getPos().squaredDistanceTo(entity.getPos());
        }
        return 10.0;
    }

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void idk2(EntityRenderState state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        Vec3d vec3d = state.nameLabelPos;
        if (vec3d == null) {
            ci.cancel();
            return;
        }

        matrices.push();
        matrices.translate(vec3d.x, vec3d.y + 0.35F, vec3d.z);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        TextRenderer textRenderer = this.getTextRenderer();
        ColorUtils labelColor = new ColorUtils(0);
        float x = -textRenderer.getWidth(text) / 2.0F;
        int j = new ColorUtils(20, 20, 20, MathHelper.ceil(0.6F * 255.0F)).getRGB();
        textRenderer.draw(
                text, x, 0, labelColor.getRainbowTextColor(50), false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, j, light
        );

        try {
            Entity currentEntity = ((IEntityRenderState) state).starclient$getEntity();
            LivingEntity currentLivingEntity = (LivingEntity) currentEntity;
            textRenderer.draw(
                    String.format("%.0f", currentLivingEntity.getHealth()), -x + 1.75F, 0, labelColor.getLerpedColor(new Color(255, 38, 96), new Color( 194, 255, 38), currentLivingEntity.getHealth() / currentLivingEntity.getMaxHealth()), false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, j, light
            );
        } catch (Exception e) { }

        matrices.pop();
        ci.cancel();
    }
}