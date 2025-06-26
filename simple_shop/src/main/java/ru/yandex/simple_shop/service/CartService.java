package ru.yandex.simple_shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(cacheNames = "cartItem", key = "#itemId")
    public Mono<CartItemEntity> findByItemId(Long itemId) {
        return cartItemRepository.findByItemEntityId(itemId);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "cartItem", key = "{#itemId, #userId}")
    public Mono<CartItemEntity> findByItemIdAndUserId(Long itemId, Long userId) {
        return cartItemRepository.findByItemEntityIdAndUserId(itemId, userId);
    }

    @Transactional
    @CacheEvict(cacheNames = "cartItem", key = "{#id, #userId}")
    public Mono<CartItemEntity> deleteByItemId(Long id, Long userId) {
        return cartItemRepository.findByItemEntityIdAndUserId(id, userId)
                .flatMap(cartItem -> cartItemRepository.delete(cartItem).thenReturn(cartItem));
    }

    @Transactional(readOnly = true)
    public Flux<CartItemEntity> getAll(Long userId) {
        return cartItemRepository.findAllByUserId(userId);
    }

    @Transactional()
    @CachePut(cacheNames = "cartItem", key = "{#item.id, #userId}")
    public Mono<CartItemEntity> addCartItem(ItemEntity item, Long userId) {
        return cartItemRepository.findByItemEntityIdAndUserId(item.getId(), userId)
                .switchIfEmpty(Mono.just(
                        CartItemEntity.builder().itemEntityId(item.getId()).quantity(0).userId(userId).build()))
                .map(cartItem -> {
                    cartItem.setQuantity(cartItem.getQuantity() + 1);
                    return cartItem;
                })
                .flatMap(cartItemRepository::save);
    }

    @Transactional()
    @CacheEvict(cacheNames = "cartItem", key = "{#item.id, #userId}")
    public Mono<CartItemEntity> removeCartItem(ItemEntity item, Long userId) {
        return cartItemRepository.findByItemEntityIdAndUserId(item.getId(), userId)
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
    @CacheEvict(cacheNames = {"cartItem"}, key = "#userId", allEntries = true)
    public Mono<Void> clearCart(Long userId) {
        return cartItemRepository.deleteAllByUserId(userId);
    }

}
