package com.yaetoti.localskinsystem.client;

import com.google.common.cache.*;
import com.yaetoti.localskinsystem.client.utils.TextureHelper;
import net.minecraft.client.util.SkinTextures;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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

  public CompletableFuture<Optional<SkinTextures>> GetSkinLoader(String username) {
    //System.out.println("Client Cache Access for: " + username);
    return m_loaderCache.getUnchecked(username);
  }

  public void Invalidate(String username) {
    m_loaderCache.invalidate(username);
  }

  public void Reset() {
    m_loaderCache.invalidateAll();
  }

  public void UpdateCache(String username, SkinTextures textures) {
    var future = m_loaderCache.getIfPresent(username);
    if (future == null) {
      m_loaderCache.put(username, CompletableFuture.completedFuture(Optional.ofNullable(textures)));
      return;
    }

    if (future.isDone()) {
      m_loaderCache.put(username, CompletableFuture.completedFuture(Optional.ofNullable(textures)));
      return;
    }

    future.complete(Optional.ofNullable(textures));
  }

  private static class SkinCacheLoader extends CacheLoader<String, CompletableFuture<Optional<SkinTextures>>> {
    @Override
    public @NotNull CompletableFuture<Optional<SkinTextures>> load(@NotNull String username) {
      System.out.println("Creating new loader for " + username);
      return new CompletableFuture<>();
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

      System.out.println("Removing textures from cache");
      TextureHelper.UnregisterTextures(textures.get());
    }
  }
}
