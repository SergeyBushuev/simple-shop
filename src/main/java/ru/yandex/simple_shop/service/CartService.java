package ru.yandex.simple_shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.simple_shop.model.CartItemEntity;
import ru.yandex.simple_shop.model.ItemEntity;
import ru.yandex.simple_shop.repository.CartRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartItemRepository;

    @Transactional(readOnly = true)
    public Mono<CartItemEntity> findByItemId(Long itemId) {
        return cartItemRepository.findByItemEntityId(itemId);
    }

    @Transactional
    public Mono<CartItemEntity> deleteByItemId(Long id) {
        return cartItemRepository.findByItemEntityId(id)
                .flatMap(cartItem -> cartItemRepository.delete(cartItem).thenReturn(cartItem));
    }

    @Transactional(readOnly = true)
    public Flux<CartItemEntity> getAll() {
        return cartItemRepository.findAll();
    }

    @Transactional()
    public Mono<CartItemEntity> addCartItem(ItemEntity item) {
        return cartItemRepository.findByItemEntityId(item.getId())
                .switchIfEmpty(Mono.just(CartItemEntity.builder().itemEntityId(item.getId()).quantity(0).build()))
                .map(cartItem -> {
                    cartItem.setQuantity(cartItem.getQuantity() + 1);
                    return cartItem;
                })
                .flatMap(cartItemRepository::save);
    }

    @Transactional()
    public Mono<CartItemEntity> removeCartItem(ItemEntity item) {
        return cartItemRepository.findByItemEntityId(item.getId())
                .flatMap(cartItem -> {
                    if (cartItem.getQuantity() < 2) {
                        return cartItemRepository.delete(cartItem).then(Mono.empty());
                    } else {
                        cartItem.setQuantity(cartItem.getQuantity() - 1);
                        return cartItemRepository.save(cartItem);
                    }
                });

    }

    @Transactional
    public Mono<Void> clearCart() {
        return cartItemRepository.deleteAll();
    }

}
