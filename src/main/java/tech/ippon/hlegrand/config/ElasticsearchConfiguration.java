package tech.ippon.hlegrand.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.vanroy.springdata.jest.JestElasticsearchTemplate;
import com.github.vanroy.springdata.jest.mapper.DefaultJestResultsMapper;
import io.searchbox.client.JestClient;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.elasticsearch.jest.JestAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.EntityMapper;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;

import java.io.IOException;

/** The intent of this configuration class is to use the Spring auto-configured ObjectMapper
 *  in JestElasticsearchTemplate, instead of the not pratical new ObjectMapper()
 */
@Configuration
@AutoConfigureBefore(JestAutoConfiguration.class)
public class ElasticsearchConfiguration {
    @Bean
    public EntityMapper entityMapper(ObjectMapper objectMapper) {
        return new EntityMapper() {
            @Override
            public String mapToString(Object object) throws IOException {
                return objectMapper.writeValueAsString(object);
            }

            @Override
            public <T> T mapToObject(String source, Class<T> clazz) throws IOException {
                return objectMapper.readValue(source, clazz);
            }
        };
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate(JestClient jestClient, EntityMapper entityMapper) {
        MappingElasticsearchConverter elasticsearchConverter =
            new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext());
        DefaultJestResultsMapper resultsMapper =
            new DefaultJestResultsMapper(elasticsearchConverter.getMappingContext(), entityMapper);
        return new JestElasticsearchTemplate(jestClient, elasticsearchConverter, resultsMapper);
    }
}
