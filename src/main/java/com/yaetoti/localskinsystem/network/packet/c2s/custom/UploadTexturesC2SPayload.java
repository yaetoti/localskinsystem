package com.yaetoti.localskinsystem.network.packet.c2s.custom;

import com.yaetoti.localskinsystem.Main;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UploadTexturesC2SPayload(
  boolean isSlimModel,
  byte[] skinData,
  byte[] capeData,
  byte[] elytraData
) implements CustomPayload {
  public static final Identifier PACKET_ID = Main.GetId("upload_textures");
  public static final CustomPayload.Id<UploadTexturesC2SPayload> ID = new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, UploadTexturesC2SPayload> CODEC = PacketCodec.of(UploadTexturesC2SPayload::Write, UploadTexturesC2SPayload::new);

  public UploadTexturesC2SPayload(PacketByteBuf buf) {
    this(
      buf.readBoolean(),
      buf.readByteArray(),
      buf.readByteArray(),
      buf.readByteArray()
    );
  }

  public void Write(PacketByteBuf buf) {
    buf.writeBoolean(this.isSlimModel);
    buf.writeByteArray(skinData);
    buf.writeByteArray(capeData);
    buf.writeByteArray(elytraData);
  }

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
