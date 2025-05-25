package ru.yandex.simple_shop.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.simple_shop.PostgresContainerConfig;
import ru.yandex.simple_shop.model.ActionType;
import ru.yandex.simple_shop.model.ItemEntity;
import ru.yandex.simple_shop.model.SortType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ItemServiceTest extends PostgresContainerConfig {


    @Autowired
    ItemService itemService;

    @Test
    @SneakyThrows
    public void getItem_OkTest() {
        ItemEntity item = itemService.findById(1L);
        assertNotNull(item);
        assertEquals(1L, item.getId());
        assertEquals("Big cat", item.getTitle());
    }

    @Test
    @SneakyThrows
    public void getItem_NotFoundTest() {
        assertThrows(EntityNotFoundException.class, () -> itemService.findById(1000L));
    }

    @Test
    @SneakyThrows
    public void getPage_OkTest() {
        Page<ItemEntity> page = itemService.getShowcase(null, SortType.NO, 1, 10);
        assertFalse(page.isEmpty());
        assertEquals(6, page.getTotalElements());
    }

    @Test
    @SneakyThrows
    public void getSearchedPage_OkTest() {
        Page<ItemEntity> page = itemService.getShowcase("cat", SortType.NO, 1, 10);
        assertFalse(page.isEmpty());
        assertEquals(4, page.getTotalElements());
    }

    @Test
    @SneakyThrows
    public void getSearchedPriceOrdered_OkTest() {
        Page<ItemEntity> page = itemService.getShowcase("cat", SortType.PRICE, 1, 10);
        assertFalse(page.isEmpty());
        assertEquals(4, page.getTotalElements());
        List<ItemEntity> items = page.getContent();
        assertTrue(items.get(0).getPrice() <= items.get(1).getPrice());
        assertTrue(items.get(1).getPrice() <= items.get(2).getPrice());
        assertTrue(items.get(2).getPrice() <= items.get(3).getPrice());
    }

    @Test
    @SneakyThrows
    public void getSearchedPriceOrderedPaging_OkTest() {
        Page<ItemEntity> page = itemService.getShowcase("cat", SortType.PRICE, 2, 2);
        assertFalse(page.isEmpty());
        List<ItemEntity> items = page.getContent();
        assertEquals(2, items.size());
        assertEquals("Big cat", items.get(0).getTitle());
        assertEquals("Funny cat", items.get(1).getTitle());
    }

    @Test
    @SneakyThrows
    public void getSearchedAlphaOrdered_OkTest() {
        Page<ItemEntity> page = itemService.getShowcase("dog", SortType.ALPHA, 1, 10);
        assertFalse(page.isEmpty());
        assertEquals(2, page.getTotalElements());
        List<ItemEntity> items = page.getContent();
        assertEquals("Big dog", items.get(0).getTitle());
        assertEquals("Marble dog", items.get(1).getTitle());
    }

    @Test
    @SneakyThrows
    public void checkEmptyCart_OkTest() {
        List<ItemEntity> itemsFromCart = itemService.getItemsFromCart();
        assertTrue(itemsFromCart.isEmpty());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void addItemToCart_OkTest() {
        itemService.addItemInCart(1L, ActionType.plus);

        List<ItemEntity> itemsFromCart = itemService.getItemsFromCart();
        assertFalse(itemsFromCart.isEmpty());
        ItemEntity item = itemsFromCart.getFirst();
        assertEquals("Big cat", item.getTitle());
        assertEquals(1L, item.getId());
        assertEquals(1, item.getCount());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void removeItemFromCartByMinus_OkTest() {
        itemService.addItemInCart(1L, ActionType.plus);
        itemService.addItemInCart(1L, ActionType.plus);
        List<ItemEntity> itemsFromCart = itemService.getItemsFromCart();
        assertEquals(1, itemsFromCart.size());
        assertEquals(2, itemsFromCart.getFirst().getCount());

        itemService.addItemInCart(1L, ActionType.minus);
        List<ItemEntity> itemsFromCartMinus = itemService.getItemsFromCart();
        assertEquals(1, itemsFromCartMinus.size());
        assertEquals(1, itemsFromCartMinus.getFirst().getCount());

        itemService.addItemInCart(1L, ActionType.minus);
        List<ItemEntity> itemsFromCartMinusMinus = itemService.getItemsFromCart();
        assertTrue(itemsFromCartMinusMinus.isEmpty());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void removeItemFromCartByDelete_OkTest() {
        itemService.addItemInCart(1L, ActionType.plus);
        itemService.addItemInCart(1L, ActionType.plus);
        List<ItemEntity> itemsFromCart = itemService.getItemsFromCart();
        assertEquals(1, itemsFromCart.size());
        assertEquals(2, itemsFromCart.getFirst().getCount());

        itemService.addItemInCart(1L, ActionType.delete);
        List<ItemEntity> itemsFromCartMinusMinus = itemService.getItemsFromCart();
        assertTrue(itemsFromCartMinusMinus.isEmpty());
    }
}
