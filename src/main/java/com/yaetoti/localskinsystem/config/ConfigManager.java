package com.yaetoti.localskinsystem.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.yaetoti.localskinsystem.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

public final class ConfigManager {
  private static ConfigManager instance;

  private final Gson m_gson;
  @NotNull
  private ConfigData m_configData;
  @Nullable
  FileTime m_lastModifiedTime;

  private ConfigManager() {
    m_gson = new GsonBuilder().setPrettyPrinting().create();
    m_configData = ConfigData.DEFAULT;
  }

  public boolean LoadConfig() {
    Path configFilePath = Main.GetConfigFilePath();
    if (!Files.isRegularFile(configFilePath)) {
      return false;
    }

    FileTime lastModifiedTime;
    try {
      lastModifiedTime = Files.getLastModifiedTime(configFilePath);
      if (lastModifiedTime.equals(m_lastModifiedTime)) {
        return true;
      }
    } catch (IOException e) {
      return false;
    }

    try (FileReader reader = new FileReader(configFilePath.toFile())) {
      m_configData = m_gson.fromJson(reader, ConfigData.class);
      m_lastModifiedTime = lastModifiedTime;
      return true;
    } catch (IOException | JsonParseException e) {
      return false;
    }
  }

  public void SaveConfig() {
    Path configFilePath = Main.GetConfigFilePath();
    Path configDirPath = configFilePath.getParent();

    if (!Files.isDirectory(configDirPath)) {
      try {
        Files.createDirectory(configDirPath);
      } catch (IOException e) {
        Main.LOGGER.error("Failed to create config directory.");
        Main.LOGGER.error(e.getMessage());
      }
    }

    if (!Files.isRegularFile(configFilePath)) {
      try {
        Files.createFile(configFilePath);
      } catch (IOException e) {
        Main.LOGGER.error("Could not create config file.");
        Main.LOGGER.error(e.getMessage());
        return;
      }
    }

    try (FileOutputStream fos = new FileOutputStream(configFilePath.toFile())) {
      fos.write(m_gson.toJson(m_configData).getBytes());
    } catch (IOException e) {
      Main.LOGGER.error("Failed to write config file.");
      Main.LOGGER.error(e.getMessage());
    }
  }

  public void LoadRestoreConfig() {
    if (!ConfigManager.Get().LoadConfig()) {
      ConfigManager.Get().SaveConfig();
    }
  }

  @NotNull
  public ConfigData ReloadAndGetConfig() {
    LoadRestoreConfig();
    return m_configData;
  }

  @NotNull
  public ConfigData GetConfig() {
    return m_configData;
  }

  public void SetConfig(@NotNull ConfigData configData) {
    m_configData = configData;
  }

  public static ConfigManager Get() {
    if (instance != null) {
      return instance;
    }

    synchronized (ConfigManager.class) {
      if (instance == null) {
        instance = new ConfigManager();
      }

      return instance;
    }
  }
}
