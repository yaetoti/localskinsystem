package com.yaetoti.localskinsystem.client.utils;

import com.google.common.hash.Hashing;
import com.yaetoti.localskinsystem.Mod;
import com.yaetoti.localskinsystem.TexturesData;
import com.yaetoti.localskinsystem.client.ModClient;
import com.yaetoti.localskinsystem.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
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
    Identifier skinId = Mod.GetId(ModClient.SKIN_PREFIX + usernameHash);
    Identifier capeId = Mod.GetId(ModClient.CAPE_PREFIX + usernameHash);
    Identifier elytraId = Mod.GetId(ModClient.ELYTRA_PREFIX + usernameHash);
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

  @Nullable
  public static TexturesData LoadSkinTextures() {
    boolean isSlimModel = ConfigManager.Get().ReloadAndGetConfig().isSlimModel();
    byte[] skinData;
    byte[] capeData;
    byte[] elytraData;

    try (FileInputStream fis = new FileInputStream(ModClient.GetSkinPath().toFile())) {
      skinData = fis.readAllBytes();
      NativeImage.read(skinData).close();
      ModClient.LOGGER.info("Skin texture was loaded");
    } catch (IOException e) {
      skinData = new byte[0];
      ModClient.LOGGER.info("Skin data could not be loaded");
    }

    try (FileInputStream fis = new FileInputStream(ModClient.GetCapePath().toFile())) {
      capeData = fis.readAllBytes();
      NativeImage.read(capeData).close();
      ModClient.LOGGER.info("Cape texture was loaded");
    } catch (IOException e) {
      capeData = new byte[0];
      ModClient.LOGGER.info("Cape data could not be loaded");
    }

    try (FileInputStream fis = new FileInputStream(ModClient.GetElytraPath().toFile())) {
      elytraData = fis.readAllBytes();
      NativeImage.read(elytraData).close();
      ModClient.LOGGER.info("Elytra texture was loaded");
    } catch (IOException e) {
      elytraData = new byte[0];
      ModClient.LOGGER.info("Elytra data could not be loaded");
    }

    if (skinData.length == 0 && capeData.length == 0 && elytraData.length == 0) {
      return null;
    }

    return new TexturesData(isSlimModel, skinData, capeData, elytraData);
  }
}
