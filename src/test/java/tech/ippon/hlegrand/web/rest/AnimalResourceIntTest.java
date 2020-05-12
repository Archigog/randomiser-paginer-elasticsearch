package tech.ippon.hlegrand.web.rest;

import tech.ippon.hlegrand.RandomizePaginateElasticsearchApp;

import tech.ippon.hlegrand.domain.Animal;
import tech.ippon.hlegrand.repository.AnimalRepository;
import tech.ippon.hlegrand.repository.search.AnimalSearchRepository;
import tech.ippon.hlegrand.service.AnimalService;
import tech.ippon.hlegrand.service.dto.AnimalDTO;
import tech.ippon.hlegrand.service.mapper.AnimalMapper;
import tech.ippon.hlegrand.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;


import static tech.ippon.hlegrand.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the AnimalResource REST controller.
 *
 * @see AnimalResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RandomizePaginateElasticsearchApp.class)
public class AnimalResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_PROMOTED = false;
    private static final Boolean UPDATED_PROMOTED = true;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private AnimalMapper animalMapper;

    @Autowired
    private AnimalService animalService;

    /**
     * This repository is mocked in the tech.ippon.hlegrand.repository.search test package.
     *
     * @see tech.ippon.hlegrand.repository.search.AnimalSearchRepositoryMockConfiguration
     */
    @Autowired
    private AnimalSearchRepository mockAnimalSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restAnimalMockMvc;

    private Animal animal;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final AnimalResource animalResource = new AnimalResource(animalService);
        this.restAnimalMockMvc = MockMvcBuilders.standaloneSetup(animalResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Animal createEntity(EntityManager em) {
        Animal animal = new Animal()
            .name(DEFAULT_NAME)
            .promoted(DEFAULT_PROMOTED);
        return animal;
    }

    @Before
    public void initTest() {
        animal = createEntity(em);
    }

    @Test
    @Transactional
    public void createAnimal() throws Exception {
        int databaseSizeBeforeCreate = animalRepository.findAll().size();

        // Create the Animal
        AnimalDTO animalDTO = animalMapper.toDto(animal);
        restAnimalMockMvc.perform(post("/api/animals")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(animalDTO)))
            .andExpect(status().isCreated());

        // Validate the Animal in the database
        List<Animal> animalList = animalRepository.findAll();
        assertThat(animalList).hasSize(databaseSizeBeforeCreate + 1);
        Animal testAnimal = animalList.get(animalList.size() - 1);
        assertThat(testAnimal.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAnimal.isPromoted()).isEqualTo(DEFAULT_PROMOTED);

        // Validate the Animal in Elasticsearch
        verify(mockAnimalSearchRepository, times(1)).save(testAnimal);
    }

    @Test
    @Transactional
    public void createAnimalWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = animalRepository.findAll().size();

        // Create the Animal with an existing ID
        animal.setId(1L);
        AnimalDTO animalDTO = animalMapper.toDto(animal);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAnimalMockMvc.perform(post("/api/animals")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(animalDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Animal in the database
        List<Animal> animalList = animalRepository.findAll();
        assertThat(animalList).hasSize(databaseSizeBeforeCreate);

        // Validate the Animal in Elasticsearch
        verify(mockAnimalSearchRepository, times(0)).save(animal);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = animalRepository.findAll().size();
        // set the field null
        animal.setName(null);

        // Create the Animal, which fails.
        AnimalDTO animalDTO = animalMapper.toDto(animal);

        restAnimalMockMvc.perform(post("/api/animals")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(animalDTO)))
            .andExpect(status().isBadRequest());

        List<Animal> animalList = animalRepository.findAll();
        assertThat(animalList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllAnimals() throws Exception {
        // Initialize the database
        animalRepository.saveAndFlush(animal);

        // Get all the animalList
        restAnimalMockMvc.perform(get("/api/animals?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(animal.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].promoted").value(hasItem(DEFAULT_PROMOTED.booleanValue())));
    }
    
    @Test
    @Transactional
    public void getAnimal() throws Exception {
        // Initialize the database
        animalRepository.saveAndFlush(animal);

        // Get the animal
        restAnimalMockMvc.perform(get("/api/animals/{id}", animal.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(animal.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.promoted").value(DEFAULT_PROMOTED.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingAnimal() throws Exception {
        // Get the animal
        restAnimalMockMvc.perform(get("/api/animals/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAnimal() throws Exception {
        // Initialize the database
        animalRepository.saveAndFlush(animal);

        int databaseSizeBeforeUpdate = animalRepository.findAll().size();

        // Update the animal
        Animal updatedAnimal = animalRepository.findById(animal.getId()).get();
        // Disconnect from session so that the updates on updatedAnimal are not directly saved in db
        em.detach(updatedAnimal);
        updatedAnimal
            .name(UPDATED_NAME)
            .promoted(UPDATED_PROMOTED);
        AnimalDTO animalDTO = animalMapper.toDto(updatedAnimal);

        restAnimalMockMvc.perform(put("/api/animals")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(animalDTO)))
            .andExpect(status().isOk());

        // Validate the Animal in the database
        List<Animal> animalList = animalRepository.findAll();
        assertThat(animalList).hasSize(databaseSizeBeforeUpdate);
        Animal testAnimal = animalList.get(animalList.size() - 1);
        assertThat(testAnimal.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAnimal.isPromoted()).isEqualTo(UPDATED_PROMOTED);

        // Validate the Animal in Elasticsearch
        verify(mockAnimalSearchRepository, times(1)).save(testAnimal);
    }

    @Test
    @Transactional
    public void updateNonExistingAnimal() throws Exception {
        int databaseSizeBeforeUpdate = animalRepository.findAll().size();

        // Create the Animal
        AnimalDTO animalDTO = animalMapper.toDto(animal);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAnimalMockMvc.perform(put("/api/animals")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(animalDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Animal in the database
        List<Animal> animalList = animalRepository.findAll();
        assertThat(animalList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Animal in Elasticsearch
        verify(mockAnimalSearchRepository, times(0)).save(animal);
    }

    @Test
    @Transactional
    public void deleteAnimal() throws Exception {
        // Initialize the database
        animalRepository.saveAndFlush(animal);

        int databaseSizeBeforeDelete = animalRepository.findAll().size();

        // Delete the animal
        restAnimalMockMvc.perform(delete("/api/animals/{id}", animal.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Animal> animalList = animalRepository.findAll();
        assertThat(animalList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Animal in Elasticsearch
        verify(mockAnimalSearchRepository, times(1)).deleteById(animal.getId());
    }

    @Test
    @Transactional
    public void searchAnimal() throws Exception {
        // Initialize the database
        animalRepository.saveAndFlush(animal);
        when(mockAnimalSearchRepository.search(queryStringQuery("id:" + animal.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(animal), PageRequest.of(0, 1), 1));
        // Search the animal
        restAnimalMockMvc.perform(get("/api/_search/animals?query=id:" + animal.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(animal.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].promoted").value(hasItem(DEFAULT_PROMOTED.booleanValue())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Animal.class);
        Animal animal1 = new Animal();
        animal1.setId(1L);
        Animal animal2 = new Animal();
        animal2.setId(animal1.getId());
        assertThat(animal1).isEqualTo(animal2);
        animal2.setId(2L);
        assertThat(animal1).isNotEqualTo(animal2);
        animal1.setId(null);
        assertThat(animal1).isNotEqualTo(animal2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AnimalDTO.class);
        AnimalDTO animalDTO1 = new AnimalDTO();
        animalDTO1.setId(1L);
        AnimalDTO animalDTO2 = new AnimalDTO();
        assertThat(animalDTO1).isNotEqualTo(animalDTO2);
        animalDTO2.setId(animalDTO1.getId());
        assertThat(animalDTO1).isEqualTo(animalDTO2);
        animalDTO2.setId(2L);
        assertThat(animalDTO1).isNotEqualTo(animalDTO2);
        animalDTO1.setId(null);
        assertThat(animalDTO1).isNotEqualTo(animalDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(animalMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(animalMapper.fromId(null)).isNull();
    }
}
