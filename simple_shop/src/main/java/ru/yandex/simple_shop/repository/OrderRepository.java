package ru.yandex.simple_shop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.simple_shop.model.OrderEntity;

import java.util.List;

@Repository
public interface OrderRepository extends R2dbcRepository<OrderEntity, Long> {
    Flux<OrderEntity> findAllByUserId(Long userId);
}
