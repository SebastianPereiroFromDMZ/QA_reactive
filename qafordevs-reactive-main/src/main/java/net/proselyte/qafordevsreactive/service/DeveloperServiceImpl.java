package net.proselyte.qafordevsreactive.service;

import lombok.RequiredArgsConstructor;
import net.proselyte.qafordevsreactive.entity.DeveloperEntity;
import net.proselyte.qafordevsreactive.entity.Status;
import net.proselyte.qafordevsreactive.exception.DeveloperNotFoundException;
import net.proselyte.qafordevsreactive.exception.DeveloperWithEmailAlreadyExistsException;
import net.proselyte.qafordevsreactive.repository.DeveloperRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DeveloperServiceImpl implements DeveloperService {

    private final DeveloperRepository developerRepository;

    private Mono<Void> checkIfExistsByEmail(String email) {//приватный метод проверки есть ли девелопер в БД по указанному эмайлу
        return developerRepository.findByEmail(email)//делаем запрос поиска девелопера по эмайлу в БД
                .flatMap(developer -> {//при разных условиях (if) преобразуем возвращаемый результат:
                    if (Objects.nonNull(developer)) {//если есть девелопер с указанным эмайлом
                        return Mono.error(new DeveloperWithEmailAlreadyExistsException("Developer with defined email already exists",
                                "DEVELOPER_DUPLICATE_EMAIL"));
                        //возвращаем кастомную ошибку
                    }
                    return Mono.empty();//в другом случае (если его нет) возвращаем пустой моно
                });
    }

    @Override
    public Mono<DeveloperEntity> createDeveloper(DeveloperEntity developer) {//при создании нового девелопера
        return checkIfExistsByEmail(developer.getEmail())//сначала проверяем есть ли такой девелопер в БД,если есть в методе checkIfExistsByEmail упадем с ошибкой
                .then(Mono.defer(() -> {//Создаем поставщика Mono, который будет предоставлять целевой Mono для подписки каждому нисходящему подписчику
                    developer.setStatus(Status.ACTIVE);//устанавливаем статус
                    return developerRepository.save(developer);//сохраняем девелопера в БД
                }));
    }

    @Override
    public Mono<DeveloperEntity> updateDeveloper(DeveloperEntity developer) {//изменение девелопера в БД
        return developerRepository.findById(developer.getId())//проверяем есть ли такой девелопер в БД
                .switchIfEmpty(Mono.error(new DeveloperNotFoundException("Developer not found", "DEVELOPER_NOT_FOUND")))//если его нет кидаем ошибку
                .flatMap(d -> {//если нашли
                    return developerRepository.save(developer);//просим сохранить нового девелопера (для обновления) и вернуть его наружу
                });
    }

    @Override
    public Flux<DeveloperEntity> getAll() {//здесь
        return developerRepository.findAll();//мы просто возвращаем всех девелоперов которых нам вернет БД
    }

    @Override
    public Flux<DeveloperEntity> findAllActiveBySpecialty(String specialty) {//здесь возвращаем всех АКТИВНЫХ и по специальности (БД отсортирует)
        return developerRepository.findAllActiveBySpecialty(specialty);
    }

    @Override
    public Mono<DeveloperEntity> getById(Integer id) {//просто ищем по ID
        return developerRepository.findById(id)
                .switchIfEmpty(Mono.error(new DeveloperNotFoundException("Developer not found", "DEVELOPER_NOT_FOUND")));
    }

    @Override
    public Mono<Void> softDeleteById(Integer id) {//совтовое удаление (изменение статуса из АКТИВ на ДЕЛЕТЕД)
        return developerRepository.findById(id)//делаем проверку что такой девелопер есть в БД
                .switchIfEmpty(Mono.error(new DeveloperNotFoundException("Developer not found", "DEVELOPER_NOT_FOUND")))//если его нет выкидываем ошибку
                .flatMap(developer -> {//если нашли его
                    developer.setStatus(Status.DELETED);//присваиваем статус ДЕЛЕТЕД
                    return developerRepository.save(developer).then();//ложим отбратно в БД,так как у нас метод void и ничего не возвращает необходимо
                    //завершить метод save методом then(Возвращает Mono<Void>, который воспроизводит только сигналы завершения и ошибки из этого Mono)
                });
    }

    @Override
    public Mono<Void> hardDeleteById(Integer id) {//окончательное удаление девелопера
        return developerRepository.findById(id)//ищем по ID
                .switchIfEmpty(Mono.error(new DeveloperNotFoundException("Developer not found", "DEVELOPER_NOT_FOUND")))//если не найден выбрасывается ошибка
                .flatMap(developer -> {//если найден
                            return developerRepository.deleteById(id).then();//удаляем
                        }
                );
    }
}
