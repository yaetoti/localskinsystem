package com.yaetoti.localskinsystem;

import com.yaetoti.localskinsystem.config.ConfigManager;
import com.yaetoti.localskinsystem.network.handler.DisconnectHandler;
import com.yaetoti.localskinsystem.network.handler.UploadTexturesHandler;
import com.yaetoti.localskinsystem.network.handler.RequestTexturesHandler;
import com.yaetoti.localskinsystem.network.packet.c2s.custom.UploadTexturesC2SPayload;
import com.yaetoti.localskinsystem.network.packet.c2s.custom.RequestTexturesC2SPayload;
import com.yaetoti.localskinsystem.network.packet.s2c.custom.UpdateTexturesS2CPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class Mod implements ModInitializer {
  public static final String MOD_ID = "localskinsystem";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID + ":common");

  @Override
  public void onInitialize() {
    ConfigManager.Get().LoadRestoreConfig();

    ServerPlayConnectionEvents.DISCONNECT.register(new DisconnectHandler());

    // Packet registration
    // C2S
    PayloadTypeRegistry.playC2S().register(UploadTexturesC2SPayload.ID, UploadTexturesC2SPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(RequestTexturesC2SPayload.ID, RequestTexturesC2SPayload.CODEC);
    // S2C
    PayloadTypeRegistry.playS2C().register(UpdateTexturesS2CPayload.ID, UpdateTexturesS2CPayload.CODEC);

    // Server handler registration
    ServerPlayNetworking.registerGlobalReceiver(UploadTexturesC2SPayload.ID, new UploadTexturesHandler());
    ServerPlayNetworking.registerGlobalReceiver(RequestTexturesC2SPayload.ID, new RequestTexturesHandler());
  }

  public static Identifier GetId(String name) {
    return Identifier.of(MOD_ID, name);
  }

  public static Path GetConfigDir() {
    return FabricLoader.getInstance().getGameDir().resolve("localskinsystem");
  }

  public static Path GetConfigFilePath() {
    return GetConfigDir().resolve("config.json");
  }
}
