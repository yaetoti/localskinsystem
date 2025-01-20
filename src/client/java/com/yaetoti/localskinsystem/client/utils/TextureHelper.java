package com.yaetoti.localskinsystem.client.utils;

import com.google.common.hash.Hashing;
import com.yaetoti.localskinsystem.Main;
import com.yaetoti.localskinsystem.TexturesData;
import com.yaetoti.localskinsystem.client.MainClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class TextureHelper {
  public static boolean RegisterTexture(Identifier id, byte[] textureData) {
    try {
      var image = NativeImage.read(textureData);
      MinecraftClient.getInstance().getTextureManager().registerTexture(
        id,
        new NativeImageBackedTexture(image)
      );

      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public static Optional<SkinTextures> RegisterSkinTextures(@NotNull String username, @NotNull TexturesData data) {
    String usernameHash = Hashing.sha1().hashUnencodedChars(username).toString();
    Identifier skinId = Main.GetId(MainClient.SKIN_PREFIX + usernameHash);
    Identifier capeId = Main.GetId(MainClient.CAPE_PREFIX + usernameHash);
    Identifier elytraId = Main.GetId(MainClient.ELYTRA_PREFIX + usernameHash);
    boolean useSkin = data.skinData().length != 0 && RegisterTexture(skinId, data.skinData());
    boolean useCape = data.capeData().length != 0 && RegisterTexture(capeId, data.capeData());
    boolean useElytra = data.elytraData().length != 0 && RegisterTexture(elytraId, data.elytraData());
    if (useSkin || useCape || useElytra) {
      return Optional.of(new SkinTextures(
        useSkin ? skinId : null,
        null,
        useCape ? capeId : null,
        useElytra ? elytraId : null,
        data.isSlimModel() ? SkinTextures.Model.SLIM : SkinTextures.Model.WIDE,
        true
      ));
    }

    return Optional.empty();
  }

  public static void UnregisterTextures(@NotNull SkinTextures textures) {
    if (textures.texture() != null) {
      MinecraftClient.getInstance().getTextureManager().destroyTexture(textures.texture());
    }

    if (textures.capeTexture() != null) {
      MinecraftClient.getInstance().getTextureManager().destroyTexture(textures.capeTexture());
    }

    if (textures.elytraTexture() != null) {
      MinecraftClient.getInstance().getTextureManager().destroyTexture(textures.elytraTexture());
    }
  }
}
