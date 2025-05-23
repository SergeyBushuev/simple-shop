package ru.yandex.simple_shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.simple_shop.model.CartItemEntity;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartItemEntity, Long> {
    public Optional<CartItemEntity> findByItemEntityId(Long itemId);
}
