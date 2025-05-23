package ru.yandex.simple_shop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.simple_shop.model.ItemEntity;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {
    Page<ItemEntity> findByTitleContainingIgnoreCase(String search, Pageable pageable);
}
