package com.yaetoti.localskinsystem.network.handler;

import com.yaetoti.localskinsystem.Mod;
import com.yaetoti.localskinsystem.SkinCacheServer;
import com.yaetoti.localskinsystem.TexturesData;
import com.yaetoti.localskinsystem.network.packet.c2s.custom.RequestTexturesC2SPayload;
import com.yaetoti.localskinsystem.network.packet.s2c.custom.UpdateTexturesS2CPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RequestTexturesHandler implements ServerPlayNetworking.PlayPayloadHandler<RequestTexturesC2SPayload> {
  @Override
  public void receive(RequestTexturesC2SPayload payload, ServerPlayNetworking.Context context) {
    Mod.LOGGER.info("Received RequestTexturesC2SPayload from: " + context.player().getGameProfile().getName());
    Mod.LOGGER.info("Requested skin for: " + payload.username());

    Mod.LOGGER.info("Creating new waiter for: " + payload.username());
    SkinCacheServer.Get().GetTexturesWaiter(payload.username())
      .thenAccept(new TexturesDataConsumer(payload, context));
  }

  private static class TexturesDataConsumer implements Consumer<TexturesData> {
    private final RequestTexturesC2SPayload m_payload;
    private final ServerPlayNetworking.Context m_context;

    public TexturesDataConsumer(RequestTexturesC2SPayload payload, ServerPlayNetworking.Context context) {
      m_payload = payload;
      m_context = context;
    }

    // Gets triggered by UploadTextureHandler, while server is still running (but request sender may be disconnected)
    @Override
    public void accept(@Nullable TexturesData data) {
      if (m_context.player().isDisconnected()) {
        return;
      }

      if (data == null) {
        Mod.LOGGER.info("No textures found for: " + m_payload.username());
        // TODO NoTexturesPayload
        return;
      }

      Mod.LOGGER.info("Textures found for: " + m_payload.username());
      m_context.responseSender().sendPacket(
        new UpdateTexturesS2CPayload(
          m_payload.username(),
          data.isSlimModel(),
          data.skinData(),
          data.capeData(),
          data.elytraData()
        )
      );
    }
  }
}
