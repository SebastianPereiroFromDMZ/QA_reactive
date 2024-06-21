package net.proselyte.qafordevsreactive.it;

import net.proselyte.qafordevsreactive.config.PostgreTestcontainerConfig;
import net.proselyte.qafordevsreactive.dto.DeveloperDto;
import net.proselyte.qafordevsreactive.entity.DeveloperEntity;
import net.proselyte.qafordevsreactive.repository.DeveloperRepository;
import net.proselyte.qafordevsreactive.util.DataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//https://javarush.com/quests/lectures/questspringboot.level01.lecture10  (хорошая статья на тему тестирования)



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//По умолчанию аннотация @SpringBootTest не запускает сервер. Вы можете использовать атрибут webEnvironment для аннотации @SpringBootTest
//для дальнейшего уточнения хода выполнения ваших тестов:

//MOCK(по умолчанию) : загружает веб-контекст ApplicationContext и предоставляет объект-имитацию веб-окружения.
//Встроенные серверы не запускаются при использовании этой аннотации. Если веб-окружение недоступно в вашем classpath,
//этот режим прозрачно возвращается к созданию обычного не-веб ApplicationContext.
//Его можно использовать в сочетании с аннотацией @AutoConfigureMockMvc или @AutoConfigureWebTestClient для тестирования веб-приложения на основе имитации.

//RANDOM_PORT: загружает WebServerApplicationContext и обеспечивает реальное веб-окружение. Встроенные серверы запускаются и прослушивают произвольный порт.

//DEFINED_PORT: загружает WebServerApplicationContext и обеспечивает реальное веб-окружение.
//Встроенные серверы запускаются и прослушивают заданный порт (из файла application.properties) или порт по умолчанию 8080.

//NONE: загружает ApplicationContext с помощью SpringApplication, но не предоставляет никакого веб-окружения (имитационного или иного).

@AutoConfigureWebTestClient
//@AutoConfigureWebTestClient: Аннотация, которую можно применить к тестовому классу, чтобы включить WebTestClient привязку непосредственно к приложению.
//Тесты не полагаются на HTTP-сервер и используют фиктивные запросы и ответы. На данный момент поддерживаются только приложения WebFlux.

//WebTestClient: Клиент для тестирования веб-серверов, который использует WebClient внутренние ресурсы для выполнения запросов,
//а также предоставляет гибкий API для проверки ответов. Этот клиент может подключаться к любому серверу через HTTP или к
//приложению WebFlux через фиктивные объекты запроса и ответа.

//WebClient: Неблокирующий реактивный клиент для выполнения HTTP-запросов, предоставляющий гибкий реактивный API через базовые клиентские библиотеки HTTP,
//такие как Reactor Netty.

@Import(PostgreTestcontainerConfig.class)//импортируем конфигурационный файл
@TestInstance(TestInstance.Lifecycle.PER_METHOD)//Аннотация @TestInstance позволяет нам настроить жизненный цикл тестов JUnit 5.
//@TestInstance имеет два режима. Одним из них является LifeCycle.PER_METHOD (по умолчанию). Другой — Lifecycle.PER_CLASS .
//Последнее позволяет нам попросить JUnit создать только один экземпляр тестового класса и повторно использовать его между тестами.

public class ItDeveloperRestControllerV1Tests {

    //здесь тестируем интеграционные тесты с контейнерной БД постгрес, мок заглушки уже не нужны, делаем все с реальной БД (контейнерной)

    @Autowired
    private DeveloperRepository developerRepository;

    @Autowired
    private WebTestClient webTestClient;//Клиент для тестирования веб-серверов, который использует WebClient для выполнения запросов,
    //а также предоставляет свободный API для проверки ответов. Этот клиент может подключаться к любому серверу по протоколу HTTP или
    //к приложению WebFlux с помощью фиктивных объектов запроса и ответа.

    //WebTestClient это для того что бы можно было делать запросы к контроллеру,
    //по аналогии с блокорующем приложением только там мы подтягивали private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        developerRepository.deleteAll().block();
    }//перед каждым тестом очищаем БД

    @Test//создания нового девелопера в БД
    @DisplayName("Test create developer functionality")
    public void givenDeveloperDto_whenCreateDeveloper_thenSuccessResponse() {
        //given
        //ниже получаем ДТО
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();
        //when
        //ниже делаем реальный запрос на сервер
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DeveloperDto.class)
                .exchange();
        //then
        //ниже проверяем ответ с сервера
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.firstName").isEqualTo("John")
                .jsonPath("$.lastName").isEqualTo("Doe")
                .jsonPath("$.status").isEqualTo("ACTIVE");

    }

    @Test//выброса исключения при создании нового девелопера
    @DisplayName("Test create developer with duplicate email functionality")
    public void givenDtoWithDuplicateEmail_whenCreateDeveloper_thenExceptionIsThrown() {
        //given
        String duplicateEmail = "duplicate@mail.com";//делаем одниковый эмайл
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();//для ДТО
        dto.setEmail(duplicateEmail);//устанавливаем эмайл в ДТО
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();//для энтити
        entity.setEmail(duplicateEmail);//устанавливаем эмайл для энтити
        developerRepository.save(entity).block();//сохраняем энтити в БД
        //when
        //делаем запрос с ДТО у которой дублик эмоаойл
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DeveloperDto.class)
                .exchange();
        //then
        //смотрим ответ (сверяем)
        result.expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_DUPLICATE_EMAIL")
                .jsonPath("$.errors[0].message").isEqualTo("Developer with defined email already exists");
    }

    @Test//изменения девелопера
    @DisplayName("Test update developer functionality")
    public void givenDeveloperDto_whenUpdateDeveloper_thenSuccessResponse() {
        //given
        String updatedEmail = "updated@mail.com";

        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        System.out.println(entity);//выводим в кнсоль девелпера с еще не измененным эмэйлом

        developerRepository.save(entity).block();

        DeveloperDto dto = DataUtils.getJohnDoeDtoPersisted();
        dto.setId(entity.getId());
        dto.setEmail(updatedEmail);
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
                .jsonPath("$.id").isEqualTo(entity.getId())
                .jsonPath("$.firstName").isEqualTo("John")
                .jsonPath("$.lastName").isEqualTo("Doe")
                .jsonPath("$.email").isEqualTo(updatedEmail)
                .jsonPath("$.status").isEqualTo("ACTIVE");

    }

    @Test//выброса исключения при изменении девелопера (не тот id)
    @DisplayName("Test update developer with incorrect id functionality")
    public void givenDtoWithIncorrectId_whenUpdateDeveloper_thenExceptionIsThrown() {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoPersisted();
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

    @Test//получения всех девелперов
    @DisplayName("Test get all developers functionality")
    public void givenThreeDeveloper_whenGetAll_thenDevelopersAreReturned() {
        //given
        DeveloperEntity entity1 = DataUtils.getJohnDoeTransient();
        DeveloperEntity entity2 = DataUtils.getFrankJonesTransient();
        DeveloperEntity entity3 = DataUtils.getMikeSmithTransient();

        developerRepository.saveAll(List.of(entity1, entity2, entity3)).blockLast();
        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/developers")
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.size()").isEqualTo(3);
    }


    @Test//получения девелопера по id
    @DisplayName("Test get developer by id functionality")
    public void givenId_whenGetById_thenDeveloperIsReturned() {
        //given
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        developerRepository.save(entity).block();
        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/developers/" + entity.getId())
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.firstName").isEqualTo(entity.getFirstName())
                .jsonPath("$.lastName").isEqualTo(entity.getLastName())
                .jsonPath("$.status").isEqualTo("ACTIVE");
    }

    @Test//выброса исключения при получении девелопера по id (некорректный id)
    @DisplayName("Test get developer by incorrect id functionality")
    public void givenIncorrectId_whenGetById_thenExceptionIsThrown() {
        //given
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

    @Test//софтовое удаление девелопера (смена статуса с ACTIVE на DELETED) и так как у нас метод developerRepository.save
    //войдвый (void) мы просто смотрим успешный был вызов или нет
    @DisplayName("Test soft delete developer by id functionality")
    public void givenId_whenSoftDeleteById_thenSuccessResponse() {
        //given
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        developerRepository.save(entity).block();
        //when
        WebTestClient.ResponseSpec result = webTestClient.delete()
                .uri("/api/v1/developers/" + entity.getId())
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);
    }


    @Test
    @DisplayName("Test soft delete developer by incorrect id functionality")
    public void givenIncorrectId_whenSoftDeleteById_thenExceptionIsThrown() {
        //given
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

    @Test//хардвго удаления девелопера (окончательного)
    @DisplayName("Test hard delete developer by id functionality")
    public void givenId_whenHardDeleteById_thenSuccessResponse() {
        //given
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        developerRepository.save(entity).block();
        //when
        WebTestClient.ResponseSpec result = webTestClient.delete()
                .uri("/api/v1/developers/" + entity.getId() + "?isHard=true")
                .exchange();
        //then
        DeveloperEntity obtainedDeveloper = developerRepository.findById(entity.getId()).block();
        assertThat(obtainedDeveloper).isNull();
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("Test hard delete developer by incorrect id functionality")
    public void givenIncorrectId_whenHardDeleteById_thenExceptionIsThrown() {
        //given
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
