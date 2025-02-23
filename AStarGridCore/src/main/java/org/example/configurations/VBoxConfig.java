package org.example.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VBoxConfig {
    @Value("${vbox.path}")
    public String vboxPath;

    @Value("${vbox.default.image.path}")
    public String defaultImagePath;
}
