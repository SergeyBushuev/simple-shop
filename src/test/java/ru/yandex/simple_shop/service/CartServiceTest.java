package ru.yandex.simple_shop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.simple_shop.PostgresContainerConfig;
import ru.yandex.simple_shop.model.ActionType;
import ru.yandex.simple_shop.model.CartItemEntity;
import ru.yandex.simple_shop.model.ItemEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CartServiceTest extends PostgresContainerConfig {
    @Autowired
    CartService cartService;
    @Autowired
    ItemService itemService;

    @BeforeEach
    @Transactional
    public void setup() {
        itemService.addItemInCart(1L, ActionType.plus);
        itemService.addItemInCart(2L, ActionType.plus);
    }

    @Test
    public void getCart_OkTest() {
        List<CartItemEntity> cart = cartService.getAll();
        assertEquals(2, cart.size());
    }

    @Test
    @Transactional
    public void getFindByItemId_OkTest() {
        Optional<CartItemEntity> optCartItem = cartService.findByItemId(1L);
        assertTrue(optCartItem.isPresent());
        CartItemEntity cartItem = optCartItem.get();
        assertEquals(1L, cartItem.getItemEntity().getId());
        assertEquals("Big cat", cartItem.getItemEntity().getTitle());
        assertEquals(1, cartItem.getQuantity());
    }

    @Test
    @Transactional
    public void getFindByItemId_EmptyTest() {
        Optional<CartItemEntity> optCartItem = cartService.findByItemId(1000L);
        assertTrue(optCartItem.isEmpty());
    }

    @Test
    @Transactional
    public void addExistingItem_OkTest() {
        ItemEntity item = itemService.findById(1L);
        cartService.addCartItem(item);
        List<CartItemEntity> cart = cartService.getAll();
        assertEquals(2, cart.size());
        assertEquals(2, cart.getFirst().getItemEntity().getCount());
    }

    @Test
    @Transactional
    public void addNewItem_OkTest() {
        ItemEntity item = itemService.findById(3L);
        cartService.addCartItem(item);
        List<CartItemEntity> cart = cartService.getAll();
        assertEquals(3, cart.size());
        assertEquals(1, cart.getLast().getItemEntity().getCount());
    }

    @Test
    @Transactional
    public void removeItem_OkTest() {
        ItemEntity item = itemService.findById(1L);
        cartService.removeCartItem(item);
        List<CartItemEntity> cart = cartService.getAll();
        assertEquals(1, cart.size());
    }

    @Test
    @Transactional
    public void removeHighQuantityItem_OkTest() {
        itemService.addItemInCart(1L, ActionType.plus);
        ItemEntity item = itemService.findById(1L);
        cartService.removeCartItem(item);

        List<CartItemEntity> cart = cartService.getAll();
        assertEquals(2, cart.size());
        assertEquals(1, cart.getFirst().getItemEntity().getCount());
    }

    @Test
    @Transactional
    public void deleteItem_OkTest() {
        cartService.deleteByItemId(1L);
        List<CartItemEntity> cart = cartService.getAll();
        assertEquals(1, cart.size());
    }

    @Test
    @Transactional
    public void clearCart_OkTest() {
        cartService.clearCart();
        List<CartItemEntity> cart = cartService.getAll();
        assertTrue(cart.isEmpty());
    }
}
