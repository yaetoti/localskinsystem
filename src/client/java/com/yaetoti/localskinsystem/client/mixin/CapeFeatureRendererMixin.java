package com.yaetoti.localskinsystem.client.mixin;

import com.yaetoti.localskinsystem.Mod;
import com.yaetoti.localskinsystem.client.texture.SampleTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Colors;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;

@Mixin(CapeFeatureRenderer.class)
public abstract class CapeFeatureRendererMixin {
  @Unique
  private float lastAge = 0;

  @Unique
  private CapeFeatureRenderer Self() {
    return (CapeFeatureRenderer) (Object) this;
  }

  @Invoker("hasCustomModelForLayer")
  public abstract boolean invokeHasCustomModelForLayer(ItemStack stack, EquipmentModel.LayerType layerType);

  @Inject(
    method = "Lnet/minecraft/client/render/entity/feature/CapeFeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/PlayerEntityRenderState;FF)V",
    at = @At("HEAD"),
    cancellable = true
  )
  public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, PlayerEntityRenderState playerEntityRenderState, float yaw, float pitch, CallbackInfo ci) {
    if (!playerEntityRenderState.invisible && playerEntityRenderState.capeVisible) {
      SkinTextures skinTextures = playerEntityRenderState.skinTextures;
      if (skinTextures.capeTexture() != null) {
        if (!invokeHasCustomModelForLayer(playerEntityRenderState.equippedChestStack, EquipmentModel.LayerType.WINGS)) {
          matrixStack.push();
          if (invokeHasCustomModelForLayer(playerEntityRenderState.equippedChestStack, EquipmentModel.LayerType.HUMANOID)) {
            matrixStack.translate(0.0F, -0.053125F, 0.06875F);
          }

          matrixStack.translate(0.0F / 16.0F, 0.0F / 16.0F, 2.0F / 16.0F);
          matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float) Math.PI));
          matrixStack.multiply(
            new Quaternionf()
            .rotateY((float) -Math.PI)
            .rotateX((6.0F + playerEntityRenderState.field_53537 / 2.0F + playerEntityRenderState.field_53536) * (float) (Math.PI / 180.0))
            .rotateZ(playerEntityRenderState.field_53538 / 2.0F * (float) (Math.PI / 180.0))
            .rotateY((180.0F - playerEntityRenderState.field_53538 / 2.0F) * (float) (Math.PI / 180.0))
          );

          // Update texture
          SampleTexture sampleTexture = (SampleTexture) MinecraftClient.getInstance().getTextureManager().getTexture(Mod.GetId("sample_texture"));

          // Age is in partial ticks
          float currentAge = playerEntityRenderState.age * 50.0f;
          System.out.println(currentAge);
          if (currentAge > lastAge + sampleTexture.GetLastDelayMs()) {
            lastAge = currentAge;
            sampleTexture.Upload();
          }

          // TODO get custom texture
          VertexConsumer vc = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(Mod.GetId("sample_texture")));
          //VertexConsumer vc = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(skinTextures.capeTexture()));
          new ModelPart.Cuboid(
            0, 0,
            -5.0F, 0.0F, -1.0F,
            10.0F, 16.0F, 1.0F,
            0.0f, 0.0f, 0.0f,
            false,
            1.0F * 12, 0.5F * 32,// 64 64
            EnumSet.allOf(Direction.class)
          ).renderCuboid(matrixStack.peek(), vc, light, OverlayTexture.DEFAULT_UV, Colors.WHITE);


          //Self().getContextModel().copyTransforms(model);
          //model.setAngles(playerEntityRenderState);
          //model.render(matrixStack, vc, light, OverlayTexture.DEFAULT_UV);
          matrixStack.pop();
        }
      }
    }

    ci.cancel();
  }
}
