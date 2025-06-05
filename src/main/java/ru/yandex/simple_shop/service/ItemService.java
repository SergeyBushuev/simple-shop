package ru.yandex.simple_shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
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
    public Mono<ItemEntity> findById(Long id) {
        return itemRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Mono<Tuple2<List<ItemEntity>, Long>> getShowcase(String search, SortType sortType, int pageNumber, int pageSize) {
        Sort sort = convertSortType(sortType);
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);

        Mono<List<ItemEntity>> page = ((search == null || search.isEmpty()) ?
                itemRepository.findAll(pageable) :
                itemRepository.findByTitleContainingIgnoreCase(search, pageable)).flatMap(this::getItemCartQuantity).collectList();
        Mono<Long> count = ((search == null || search.isEmpty()) ?
                itemRepository.count() : itemRepository.countByTitleContainingIgnoreCase(search));
        return Mono.zip(page, count);
    }

    @Transactional()
    public Mono<CartItemEntity> addItemInCart(Long id, ActionType action) {
        return itemRepository.findById(id).flatMap( item ->
                {
                    switch (action) {
                        case plus -> {
                            return cartService.addCartItem(item);
                        }
                        case minus -> {
                            return cartService.removeCartItem(item);
                        }
                        case delete -> {
                            return cartService.deleteByItemId(id);
                        }
                        default -> {
                            return Mono.empty();
                        }
                    }
                }
        );

    }

    @Transactional(readOnly = true)
    public Flux<ItemEntity> getItemsFromCart() {
        return cartService.getAll().map(CartItemEntity::getItemEntity);
    }

    private Sort convertSortType(SortType sortType) {
        return switch (sortType) {
            case ALPHA -> Sort.by(Sort.Direction.ASC, "title");
            case PRICE -> Sort.by(Sort.Direction.ASC, "price");
            default -> Sort.unsorted();
        };
    }

    private Mono<ItemEntity> getItemCartQuantity(ItemEntity item) {
        return cartService.findByItemId(item.getId())
                .doOnNext(cartItem -> item.setCount(cartItem.getQuantity())).then(Mono.just(item));
    }
}