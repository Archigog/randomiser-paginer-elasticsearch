package tech.ippon.hlegrand.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.ippon.hlegrand.domain.Animal;
import tech.ippon.hlegrand.repository.AnimalRepository;
import tech.ippon.hlegrand.repository.search.AnimalSearchRepository;
import tech.ippon.hlegrand.service.dto.AnimalDTO;
import tech.ippon.hlegrand.service.mapper.AnimalMapper;

import java.util.List;
import java.util.Optional;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing Animal.
 */
@Service
@Transactional
public class AnimalService {

    private final Logger log = LoggerFactory.getLogger(AnimalService.class);

    private final AnimalRepository animalRepository;

    private final AnimalMapper animalMapper;

    private final AnimalSearchRepository animalSearchRepository;

    public AnimalService(AnimalRepository animalRepository, AnimalMapper animalMapper, AnimalSearchRepository animalSearchRepository) {
        this.animalRepository = animalRepository;
        this.animalMapper = animalMapper;
        this.animalSearchRepository = animalSearchRepository;
    }

    /**
     * Save a animal.
     *
     * @param animalDTO the entity to save
     * @return the persisted entity
     */
    public AnimalDTO save(AnimalDTO animalDTO) {
        log.debug("Request to save Animal : {}", animalDTO);
        Animal animal = animalMapper.toEntity(animalDTO);
        animal = animalRepository.save(animal);
        AnimalDTO result = animalMapper.toDto(animal);
        animalSearchRepository.save(animal);
        return result;
    }

    public void reindexAll() {
        log.debug("Request to reindex all Animals in Elasticsearch");
        List<Animal> animals = animalRepository.findAll();
        animalSearchRepository.saveAll(animals);
    }

    /**
     * Get all the animals.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<AnimalDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Animals");
        return animalRepository.findAll(pageable)
            .map(animalMapper::toDto);
    }


    /**
     * Get one animal by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<AnimalDTO> findOne(Long id) {
        log.debug("Request to get Animal : {}", id);
        return animalRepository.findById(id)
            .map(animalMapper::toDto);
    }

    /**
     * Delete the animal by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Animal : {}", id);
        animalRepository.deleteById(id);
        animalSearchRepository.deleteById(id);
    }

    /**
     * Search for the animal corresponding to the query.
     *
     * @param query    the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<AnimalDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Animals for query {}", query);
        return animalSearchRepository.search(queryStringQuery(query), pageable)
            .map(animalMapper::toDto);
    }
}
