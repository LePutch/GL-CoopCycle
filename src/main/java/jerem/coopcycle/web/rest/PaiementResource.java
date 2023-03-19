package jerem.coopcycle.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import jerem.coopcycle.domain.Paiement;
import jerem.coopcycle.repository.PaiementRepository;
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
 * REST controller for managing {@link jerem.coopcycle.domain.Paiement}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class PaiementResource {

    private final Logger log = LoggerFactory.getLogger(PaiementResource.class);

    private static final String ENTITY_NAME = "paiement";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PaiementRepository paiementRepository;

    public PaiementResource(PaiementRepository paiementRepository) {
        this.paiementRepository = paiementRepository;
    }

    /**
     * {@code POST  /paiements} : Create a new paiement.
     *
     * @param paiement the paiement to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new paiement, or with status {@code 400 (Bad Request)} if the paiement has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/paiements")
    public Mono<ResponseEntity<Paiement>> createPaiement(@Valid @RequestBody Paiement paiement) throws URISyntaxException {
        log.debug("REST request to save Paiement : {}", paiement);
        if (paiement.getId() != null) {
            throw new BadRequestAlertException("A new paiement cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return paiementRepository
            .save(paiement)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/paiements/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /paiements/:id} : Updates an existing paiement.
     *
     * @param id the id of the paiement to save.
     * @param paiement the paiement to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated paiement,
     * or with status {@code 400 (Bad Request)} if the paiement is not valid,
     * or with status {@code 500 (Internal Server Error)} if the paiement couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/paiements/{id}")
    public Mono<ResponseEntity<Paiement>> updatePaiement(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Paiement paiement
    ) throws URISyntaxException {
        log.debug("REST request to update Paiement : {}, {}", id, paiement);
        if (paiement.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, paiement.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return paiementRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return paiementRepository
                    .save(paiement)
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
     * {@code PATCH  /paiements/:id} : Partial updates given fields of an existing paiement, field will ignore if it is null
     *
     * @param id the id of the paiement to save.
     * @param paiement the paiement to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated paiement,
     * or with status {@code 400 (Bad Request)} if the paiement is not valid,
     * or with status {@code 404 (Not Found)} if the paiement is not found,
     * or with status {@code 500 (Internal Server Error)} if the paiement couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/paiements/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Paiement>> partialUpdatePaiement(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Paiement paiement
    ) throws URISyntaxException {
        log.debug("REST request to partial update Paiement partially : {}, {}", id, paiement);
        if (paiement.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, paiement.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return paiementRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Paiement> result = paiementRepository
                    .findById(paiement.getId())
                    .map(existingPaiement -> {
                        if (paiement.getAmount() != null) {
                            existingPaiement.setAmount(paiement.getAmount());
                        }
                        if (paiement.getPaymentType() != null) {
                            existingPaiement.setPaymentType(paiement.getPaymentType());
                        }

                        return existingPaiement;
                    })
                    .flatMap(paiementRepository::save);

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
     * {@code GET  /paiements} : get all the paiements.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of paiements in body.
     */
    @GetMapping("/paiements")
    public Mono<ResponseEntity<List<Paiement>>> getAllPaiements(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to get a page of Paiements");
        return paiementRepository
            .count()
            .zipWith(paiementRepository.findAllBy(pageable).collectList())
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
     * {@code GET  /paiements/:id} : get the "id" paiement.
     *
     * @param id the id of the paiement to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the paiement, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/paiements/{id}")
    public Mono<ResponseEntity<Paiement>> getPaiement(@PathVariable Long id) {
        log.debug("REST request to get Paiement : {}", id);
        Mono<Paiement> paiement = paiementRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(paiement);
    }

    /**
     * {@code DELETE  /paiements/:id} : delete the "id" paiement.
     *
     * @param id the id of the paiement to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/paiements/{id}")
    public Mono<ResponseEntity<Void>> deletePaiement(@PathVariable Long id) {
        log.debug("REST request to delete Paiement : {}", id);
        return paiementRepository
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
