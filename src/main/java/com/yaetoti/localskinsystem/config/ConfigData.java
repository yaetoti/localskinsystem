package com.yaetoti.localskinsystem.config;

public record ConfigData(boolean isSlimModel) {
  public static final ConfigData DEFAULT = new ConfigData(false);
}
