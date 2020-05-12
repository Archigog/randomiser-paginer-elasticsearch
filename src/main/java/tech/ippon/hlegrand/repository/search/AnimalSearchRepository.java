package tech.ippon.hlegrand.repository.search;

import tech.ippon.hlegrand.domain.Animal;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Animal entity.
 */
public interface AnimalSearchRepository extends ElasticsearchRepository<Animal, Long> {
}
