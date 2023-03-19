package jerem.coopcycle.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import jerem.coopcycle.domain.Restaurant;
import jerem.coopcycle.repository.RestaurantRepository;
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
 * REST controller for managing {@link jerem.coopcycle.domain.Restaurant}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class RestaurantResource {

    private final Logger log = LoggerFactory.getLogger(RestaurantResource.class);

    private static final String ENTITY_NAME = "restaurant";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RestaurantRepository restaurantRepository;

    public RestaurantResource(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * {@code POST  /restaurants} : Create a new restaurant.
     *
     * @param restaurant the restaurant to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new restaurant, or with status {@code 400 (Bad Request)} if the restaurant has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/restaurants")
    public Mono<ResponseEntity<Restaurant>> createRestaurant(@Valid @RequestBody Restaurant restaurant) throws URISyntaxException {
        log.debug("REST request to save Restaurant : {}", restaurant);
        if (restaurant.getId() != null) {
            throw new BadRequestAlertException("A new restaurant cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return restaurantRepository
            .save(restaurant)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/restaurants/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /restaurants/:id} : Updates an existing restaurant.
     *
     * @param id the id of the restaurant to save.
     * @param restaurant the restaurant to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated restaurant,
     * or with status {@code 400 (Bad Request)} if the restaurant is not valid,
     * or with status {@code 500 (Internal Server Error)} if the restaurant couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/restaurants/{id}")
    public Mono<ResponseEntity<Restaurant>> updateRestaurant(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Restaurant restaurant
    ) throws URISyntaxException {
        log.debug("REST request to update Restaurant : {}, {}", id, restaurant);
        if (restaurant.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, restaurant.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return restaurantRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return restaurantRepository
                    .save(restaurant)
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
     * {@code PATCH  /restaurants/:id} : Partial updates given fields of an existing restaurant, field will ignore if it is null
     *
     * @param id the id of the restaurant to save.
     * @param restaurant the restaurant to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated restaurant,
     * or with status {@code 400 (Bad Request)} if the restaurant is not valid,
     * or with status {@code 404 (Not Found)} if the restaurant is not found,
     * or with status {@code 500 (Internal Server Error)} if the restaurant couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/restaurants/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Restaurant>> partialUpdateRestaurant(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Restaurant restaurant
    ) throws URISyntaxException {
        log.debug("REST request to partial update Restaurant partially : {}, {}", id, restaurant);
        if (restaurant.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, restaurant.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return restaurantRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Restaurant> result = restaurantRepository
                    .findById(restaurant.getId())
                    .map(existingRestaurant -> {
                        if (restaurant.getName() != null) {
                            existingRestaurant.setName(restaurant.getName());
                        }
                        if (restaurant.getAddress() != null) {
                            existingRestaurant.setAddress(restaurant.getAddress());
                        }
                        if (restaurant.getMenu() != null) {
                            existingRestaurant.setMenu(restaurant.getMenu());
                        }

                        return existingRestaurant;
                    })
                    .flatMap(restaurantRepository::save);

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
     * {@code GET  /restaurants} : get all the restaurants.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of restaurants in body.
     */
    @GetMapping("/restaurants")
    public Mono<ResponseEntity<List<Restaurant>>> getAllRestaurants(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to get a page of Restaurants");
        return restaurantRepository
            .count()
            .zipWith(restaurantRepository.findAllBy(pageable).collectList())
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
     * {@code GET  /restaurants/:id} : get the "id" restaurant.
     *
     * @param id the id of the restaurant to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the restaurant, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/restaurants/{id}")
    public Mono<ResponseEntity<Restaurant>> getRestaurant(@PathVariable Long id) {
        log.debug("REST request to get Restaurant : {}", id);
        Mono<Restaurant> restaurant = restaurantRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(restaurant);
    }

    /**
     * {@code DELETE  /restaurants/:id} : delete the "id" restaurant.
     *
     * @param id the id of the restaurant to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/restaurants/{id}")
    public Mono<ResponseEntity<Void>> deleteRestaurant(@PathVariable Long id) {
        log.debug("REST request to delete Restaurant : {}", id);
        return restaurantRepository
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
