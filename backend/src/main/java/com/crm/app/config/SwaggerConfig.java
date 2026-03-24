package com.crm.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Documentation: CRM-Cross-Industry")
                        .version("1.0")
                        .description("""
                                Documentación de la API de CRM-Cross-Industry.

                                🔗 Repositorio en GitHub: [CRM-Cross-Industry Backend](https://github.com/No-Country-simulation/S03-26-Equipo-19-Web-App-Development)  
                                  Frontend Deploy: [CRM-Cross-Industry Frontend](https://cross-industry.vercel.app/)
                                """)
                        .contact(new Contact()
                                .name("Equipo 9 / CRM-Cross-Industry")
                        )
                )
                // Servidores
                .addServersItem(new Server()
                        .url("https://cross-industry-backend.onrender.com") // URL real de producción
                        .description("Servidor de Producción (Render)"))
                .addServersItem(new Server()
                        .url("http://localhost:8080") // URL de desarrollo local
                        .description("Servidor de Desarrollo Local"))
                // Seguridad JWT
                .addSecurityItem(new SecurityRequirement().addList("TOKEN"))
                .components(new Components()
                        .addSecuritySchemes("TOKEN",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}
