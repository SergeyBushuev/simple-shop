package ru.yandex.simple_shop.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.simple_shop.PostgresContainerConfig;
import ru.yandex.simple_shop.model.ActionType;
import ru.yandex.simple_shop.model.OrderEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderServiceTest extends PostgresContainerConfig {
    @Autowired
    OrderService orderService;
    @Autowired
    ItemService itemService;

    @BeforeEach
    @Transactional
    public void setup() {
        itemService.addItemInCart(1L, ActionType.plus);
    }

    @Test
    @SneakyThrows
    public void createOrder_OkTest() {
        OrderEntity order = orderService.createOrder().block();
        OrderEntity foundOrder = orderService.findById(order.getId()).block();
        assertEquals(order.getId(), foundOrder.getId());
        assertEquals(order.getTotalPrice(), foundOrder.getTotalPrice());
    }

    @Test
    @SneakyThrows
    public void getOrders_OkTest() {
        OrderEntity order = orderService.createOrder().block();
        List<OrderEntity> foundOrders = orderService.getOrders().collectList().block();
        assertEquals(order.getId(), foundOrders.getFirst().getId());
        assertEquals(order.getTotalPrice(), foundOrders.getFirst().getTotalPrice());
    }

    @Test
    @SneakyThrows
    public void getOrder_NotFoundTest() {
        OrderEntity order = orderService.findById(1000L).block();
        assertNull(order);
    }
}
