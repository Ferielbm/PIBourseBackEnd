package tn.esprit.piboursebackend.Player.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8084");
        localServer.setDescription("Local Development Server");

        Contact contact = new Contact();
        contact.setName("PiBourse Team");
        contact.setEmail("contact@pibourse.tn");

        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");

        Info info = new Info()
                .title("PiBourse Player & Transaction Management API")
                .version("1.0.0")
                .description("Complete REST API for managing players and transactions in the PiBourse trading platform. " +
                        "This API provides full CRUD operations for player management and transaction tracking.")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
