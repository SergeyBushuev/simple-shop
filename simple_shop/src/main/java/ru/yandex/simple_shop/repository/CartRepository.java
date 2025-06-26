package ru.yandex.simple_shop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.simple_shop.model.CartItemEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends R2dbcRepository<CartItemEntity, Long> {
    Mono<CartItemEntity> findByItemEntityId(Long itemId);

    Mono<CartItemEntity> findByItemEntityIdAndUserId(Long itemEntityId, Long userId);

    Mono<Void> deleteAllByUserId(Long userId);

    Flux<CartItemEntity> findAllByUserId(Long userId);
}
