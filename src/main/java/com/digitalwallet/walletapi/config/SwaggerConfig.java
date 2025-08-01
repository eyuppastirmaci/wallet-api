package com.digitalwallet.walletapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080" + contextPath)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.digitalwallet.com" + contextPath)
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("Digital Wallet API")
                .description("REST API for Digital Wallet Management System\n\n" +
                           "This API provides endpoints for managing digital wallets, transactions, and customer accounts. " +
                           "It supports multi-currency wallets, secure transactions, and role-based access control.\n\n" +
                           "## Authentication\n" +
                           "- Use `/api/auth/login` to get JWT token\n" +
                           "- Include token in Authorization header as `Bearer <token>`\n\n" +
                           "## User Roles\n" +
                           "- **CUSTOMER**: Can manage own wallets and transactions\n" +
                           "- **EMPLOYEE**: Can manage all customer wallets and approve transactions\n" +
                           "- **ADMIN**: Full system access including administrative functions\n\n" +
                           "## Test Credentials\n" +
                           "- Employee: `username: employee, password: emp123`\n" +
                           "- Customer: `username: 12345678901, password: cust123`\n" +
                           "- Admin: `username: admin, password: admin123`")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Digital Wallet Support")
                        .email("support@digitalwallet.com")
                        .url("https://digitalwallet.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token obtained from /api/auth/login endpoint");
    }
}