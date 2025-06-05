package ru.yandex.simple_shop.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.simple_shop.model.CartItemEntity;
import ru.yandex.simple_shop.model.OrderEntity;
import ru.yandex.simple_shop.model.OrderItemEntity;
import ru.yandex.simple_shop.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;

    @Transactional
    public Flux<OrderEntity> getOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Mono<OrderEntity> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public OrderEntity createOrder() {
        List<CartItemEntity> cartItems = cartService.getAll();

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setCreatedAt(LocalDateTime.now());
        List<OrderItemEntity> orderItems = cartItems.stream().map(cartItem -> OrderItemEntity.builder()
                .order(orderEntity)
                .item(cartItem.getItemEntity())
                .quantity(cartItem.getQuantity())
                .build()).toList();
        orderEntity.setOrderItems(orderItems);
        Double totalPrice = cartItems.stream()
                .map(item -> item.getItemEntity().getPrice() * item.getQuantity())
                .reduce(0.0, Double::sum);
        orderEntity.setTotalPrice(totalPrice);
        cartService.clearCart();
        return orderRepository.save(orderEntity);
    }

}
