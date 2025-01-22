package com.yaetoti.localskinsystem.client.mixin;

import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.yaetoti.localskinsystem.client.ModClient;
import com.yaetoti.localskinsystem.client.SkinCacheClient;
import com.yaetoti.localskinsystem.client.utils.LocalSkinSupplier;
import com.yaetoti.localskinsystem.network.packet.c2s.custom.RequestTexturesC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(PlayerSkinProvider.class)
public abstract class PlayerSkinProviderMixin {
  @Final
  @Shadow
  private MinecraftSessionService sessionService;
  @Final
  @Shadow
  private LoadingCache<PlayerSkinProvider.Key, CompletableFuture<Optional<SkinTextures>>> cache;

  @Unique
  public CompletableFuture<Optional<SkinTextures>> FetchMojangSkin(GameProfile profile) {
    Property property = this.sessionService.getPackedTextures(profile);
    return this.cache.getUnchecked(new PlayerSkinProvider.Key(profile.getId(), property));
  }

  @Unique
  private CompletableFuture<Optional<SkinTextures>> LoadOrUseMojangLoader(GameProfile profile, CompletableFuture<Optional<SkinTextures>> loader) {
    return CompletableFuture.supplyAsync(() -> {
      var result = loader.join();
      if (result.isPresent()) {
        return result;
      }

      ModClient.LOGGER.info("Failed to fetch textures. Trying to fetch textures from Mojang auth service.");
      return FetchMojangSkin(profile).join();
    });
  }

  @Inject(
    method = "Lnet/minecraft/client/texture/PlayerSkinProvider;fetchSkinTextures(Lcom/mojang/authlib/GameProfile;)Ljava/util/concurrent/CompletableFuture;",
    at = @At(value = "HEAD"),
    cancellable = true
  )
  public void fetchSkinTextures(GameProfile profile, CallbackInfoReturnable<CompletableFuture<Optional<SkinTextures>>> callback) {
    String username = profile.getName();

    // Some heads don't have name, just UUID
    if (username.isEmpty()) {
      callback.setReturnValue(FetchMojangSkin(profile));
      return;
    }

    // Use local textures for own player
    var localPlayer = MinecraftClient.getInstance().player;
    if (localPlayer != null && profile.equals(localPlayer.getGameProfile())) {
      var loader = SkinCacheClient.Get().GetSkinLoaderIfPresent(username);
      if (loader != null) {
        callback.setReturnValue(loader);
        return;
      }

      ModClient.LOGGER.info("Creating local skin loader for " + username);
      loader = SkinCacheClient.Get().GetSkinLoader(username);
      loader.completeAsync(new LocalSkinSupplier(username));
      callback.setReturnValue(LoadOrUseMojangLoader(profile, loader));
    }

    // Load skin from server
    var loader = SkinCacheClient.Get().GetSkinLoaderIfPresent(username);
    if (loader != null) {
      callback.setReturnValue(loader);
      return;
    }

    ModClient.LOGGER.info("Creating server skin loader for " + username);
    loader = SkinCacheClient.Get().GetSkinLoader(username);
    ClientPlayNetworking.send(new RequestTexturesC2SPayload(profile.getName()));
    LoadOrUseMojangLoader(profile, loader);
    callback.setReturnValue(loader);
  }
}
