package net.proselyte.qafordevsreactive.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.proselyte.qafordevsreactive.entity.DeveloperEntity;
import net.proselyte.qafordevsreactive.entity.Status;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class
DeveloperDto {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String specialty;
    private Status status;

    public static DeveloperDto fromEntity(DeveloperEntity entity) {
        return DeveloperDto.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .specialty(entity.getSpecialty())
                .status(entity.getStatus())
                .build();
    }

    public DeveloperEntity toEntity() {
        return DeveloperEntity.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .specialty(specialty)
                .status(status)
                .build();
    }
}
