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

    // Waiter put must be synchronized
    m_skinCacheLock.readLock().unlock();
    return waiter;
  }

  public void PutTextures(String username, TexturesData data) {
    System.out.println("Write-locking server cache");
    m_skinCacheLock.writeLock().lock();
    if (data == null) {
      m_skinCache.remove(username);
    } else {
      m_skinCache.put(username, data);
    }
    m_skinCacheLock.writeLock().unlock();
    System.out.println("Server cache has been updated");

    var waiter = m_skinWaiters.get(username);
    if (waiter != null) {
      System.out.println("Notifying waiter");
      waiter.complete(data);
      m_skinWaiters.remove(username);
      System.out.println("Waiter notified");
    }
  }

  public void RemoveTextures(String username) {
    m_skinCacheLock.writeLock().lock();
    m_skinCache.remove(username);
    m_skinCacheLock.writeLock().unlock();

    var waiter = m_skinWaiters.get(username);
    if (waiter != null) {
      waiter.complete(null);
      m_skinWaiters.remove(username);
    }
  }
}
