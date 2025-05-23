package ru.yandex.simple_shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
    public Optional<CartItemEntity> findByItemId(Long itemId) {
        return cartItemRepository.findByItemEntityId(itemId);
    }

    @Transactional
    public void save(CartItemEntity cartItem) {
        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void delete(CartItemEntity cartItem) {
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void deleteByItemId(Long id) {
        Optional<CartItemEntity> optEntity = cartItemRepository.findByItemEntityId(id);
        optEntity.ifPresent(cartItemRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<CartItemEntity> getAll() {
        return cartItemRepository.findAll();
    }

    @Transactional()
    public void addCartItem (Long cartId, ItemEntity item) {
        CartItemEntity cartItem = cartItemRepository.findByItemEntityId(cartId)
                .orElse(CartItemEntity.builder().itemEntity(item).quantity(0).build());
        cartItem.setQuantity(cartItem.getQuantity() + 1);
        cartItemRepository.save(cartItem);
    }

    @Transactional()
    public void removeCartItem (Long cartId, ItemEntity item) {
        CartItemEntity cartItem = cartItemRepository.findByItemEntityId(cartId)
                .orElse(CartItemEntity.builder().itemEntity(item).quantity(0).build());
        if (cartItem.getQuantity() < 2) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() - 1);
            cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    public void clearCart() {
        cartItemRepository.deleteAll();
    }

}
