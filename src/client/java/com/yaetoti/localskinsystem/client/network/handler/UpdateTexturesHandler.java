package com.yaetoti.localskinsystem.client.network.handler;

import com.yaetoti.localskinsystem.TexturesData;
import com.yaetoti.localskinsystem.client.SkinCacheClient;
import com.yaetoti.localskinsystem.client.utils.TextureHelper;
import com.yaetoti.localskinsystem.network.packet.s2c.custom.UpdateTexturesS2CPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class UpdateTexturesHandler implements ClientPlayNetworking.PlayPayloadHandler<UpdateTexturesS2CPayload> {
  @Override
  public void receive(UpdateTexturesS2CPayload payload, ClientPlayNetworking.Context context) {
    System.out.println("Received TexturesUpdateS2CPayload");
    System.out.println("Username: " + payload.username());
    System.out.println("Skin size: " + payload.skinData().length);
    System.out.println("Cape size: " + payload.capeData().length);
    System.out.println("Elytra size: " + payload.elytraData().length);

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
        System.out.println("Removed texture");
        return;
      }

      SkinCacheClient.Get().UpdateCache(payload.username(), textures.get());
      System.out.println("Cache was updated");
    });
  }
}
