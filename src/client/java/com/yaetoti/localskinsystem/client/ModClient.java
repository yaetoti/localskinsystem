package com.yaetoti.localskinsystem.client;

import com.yaetoti.localskinsystem.Mod;
import com.yaetoti.localskinsystem.client.network.handler.DisconnectHandler;
import com.yaetoti.localskinsystem.client.network.handler.JoinHandler;
import com.yaetoti.localskinsystem.client.network.handler.UpdateTexturesHandler;
import com.yaetoti.localskinsystem.client.texture.SampleTexture;
import com.yaetoti.localskinsystem.network.packet.s2c.custom.UpdateTexturesS2CPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class ModClient implements ClientModInitializer {
  public static final Logger LOGGER = LoggerFactory.getLogger(Mod.MOD_ID + ":client");
  public static final String SKIN_PREFIX = "skin_";
  public static final String CAPE_PREFIX = "cape_";
  public static final String ELYTRA_PREFIX = "elytra_";

  @Override
  public void onInitializeClient() {
    // TODO add sample texture
    MinecraftClient.getInstance().execute(() -> {
      try {
        MinecraftClient.getInstance().getTextureManager().registerTexture(Mod.GetId("sample_texture"), new SampleTexture());
      } catch (IOException e) {
        e.printStackTrace();
        System.out.println("Can't load sample texture");
      }
    });

    ClientPlayConnectionEvents.JOIN.register(new JoinHandler());
    ClientPlayConnectionEvents.DISCONNECT.register(new DisconnectHandler());
    ClientPlayNetworking.registerGlobalReceiver(UpdateTexturesS2CPayload.ID, new UpdateTexturesHandler());
  }

  public static Path GetSkinPath() {
    return Mod.GetConfigDir().resolve("skin.png");
  }

  public static Path GetCapePath() {
    return Mod.GetConfigDir().resolve("cape.png");
  }

  public static Path GetElytraPath() {
    return Mod.GetConfigDir().resolve("elytra.png");
  }
}
