package lt.komplektatorius.component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import lt.komplektatorius.component.dto.ComponentRequest;
import lt.komplektatorius.component.dto.ComponentResponse;
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
@RequestMapping("/api/v1/components")
public class ComponentController {
    private final ComponentService service;

    public ComponentController(ComponentService service) { this.service = service; }

    @Operation(summary = "Gauti komponentų katalogą")
    @GetMapping
    ResponseEntity<PagedModel<EntityModel<ComponentResponse>>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ComponentCategory category,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) Integer minPowerWatts,
            @RequestParam(required = false) Integer maxPowerWatts,
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            PagedResourcesAssembler<ComponentResponse> assembler) {
        var model = assembler.toModel(service.list(q, category, manufacturer, minPowerWatts, maxPowerWatts,
                pageable), this::model);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofSeconds(60)).cachePublic()).body(model);
    }

    @Operation(summary = "Gauti vieną komponentą")
    @GetMapping("/{id}")
    ResponseEntity<EntityModel<ComponentResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofSeconds(60)).cachePublic())
                .body(model(service.get(id)));
    }

    @Operation(summary = "Sukurti katalogo komponentą", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('EDITOR')")
    @PostMapping
    ResponseEntity<EntityModel<ComponentResponse>> create(@Valid @RequestBody ComponentRequest request) {
        EntityModel<ComponentResponse> body = model(service.create(request));
        URI location = body.getRequiredLink("self").toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Operation(summary = "Atnaujinti komponentą", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('EDITOR')")
    @PutMapping("/{id}")
    EntityModel<ComponentResponse> update(@PathVariable UUID id, @Valid @RequestBody ComponentRequest request) {
        return model(service.update(id, request));
    }

    @Operation(summary = "Ištrinti nenaudojamą komponentą", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('EDITOR')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id) { service.delete(id); }

    private EntityModel<ComponentResponse> model(ComponentResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/components/" + response.id()).withSelfRel(),
                Link.of("/api/v1/components").withRel("collection"),
                Link.of("/api/v1/components/" + response.id() + "/offers").withRel("offers"));
    }
}
