package com.zhuerta.coremanager.announcements.scheduler;

import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.proxy.Player;
import com.zhuerta.coremanager.CoreManager;
import com.zhuerta.coremanager.announcements.bossbar.BossBarColor;
import com.zhuerta.coremanager.announcements.bossbar.BossBarStyle;
import com.zhuerta.coremanager.announcements.config.AnnouncementsConfig;
import com.zhuerta.coremanager.announcements.config.AnnouncementsConfig.Announcement;
import com.zhuerta.coremanager.utils.TextProcessor;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AnnouncementScheduler {

    private final CoreManager plugin;
    private final AnnouncementsConfig config;
    private final TextProcessor textProcessor;
    private final Map<String, Long> lastSentTimes; // Referencia al mapa en CoreManager
    private final List<ScheduledTask> tasks = new ArrayList<>();
    private int announcementIndex = 0;

    public AnnouncementScheduler(CoreManager plugin, AnnouncementsConfig config, TextProcessor textProcessor, Map<String, Long> lastSentTimes) {
        this.plugin = plugin;
        this.config = config;
        this.textProcessor = textProcessor;
        this.lastSentTimes = lastSentTimes;
    }

    public void start() {
        announcementIndex = 0;
        for (Announcement announcement : config.getAnnouncements()) {
            scheduleAnnouncement(announcement);
            announcementIndex++;
        }
    }

    private void scheduleAnnouncement(Announcement announcement) {
        long intervalSeconds = parseTimeToSeconds(announcement.getCooldown());
        long initialDelay;

        // Calcular el retraso inicial basado en el tiempo transcurrido desde el último envío
        Long lastSent = lastSentTimes.get(announcement.getId());
        if (lastSent != null) {
            long timeSinceLastSent = (System.currentTimeMillis() - lastSent) / 1000; // Tiempo transcurrido en segundos
            initialDelay = Math.max(0, intervalSeconds - timeSinceLastSent); // Retraso restante
        } else {
            // Si es la primera vez que se programa, usar un retraso inicial escalonado
            initialDelay = announcementIndex * 2L;
        }

        ScheduledTask task = plugin.getServer().getScheduler()
                .buildTask(plugin, () -> {
                    String currentDay = LocalDateTime.now().getDayOfWeek().toString().toLowerCase();
                    String dayInSpanish = translateDayToSpanish(currentDay);
                    if (!announcement.getDays().contains(dayInSpanish)) {
                        return;
                    }

                    if (announcement.getServers().contains("global")) {
                        sendToAllPlayers(announcement);
                    } else {
                        announcement.getServers().forEach(serverName -> {
                            plugin.getServer().getServer(serverName).ifPresent(registeredServer ->
                                    registeredServer.getPlayersConnected().forEach(player ->
                                            sendToPlayer(player, announcement)));
                        });
                    }

                    // Actualizar el tiempo del último envío
                    lastSentTimes.put(announcement.getId(), System.currentTimeMillis());
                })
                .delay(initialDelay, TimeUnit.SECONDS)
                .repeat(intervalSeconds, TimeUnit.SECONDS)
                .schedule();
        tasks.add(task);
    }

    private long parseTimeToSeconds(String timeStr) {
        LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
        return time.toSecondOfDay();
    }

    private String translateDayToSpanish(String day) {
        return switch (day) {
            case "monday" -> "lunes";
            case "tuesday" -> "martes";
            case "wednesday" -> "miercoles";
            case "thursday" -> "jueves";
            case "friday" -> "viernes";
            case "saturday" -> "sabado";
            case "sunday" -> "domingo";
            default -> day;
        };
    }

    private void sendToAllPlayers(Announcement announcement) {
        plugin.getServer().getAllPlayers().forEach(player -> sendToPlayer(player, announcement));
    }

    private void sendToPlayer(Player player, Announcement announcement) {
        // Verificar si el jugador tiene el permiso para ver el anuncio
        if (!player.hasPermission(announcement.getPermission())) {
            return;
        }

        switch (announcement.getType().toUpperCase()) {
            case "TEXTO":
                List<String> lines = announcement.getText();
                lines.forEach(line -> {
                    Component message = textProcessor.processText(line, player);
                    player.sendMessage(message);
                });
                break;
            case "ACTIONBAR":
                List<String> actionbarLines = announcement.getActionbar();
                Component actionbar = textProcessor.processText(actionbarLines.get(0), player);
                player.sendActionBar(actionbar);
                break;
            case "BOSSBAR":
                String bossbarText = announcement.getBossbar().get(0);
                Component bossbarComponent = textProcessor.processText(bossbarText, player);
                BossBarColor color = BossBarColor.parseBossBarColor(announcement.getColor());
                BossBarStyle style = BossBarStyle.parseBossBarStyle(announcement.getStyle());
                BossBar bossBar = BossBar.bossBar(bossbarComponent, 1.0f, color.getColor(), style.getOverlay());
                player.showBossBar(bossBar);

                if (announcement.isProgresoDinamico()) {
                    float progressStep = 1.0f / announcement.getDuration();
                    plugin.getServer().getScheduler().buildTask(plugin, new Runnable() {
                        int timeLeft = announcement.getDuration();
                        @Override
                        public void run() {
                            if (timeLeft > 0) {
                                float progress = Math.max(0.0f, timeLeft * progressStep);
                                bossBar.progress(progress);
                                timeLeft--;
                            } else {
                                player.hideBossBar(bossBar);
                            }
                        }
                    }).repeat(1, TimeUnit.SECONDS).delay(1, TimeUnit.SECONDS).schedule();
                } else {
                    plugin.getServer().getScheduler().buildTask(plugin, () -> player.hideBossBar(bossBar))
                            .delay(announcement.getDuration(), TimeUnit.SECONDS)
                            .schedule();
                }
                break;
            case "TITULOS":
                Component title = textProcessor.processText(announcement.getTitle(), player);
                Component subtitle = textProcessor.processText(announcement.getSubtitle(), player);
                Title titleComponent = Title.title(title, subtitle, Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1)));
                player.showTitle(titleComponent);
                break;
        }
    }

    public void stop() {
        tasks.forEach(ScheduledTask::cancel);
        tasks.clear();
    }
}