package lt.komplektatorius.offer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import lt.komplektatorius.configuration.ConfigurationService;
import lt.komplektatorius.offer.dto.OfferRequest;
import lt.komplektatorius.offer.dto.OfferResponse;
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
@RequestMapping("/api/v1")
public class OfferController {
    private final OfferService service;
    private final ConfigurationService configurations;

    public OfferController(OfferService service, ConfigurationService configurations) {
        this.service = service;
        this.configurations = configurations;
    }

    @Operation(summary = "Gauti komponento pasiūlymus")
    @GetMapping("/components/{componentId}/offers")
    ResponseEntity<PagedModel<EntityModel<OfferResponse>>> list(
            @PathVariable UUID componentId,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) StockStatus stockStatus,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "priceEur") Pageable pageable,
            PagedResourcesAssembler<OfferResponse> assembler) {
        return offerPage(componentId, supplier, stockStatus, minPrice, maxPrice, pageable, assembler);
    }

    @Operation(summary = "Gauti pasiūlymus per visų trijų objektų hierarchiją")
    @GetMapping("/configurations/{configurationId}/components/{componentId}/offers")
    ResponseEntity<PagedModel<EntityModel<OfferResponse>>> hierarchicalList(
            @PathVariable UUID configurationId,
            @PathVariable UUID componentId,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) StockStatus stockStatus,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "priceEur") Pageable pageable,
            PagedResourcesAssembler<OfferResponse> assembler) {
        configurations.assertComponentAccessible(configurationId, componentId);
        return offerPage(componentId, supplier, stockStatus, minPrice, maxPrice, pageable, assembler);
    }

    @Operation(summary = "Gauti vieną pasiūlymą")
    @GetMapping("/offers/{id}")
    ResponseEntity<EntityModel<OfferResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofSeconds(60)).cachePublic())
                .body(model(service.get(id)));
    }

    @Operation(summary = "Sukurti mockintą pasiūlymą", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('EDITOR')")
    @PostMapping("/components/{componentId}/offers")
    ResponseEntity<EntityModel<OfferResponse>> create(@PathVariable UUID componentId,
                                                      @Valid @RequestBody OfferRequest request) {
        EntityModel<OfferResponse> body = model(service.create(componentId, request));
        URI location = body.getRequiredLink("self").toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Operation(summary = "Atnaujinti pasiūlymą", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('EDITOR')")
    @PutMapping("/offers/{id}")
    EntityModel<OfferResponse> update(@PathVariable UUID id, @Valid @RequestBody OfferRequest request) {
        return model(service.update(id, request));
    }

    @Operation(summary = "Ištrinti pasiūlymą", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('EDITOR')")
    @DeleteMapping("/offers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id) { service.delete(id); }

    private ResponseEntity<PagedModel<EntityModel<OfferResponse>>> offerPage(
            UUID componentId, String supplier, StockStatus stockStatus, BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable, PagedResourcesAssembler<OfferResponse> assembler) {
        var body = assembler.toModel(service.list(componentId, supplier, stockStatus, minPrice, maxPrice, pageable),
                this::model);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofSeconds(60)).cachePublic()).body(body);
    }

    private EntityModel<OfferResponse> model(OfferResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/offers/" + response.id()).withSelfRel(),
                Link.of("/api/v1/components/" + response.componentId()).withRel("component"),
                Link.of("/api/v1/components/" + response.componentId() + "/offers").withRel("collection"));
    }
}
