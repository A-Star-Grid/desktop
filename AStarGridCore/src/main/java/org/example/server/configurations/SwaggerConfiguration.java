package org.example.server.configurations;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfiguration {
    @Value("${swagger.server.url:#{null}}")
    private String swaggerServerUrl;

    @Value("${server.port}")
    private Integer serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:"+ serverPort.toString()).description("")
                ))
                .info(new Info()
                        .title("AStarGrid Desktop API ")
                        .version("1.0")
                        .description("From AStarGrid Company"));

        // Если серверный URL определён в properties, добавляем его
        if (swaggerServerUrl != null) {
            openAPI.addServersItem(new Server().url(swaggerServerUrl));
        }

        return openAPI;
    }
}