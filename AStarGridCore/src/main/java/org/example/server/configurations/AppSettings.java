package org.example.server.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppSettings {
    @Value("${server.url}")
    public String serverUrl;

    @Value("${compute.task.archives.directory}")
    public String taskArchivesDirectory; // Загружаем значение из конфига

    @Value("${compute.process.interval}")
    public static long interval; // Загружаем значение из конфига
}
