package tech.ippon.hlegrand.service;

import com.github.vanroy.springdata.jest.JestElasticsearchTemplate;
import com.github.vanroy.springdata.jest.mapper.JestResultsExtractor;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.ippon.hlegrand.domain.Animal;
import tech.ippon.hlegrand.repository.AnimalRepository;
import tech.ippon.hlegrand.repository.search.AnimalSearchRepository;
import tech.ippon.hlegrand.service.dto.AnimalDTO;
import tech.ippon.hlegrand.service.mapper.AnimalMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.matchPhrasePrefixQuery;

/**
 * Service Implementation for managing Animal.
 */
@Service
@Transactional
public class AnimalService {

    private static final Integer PHRASE_PREFIX_NAME_SLOP = 3;

    private final Logger log = LoggerFactory.getLogger(AnimalService.class);

    private final AnimalRepository animalRepository;

    private final AnimalMapper animalMapper;

    private final AnimalSearchRepository animalSearchRepository;

    private final JestElasticsearchTemplate elasticsearchTemplate;

    public AnimalService(
        AnimalRepository animalRepository,
        AnimalMapper animalMapper,
        AnimalSearchRepository animalSearchRepository,
        JestElasticsearchTemplate elasticsearchTemplate
    ) {
        this.animalRepository = animalRepository;
        this.animalMapper = animalMapper;
        this.animalSearchRepository = animalSearchRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
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
        animals.forEach(animalSearchRepository::save);
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
     * @param seed
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<AnimalDTO> search(String query, String seed, Pageable pageable) {
        log.debug("Request to search for a page of Animals for query {}", query);
        final Pageable pageableWithoutSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return this.getSearchResults(query, seed, pageableWithoutSort);
    }

    private Page<AnimalDTO> getSearchResults(String term, String seed, Pageable pageable) {
        final String nameField = "name";
        final SortBuilder sort;

        String scriptString = "" +
            // Taking a random base value with the salt plus the doc id as seed
            "double score = Math.abs(new Random(params.seed + Integer.parseInt(doc['_id'].value)).nextInt() % 100);\n" +
            "score += params.seed;\n" +
            "if (doc['promoted'].value) {\n" +
            "  score += 10 * params.seed;\n" +
            "}\n" +
            "return score;";
        Map<String, Object> params = new HashMap<>();
        // Getting a minimum salt of 100,000 to make sure IDs don't go over it
        params.put("seed", Math.abs(seed.hashCode() % 100_000) + 100_000);
        Script script = new Script(ScriptType.INLINE, "painless", scriptString, params);
        sort = SortBuilders.scriptSort(script, ScriptSortBuilder.ScriptSortType.STRING).order(SortOrder.DESC);

        QueryBuilder query = matchPhrasePrefixQuery(nameField, term)
            .slop(PHRASE_PREFIX_NAME_SLOP);

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withQuery(query)
            .withSort(sort)
            .withPageable(pageable)
            .withIndices("animal")
            .build();

        return elasticsearchTemplate.query(searchQuery, extractResults(pageable));
    }

    private JestResultsExtractor<Page<AnimalDTO>> extractResults(Pageable pageable) {
        return response -> {
            List<AnimalDTO> results = response.getHits(Animal.class)
                .stream()
                .map(hit -> {
                    if (hit.type.equalsIgnoreCase(Animal.class.getSimpleName())) {
                        return animalMapper.toDTOWithScore(hit.source, Double.parseDouble(hit.sort.get(0)));
                    }
                    return null;
                })
                .collect(Collectors.toList());
            return new PageImpl<>(results, pageable, response.getTotal());
        };
    }
}
