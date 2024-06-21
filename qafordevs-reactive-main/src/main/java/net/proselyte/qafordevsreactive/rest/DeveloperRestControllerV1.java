package net.proselyte.qafordevsreactive.rest;

import lombok.RequiredArgsConstructor;
import net.proselyte.qafordevsreactive.dto.DeveloperDto;
import net.proselyte.qafordevsreactive.service.DeveloperService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/developers")
public class DeveloperRestControllerV1 {

    private final DeveloperService developerService;

    @PostMapping
    public Mono<?> createDeveloper(@RequestBody DeveloperDto developerDto) {//создаем нового девелопера
        return developerService.createDeveloper(developerDto.toEntity())//отдаем сервису уже ЭНТИТИ конвертируя его из ДТО
                .flatMap(entity -> Mono.just(DeveloperDto.fromEntity(entity)));//вернувшуюся ЭНТИТИ конвертируем в ДТО и отдаем в ответе
    }

    @PutMapping
    public Mono<?> updateDeveloper(@RequestBody DeveloperDto developerDto) {//изменяем девелопера
        return developerService.updateDeveloper(developerDto.toEntity())//также отдаем ЭНТИТИ сервису предварительно сконвертировав его из ДТО
                .flatMap(entity -> Mono.just(DeveloperDto.fromEntity(entity)));//вернувшуюся ЭНТИТИ мапив в ДТО и отдаем в ответ
    }

    @GetMapping
    public Flux<?> getAll() {//отдаем всех девелоперов
        return developerService.getAll()
                .flatMap(entity -> Mono.just(DeveloperDto.fromEntity(entity)));//мапив в ДТО пришедших ЭНТИТИ .just это для каждой
    }

    @GetMapping("/specialty/{specialty}")
    public Flux<?> getAllBySpecialty(@PathVariable("specialty") String specialty) {//возвращаем всех девелоперов по специальности
        return developerService.findAllActiveBySpecialty(specialty)//вызываем сервис отдавая специальность
                .flatMap(entity -> Mono.just(DeveloperDto.fromEntity(entity)));//мапим вернувшиеса ЭНТИТИ на ДТО и отдаем наверх
    }

    @GetMapping("/{id}")
    public Mono<?> getById(@PathVariable("id") Integer id) {//поиск девелопера по ID
        return developerService.getById(id)
                .flatMap(entity -> Mono.just(DeveloperDto.fromEntity(entity)));
    }

    @DeleteMapping("/{id}")
    public Mono<?> deleteById(@PathVariable("id") Integer id, @RequestParam(value = "isHard", defaultValue = "false") boolean isHard) {//удаление девелопера
        //с переменной пути id, и булевским параметром isHard дефолтным значением false

        if(isHard) {//если хард
            return developerService.hardDeleteById(id);//то хард делит
        }
        return developerService.softDeleteById(id);//иначе софт делит
    }
}
