package com.zhuerta.coremanager.announcements;

import com.zhuerta.coremanager.CoreManager;
import com.zhuerta.coremanager.announcements.config.AnnouncementsConfig;
import com.zhuerta.coremanager.announcements.scheduler.AnnouncementScheduler;
import com.zhuerta.coremanager.utils.TextProcessor;

public class AnnouncementsModule {

    private final CoreManager plugin;
    private final TextProcessor textProcessor;
    private AnnouncementsConfig config;
    private AnnouncementScheduler scheduler;

    public AnnouncementsModule(CoreManager plugin, TextProcessor textProcessor) {
        this.plugin = plugin;
        this.textProcessor = textProcessor;
    }

    public void start() {
        this.config = new AnnouncementsConfig(plugin, textProcessor);
        this.scheduler = new AnnouncementScheduler(plugin, config, textProcessor, plugin.getLastSentTimes());
        this.scheduler.start();
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.stop();
        }
    }

    public AnnouncementsConfig getConfig() {
        return config;
    }
}