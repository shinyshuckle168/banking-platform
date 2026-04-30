package com.group1.banking.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        Server cloudRun = new Server();
        cloudRun.setUrl("https://digital-banking-service-524103119199.northamerica-northeast1.run.app");
        cloudRun.setDescription("Cloud Run HTTPS Server");

        Server kubernetes = new Server();
        kubernetes.setUrl("http://34.72.76.94");
        kubernetes.setDescription("Kubernetes LoadBalancer");

        return new OpenAPI()
                .servers(List.of(cloudRun, kubernetes));
    }
}