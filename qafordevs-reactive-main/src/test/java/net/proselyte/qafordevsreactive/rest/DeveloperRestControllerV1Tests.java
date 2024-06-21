package net.proselyte.qafordevsreactive.rest;

import net.proselyte.qafordevsreactive.dto.DeveloperDto;
import net.proselyte.qafordevsreactive.entity.DeveloperEntity;
import net.proselyte.qafordevsreactive.exception.DeveloperNotFoundException;
import net.proselyte.qafordevsreactive.exception.DeveloperWithEmailAlreadyExistsException;
import net.proselyte.qafordevsreactive.service.DeveloperService;
import net.proselyte.qafordevsreactive.util.DataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ComponentScan({"net.proselyte.qafordevsreactive.errorhandling"})//также говорим что понадобится, так что подтягиваем
@ExtendWith(SpringExtension.class)//для того что бы можно было работать с контекстом
@WebFluxTest(controllers = {DeveloperRestControllerV1.class})//указываем какой контроллер мы тестируем, он подтягивает только контроллер
public class DeveloperRestControllerV1Tests {

    @Autowired
    private WebTestClient webTestClient;//это для того что бы можно было делать запросы к контроллеру,
    //по аналогии с блокорующем приложением только там мы подтягивали private MockMvc mockMvc;

    @MockBean
    private DeveloperService developerService;//понадобиться сервис, мокаем его!

    @Test
    @DisplayName("Test create developer functionality")
    public void givenDeveloperDto_whenCreateDeveloper_thenSuccessResponse() {
        //given дано
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();//берем транзиентного девелопера

        DeveloperEntity entity = DataUtils.getJohnDoePersisted();//берем персистентного девелопера

        BDDMockito.given(developerService.createDeveloper(any(DeveloperEntity.class)))//тут задаем логику заглушке: BDDMockito.given когда у тебя
                // произойдет вызов у обьекта developerService метода createDeveloper для любого (any) обьекта класса DeveloperEntity
                .willReturn(Mono.just(entity));//то в этом случае должен вернуться конкретный обьект класса DeveloperEntity (entity)
        //when момент действия
        WebTestClient.ResponseSpec result = webTestClient.post()//делаем пост запрос и результат запроса фиксируем в result
                .uri("/api/v1/developers")//на адрес
                .contentType(MediaType.APPLICATION_JSON)//указываем тайп
                .body(Mono.just(dto), DeveloperDto.class)//указываем тело (ДТО)
                .exchange();//обмен
        //then результат
        result.expectStatus().isOk()//проверяем результ, для начала спрашиваем статус isOk()
                .expectBody()//исследуем тело
                .consumeWith(System.out::println)//которое выводим в консоль
                //анлизируем само тело:
                .jsonPath("$.id").isEqualTo(1)//проверяем ID который должен равен 1
                .jsonPath("$.firstName").isEqualTo("John")//ну ту все понятно))
                .jsonPath("$.lastName").isEqualTo("Doe")
                .jsonPath("$.status").isEqualTo("ACTIVE");

    }

    @Test//тес на выброс исключения при дубликате эмайла
    @DisplayName("Test create developer with duplicate email functionality")
    public void givenDtoWithDuplicateEmail_whenCreateDeveloper_thenExceptionIsThrown() {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();

        BDDMockito.given(developerService.createDeveloper(any(DeveloperEntity.class)))//здаем лгику заглушке
                .willThrow(new DeveloperWithEmailAlreadyExistsException("Developer with defined email is already exists", "DEVELOPER_DUPLICATE_EMAIL"));
        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DeveloperDto.class)
                .exchange();
        //then
        result.expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_DUPLICATE_EMAIL")
                .jsonPath("$.errors[0].message").isEqualTo("Developer with defined email is already exists");
    }

    @Test//изменения девелпера
    @DisplayName("Test update developer functionality")
    public void givenDeveloperDto_whenUpdateDeveloper_thenSuccessResponse() {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoPersisted();

        DeveloperEntity entity = DataUtils.getJohnDoePersisted();//получаем персистентного девелопера
        entity.setEmail("john.updated-doe@mail.com");//менем ему эмайл

        BDDMockito.given(developerService.updateDeveloper(any(DeveloperEntity.class)))//задаем логику заглушке
                .willReturn(Mono.just(entity));//при вызове developerService.updateDeveloper возвращается измененный девелопер
        //when
        WebTestClient.ResponseSpec result = webTestClient.put()
                .uri("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DeveloperDto.class)
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.firstName").isEqualTo("John")
                .jsonPath("$.lastName").isEqualTo("Doe")
                .jsonPath("$.email").isEqualTo("john.updated-doe@mail.com")
                .jsonPath("$.status").isEqualTo("ACTIVE");

    }

    @Test//выброса исключения при вводе некоректного id для изменения пользователя
    @DisplayName("Test update developer with incorrect id functionality")
    public void givenDtoWithIncorrectId_whenUpdateDeveloper_thenExceptionIsThrown() {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoPersisted();

        BDDMockito.given(developerService.updateDeveloper(any(DeveloperEntity.class)))
                .willThrow(new DeveloperNotFoundException("Developer not found", "DEVELOPER_NOT_FOUND"));
        //when
        WebTestClient.ResponseSpec result = webTestClient.put()
                .uri("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DeveloperDto.class)
                .exchange();
        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_NOT_FOUND")
                .jsonPath("$.errors[0].message").isEqualTo("Developer not found");
    }

    @Test//получения всех девелоперов
    @DisplayName("Test get all developers functionality")
    public void givenThreeDeveloper_whenGetAll_thenDevelopersAreReturned() {
        //given
        DeveloperEntity entity1 = DataUtils.getJohnDoePersisted();//берем три девелопера
        DeveloperEntity entity2 = DataUtils.getFrankJonesPersisted();
        DeveloperEntity entity3 = DataUtils.getMikeSmithPersisted();

        BDDMockito.given(developerService.getAll())//задаем логику заглушке что при вызове developerService.getAll()
                .willReturn(Flux.just(entity1, entity2, entity3));//возвращаю вышеуказанные девелоперы
        //when
        //делаем запрос:
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/developers")
                .exchange();
        //then
        //Провераем:
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.size()").isEqualTo(3);
    }


    @Test//получение девелопера по id
    @DisplayName("Test get developer by id functionality")
    public void givenId_whenGetById_thenDeveloperIsReturned() {
        //given
        DeveloperEntity entity = DataUtils.getJohnDoePersisted();

        BDDMockito.given(developerService.getById(anyInt()))
                .willReturn(Mono.just(entity));
        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/developers/1")
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.firstName").isEqualTo(entity.getFirstName())
                .jsonPath("$.lastName").isEqualTo(entity.getLastName())
                .jsonPath("$.status").isEqualTo("ACTIVE");
    }

    @Test//выброса ошибке при неккоректном id
    @DisplayName("Test get developer by incorrect id functionality")
    public void givenIncorrectId_whenGetById_thenExceptionIsThrown() {
        //given
        BDDMockito.given(developerService.getById(anyInt()))
                .willThrow(new DeveloperNotFoundException("Developer not found", "DEVELOPER_NOT_FOUND"));
        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/developers/1")
                .exchange();
        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_NOT_FOUND")
                .jsonPath("$.errors[0].message").isEqualTo("Developer not found");
    }

    @Test//софтового удаления (изменение статуса)
    @DisplayName("Test soft delete developer by id functionality")
    public void givenId_whenSoftDeleteById_thenSuccessResponse() {
        //given
        BDDMockito.given(developerService.softDeleteById(anyInt()))//задаем логику заглушке
                .willReturn(Mono.empty());//и так как у нас метод сервиса developerService.softDeleteById войдовый (void)
        //тоесть ничего не возвращает, то здесь мы также ничего не возвращаем Mono.empty()
        //when
        WebTestClient.ResponseSpec result = webTestClient.delete()
                .uri("/api/v1/developers/1")
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);
    }


    @Test//выброса исключения при софтовом удалении когда неправильно указан id
    @DisplayName("Test soft delete developer by incorrect id functionality")
    public void givenIncorrectId_whenSoftDeleteById_thenExceptionIsThrown() {
        //given
        BDDMockito.given(developerService.softDeleteById(anyInt()))
                .willThrow(new DeveloperNotFoundException("Developer not found", "DEVELOPER_NOT_FOUND"));
        //when
        WebTestClient.ResponseSpec result = webTestClient.delete()
                .uri("/api/v1/developers/1")
                .exchange();
        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_NOT_FOUND")
                .jsonPath("$.errors[0].message").isEqualTo("Developer not found");
    }

    @Test//хардового удаления (окончательного)
    @DisplayName("Test hard delete developer by id functionality")
    public void givenId_whenHardDeleteById_thenSuccessResponse() {
        //given
        BDDMockito.given(developerService.hardDeleteById(anyInt()))
                .willReturn(Mono.empty());
        //when
        WebTestClient.ResponseSpec result = webTestClient.delete()
                .uri("/api/v1/developers/1?isHard=true")
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);
    }

    @Test//выброса исключения при хард удалении когда неправильно указан id
    @DisplayName("Test hard delete developer by incorrect id functionality")
    public void givenIncorrectId_whenHardDeleteById_thenExceptionIsThrown() {
        //given
        BDDMockito.given(developerService.hardDeleteById(anyInt()))
                .willThrow(new DeveloperNotFoundException("Developer not found", "DEVELOPER_NOT_FOUND"));
        //when
        WebTestClient.ResponseSpec result = webTestClient.delete()
                .uri("/api/v1/developers/1?isHard=true")
                .exchange();
        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_NOT_FOUND")
                .jsonPath("$.errors[0].message").isEqualTo("Developer not found");
    }

}
