package ru.yandex.simple_shop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.simple_shop.model.OrderEntity;
import ru.yandex.simple_shop.model.OrderItemEntity;

import java.util.List;

@Repository
public interface OrderItemRepository extends R2dbcRepository<OrderItemEntity, Long> {

    Flux<OrderItemEntity> findByOrderId(Long orderId);
}
