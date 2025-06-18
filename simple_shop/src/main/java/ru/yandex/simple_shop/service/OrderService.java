package ru.yandex.simple_shop.service;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.simple_shop.model.ItemEntity;
import ru.yandex.simple_shop.model.OrderEntity;
import ru.yandex.simple_shop.model.OrderItemEntity;
import ru.yandex.simple_shop.repository.OrderItemRepository;
import ru.yandex.simple_shop.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final ItemService itemService;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    @Cacheable(cacheNames = "orders")
    public Flux<OrderEntity> getOrders() {
        return orderRepository.findAll().flatMap(order -> getItemsForOrder(order.getId())
                .collectList()
                .flatMap(items -> setItemsForOrder(order, items)));
    }

    @Transactional
    @Cacheable(cacheNames = "order", key = "#id")
    public Mono<OrderEntity> findById(Long id) {
        Mono<List<ItemEntity>> items = getItemsForOrder(id).collectList();
        return orderRepository.findById(id).zipWith(items).map(tuple2 -> {
            OrderEntity order = tuple2.getT1();
            order.setItems(tuple2.getT2());
            return order;
        });
    }

    private Mono<OrderEntity> setItemsForOrder(OrderEntity order, List<ItemEntity> items) {
        order.setItems(items);
        return Mono.just(order);
    }
    private Flux<ItemEntity> getItemsForOrder(Long id) {
        return orderItemRepository.findByOrderId(id)
                .flatMap(orderItemEntity -> itemService.findById(orderItemEntity.getItemId())
                        .doOnNext(item -> item.setCount(orderItemEntity.getQuantity())));
    }

    @Transactional
    @CacheEvict(cacheNames = "cartItems", allEntries = true)
    public Mono<OrderEntity> createOrder() {
        OrderEntity orderEntity = new OrderEntity();
        Mono<List<ItemEntity>> itemsFromCart = itemService.getItemsFromCart().doOnNext(orderEntity::setItems).cache();

        Mono<Double> totalPrice = itemsFromCart.map(items -> items.stream().map(item -> item.getPrice() * item.getCount())
                .reduce(0.0, Double::sum));

        Mono<List<OrderItemEntity>> orderItems = itemsFromCart
                .map(items -> items.stream().map(item -> OrderItemEntity.builder()
                        .orderId(orderEntity.getId())
                        .itemId(item.getId())
                        .quantity(item.getCount())
                        .build()).toList());

        return orderItems.zipWith(totalPrice).map(tuple2 ->
                {
                    orderEntity.setCreatedAt(LocalDateTime.now());
                    orderEntity.setOrderItems(tuple2.getT1());
                    orderEntity.setTotalPrice(tuple2.getT2());
                    return orderEntity;
                })
                .flatMap(this::clearCart)
                .flatMap(orderRepository::save)
                .flatMap(order -> orderItemRepository.saveAll(getOrderItemsWithId(orderEntity))
                        .then(Mono.just(order)));

    }

    private Mono<OrderEntity> clearCart(OrderEntity orderEntity) {
        return cartService.clearCart().thenReturn(orderEntity);
    }

    private List<OrderItemEntity> getOrderItemsWithId(OrderEntity orderEntity) {
        orderEntity.getOrderItems()
                .forEach(orderItemEntity -> orderItemEntity.setOrderId(orderEntity.getId()));
        return orderEntity.getOrderItems();
    }

}
