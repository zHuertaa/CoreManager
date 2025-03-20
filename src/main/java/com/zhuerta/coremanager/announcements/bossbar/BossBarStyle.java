package com.zhuerta.coremanager.announcements.bossbar;

import net.kyori.adventure.bossbar.BossBar;

public enum BossBarStyle {
    PROGRESS(BossBar.Overlay.PROGRESS),
    NOTCHED_6(BossBar.Overlay.NOTCHED_6),
    NOTCHED_10(BossBar.Overlay.NOTCHED_10),
    NOTCHED_12(BossBar.Overlay.NOTCHED_12),
    NOTCHED_20(BossBar.Overlay.NOTCHED_20);

    private final BossBar.Overlay overlay;

    BossBarStyle(BossBar.Overlay overlay) {
        this.overlay = overlay;
    }

    public BossBar.Overlay getOverlay() {
        return overlay;
    }

    public static BossBarStyle parseBossBarStyle(String styleStr) {
        try {
            return valueOf(styleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Estilo inválido para boss bar: " + styleStr + ". Usando NOTCHED_10 por defecto.");
            return NOTCHED_10;
        }
    }
}