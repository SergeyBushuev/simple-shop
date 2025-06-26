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
import ru.yandex.simple_shop.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderServiceTest extends PostgresContainerConfig {
    @Autowired
    OrderService orderService;
    @Autowired
    ItemService itemService;
    @Autowired
    UserService userService;

    @BeforeEach
    @Transactional
    public void setup() {
        itemService.addItemInCart(1L, ActionType.plus, "testuser");
    }

    @Test
    @SneakyThrows
    public void createOrder_OkTest() {
        User user = userService.findByUsername("testuser").block();
        OrderEntity order = orderService.createOrder(user).block();
        OrderEntity foundOrder = orderService.findById(order.getId()).block();
        assertEquals(order.getId(), foundOrder.getId());
        assertEquals(order.getTotalPrice(), foundOrder.getTotalPrice());
    }

    @Test
    @SneakyThrows
    public void getOrders_OkTest() {
        User user = userService.findByUsername("testuser").block();

        OrderEntity order = orderService.createOrder(user).block();
        List<OrderEntity> foundOrders = orderService.getOrders(1L).collectList().block();
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
