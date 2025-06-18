package ru.yandex.simple_shop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.simple_shop.model.OrderEntity;

@Repository
public interface OrderRepository extends R2dbcRepository<OrderEntity, Long> {

}
