package com.yaetoti.localskinsystem.client.mixin;

import com.mojang.authlib.GameProfile;
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

    callback.setReturnValue(future);
  }
}
