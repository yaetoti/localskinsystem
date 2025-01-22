package com.yaetoti.localskinsystem.client;

import com.google.common.cache.*;
import com.yaetoti.localskinsystem.client.utils.LocalSkinSupplier;
import com.yaetoti.localskinsystem.client.utils.TextureHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SkinTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class SkinCacheClient {
  private static volatile SkinCacheClient instance;

  private final LoadingCache<String, CompletableFuture<Optional<SkinTextures>>> m_loaderCache;

  private SkinCacheClient() {
    m_loaderCache = CacheBuilder.newBuilder()
      //.expireAfterAccess(Duration.ofSeconds(15))
      // TODO removes own skin textures after timeout. No reload occurs afterwards
      //.removalListener(new SkinCacheRemovalListener())
      .build(new SkinCacheLoader());
  }

  public static SkinCacheClient Get() {
    if (instance != null) {
      return instance;
    }

    synchronized (SkinCacheClient.class) {
      if (instance == null) {
        instance = new SkinCacheClient();
      }

      return instance;
    }
  }

  @NotNull
  public CompletableFuture<Optional<SkinTextures>> GetSkinLoader(String username) {
    return m_loaderCache.getUnchecked(username);
  }

  @Nullable
  public CompletableFuture<Optional<SkinTextures>> GetSkinLoaderIfPresent(String username) {
    return m_loaderCache.getIfPresent(username);
  }

  public void Invalidate(String username) {
    m_loaderCache.invalidate(username);
  }

  public void Reset() {
    m_loaderCache.invalidateAll();
  }

  public void UpdateCache(@NotNull String username, @Nullable SkinTextures textures) {
    var future = m_loaderCache.getIfPresent(username);
    if (future == null) {
      // Place if missing
      m_loaderCache.put(username, CompletableFuture.completedFuture(Optional.ofNullable(textures)));
      return;
    }

    if (future.isDone()) {
      // If completed - replace with another result
      m_loaderCache.put(username, CompletableFuture.completedFuture(Optional.ofNullable(textures)));
      return;
    }

    // Complete incomplete future
    future.complete(Optional.ofNullable(textures));
  }

  private static class SkinCacheLoader extends CacheLoader<String, CompletableFuture<Optional<SkinTextures>>> {
    @Override
    public @NotNull CompletableFuture<Optional<SkinTextures>> load(@NotNull String username) {
      CompletableFuture<Optional<SkinTextures>> loader = new CompletableFuture<>();
      loader.completeOnTimeout(Optional.empty(), 5, TimeUnit.SECONDS);
      return loader;
    }
  }

  private static class SkinCacheRemovalListener implements RemovalListener<String, CompletableFuture<Optional<SkinTextures>>> {
    @Override
    public void onRemoval(RemovalNotification<String, CompletableFuture<Optional<SkinTextures>>> notification) {
      var future = notification.getValue();
      if (notification.getValue() == null || !future.isDone()) {
        return;
      }

      var textures = future.getNow(Optional.empty());
      if (textures.isEmpty()) {
        return;
      }

      ModClient.LOGGER.info("Removing textures from cache");
      TextureHelper.UnregisterTextures(textures.get());
    }
  }
}
