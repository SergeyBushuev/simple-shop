package ru.yandex.simple_shop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
    public void setup() {
        itemService.addItemInCart(1L, ActionType.plus, "testuser").block();
        itemService.addItemInCart(2L, ActionType.plus, "testuser").block();
    }

    @Test
    public void getCart_OkTest() {
        List<CartItemEntity> cart = cartService.getAll(1L).collectList().block();
        assertNotNull(cart);
        assertEquals(2, cart.size());
    }

    @Test
    public void getFindByItemId_OkTest() {
        Optional<CartItemEntity> optCartItem = cartService.findByItemId(1L).blockOptional();
        assertTrue(optCartItem.isPresent());
        CartItemEntity cartItem = optCartItem.get();
        assertEquals(1L, cartItem.getItemEntityId());
        assertEquals(1, cartItem.getQuantity());
    }

    @Test
    public void getFindByItemId_EmptyTest() {
        Optional<CartItemEntity> optCartItem = cartService.findByItemId(1000L).blockOptional();
        assertTrue(optCartItem.isEmpty());
    }

    @Test
    public void addExistingItem_OkTest() {
        ItemEntity item = itemService.findById(1L).block();
        assertNotNull(item);

        cartService.addCartItem(item, 1L).block();
        List<CartItemEntity> cart = cartService.getAll(1L).collectList().block();
        assertNotNull(cart);
        assertEquals(2, cart.size());
        assertEquals(2, cart.getLast().getQuantity());
    }

    @Test
    public void addNewItem_OkTest() {
        ItemEntity item = itemService.findById(3L).block();
        cartService.addCartItem(item, 1L).block();
        List<CartItemEntity> cart = cartService.getAll(1L).collectList().block();
        assertEquals(3, cart.size());
        assertEquals(1, cart.getLast().getQuantity());
    }

    @Test
    public void removeItem_OkTest() {
        ItemEntity item = itemService.findById(1L).block();
        cartService.removeCartItem(item, 1L).block();
        List<CartItemEntity> cart = cartService.getAll(1L).collectList().block();
        assertEquals(1, cart.size());
    }

    @Test
    public void removeHighQuantityItem_OkTest() {
        itemService.addItemInCart(1L, ActionType.plus, "testuser").block();
        ItemEntity item = itemService.findById(1L).block();
        cartService.removeCartItem(item, 1L).block();

        List<CartItemEntity> cart = cartService.getAll(1L).collectList().block();
        assertEquals(2, cart.size());
        assertEquals(1, cart.getFirst().getQuantity());
    }

    @Test
    public void deleteItem_OkTest() {
        cartService.deleteByItemId(1L, 1L).block();
        List<CartItemEntity> cart = cartService.getAll(1L).collectList().block();
        assertEquals(1, cart.size());
    }

    @Test
    public void clearCart_OkTest() {
        cartService.clearCart(1L).block();
        List<CartItemEntity> cart = cartService.getAll(1L).collectList().block();
        assertTrue(cart.isEmpty());
    }
}
