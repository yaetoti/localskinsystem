package com.yaetoti.localskinsystem;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class SkinCacheServer {
  private static volatile SkinCacheServer instance;

  private final ReentrantReadWriteLock m_skinCacheLock = new ReentrantReadWriteLock();
  private final HashMap<String, TexturesData> m_skinCache = new HashMap<>();
  private final ConcurrentHashMap<String, CompletableFuture<TexturesData>> m_skinWaiters = new ConcurrentHashMap<>();

  public static SkinCacheServer Get() {
    if (instance != null) {
      return instance;
    }

    synchronized (SkinCacheServer.class) {
      if (instance == null) {
        instance = new SkinCacheServer();
      }

      return instance;
    }
  }

  public CompletableFuture<TexturesData> GetTexturesWaiter(String username) {
    m_skinCacheLock.readLock().lock();

    // If cache has textures - return completed
    TexturesData data = m_skinCache.get(username);
    if (data != null) {
      m_skinCacheLock.readLock().unlock();
      return CompletableFuture.completedFuture(data);
    }

    // If loader exists - return it
    var waiter = m_skinWaiters.get(username);
    if (waiter != null) {
      m_skinCacheLock.readLock().unlock();
      return waiter;
    }

    // Otherwise put loader
    waiter = new CompletableFuture<TexturesData>()
      .completeOnTimeout(null, 5, TimeUnit.SECONDS);
    m_skinWaiters.put(username, waiter);

    // TODO FUCK THE FUCK
    Mod.LOGGER.warn("Waiters queue size: " + m_skinWaiters.size());

    // Waiter put must be synchronized
    m_skinCacheLock.readLock().unlock();
    return waiter;
  }

  public void PutTextures(String username, TexturesData data) {
    Mod.LOGGER.info("Write-locking server cache");
    m_skinCacheLock.writeLock().lock();
    if (data == null) {
      m_skinCache.remove(username);
    } else {
      m_skinCache.put(username, data);
    }
    m_skinCacheLock.writeLock().unlock();
    Mod.LOGGER.info("Server cache has been updated");

    var waiter = m_skinWaiters.get(username);
    if (waiter != null) {
      // TODO nasty but should work
      if (waiter.isDone()) {
        waiter.obtrudeValue(data);
      } else {
        waiter.complete(data);
      }

      m_skinWaiters.remove(username);
      Mod.LOGGER.info("Waiter notified");
    }
  }

  public void RemoveTextures(String username) {
    m_skinCacheLock.writeLock().lock();
    m_skinCache.remove(username);

    var waiter = m_skinWaiters.get(username);
    if (waiter != null) {
      // TODO nasty but should work
      if (waiter.isDone()) {
        waiter.obtrudeValue(null);
      } else {
        waiter.complete(null);
      }

      m_skinWaiters.remove(username);
    }
    m_skinCacheLock.writeLock().unlock();
  }
}
