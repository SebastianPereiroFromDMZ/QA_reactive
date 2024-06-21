package net.proselyte.qafordevsreactive.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)//Еще одна интересная особенность — плавная интеграция тестконтейнеров в локальную разработку
//с минимальной настройкой. Эта функциональность позволяет нам тиражировать производственную среду не только во время тестирования, но и для локальной разработки.
//Чтобы включить его, нам сначала нужно создать @ TestConfiguration и объявить все тестовые контейнеры как Spring Beans
public class PostgreTestcontainerConfig {

    @Bean
    @ServiceConnection//аннотация @ServiceConnection позволит легко привязать приложение к базе данных
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:latest");
    }
}
