package ru.yandex.simple_shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final UserService userService;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "item", key = "#id")
    public Mono<ItemEntity> findById(Long id) {
        return itemRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Mono<ItemEntity> findById(Long id, String username) {
        if (username == null || username.isEmpty()) {
            return itemRepository.findById(id);
        }
        return itemRepository.findById(id).flatMap(item -> userService.findByUsername(username)
                .flatMap(user -> getItemCartQuantity(item, user.getId())));
    }

    @Transactional(readOnly = true)
    public Mono<Tuple2<List<ItemEntity>, Long>> getShowcase(String search,
                                                            SortType sortType,
                                                            int pageNumber,
                                                            int pageSize,
                                                            String username) {
        Sort sort = convertSortType(sortType);
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);

        Flux<ItemEntity> fluxPage = ((search == null || search.isEmpty()) ?
                itemRepository.findAll() :
                itemRepository.findByTitleContainingIgnoreCase(search, pageable));

        Mono<List<ItemEntity>> page = userService.findByUsername(username).flatMap(user -> {
            if (user != null) {
                return fluxPage.flatMap(item -> getItemCartQuantity(item, user.getId())).collectList();
            } else {
                return fluxPage.collectList();
            }
        });


        Mono<Long> count = ((search == null || search.isEmpty()) ?
                itemRepository.count() : itemRepository.countByTitleContainingIgnoreCase(search));
        return Mono.zip(page, count);
    }

    @Transactional()
    @CacheEvict(cacheNames = "cartItems", allEntries = true)
    public Mono<CartItemEntity> addItemInCart(Long id, ActionType action, String username) {
        return userService.findByUsername(username).flatMap(user -> itemRepository.findById(id).flatMap( item ->
                {
                    switch (action) {
                        case plus -> {
                            return cartService.addCartItem(item, user.getId());
                        }
                        case minus -> {
                            return cartService.removeCartItem(item, user.getId());
                        }
                        case delete -> {
                            return cartService.deleteByItemId(id,  user.getId());
                        }
                        default -> {
                            return Mono.empty();
                        }
                    }
                }
        ));

    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "cartItems")
    public Mono<List<ItemEntity>> getItemsFromCart(String username) {
        return userService.findByUsername(username)
                .flatMap(user -> cartService.getAll(user.getId()).flatMap(cartItemEntities ->
                findById(cartItemEntities.getItemEntityId())
                        .map(itemEntity -> {
                            itemEntity.setCount(cartItemEntities.getQuantity());
                            return itemEntity;
                        })).collectList());

    }

    private Sort convertSortType(SortType sortType) {
        return switch (sortType) {
            case ALPHA -> Sort.by(Sort.Direction.ASC, "title");
            case PRICE -> Sort.by(Sort.Direction.ASC, "price");
            default -> Sort.unsorted();
        };
    }

    private Mono<ItemEntity> getItemCartQuantity(ItemEntity item, Long userId) {
        return cartService.findByItemIdAndUserId(item.getId(), userId)
                .doOnNext(cartItem -> item.setCount(cartItem.getQuantity())).then(Mono.just(item));
    }
}