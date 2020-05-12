package tech.ippon.hlegrand.web.rest;
import org.springframework.security.access.annotation.Secured;
import tech.ippon.hlegrand.security.AuthoritiesConstants;
import tech.ippon.hlegrand.service.AnimalService;
import tech.ippon.hlegrand.web.rest.errors.BadRequestAlertException;
import tech.ippon.hlegrand.web.rest.util.HeaderUtil;
import tech.ippon.hlegrand.web.rest.util.PaginationUtil;
import tech.ippon.hlegrand.service.dto.AnimalDTO;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Animal.
 */
@RestController
@RequestMapping("/api")
public class AnimalResource {

    private final Logger log = LoggerFactory.getLogger(AnimalResource.class);

    private static final String ENTITY_NAME = "animal";

    private final AnimalService animalService;

    public AnimalResource(AnimalService animalService) {
        this.animalService = animalService;
    }

    /**
     * POST  /animals : Create a new animal.
     *
     * @param animalDTO the animalDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new animalDTO, or with status 400 (Bad Request) if the animal has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/animals")
    public ResponseEntity<AnimalDTO> createAnimal(@Valid @RequestBody AnimalDTO animalDTO) throws URISyntaxException {
        log.debug("REST request to save Animal : {}", animalDTO);
        if (animalDTO.getId() != null) {
            throw new BadRequestAlertException("A new animal cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AnimalDTO result = animalService.save(animalDTO);
        return ResponseEntity.created(new URI("/api/animals/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /animals : Updates an existing animal.
     *
     * @param animalDTO the animalDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated animalDTO,
     * or with status 400 (Bad Request) if the animalDTO is not valid,
     * or with status 500 (Internal Server Error) if the animalDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/animals")
    public ResponseEntity<AnimalDTO> updateAnimal(@Valid @RequestBody AnimalDTO animalDTO) throws URISyntaxException {
        log.debug("REST request to update Animal : {}", animalDTO);
        if (animalDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AnimalDTO result = animalService.save(animalDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, animalDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /animals : get all the animals.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of animals in body
     */
    @GetMapping("/animals")
    public ResponseEntity<List<AnimalDTO>> getAllAnimals(Pageable pageable) {
        log.debug("REST request to get a page of Animals");
        Page<AnimalDTO> page = animalService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/animals");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * GET  /animals/:id : get the "id" animal.
     *
     * @param id the id of the animalDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the animalDTO, or with status 404 (Not Found)
     */
    @GetMapping("/animals/{id}")
    public ResponseEntity<AnimalDTO> getAnimal(@PathVariable Long id) {
        log.debug("REST request to get Animal : {}", id);
        Optional<AnimalDTO> animalDTO = animalService.findOne(id);
        return ResponseUtil.wrapOrNotFound(animalDTO);
    }

    /**
     * DELETE  /animals/:id : delete the "id" animal.
     *
     * @param id the id of the animalDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/animals/{id}")
    public ResponseEntity<Void> deleteAnimal(@PathVariable Long id) {
        log.debug("REST request to delete Animal : {}", id);
        animalService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/animals?query=:query : search for the animal corresponding
     * to the query.
     *
     * @param query the query of the animal search
     * @param pageable the pagination information
     * @return the result of the search
     */
    @GetMapping("/_search/animals")
    public ResponseEntity<List<AnimalDTO>> searchAnimals(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of Animals for query {}", query);
        Page<AnimalDTO> page = animalService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/animals");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PostMapping("/_search/animals")
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<Void> reindexAnimals() {
        log.debug("REST request to reindex all Animal in Elasticsearch");
        animalService.reindexAll();
        return ResponseEntity.ok().build();
    }
}
