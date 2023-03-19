package jerem.coopcycle.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import jerem.coopcycle.domain.Panier;
import jerem.coopcycle.repository.PanierRepository;
import jerem.coopcycle.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link jerem.coopcycle.domain.Panier}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class PanierResource {

    private final Logger log = LoggerFactory.getLogger(PanierResource.class);

    private static final String ENTITY_NAME = "panier";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PanierRepository panierRepository;

    public PanierResource(PanierRepository panierRepository) {
        this.panierRepository = panierRepository;
    }

    /**
     * {@code POST  /paniers} : Create a new panier.
     *
     * @param panier the panier to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new panier, or with status {@code 400 (Bad Request)} if the panier has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/paniers")
    public Mono<ResponseEntity<Panier>> createPanier(@Valid @RequestBody Panier panier) throws URISyntaxException {
        log.debug("REST request to save Panier : {}", panier);
        if (panier.getId() != null) {
            throw new BadRequestAlertException("A new panier cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return panierRepository
            .save(panier)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/paniers/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /paniers/:id} : Updates an existing panier.
     *
     * @param id the id of the panier to save.
     * @param panier the panier to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated panier,
     * or with status {@code 400 (Bad Request)} if the panier is not valid,
     * or with status {@code 500 (Internal Server Error)} if the panier couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/paniers/{id}")
    public Mono<ResponseEntity<Panier>> updatePanier(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Panier panier
    ) throws URISyntaxException {
        log.debug("REST request to update Panier : {}, {}", id, panier);
        if (panier.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, panier.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return panierRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return panierRepository
                    .save(panier)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /paniers/:id} : Partial updates given fields of an existing panier, field will ignore if it is null
     *
     * @param id the id of the panier to save.
     * @param panier the panier to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated panier,
     * or with status {@code 400 (Bad Request)} if the panier is not valid,
     * or with status {@code 404 (Not Found)} if the panier is not found,
     * or with status {@code 500 (Internal Server Error)} if the panier couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/paniers/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Panier>> partialUpdatePanier(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Panier panier
    ) throws URISyntaxException {
        log.debug("REST request to partial update Panier partially : {}, {}", id, panier);
        if (panier.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, panier.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return panierRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Panier> result = panierRepository
                    .findById(panier.getId())
                    .map(existingPanier -> {
                        if (panier.getDescription() != null) {
                            existingPanier.setDescription(panier.getDescription());
                        }
                        if (panier.getPrice() != null) {
                            existingPanier.setPrice(panier.getPrice());
                        }

                        return existingPanier;
                    })
                    .flatMap(panierRepository::save);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /paniers} : get all the paniers.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of paniers in body.
     */
    @GetMapping("/paniers")
    public Mono<ResponseEntity<List<Panier>>> getAllPaniers(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to get a page of Paniers");
        return panierRepository
            .count()
            .zipWith(panierRepository.findAllBy(pageable).collectList())
            .map(countWithEntities ->
                ResponseEntity
                    .ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            UriComponentsBuilder.fromHttpRequest(request),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2())
            );
    }

    /**
     * {@code GET  /paniers/:id} : get the "id" panier.
     *
     * @param id the id of the panier to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the panier, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/paniers/{id}")
    public Mono<ResponseEntity<Panier>> getPanier(@PathVariable Long id) {
        log.debug("REST request to get Panier : {}", id);
        Mono<Panier> panier = panierRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(panier);
    }

    /**
     * {@code DELETE  /paniers/:id} : delete the "id" panier.
     *
     * @param id the id of the panier to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/paniers/{id}")
    public Mono<ResponseEntity<Void>> deletePanier(@PathVariable Long id) {
        log.debug("REST request to delete Panier : {}", id);
        return panierRepository
            .deleteById(id)
            .then(
                Mono.just(
                    ResponseEntity
                        .noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
