package org.example.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppSettings {
    @Value("${server.url}")
    public String serverUrl;
}
