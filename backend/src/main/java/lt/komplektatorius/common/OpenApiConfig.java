package lt.komplektatorius.common;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info().title("PC Komplektatorius API").version("1.0.0")
                        .description("Kompiuterių komplektacijų, komponentų ir mockintų pasiūlymų REST API."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                        .addSchemas("ProblemDetail", new ObjectSchema()
                                .addProperty("type", new StringSchema().format("uri")
                                        .example("https://pc-builder.example/problems/400"))
                                .addProperty("title", new StringSchema().example("Netinkami duomenys"))
                                .addProperty("status", new IntegerSchema().example(400))
                                .addProperty("detail", new StringSchema().example("Patikrinkite pažymėtus laukus."))
                                .addProperty("instance", new StringSchema().format("uri").example("/api/v1/components"))
                                .addProperty("errors", new ObjectSchema()
                                        .description("Laukų validacijos klaidos")
                                        .example(Map.of("name", "Pavadinimą turi sudaryti 3–100 simbolių")))));
    }

    @Bean
    OperationCustomizer standardProblemResponses() {
        return (operation, handlerMethod) -> {
            addProblem(operation.getResponses(), "400", "Netinkama užklausa arba validacijos klaida");
            addProblem(operation.getResponses(), "401", "Nėra galiojančio access tokeno");
            addProblem(operation.getResponses(), "403", "Rolė arba savininkas nesuteikia teisės");
            addProblem(operation.getResponses(), "404", "Objektas arba hierarchinis ryšys nerastas");
            addProblem(operation.getResponses(), "409", "Duomenų arba būsenos konfliktas");
            return operation;
        };
    }

    private void addProblem(io.swagger.v3.oas.models.responses.ApiResponses responses,
                            String status, String description) {
        if (!responses.containsKey(status)) {
            responses.addApiResponse(status, new ApiResponse().description(description)
                    .content(new Content().addMediaType("application/problem+json",
                            new io.swagger.v3.oas.models.media.MediaType()
                                    .schema(new Schema<>().$ref("#/components/schemas/ProblemDetail")))));
        }
    }
}
