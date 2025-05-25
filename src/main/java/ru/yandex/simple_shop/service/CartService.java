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
        Optional<CartItemEntity> optCartItem = cartItemRepository.findByItemEntityId(itemId);
        optCartItem.ifPresent((cartItemEntity ->
                        cartItemEntity.getItemEntity().setCount(cartItemEntity.getQuantity())));
        return optCartItem;
    }

    @Transactional
    public void deleteByItemId(Long id) {
        Optional<CartItemEntity> optEntity = cartItemRepository.findByItemEntityId(id);
        optEntity.ifPresent(cartItemRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<CartItemEntity> getAll() {
        List<CartItemEntity> cartItemEntities = cartItemRepository.findAll();
        cartItemEntities.forEach(cartItemEntity -> cartItemEntity.getItemEntity().setCount(cartItemEntity.getQuantity()));
        return cartItemEntities;
    }

    @Transactional()
    public void addCartItem (ItemEntity item) {
        CartItemEntity cartItem = cartItemRepository.findByItemEntityId(item.getId())
                .orElse(CartItemEntity.builder().itemEntity(item).quantity(0).build());
        cartItem.setQuantity(cartItem.getQuantity() + 1);
        cartItemRepository.save(cartItem);
    }

    @Transactional()
    public void removeCartItem (ItemEntity item) {
        CartItemEntity cartItem = cartItemRepository.findByItemEntityId(item.getId())
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
