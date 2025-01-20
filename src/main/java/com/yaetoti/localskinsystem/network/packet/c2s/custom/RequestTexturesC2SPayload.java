package com.yaetoti.localskinsystem.network.packet.c2s.custom;

import com.yaetoti.localskinsystem.Main;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestTexturesC2SPayload(
  String username
) implements CustomPayload {
  public static final Identifier PACKET_ID = Main.GetId("request_textures");
  public static final Id<RequestTexturesC2SPayload> ID = new Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, RequestTexturesC2SPayload> CODEC = PacketCodec.of(RequestTexturesC2SPayload::Write, RequestTexturesC2SPayload::new);

  public RequestTexturesC2SPayload(PacketByteBuf buf) {
    this(buf.readString());
  }

  public void Write(PacketByteBuf buf) {
    buf.writeString(username);
  }

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
