package tech.ippon.hlegrand.service.dto;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the Animal entity.
 */
public class AnimalDTO implements Serializable {

    private Long id;

    @NotNull
    private String name;

    private Boolean promoted;

    private Double score;

    public Long getId() {
        return id;
    }

    public AnimalDTO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public AnimalDTO setName(String name) {
        this.name = name;
        return this;
    }

    public Boolean getPromoted() {
        return promoted;
    }

    public AnimalDTO setPromoted(Boolean promoted) {
        this.promoted = promoted;
        return this;
    }

    public Double getScore() {
        return score;
    }

    public AnimalDTO setScore(Double score) {
        this.score = score;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AnimalDTO animalDTO = (AnimalDTO) o;
        if (animalDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), animalDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "AnimalDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", promoted='" + getPromoted() + "'" +
            "}";
    }
}
