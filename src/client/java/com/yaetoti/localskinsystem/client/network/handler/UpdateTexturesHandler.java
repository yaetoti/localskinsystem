package com.yaetoti.localskinsystem.client.network.handler;

import com.yaetoti.localskinsystem.TexturesData;
import com.yaetoti.localskinsystem.client.ModClient;
import com.yaetoti.localskinsystem.client.SkinCacheClient;
import com.yaetoti.localskinsystem.client.utils.TextureHelper;
import com.yaetoti.localskinsystem.network.packet.s2c.custom.UpdateTexturesS2CPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class UpdateTexturesHandler implements ClientPlayNetworking.PlayPayloadHandler<UpdateTexturesS2CPayload> {
  @Override
  public void receive(UpdateTexturesS2CPayload payload, ClientPlayNetworking.Context context) {
    ModClient.LOGGER.info("Received UpdateTexturesS2CPayload");
    ModClient.LOGGER.info("Username: " + payload.username());
    ModClient.LOGGER.info("Skin image size: " + payload.skinData().length);
    ModClient.LOGGER.info("Cape image size: " + payload.capeData().length);
    ModClient.LOGGER.info("Elytra image size: " + payload.elytraData().length);

    MinecraftClient.getInstance().execute(() -> {
      var textures = TextureHelper.RegisterSkinTextures(
        payload.username(),
        new TexturesData(
          payload.isSlimModel(),
          payload.skinData(),
          payload.capeData(),
          payload.elytraData()
        )
      );

      if (textures.isEmpty()) {
        SkinCacheClient.Get().Invalidate(payload.username());
        ModClient.LOGGER.info("Skin was removed from cache");
        return;
      }

      SkinCacheClient.Get().UpdateCache(payload.username(), textures.get());
      ModClient.LOGGER.info("Cache was updated");
    });
  }
}
