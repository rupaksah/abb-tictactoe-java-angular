package com.tictactoe.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the OpenAPI document's title/description/version. springdoc
 * (added in pom.xml) does the rest automatically: it scans the
 * {@code @RestController} classes and serves the generated spec at
 * {@code /v3/api-docs} and an interactive Swagger UI at
 * {@code /swagger-ui.html}. No hand-written path/schema definitions are
 * needed beyond the {@code @Tag}/{@code @Operation} annotations on the
 * controllers themselves.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ticTacToeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tic Tac Toe API")
                        .description("REST API for a session-based Tic Tac Toe game: game "
                                + "creation, moves, undo, reset, and a session-level scoreboard. "
                                + "See the project README for the full written contract.")
                        .version("v1")
                        .contact(new Contact().name("Rupak Sah")));
    }
}
