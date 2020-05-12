package tech.ippon.hlegrand.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of AnimalSearchRepository to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class AnimalSearchRepositoryMockConfiguration {

    @MockBean
    private AnimalSearchRepository mockAnimalSearchRepository;

}
