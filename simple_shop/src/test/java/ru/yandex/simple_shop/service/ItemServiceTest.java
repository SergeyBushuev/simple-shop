package ru.yandex.simple_shop.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import reactor.util.function.Tuple2;
import ru.yandex.simple_shop.PostgresContainerConfig;
import ru.yandex.simple_shop.model.ActionType;
import ru.yandex.simple_shop.model.ItemEntity;
import ru.yandex.simple_shop.model.SortType;
import ru.yandex.simple_shop.repository.ItemRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class ItemServiceTest extends PostgresContainerConfig {


    @Autowired
    ItemService itemService;

    @SpyBean
    ItemRepository itemRepository;

    @Test
    @SneakyThrows
    public void getItem_OkTest() {
        ItemEntity item = itemService.findById(1L).block();
        assertNotNull(item);
        assertEquals(1L, item.getId());
        assertEquals("Big cat", item.getTitle());
    }

    @Test
    @SneakyThrows
    public void getPage_OkTest() {
        Tuple2<List<ItemEntity>, Long> page = itemService.getShowcase(null, SortType.NO, 1, 10, "testuser").block();
        assertFalse(page.getT1().isEmpty());
        assertEquals(6, page.getT1().size());
    }

    @Test
    @SneakyThrows
    public void getPage_CachedOkTest() {
        Tuple2<List<ItemEntity>, Long> page = itemService.getShowcase(null, SortType.NO, 1, 10, "testuser").block();
        Tuple2<List<ItemEntity>, Long> page2 = itemService.getShowcase(null, SortType.NO, 1, 10, "testuser").block();
        verify(itemRepository, times(1)).findAll();
        assertFalse(page.getT1().isEmpty());
        assertFalse(page2.getT1().isEmpty());
        assertEquals(6, page.getT1().size());
        assertEquals(6, page2.getT1().size());
    }

    @Test
    @SneakyThrows
    public void getSearchedPage_OkTest() {
        Tuple2<List<ItemEntity>, Long> page = itemService.getShowcase("cat", SortType.NO, 1, 10, "testuser").block();
        assertFalse(page.getT1().isEmpty());
        assertEquals(4, page.getT1().size());
    }

    @Test
    @SneakyThrows
    public void getSearchedPriceOrdered_OkTest() {
        Tuple2<List<ItemEntity>, Long> page = itemService.getShowcase("cat", SortType.PRICE, 1, 10, "testuser").block();
        assertFalse(page.getT1().isEmpty());
        assertEquals(4, page.getT1().size());
        List<ItemEntity> items = page.getT1();
        assertTrue(items.get(0).getPrice() <= items.get(1).getPrice());
        assertTrue(items.get(1).getPrice() <= items.get(2).getPrice());
        assertTrue(items.get(2).getPrice() <= items.get(3).getPrice());
    }

    @Test
    @SneakyThrows
    public void getSearchedPriceOrderedPaging_OkTest() {
        Tuple2<List<ItemEntity>, Long> page = itemService.getShowcase("cat", SortType.PRICE, 2, 2, "testuser").block();
        assertFalse(page.getT1().isEmpty());
        List<ItemEntity> items = page.getT1();
        assertEquals(2, items.size());
        assertEquals("Big cat", items.get(0).getTitle());
        assertEquals("Funny cat", items.get(1).getTitle());
    }

    @Test
    @SneakyThrows
    public void getSearchedPriceOrderedPaging_CachedOkTest() {
        Tuple2<List<ItemEntity>, Long> page = itemService.getShowcase("cat", SortType.PRICE, 2, 2, "testuser").block();
        Tuple2<List<ItemEntity>, Long> page2 = itemService.getShowcase("cat", SortType.PRICE, 2, 2, "testuser").block();
        Pageable pageable = PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "price"));
        verify(itemRepository, times(1)).findByTitleContainingIgnoreCase("cat", pageable);

        Tuple2<List<ItemEntity>, Long> page3 = itemService.getShowcase("dog", SortType.PRICE, 2, 2, "testuser").block();
        verify(itemRepository, times(1)).findByTitleContainingIgnoreCase("dog", pageable);
    }

    @Test
    @SneakyThrows
    public void getSearchedAlphaOrdered_OkTest() {
        Tuple2<List<ItemEntity>, Long> page = itemService.getShowcase("dog", SortType.ALPHA, 1, 10, "testuser").block();
        assertFalse(page.getT1().isEmpty());
        assertEquals(2, page.getT1().size());
        List<ItemEntity> items = page.getT1();
        assertEquals("Big dog", items.get(0).getTitle());
        assertEquals("Marble dog", items.get(1).getTitle());
    }

    @Test
    @SneakyThrows
    public void checkEmptyCart_OkTest() {
        List<ItemEntity> itemsFromCart = itemService.getItemsFromCart("testuser").block();
        assertTrue(itemsFromCart.isEmpty());
    }

    @Test
    @SneakyThrows
    public void addItemToCart_OkTest() {
        itemService.addItemInCart(1L, ActionType.plus, "testuser").block();

        List<ItemEntity> itemsFromCart = itemService.getItemsFromCart("testuser").block();
        assertFalse(itemsFromCart.isEmpty());
        ItemEntity item = itemsFromCart.getFirst();
        assertEquals("Big cat", item.getTitle());
        assertEquals(1L, item.getId());
        assertEquals(1, item.getCount());
    }

    @Test
    @SneakyThrows
    public void getItemFromOtherUser_OkTest() {
        itemService.addItemInCart(1L, ActionType.plus, "testuser").block();

        List<ItemEntity> itemsFromCartMainUser = itemService.getItemsFromCart("mainuser").block();
        assertTrue(itemsFromCartMainUser.isEmpty());

        List<ItemEntity> itemsFromCart = itemService.getItemsFromCart("testuser").block();
        assertFalse(itemsFromCart.isEmpty());
        ItemEntity item = itemsFromCart.getFirst();
        assertEquals("Big cat", item.getTitle());
        assertEquals(1L, item.getId());
        assertEquals(1, item.getCount());
    }

    @Test
    @SneakyThrows
    public void removeItemFromCartByMinus_OkTest() {
        itemService.addItemInCart(1L, ActionType.plus, "testuser").block();
        itemService.addItemInCart(1L, ActionType.plus, "testuser").block();
        List<ItemEntity> itemsFromCart = itemService.getItemsFromCart("testuser").block();
        assertEquals(1, itemsFromCart.size());
        assertEquals(2, itemsFromCart.getFirst().getCount());

        itemService.addItemInCart(1L, ActionType.minus, "testuser").block();
        List<ItemEntity> itemsFromCartMinus = itemService.getItemsFromCart("testuser").block();
        assertEquals(1, itemsFromCartMinus.size());
        assertEquals(1, itemsFromCartMinus.getFirst().getCount());

        itemService.addItemInCart(1L, ActionType.minus, "testuser").block();
        List<ItemEntity> itemsFromCartMinusMinus = itemService.getItemsFromCart("testuser").block();
        assertTrue(itemsFromCartMinusMinus.isEmpty());
    }

    @Test
    @SneakyThrows
    public void removeItemFromCartByDelete_OkTest() {
        itemService.addItemInCart(1L, ActionType.plus, "testuser").block();
        itemService.addItemInCart(1L, ActionType.plus, "testuser").block();
        List<ItemEntity> itemsFromCart = itemService.getItemsFromCart("testuser").block();
        assertEquals(1, itemsFromCart.size());
        assertEquals(2, itemsFromCart.getFirst().getCount());

        itemService.addItemInCart(1L, ActionType.delete, "testuser").block();
        List<ItemEntity> itemsFromCartMinusMinus = itemService.getItemsFromCart("testuser").block();
        assertTrue(itemsFromCartMinusMinus.isEmpty());
    }
}
