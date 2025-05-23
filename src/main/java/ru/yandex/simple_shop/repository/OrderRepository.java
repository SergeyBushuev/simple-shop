package ru.yandex.simple_shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.simple_shop.model.OrderEntity;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

}
