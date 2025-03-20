package com.zhuerta.coremanager.announcements;

import com.zhuerta.coremanager.CoreManager;
import com.zhuerta.coremanager.announcements.config.AnnouncementsConfig;
import com.zhuerta.coremanager.announcements.scheduler.AnnouncementScheduler;

public class AnnouncementsModule {

    private final AnnouncementsConfig config;
    private final AnnouncementScheduler scheduler;

    public AnnouncementsModule(CoreManager plugin) {
        this.config = new AnnouncementsConfig(plugin);
        this.scheduler = new AnnouncementScheduler(plugin, config);
    }

    public void start() {
        scheduler.start();
    }

    public void stop() {
        scheduler.stop();
    }
}