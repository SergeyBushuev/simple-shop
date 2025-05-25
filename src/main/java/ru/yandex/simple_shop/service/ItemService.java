package ru.yandex.simple_shop.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.simple_shop.model.ActionType;
import ru.yandex.simple_shop.model.CartItemEntity;
import ru.yandex.simple_shop.model.ItemEntity;
import ru.yandex.simple_shop.model.SortType;
import ru.yandex.simple_shop.repository.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CartService cartService;

    @Transactional(readOnly = true)
    public ItemEntity findById(Long id) {
        return itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Page<ItemEntity> getShowcase(String search, SortType sortType, int pageNumber, int pageSize) {
        Sort sort = convertSortType(sortType);
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);

        Page<ItemEntity> page = (search == null || search.isEmpty()) ?
                itemRepository.findAll(pageable) :
                itemRepository.findByTitleContainingIgnoreCase(search, pageable);
        page.getContent().forEach(this::getItemCartQuantity);
        return page;
    }

    @Transactional()
    public void addItemInCart(Long id, ActionType action) {
        ItemEntity item = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        switch (action) {
            case plus -> cartService.addCartItem(id, item);
            case minus -> cartService.removeCartItem(id, item);
            case delete -> cartService.deleteByItemId(id);
        }

    }

    @Transactional(readOnly = true)
    public List<ItemEntity> getItemsFromCart() {
        return cartService.getAll().stream().map(CartItemEntity::getItemEntity).toList();
    }

    private Sort convertSortType(SortType sortType) {
        return switch (sortType) {
            case ALPHA -> Sort.by(Sort.Direction.ASC, "title");
            case PRICE -> Sort.by(Sort.Direction.ASC, "price");
            default -> Sort.unsorted();
        };
    }

    private void getItemCartQuantity(ItemEntity item) {
        cartService.findByItemId(item.getId())
                .ifPresent(cartItem -> item.setCount(cartItem.getQuantity()));
    }
}