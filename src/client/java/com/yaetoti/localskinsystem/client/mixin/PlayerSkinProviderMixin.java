package com.yaetoti.localskinsystem.client.mixin;

import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.yaetoti.localskinsystem.client.SkinCacheClient;
import com.yaetoti.localskinsystem.network.packet.c2s.custom.RequestTexturesC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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

  @Inject(
    method = "Lnet/minecraft/client/texture/PlayerSkinProvider;fetchSkinTextures(Lcom/mojang/authlib/GameProfile;)Ljava/util/concurrent/CompletableFuture;",
    at = @At(value = "HEAD"),
    cancellable = true
  )
  public void fetchSkinTextures(GameProfile profile, CallbackInfoReturnable<CompletableFuture<Optional<SkinTextures>>> callback) {
    // Use local textures for own player

    // Load textures
    var future = SkinCacheClient.Get().GetSkinLoader(profile.getName());
    if (!future.isDone()) {
      System.out.println("Fetching textures for " + profile.getName());
      ClientPlayNetworking.send(new RequestTexturesC2SPayload(profile.getName()));
    }

    future.thenApply((textures) -> {
      if (textures.isPresent()) {
        return textures;
      }

      System.out.println("Failed to fetch textures. Trying to fetch textures from Minecraft servers.");

      Property property = this.sessionService.getPackedTextures(profile);
      return this.cache.getUnchecked(new PlayerSkinProvider.Key(profile.getId(), property));
    });

    callback.setReturnValue(future);
  }
}
