package com.yaetoti.localskinsystem.client.network.handler;

import com.yaetoti.localskinsystem.client.MainClient;
import com.yaetoti.localskinsystem.config.ConfigManager;
import com.yaetoti.localskinsystem.network.packet.c2s.custom.UploadTexturesC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.texture.NativeImage;

import java.io.FileInputStream;
import java.io.IOException;

public class JoinHandler implements ClientPlayConnectionEvents.Join {
  @Override
  public void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
    boolean isSlimModel = ConfigManager.Get().ReloadAndGetConfig().isSlimModel();
    byte[] skinData;
    byte[] capeData;
    byte[] elytraData;

    try (FileInputStream fis = new FileInputStream(MainClient.GetSkinPath().toFile())) {
      skinData = fis.readAllBytes();
      NativeImage.read(skinData).close();
      System.out.println("Skin loaded");
    } catch (IOException e) {
      skinData = new byte[0];
      System.out.println("Skin data could not be loaded");
    }

    try (FileInputStream fis = new FileInputStream(MainClient.GetCapePath().toFile())) {
      capeData = fis.readAllBytes();
      NativeImage.read(capeData).close();
      System.out.println("Cape loaded");
    } catch (IOException e) {
      capeData = new byte[0];
      System.out.println("Cape data could not be loaded");
    }

    try (FileInputStream fis = new FileInputStream(MainClient.GetElytraPath().toFile())) {
      elytraData = fis.readAllBytes();
      NativeImage.read(elytraData).close();
      System.out.println("Elytra loaded");
    } catch (IOException e) {
      elytraData = new byte[0];
      System.out.println("Elytra data could not be loaded");
    }

    if (skinData.length == 0 && capeData.length == 0 && elytraData.length == 0) {
      return;
    }

    ClientPlayNetworking.send(new UploadTexturesC2SPayload(isSlimModel, skinData, capeData, elytraData));
  }
}
