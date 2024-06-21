package net.proselyte.qafordevsreactive.service;

import net.proselyte.qafordevsreactive.entity.DeveloperEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeveloperService {

    Mono<DeveloperEntity> createDeveloper(DeveloperEntity developer);
    Mono<DeveloperEntity> updateDeveloper(DeveloperEntity developer);

    Flux<DeveloperEntity> getAll();

    Flux<DeveloperEntity> findAllActiveBySpecialty(String specialty);

    Mono<DeveloperEntity> getById(Integer id);

    Mono<Void> softDeleteById(Integer id);
    Mono<Void> hardDeleteById(Integer id);
}
