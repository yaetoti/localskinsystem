package com.yaetoti.localskinsystem.client;

import com.yaetoti.localskinsystem.Main;
import com.yaetoti.localskinsystem.client.network.handler.DisconnectHandler;
import com.yaetoti.localskinsystem.client.network.handler.JoinHandler;
import com.yaetoti.localskinsystem.client.network.handler.UpdateTexturesHandler;
import com.yaetoti.localskinsystem.network.packet.s2c.custom.UpdateTexturesS2CPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class MainClient implements ClientModInitializer {
  public static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID + ":client");
  public static final String SKIN_PREFIX = "skin_";
  public static final String CAPE_PREFIX = "cape_";
  public static final String ELYTRA_PREFIX = "elytra_";

  @Override
  public void onInitializeClient() {
    ClientPlayConnectionEvents.JOIN.register(new JoinHandler());
    ClientPlayConnectionEvents.DISCONNECT.register(new DisconnectHandler());
    ClientPlayNetworking.registerGlobalReceiver(UpdateTexturesS2CPayload.ID, new UpdateTexturesHandler());
  }

  public static Path GetSkinPath() {
    return Main.GetConfigDir().resolve("skin.png");
  }

  public static Path GetCapePath() {
    return Main.GetConfigDir().resolve("cape.png");
  }

  public static Path GetElytraPath() {
    return Main.GetConfigDir().resolve("elytra.png");
  }
}
