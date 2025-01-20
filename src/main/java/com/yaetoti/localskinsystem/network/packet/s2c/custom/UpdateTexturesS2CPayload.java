package com.yaetoti.localskinsystem.network.packet.s2c.custom;

import com.yaetoti.localskinsystem.Main;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UpdateTexturesS2CPayload(
  String username,
  boolean isSlimModel,
  byte[] skinData,
  byte[] capeData,
  byte[] elytraData
) implements CustomPayload {
  public static final Identifier PACKET_ID = Main.GetId("update_textures");
  public static final Id<UpdateTexturesS2CPayload> ID = new Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, UpdateTexturesS2CPayload> CODEC = PacketCodec.of(UpdateTexturesS2CPayload::Write, UpdateTexturesS2CPayload::new);

  public UpdateTexturesS2CPayload(PacketByteBuf buf) {
    this(
      buf.readString(),
      buf.readBoolean(),
      buf.readByteArray(),
      buf.readByteArray(),
      buf.readByteArray()
    );
  }

  public void Write(PacketByteBuf buf) {
    buf.writeString(username);
    buf.writeBoolean(isSlimModel);
    buf.writeByteArray(skinData);
    buf.writeByteArray(capeData);
    buf.writeByteArray(elytraData);
  }

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
