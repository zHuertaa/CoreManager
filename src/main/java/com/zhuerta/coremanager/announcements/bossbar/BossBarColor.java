package com.zhuerta.coremanager.announcements.bossbar;

import net.kyori.adventure.bossbar.BossBar;

public enum BossBarColor {
    PINK(BossBar.Color.PINK),
    BLUE(BossBar.Color.BLUE),
    RED(BossBar.Color.RED),
    GREEN(BossBar.Color.GREEN),
    YELLOW(BossBar.Color.YELLOW),
    PURPLE(BossBar.Color.PURPLE),
    WHITE(BossBar.Color.WHITE);

    private final BossBar.Color color;

    BossBarColor(BossBar.Color color) {
        this.color = color;
    }

    public BossBar.Color getColor() {
        return color;
    }

    public static BossBarColor parseBossBarColor(String colorStr) {
        try {
            return valueOf(colorStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Color inválido para boss bar: " + colorStr + ". Usando YELLOW por defecto.");
            return YELLOW;
        }
    }
}