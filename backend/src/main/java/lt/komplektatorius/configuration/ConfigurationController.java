package lt.komplektatorius.configuration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lt.komplektatorius.configuration.dto.ConfigurationRequest;
import lt.komplektatorius.configuration.dto.ConfigurationResponse;
import lt.komplektatorius.configuration.dto.ConfigurationSummaryResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/configurations")
public class ConfigurationController {
    private final ConfigurationService service;

    public ConfigurationController(ConfigurationService service) { this.service = service; }

    @Operation(summary = "Gauti matomų komplektacijų sąrašą")
    @GetMapping
    ResponseEntity<PagedModel<EntityModel<ConfigurationResponse>>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Visibility visibility,
            @RequestParam(required = false) UsageType usageType,
            @RequestParam(required = false) String owner,
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable,
            PagedResourcesAssembler<ConfigurationResponse> assembler) {
        if (owner != null && !owner.equalsIgnoreCase("me")) {
            throw new IllegalArgumentException("Leidžiama tik owner=me reikšmė.");
        }
        var page = service.list(q, visibility, usageType, owner != null, pageable);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(assembler.toModel(page, this::model));
    }

    @Operation(summary = "Gauti vieną komplektaciją")
    @GetMapping("/{id}")
    ResponseEntity<EntityModel<ConfigurationResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(model(service.get(id)));
    }

    @Operation(summary = "Sukurti komplektaciją", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping
    ResponseEntity<EntityModel<ConfigurationResponse>> create(@Valid @RequestBody ConfigurationRequest request) {
        EntityModel<ConfigurationResponse> body = model(service.create(request));
        URI location = body.getRequiredLink("self").toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Operation(summary = "Atnaujinti komplektaciją", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('MEMBER')")
    @PutMapping("/{id}")
    EntityModel<ConfigurationResponse> update(@PathVariable UUID id,
                                              @Valid @RequestBody ConfigurationRequest request) {
        return model(service.update(id, request));
    }

    @Operation(summary = "Ištrinti komplektaciją", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('MEMBER')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id) { service.delete(id); }

    @Operation(summary = "Įtraukti katalogo komponentą", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('MEMBER')")
    @PutMapping("/{configurationId}/components/{componentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void attach(@PathVariable UUID configurationId, @PathVariable UUID componentId) {
        service.attachComponent(configurationId, componentId);
    }

    @Operation(summary = "Pašalinti komponentą iš komplektacijos",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('MEMBER')")
    @DeleteMapping("/{configurationId}/components/{componentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void detach(@PathVariable UUID configurationId, @PathVariable UUID componentId) {
        service.detachComponent(configurationId, componentId);
    }

    @Operation(summary = "Gauti iš kelių esybių sudarytą komplektacijos suvestinę")
    @GetMapping("/{id}/summary")
    EntityModel<ConfigurationSummaryResponse> summary(@PathVariable UUID id) {
        ConfigurationSummaryResponse response = service.summary(id);
        return EntityModel.of(response,
                Link.of("/api/v1/configurations/" + id + "/summary").withSelfRel(),
                Link.of("/api/v1/configurations/" + id).withRel("configuration"));
    }

    private EntityModel<ConfigurationResponse> model(ConfigurationResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/configurations/" + response.id()).withSelfRel(),
                Link.of("/api/v1/configurations").withRel("collection"),
                Link.of("/api/v1/configurations/" + response.id() + "/summary").withRel("summary"),
                Link.of("/api/v1/components").withRel("component-catalog"));
    }
}
