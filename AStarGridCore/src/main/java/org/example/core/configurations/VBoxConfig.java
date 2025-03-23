package org.example.core.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class VBoxConfig {
    @Value("${vbox.default.image.path}")
    public String defaultImagePath;
    @Value("${vbox.shared.folder.name}")
    public String sharedFolder;

    public String getDefaultImagePath() {
        return Path.of("app", defaultImagePath).toString();
    }
}
