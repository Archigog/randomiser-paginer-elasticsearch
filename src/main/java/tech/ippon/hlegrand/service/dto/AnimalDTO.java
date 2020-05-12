package tech.ippon.hlegrand.service.dto;
import javax.validation.constraints.*;
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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isPromoted() {
        return promoted;
    }

    public void setPromoted(Boolean promoted) {
        this.promoted = promoted;
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
            ", promoted='" + isPromoted() + "'" +
            "}";
    }
}
