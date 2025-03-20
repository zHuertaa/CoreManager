package com.zhuerta.coremanager.announcements.config;

import com.zhuerta.coremanager.CoreManager;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import net.kyori.adventure.text.Component; // Import añadido

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnnouncementsConfig {

    private final CoreManager plugin;
    private ConfigurationNode config;
    private final Path configPath;
    private List<Announcement> announcements;
    private String globalPermission;

    public AnnouncementsConfig(CoreManager plugin) {
        this.plugin = plugin;
        this.configPath = new File("plugins/CoreManager/anuncios.yml").toPath();
        this.announcements = new ArrayList<>();
        loadConfig();
        loadAnnouncements();
    }

    private void loadConfig() {
        try {
            if (!Files.exists(configPath.getParent())) {
                Files.createDirectories(configPath.getParent());
            }
            if (!Files.exists(configPath)) {
                // Intentar copiar el archivo desde los recursos
                try (InputStream inputStream = getClass().getResourceAsStream("/anuncios.yml")) {
                    if (inputStream != null) {
                        Files.copy(inputStream, configPath);
                    } else {
                        // Si no se encuentra en los recursos, crear un archivo vacío
                        Files.createFile(configPath);
                        plugin.getServer().getConsoleCommandSource().sendMessage(
                            Component.text("No se encontró anuncios.yml en los recursos. Se creó un archivo vacío en " + configPath)
                        );
                    }
                }
            }
            YAMLConfigurationLoader loader = YAMLConfigurationLoader.builder()
                    .setPath(configPath)
                    .build();
            this.config = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAnnouncements() {
        ConfigurationNode anunciosNode = config.getNode("anuncios");
        if (anunciosNode.isEmpty()) return;

        // Leer el permiso global
        this.globalPermission = anunciosNode.getNode("permiso").getString("coremanager.anuncios.view");

        for (Map.Entry<Object, ? extends ConfigurationNode> entry : anunciosNode.getChildrenMap().entrySet()) {
            String id = entry.getKey().toString();
            // Saltar el nodo "permiso" ya que no es un anuncio
            if (id.equals("permiso")) continue;

            ConfigurationNode announcementNode = entry.getValue();

            List<String> servers = announcementNode.getNode("servidores").getList(String::valueOf, new ArrayList<>());
            ConfigurationNode intervaloNode = announcementNode.getNode("intervalo");
            List<String> days = intervaloNode.getNode("dias").getList(String::valueOf, new ArrayList<>());
            String cooldown = intervaloNode.getNode("cooldown").getString("00:00:00");
            String type = announcementNode.getNode("tipo").getString("TEXTO");

            Announcement announcement = new Announcement(id, servers, days, cooldown, type, globalPermission);

            // Cargar datos específicos según el tipo
            if ("TEXTO".equalsIgnoreCase(type)) {
                announcement.setText(announcementNode.getNode("texto").getList(String::valueOf, new ArrayList<>()));
            } else if ("ACTIONBAR".equalsIgnoreCase(type)) {
                announcement.setActionbar(announcementNode.getNode("actionbar").getList(String::valueOf, new ArrayList<>()));
            } else if ("BOSSBAR".equalsIgnoreCase(type)) {
                announcement.setBossbar(announcementNode.getNode("bossbar").getList(String::valueOf, new ArrayList<>()));
                announcement.setDuration(announcementNode.getNode("duracion").getInt(5));
                announcement.setProgresoDinamico(announcementNode.getNode("progreso-dinamico").getBoolean(false));
                announcement.setColor(announcementNode.getNode("color").getString("YELLOW"));
                announcement.setStyle(announcementNode.getNode("estilo").getString("NOTCHED_10"));
            } else if ("TITULOS".equalsIgnoreCase(type)) {
                ConfigurationNode titulosNode = announcementNode.getNode("titulos");
                announcement.setTitle(titulosNode.getNode("titulo").getString(""));
                announcement.setSubtitle(titulosNode.getNode("subtitulo").getString(""));
            }

            announcements.add(announcement);
        }
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }

    public String getGlobalPermission() {
        return globalPermission;
    }

    // Clase interna para representar un anuncio
    public static class Announcement {
        private final String id;
        private final List<String> servers;
        private final List<String> days;
        private final String cooldown;
        private final String type;
        private final String permission;
        private List<String> text;
        private List<String> actionbar;
        private List<String> bossbar;
        private int duration;
        private boolean progresoDinamico;
        private String color;
        private String style;
        private String title;
        private String subtitle;

        public Announcement(String id, List<String> servers, List<String> days, String cooldown, String type, String permission) {
            this.id = id;
            this.servers = servers;
            this.days = days;
            this.cooldown = cooldown;
            this.type = type;
            this.permission = permission;
        }

        // Getters
        public String getId() { return id; }
        public List<String> getServers() { return servers; }
        public List<String> getDays() { return days; }
        public String getCooldown() { return cooldown; }
        public String getType() { return type; }
        public String getPermission() { return permission; }
        public List<String> getText() { return text; }
        public List<String> getActionbar() { return actionbar; }
        public List<String> getBossbar() { return bossbar; }
        public int getDuration() { return duration; }
        public boolean isProgresoDinamico() { return progresoDinamico; }
        public String getColor() { return color; }
        public String getStyle() { return style; }
        public String getTitle() { return title; }
        public String getSubtitle() { return subtitle; }

        // Setters
        public void setText(List<String> text) { this.text = text; }
        public void setActionbar(List<String> actionbar) { this.actionbar = actionbar; }
        public void setBossbar(List<String> bossbar) { this.bossbar = bossbar; }
        public void setDuration(int duration) { this.duration = duration; }
        public void setProgresoDinamico(boolean progresoDinamico) { this.progresoDinamico = progresoDinamico; }
        public void setColor(String color) { this.color = color; }
        public void setStyle(String style) { this.style = style; }
        public void setTitle(String title) { this.title = title; }
        public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    }
}