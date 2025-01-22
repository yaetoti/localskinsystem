package com.yaetoti.localskinsystem.client.utils;

import net.minecraft.client.util.SkinTextures;

import java.util.Optional;
import java.util.function.Supplier;

public final class LocalSkinSupplier implements Supplier<Optional<SkinTextures>> {
  private final String m_username;

  public LocalSkinSupplier(String username) {
    m_username = username;
  }

  @Override
  public Optional<SkinTextures> get() {
    var textures = TextureHelper.LoadSkinTextures();
    if (textures == null) {
      return Optional.empty();
    }

    return TextureHelper.RegisterSkinTextures(m_username, textures);
  }
}
