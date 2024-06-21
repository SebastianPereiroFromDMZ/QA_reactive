package net.proselyte.qafordevsreactive.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("developers")//важно что здесь мы используем спринговые зависимости (import org.springframework.data.relational.core.mapping.Table)
//в прошлом проэкте где тестирововали РЕСТ приложение зависимости все были джакартовские (import jakarta.persistence.*)
public class DeveloperEntity implements Persistable<Integer> {
    //интерфейс Persistable<Integer> нужен для определения новыя сущьность которую сохраняем в БД или она уже существует,
    //спринг будет разбираться положить ли новую сущьность или проапдейтить уже существующую

    //Если идентификатор объекта формируется на стороне базы данных (наверное, самый частый случай), то достаточно на поле с идентификатором поставить аннотацию Id.
    //Если идентификатор создается на стороне приложения, то такой объект должен имплементировать интерфейс Persistable и реализовать метод isNew.
    @Id
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String specialty;

    private Status status;

    @Override
    public boolean isNew() {//проверяем обьет новый или нет
        return Objects.isNull(id);
    }
}
