package ru.yandex.simple_shop.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.simple_shop.model.ItemEntity;

@Repository
public interface ItemRepository extends R2dbcRepository<ItemEntity, Long> {
    Mono<Long> countByTitleContainingIgnoreCase(String title);

    @Override
    @Cacheable(cacheNames = "searchItems")
    Flux<ItemEntity> findAll();

    @Cacheable(cacheNames = "searchItems", key = "{#search, #pageable.sort, #pageable.pageNumber, #pageable.pageSize}")
    Flux<ItemEntity> findByTitleContainingIgnoreCase(String search, Pageable pageable);
}
