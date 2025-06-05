package ru.yandex.simple_shop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.simple_shop.model.CartItemEntity;

import java.util.Optional;

@Repository
public interface CartRepository extends R2dbcRepository<CartItemEntity, Long> {
    Mono<CartItemEntity> findByItemEntityId(Long itemId);
}
