package tn.esprit.piboursebackend.Player.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Configuration du serveur
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8084");
        localServer.setDescription("Local Development Server");

        // Contact
        Contact contact = new Contact();
        contact.setName("PiBourse Team");
        contact.setEmail("contact@pibourse.tn");

        // License
        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");

        // Informations de l'API
        Info info = new Info()
                .title("PiBourse API - Trading Platform")
                .version("2.0.0")
                .description("Complete REST API for PiBourse trading platform including:\n" +
                        "- Player Management\n" +
                        "- Authentication & Authorization (JWT)\n" +
                        "- Password Reset functionality\n" +
                        "- Portfolio & Order Management")
                .contact(contact)
                .license(license);

        // Schéma de sécurité JWT
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT token authentication");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        // Composants (schémas de sécurité)
        Components components = new Components()
                .addSecuritySchemes("Bearer Authentication", securityScheme);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer))
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}
