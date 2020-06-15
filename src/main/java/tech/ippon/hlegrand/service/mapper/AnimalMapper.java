package tech.ippon.hlegrand.service.mapper;

import tech.ippon.hlegrand.domain.*;
import tech.ippon.hlegrand.service.dto.AnimalDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Animal and its DTO AnimalDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface AnimalMapper extends EntityMapper<AnimalDTO, Animal> {



    default Animal fromId(Long id) {
        if (id == null) {
            return null;
        }
        Animal animal = new Animal();
        animal.setId(id);
        return animal;
    }

    default AnimalDTO toDTOWithScore(Animal animal, Double score) {
        if (animal == null) {
            return null;
        }
        return new AnimalDTO()
            .setId(animal.getId())
            .setName(animal.getName())
            .setPromoted(animal.isPromoted())
            .setScore(score);
    }
}
