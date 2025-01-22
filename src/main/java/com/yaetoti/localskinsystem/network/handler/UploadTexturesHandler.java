package com.yaetoti.localskinsystem.network.handler;

import com.yaetoti.localskinsystem.Mod;
import com.yaetoti.localskinsystem.SkinCacheServer;
import com.yaetoti.localskinsystem.TexturesData;
import com.yaetoti.localskinsystem.network.packet.c2s.custom.UploadTexturesC2SPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class UploadTexturesHandler implements ServerPlayNetworking.PlayPayloadHandler<UploadTexturesC2SPayload> {
  @Override
  public void receive(UploadTexturesC2SPayload payload, ServerPlayNetworking.Context context) {
    Mod.LOGGER.info("Received textures from: " + context.player().getGameProfile().getName());

    SkinCacheServer.Get().PutTextures(
      context.player().getGameProfile().getName(),
      new TexturesData(
        payload.isSlimModel(),
        payload.skinData(),
        payload.capeData(),
        payload.elytraData()
      )
    );
  }
}
