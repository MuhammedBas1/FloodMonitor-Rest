package com.example.floodmonitor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI floodMonitorOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("FloodMonitor REST API")
                .version("1.0")
                .description("Hochwasser-Frühwarnsystem: Stationen, Messwerte, Warnungen und Statistiken. "
                        + "Unterstützt JSON und XML über Content Negotiation (Accept-Header)."));
    }
}